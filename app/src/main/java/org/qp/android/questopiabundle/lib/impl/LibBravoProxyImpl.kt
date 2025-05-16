package org.qp.android.questopiabundle.lib.impl

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.child
import org.libndkqsp.jni.NDKLib
import org.qp.android.questopiabundle.GameInterface
import org.qp.android.questopiabundle.dto.LibGenItem
import org.qp.android.questopiabundle.dto.LibGameState
import org.qp.android.questopiabundle.lib.LibIProxy
import org.qp.android.questopiabundle.lib.LibRefIRequest
import org.qp.android.questopiabundle.lib.LibTypeDialog
import org.qp.android.questopiabundle.lib.LibTypeWindow
import org.qp.android.questopiabundle.utils.FileUtil.documentWrap
import org.qp.android.questopiabundle.utils.FileUtil.fromFullPath
import org.qp.android.questopiabundle.utils.FileUtil.getFileContents
import org.qp.android.questopiabundle.utils.FileUtil.isWritableDir
import org.qp.android.questopiabundle.utils.FileUtil.isWritableFile
import org.qp.android.questopiabundle.utils.FileUtil.writeFileContents
import org.qp.android.questopiabundle.utils.HtmlUtil.getSrcDir
import org.qp.android.questopiabundle.utils.HtmlUtil.isContainsHtmlTags
import org.qp.android.questopiabundle.utils.PathUtil.getFilename
import org.qp.android.questopiabundle.utils.PathUtil.normalizeContentPath
import org.qp.android.questopiabundle.utils.StringUtil.getStringOrEmpty
import org.qp.android.questopiabundle.utils.StringUtil.isNotEmptyOrBlank
import org.qp.android.questopiabundle.utils.ThreadUtil.isSameThread
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.Volatile
import kotlin.contracts.ExperimentalContracts

class LibBravoProxyImpl(
    private val context: Context,
    private var gameRequest: LibRefIRequest = LibRefIRequest(),
    override var gameState: LibGameState = LibGameState()
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

        gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, """
            Location: $locName
            Action: ${errorData.index}
            Line: ${errorData.line}
            Error number: ${errorData.errorNum}
            Description: $desc
        """.trimIndent())
    }

    /**
     * Loads the interface configuration - using HTML, font and colors - from the library.
     *
     * @return `true` if the configuration has changed, otherwise `false`
     */
    private fun loadInterfaceConfiguration(): Boolean {
        val oldConfig = gameState.interfaceConfig

        val htmlResult = QSPGetVarValues("USEHTML", 0) as VarValResp
        val fSizeResult = QSPGetVarValues("FSIZE", 0) as VarValResp
        val bColorResult = QSPGetVarValues("BCOLOR", 0) as VarValResp
        val fColorResult = QSPGetVarValues("FCOLOR", 0) as VarValResp
        val lColorResult = QSPGetVarValues("LCOLOR", 0) as VarValResp

        val useHtml = htmlResult.intValue != 0
        val newConfig = oldConfig.copy(
            useHtml = if (htmlResult.isSuccess) useHtml else oldConfig.useHtml,
            fontSize = if (fSizeResult.isSuccess) fSizeResult.intValue.toLong() else oldConfig.fontSize,
            backColor = if (bColorResult.isSuccess) bColorResult.intValue.toLong() else oldConfig.backColor,
            fontColor = if (fColorResult.isSuccess) fColorResult.intValue.toLong() else oldConfig.fontColor,
            linkColor = if (lColorResult.isSuccess) lColorResult.intValue.toLong() else oldConfig.linkColor
        )

        return when {
            newConfig != oldConfig -> {
                gameState = gameState.copy(interfaceConfig = newConfig)
                true
            }

            else -> false
        }
    }

    @OptIn(ExperimentalContracts::class)
    private val actionsList: List<LibGenItem>
        get() {
            val gameDir = currGameDir
            if (!isWritableDir(context, gameDir)) return emptyList()

            val actions = mutableListOf<LibGenItem>()

            for (element in QSPGetActionData() ?: return emptyList()) {
                val safeElement = element ?: continue
                var tempImagePath = safeElement.image
                val tempText = safeElement.text

                if (isNotEmptyOrBlank(tempImagePath)) {
                    val tempPath = normalizeContentPath(getFilename(tempImagePath))
                    val fileFromPath = gameDir.child(context, tempPath)
                    if (isWritableFile(context, fileFromPath)) {
                        tempImagePath = fileFromPath.uri.toString()
                    }
                }

                actions.add(LibGenItem(tempText ?: "", tempImagePath ?: ""))
            }

            return actions
        }

    @OptIn(ExperimentalContracts::class)
    private val objectsList: List<LibGenItem>
        get() {
            val gameDir = currGameDir
            if (!isWritableDir(context, gameDir)) return emptyList()

            val objects = mutableListOf<LibGenItem>()
            for (element in QSPGetObjectData() ?: return emptyList()) {
                val safeElement = element ?: continue
                var tempImagePath = safeElement.image
                val tempText = safeElement.text

                if (tempText.contains("<img")) {
                    if (!isContainsHtmlTags(tempText)) {
                        val fileFromPath = gameDir.child(context, tempText)
                        if (isWritableFile(context, fileFromPath)) {
                            tempImagePath = fileFromPath.uri.toString()
                        }
                    } else {
                        val tempPath = getSrcDir(tempText)
                        val fileFromPath = gameDir.child(context, tempPath)
                        if (isWritableFile(context, fileFromPath)) {
                            tempImagePath = fileFromPath.uri.toString()
                        }
                    }
                }

                objects.add(LibGenItem(tempText ?: "", tempImagePath ?: ""))
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
            gameState = gameState.copy(
                gameRunning = true,
                gameId = id,
                gameTitle = title,
                gameDirUri = dir,
                gameFileUri = file
            )
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
        val newState = gameState.copy(
            mainDesc = if (QSPIsMainDescChanged()) QSPGetMainDesc() ?: "" else gameState.mainDesc,
            varsDesc = if (QSPIsVarsDescChanged()) QSPGetVarsDesc() ?: "" else gameState.varsDesc,
            actionsList = if (QSPIsActionsChanged()) actionsList else gameState.actionsList,
            objectsList = if (QSPIsObjectsChanged()) objectsList else gameState.objectsList
        )

        gameState = if (newState != gameState) newState else gameState

        val newRequest = gameRequest.copy(
            isIConfigChanged = loadInterfaceConfiguration(),
            isMainDescChanged = QSPIsMainDescChanged(),
            isVarsDescChanged = QSPIsVarsDescChanged(),
            isActionsChanged = QSPIsActionsChanged(),
            isObjectsChanged = QSPIsObjectsChanged()
        )

        if (newRequest != gameRequest) {
            gameRequest = newRequest
            gameInterface.doUpdateState(newRequest)
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun ShowPicture(path: String?) {
        if (!isNotEmptyOrBlank(path)) return
        gameInterface.showLibDialog(LibTypeDialog.DIALOG_PICTURE, path)
    }

    override fun SetTimer(msecs: Int) {
        gameInterface.setCountInter(msecs)
    }

    override fun ShowMessage(message: String?) {
        gameInterface.showLibDialog(LibTypeDialog.DIALOG_MESSAGE, message)
    }

    @OptIn(ExperimentalContracts::class)
    override fun PlayFile(path: String?, volume: Int) {
        if (!isNotEmptyOrBlank(path)) return
        gameInterface.playFile(path, volume)
    }

    @OptIn(ExperimentalContracts::class)
    override fun IsPlayingFile(path: String?): Boolean {
        return isNotEmptyOrBlank(path) && gameInterface.isPlayingFile(path)
    }

    @OptIn(ExperimentalContracts::class)
    override fun CloseFile(path: String?) {
        if (isNotEmptyOrBlank(path)) {
            gameInterface.closeFile(path)
        } else {
            gameInterface.closeAllFiles()
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun OpenGame(filename: String?) {
        if (!isNotEmptyOrBlank(filename)) {
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

    override fun InputBox(prompt: String?): String {
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

    override fun AddMenuItem(name: String?, imgPath: String?) {
        gameState = gameState.copy(menuItemsList = listOf(LibGenItem(
            name ?: "",
            imgPath ?: ""
        )))
    }

    override fun ShowMenu() {
        val doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_MENU, null) ?: return
        val result = doShow.outNumValue
        if (result != -1) {
            QSPSelectMenuItem(result)
        }
    }

    override fun DeleteMenu() {
        gameState = gameState.copy(menuItemsList = emptyList())
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

    @OptIn(ExperimentalContracts::class)
    override fun GetFileContents(path: String?): ByteArray? {
        if (!isNotEmptyOrBlank(path)) return byteArrayOf()

        val targetFile = fromFullPath(context, path.toString()) ?: return null
        val targetFileUri = targetFile.uri
        gameInterface.requestPermFile(targetFileUri)

        return getFileContents(context, targetFileUri)
    }

    @OptIn(ExperimentalContracts::class)
    override fun ChangeQuestPath(path: String?) {
        if (!isNotEmptyOrBlank(path)) return

        val newGameDir = fromFullPath(context, path.toString())
        if (newGameDir == null || !newGameDir.exists()) {
            Log.e(TAG, "Game directory not found: $path")
            return
        }
        val currGameDirUri = currGameDir?.uri
        val newGameDirUri = newGameDir.uri
        if (currGameDirUri != newGameDirUri) {
            gameState = gameState.copy(gameDirUri = newGameDirUri)
            gameInterface.doChangeCurrGameDir(newGameDirUri)
        }
    } // endregion LibQpCallbacks
}
