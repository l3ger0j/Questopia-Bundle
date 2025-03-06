package org.qp.android.questopiabundle.lib.impl

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import org.libndkqsp.jni.NDKLib
import org.qp.android.questopiabundle.GameInterface
import org.qp.android.questopiabundle.dto.LibListItem
import org.qp.android.questopiabundle.dto.LibMenuItem
import org.qp.android.questopiabundle.lib.LibGameState
import org.qp.android.questopiabundle.lib.LibIProxy
import org.qp.android.questopiabundle.lib.LibRefIRequest
import org.qp.android.questopiabundle.lib.LibTypeDialog
import org.qp.android.questopiabundle.lib.LibTypeWindow
import org.qp.android.questopiabundle.utils.FileUtil.documentWrap
import org.qp.android.questopiabundle.utils.FileUtil.fromFullPath
import org.qp.android.questopiabundle.utils.FileUtil.fromRelPath
import org.qp.android.questopiabundle.utils.FileUtil.getFileContents
import org.qp.android.questopiabundle.utils.FileUtil.isWritableFile
import org.qp.android.questopiabundle.utils.FileUtil.writeFileContents
import org.qp.android.questopiabundle.utils.HtmlUtil.getSrcDir
import org.qp.android.questopiabundle.utils.HtmlUtil.isContainsHtmlTags
import org.qp.android.questopiabundle.utils.HtmlUtil.removeHtmlTags
import org.qp.android.questopiabundle.utils.PathUtil.getFilename
import org.qp.android.questopiabundle.utils.PathUtil.normalizeContentPath
import org.qp.android.questopiabundle.utils.StringUtil.getStringOrEmpty
import org.qp.android.questopiabundle.utils.StringUtil.isNotEmptyOrBlank
import org.qp.android.questopiabundle.utils.ThreadUtil.isSameThread
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.Volatile

class LibBravoProxyImpl(
    private val context: Context,
    override val gameState: LibGameState = LibGameState()
) : NDKLib(), LibIProxy {

    private val TAG = javaClass.simpleName
    private val libLock = ReentrantLock()
    private lateinit var libThread: Thread
    @Volatile private lateinit var libHandler: Handler
    @Volatile private var libThreadInit = false
    @Volatile private var gameStartTime: Long = 0L
    @Volatile private var lastMsCountCallTime: Long = 0L
    private lateinit var gameInterface: GameInterface
    private val currGameDir: DocumentFile?
        get() = fromUri(context, gameState.gameDirUri)

    @Synchronized
    private fun runOnQspThread(runnable: Runnable) {
        if (!libThreadInit) {
            Log.w(TAG, "Lib thread has been started, but not initialized!")
            return
        }
        val mLibHandler = libHandler
        mLibHandler.post {
            libLock.lock()
            try {
                runnable.run()
            } finally {
                libLock.unlock()
            }
        }
    }

    private fun loadGameWorld(): Boolean {
        val gameFileUri = gameState.gameFileUri
        val gameFile = fromUri(
            context,
            gameState.gameFileUri
        )
        if (gameFile == null) return false
        val gameFileFullPath = documentWrap(gameFile).getAbsolutePath(context)
        val gameData = getFileContents(context, gameFileUri) ?: return false

        if (!QSPLoadGameWorldFromData(gameData, gameFileFullPath)) {
            showLastQspError()
            Log.d("QSP", "World is not loaded!")
            return false
        }
        Log.d("QSP", "World is loaded!")
        return true
    }

    private fun showLastQspError() {
        val errorData = QSPGetLastErrorData() as ErrorData
        val locName = getStringOrEmpty(errorData.locName)
        val desc = getStringOrEmpty(QSPGetErrorDesc(errorData.errorNum))
        val message = String.format(
            Locale.getDefault(),
            "Location: %s\nAction: %d\nLine: %d\nError number: %d\nDescription: %s",
            locName,
            errorData.index,
            errorData.line,
            errorData.errorNum,
            desc
        )
        gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, message)
    }

    /**
     * Loads the interface configuration - using HTML, font and colors - from the library.
     *
     * @return `true` if the configuration has changed, otherwise `false`
     */
    private fun loadInterfaceConfiguration(): Boolean {
        val config = gameState.interfaceConfig
        var changed = false

        val htmlResult = QSPGetVarValues("USEHTML", 0) as VarValResp
        if (htmlResult.isSuccess) {
            val useHtml = htmlResult.intValue != 0
            if (config.useHtml != useHtml) {
                config.useHtml = useHtml
                changed = true
            }
        }
        val fSizeResult = QSPGetVarValues("FSIZE", 0) as VarValResp
        if (fSizeResult.isSuccess && config.fontSize != fSizeResult.intValue.toLong()) {
            config.fontSize = fSizeResult.intValue.toLong()
            changed = true
        }
        val bColorResult = QSPGetVarValues("BCOLOR", 0) as VarValResp
        if (bColorResult.isSuccess && config.backColor != bColorResult.intValue.toLong()) {
            config.backColor = bColorResult.intValue.toLong()
            changed = true
        }
        val fColorResult = QSPGetVarValues("FCOLOR", 0) as VarValResp
        if (fColorResult.isSuccess && config.fontColor != fColorResult.intValue.toLong()) {
            config.fontColor = fColorResult.intValue.toLong()
            changed = true
        }
        val lColorResult = QSPGetVarValues("LCOLOR", 0) as VarValResp
        if (lColorResult.isSuccess && config.linkColor != lColorResult.intValue.toLong()) {
            config.linkColor = lColorResult.intValue.toLong()
            changed = true
        }

        return changed
    }

    private val actionsList: ArrayList<LibListItem>
        get() {
            val actions = ArrayList<LibListItem>()
            val currGameDir = currGameDir

            for (element in QSPGetActionData()) {
                val newElement = LibListItem(element)
                if (isNotEmptyOrBlank(newElement.pathToImage) && currGameDir != null) {
                    val tempPath =
                        normalizeContentPath(getFilename(newElement.pathToImage))
                    val fileFromPath =
                        fromRelPath(context, tempPath, currGameDir, false)
                    if (fileFromPath != null) {
                        newElement.pathToImage = fileFromPath.uri.toString()
                    } else {
                        newElement.pathToImage = ""
                    }
                }
                newElement.text = if (gameState.interfaceConfig.useHtml) {
                    removeHtmlTags(newElement.text)
                } else {
                    newElement.text
                }
                actions.add(newElement)
            }

            return actions
        }

    private val objectsList: ArrayList<LibListItem>
        get() {
            val objects = ArrayList<LibListItem>()
            val currGameDir = currGameDir

            for (element in QSPGetObjectData()) {
                val newElement = LibListItem(element)
                if (newElement.text.contains("<img") && currGameDir != null) {
                    if (isContainsHtmlTags(newElement.text)) {
                        val tempPath = getSrcDir(newElement.text)
                        val fileFromPath =
                            fromRelPath(context, tempPath, currGameDir, false)
                        newElement.pathToImage = fileFromPath.toString()
                    } else {
                        val fileFromPath =
                            fromRelPath(context, newElement.text, currGameDir, false)
                        newElement.pathToImage = fileFromPath.toString()
                    }
                } else {
                    newElement.text = if (gameState.interfaceConfig.useHtml)
                        removeHtmlTags(newElement.text)
                    else
                        newElement.text
                }
                objects.add(newElement)
            }

            return objects
        }

    // region LibQpProxy
    override fun startLibThread() {
        libThread = Thread({
            while (!Thread.currentThread().isInterrupted) {
                try {
                    QSPInit()
                    if (Looper.myLooper() == null) {
                        Looper.prepare()
                    }
                    libHandler = Handler(Looper.myLooper()!!)
                    libThreadInit = true
                    Looper.loop()
                    QSPDeInit()
                } catch (t: Throwable) {
                    Log.e(TAG, "lib thread has stopped exceptionally", t)
                    Thread.currentThread().interrupt()
                }
            }
        }, "libNDKQSP")
        libThread.start()
    }

    @Synchronized
    override fun stopLibThread() {
        if (libThreadInit) {
            val handler = libHandler
            handler.looper.quitSafely()
            libThreadInit = false
        } else {
            Log.w(TAG, "lib thread has been started, but not initialized")
        }
        libThread.interrupt()
    }

    override fun enableDebugMode(isDebug: Boolean) {
        runOnQspThread { enableDebugMode(isDebug) }
    }

    override fun runGame(
        gameId: Long,
        gameTitle: String,
        gameDirUri: Uri,
        gameFileUri: Uri
    ) {
        runOnQspThread { doRunGame(gameId, gameTitle, gameDirUri, gameFileUri) }
    }

    override fun restartGame() {
        runOnQspThread {
            doRunGame(
                gameState.gameId,
                gameState.gameTitle,
                gameState.gameDirUri,
                gameState.gameFileUri
            )
        }
    }

    private fun doRunGame(id: Long, title: String, dir: Uri, file: Uri) {
        gameInterface.doWithCounterDisabled {
            gameInterface.closeAllFiles()
            gameState.reset()
            gameState.gameRunning = true
            gameState.gameId = id
            gameState.gameTitle = title
            gameState.gameDirUri = dir
            gameState.gameFileUri = file
            gameInterface.doChangeCurrGameDir(dir)
            if (!loadGameWorld()) return@doWithCounterDisabled
            gameStartTime = SystemClock.elapsedRealtime()
            lastMsCountCallTime = 0
            if (!QSPRestartGame(true)) {
                showLastQspError()
            }
        }
    }

    override fun loadGameState(uri: Uri) {
        if (!isSameThread(libHandler.looper.thread)) {
            runOnQspThread { loadGameState(uri) }
            return
        }

        gameInterface.requestPermFile(uri)
        val gameData = getFileContents(context, uri) ?: return
        if (!QSPOpenSavedGameFromData(gameData, gameData.size, true)) {
            showLastQspError()
        }
    }

    override fun saveGameState(uri: Uri) {
        if (!isSameThread(libHandler.looper.thread)) {
            runOnQspThread { saveGameState(uri) }
            return
        }

        val gameData = QSPSaveGameAsData(false) ?: return
        gameInterface.requestPermFile(uri)
        writeFileContents(context, uri, gameData)
    }

    override fun onActionClicked(index: Int) {
        runOnQspThread {
            if (!QSPSetSelActionIndex(index, false)) {
                showLastQspError()
            }
            if (!QSPExecuteSelActionCode(true)) {
                showLastQspError()
            }
        }
    }

    override fun onObjectSelected(index: Int) {
        runOnQspThread {
            if (!QSPSetSelObjectIndex(index, true)) {
                showLastQspError()
            }
        }
    }

    override fun onInputAreaClicked() {
        runOnQspThread {
            val doShow =
                gameInterface.showLibDialog(LibTypeDialog.DIALOG_INPUT, "userInputTitle")
                    ?: return@runOnQspThread
            val input = doShow.outTextValue
            QSPSetInputStrText(input)
            if (!QSPExecUserInput(true)) {
                showLastQspError()
            }
        }
    }

    override fun onUseExecutorString() {
        runOnQspThread {
            val doShow =
                gameInterface.showLibDialog(LibTypeDialog.DIALOG_EXECUTOR, "execStringTitle")
                    ?: return@runOnQspThread
            val input = doShow.outTextValue
            if (!QSPExecString(input, true)) {
                showLastQspError()
            }
        }
    }

    override fun execute(code: String?) {
        runOnQspThread {
            if (!QSPExecString(code, true)) {
                showLastQspError()
            }
        }
    }

    override fun executeCounter() {
        if (libLock.isLocked) return
        runOnQspThread {
            if (!QSPExecCounter(true)) {
                showLastQspError()
            }
        }
    }

    override fun setGameInterface(inter: GameInterface) {
        this.gameInterface = inter
    }

    // endregion LibQpProxy
    // region LibQpCallbacks
    override fun RefreshInt() {
        val request = LibRefIRequest()
        val configChanged = loadInterfaceConfiguration()

        if (configChanged) {
            request.isIConfigChanged = true
        }
        if (QSPIsMainDescChanged()) {
            if (gameState.mainDesc !== QSPGetMainDesc()) {
                gameState.mainDesc = QSPGetMainDesc()
                request.isMainDescChanged = true
            }
        }
        if (QSPIsActionsChanged()) {
            if (gameState.actionsList !== actionsList) {
                gameState.actionsList = actionsList
                request.isActionsChanged = true
            }
        }
        if (QSPIsObjectsChanged()) {
            if (gameState.objectsList !== objectsList) {
                gameState.objectsList = objectsList
                request.isObjectsChanged = true
            }
        }
        if (QSPIsVarsDescChanged()) {
            if (gameState.varsDesc != QSPGetVarsDesc()) {
                gameState.varsDesc = QSPGetVarsDesc() ?: ""
                request.isVarsDescChanged = true
            }
        }

        val inter = gameInterface
        inter.doRefresh(request)
    }

    override fun ShowPicture(path: String) {
        val inter = gameInterface
        if (!isNotEmptyOrBlank(path)) return
        inter.showLibDialog(LibTypeDialog.DIALOG_PICTURE, path)
    }

    override fun SetTimer(msecs: Int) {
        val inter = gameInterface
        inter.setCountInter(msecs)
    }

    override fun ShowMessage(message: String) {
        val inter = gameInterface
        inter.showLibDialog(LibTypeDialog.DIALOG_MESSAGE, message)
    }

    override fun PlayFile(path: String, volume: Int) {
        if (!isNotEmptyOrBlank(path)) return
        gameInterface.playFile(path, volume)
    }

    override fun IsPlayingFile(path: String): Boolean {
        return isNotEmptyOrBlank(path) && gameInterface.isPlayingFile(path)
    }

    override fun CloseFile(path: String) {
        if (isNotEmptyOrBlank(path)) {
            gameInterface.closeFile(path)
        } else {
            gameInterface.closeAllFiles()
        }
    }

    override fun OpenGame(filename: String?) {
        if (filename == null) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_POPUP_LOAD, null)
        } else {
            try {
                val saveFile = fromFullPath(context, filename) ?: return
                gameInterface.requestPermFile(saveFile.uri)
                if (isWritableFile(context, saveFile)) {
                    gameInterface.doWithCounterDisabled { loadGameState(saveFile.uri) }
                } else {
                    gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, "Save file not found")
                }
            } catch (e: Exception) {
                gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, e.toString())
            }
        }
    }

    override fun SaveGame(filename: String?) {
        if (filename == null) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_POPUP_SAVE, null)
        } else {
            val currGameDir = currGameDir ?: return
            val saveFileUri = gameInterface.requestCreateFile(currGameDir.uri, filename)
            if (saveFileUri != Uri.EMPTY) {
                saveGameState(saveFileUri)
            } else {
                gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, "Error access dir")
                Log.e(TAG, "Error access dir")
            }
        }
    }

    override fun InputBox(prompt: String): String {
        val doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_INPUT, prompt) ?: return ""
        return doShow.outTextValue
    }

    override fun GetMSCount(): Int {
        val now = SystemClock.elapsedRealtime()
        if (lastMsCountCallTime == 0L) {
            lastMsCountCallTime = gameStartTime
        }
        val dt = (now - lastMsCountCallTime).toInt()
        lastMsCountCallTime = now
        return dt
    }

    override fun AddMenuItem(name: String, imgPath: String) {
        val item = LibMenuItem()
        item.name = name
        item.pathToImage = imgPath
        gameState.menuItemsList.add(item)
    }

    override fun ShowMenu() {
        val doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_MENU, null) ?: return
        val result = doShow.outNumValue
        if (result != -1) {
            QSPSelectMenuItem(result)
        }
    }

    override fun DeleteMenu() {
        gameState.menuItemsList.clear()
    }

    override fun Wait(msecs: Int) {
        try {
            Thread.sleep(msecs.toLong())
        } catch (ex: InterruptedException) {
            Log.e(TAG, "Wait failed", ex)
        }
    }

    override fun ShowWindow(type: Int, isShow: Boolean) {
        val windowType = LibTypeWindow.entries[type]
        gameInterface.changeVisWindow(windowType, isShow)
    }

    override fun GetFileContents(path: String): ByteArray? {
        val targetFile = fromFullPath(context, path) ?: return null
        val targetFileUri = targetFile.uri
        gameInterface.requestPermFile(targetFileUri)
        return getFileContents(context, targetFileUri)!!
    }

    override fun ChangeQuestPath(path: String) {
        val newGameDir = fromFullPath(context, path)
        if (newGameDir == null || !newGameDir.exists()) {
            Log.e(TAG, "Game directory not found: $path")
            return
        }
        val currGameDirUri = currGameDir?.uri
        val newGameDirUri = newGameDir.uri
        if (currGameDirUri != newGameDirUri) {
            gameState.gameDirUri = newGameDirUri
            gameInterface.doChangeCurrGameDir(newGameDirUri)
        }
    } // endregion LibQpCallbacks
}
