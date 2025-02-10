package org.qp.android.questopiabundle.lib.impl

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.libqsp.jni.QSPLib
import org.qp.android.questopiabundle.GameInterface
import org.qp.android.questopiabundle.dto.LibListItem
import org.qp.android.questopiabundle.lib.LibGameState
import org.qp.android.questopiabundle.lib.LibIProxy
import org.qp.android.questopiabundle.lib.LibRefIRequest
import org.qp.android.questopiabundle.lib.LibTypeDialog
import org.qp.android.questopiabundle.lib.LibTypeWindow
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

class LibAlphaProxyImpl(
    private val context: Context,
    override val gameState: LibGameState = LibGameState()
) : QSPLib(), LibIProxy {

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
        val gameData = getFileContents(context, gameFileUri) ?: return false

        if (!loadGameWorldFromData(gameData, true)) {
            showLastQspError()
            return false
        }
        return true
    }

    private fun showLastQspError() {
        val errorData = lastErrorData
        val locName = getStringOrEmpty(errorData.locName)
        val desc = getStringOrEmpty(getErrorDesc(errorData.errorNum))
        val message = String.format(
            Locale.getDefault(),
            "Location: %s\nAction: %d\nLine: %d\nError number: %d\nDescription: %s",
            locName,
            errorData.actIndex,
            errorData.intLineNum,
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

        val htmlResult = getNumVarValue("USEHTML", 0)
        val useHtml = htmlResult != 0L
        if (config.useHtml != useHtml) {
            config.useHtml = useHtml
            changed = true
        }

        val fSizeResult = getNumVarValue("FSIZE", 0)
        if (config.fontSize != fSizeResult) {
            config.fontSize = fSizeResult
            changed = true
        }

        val bColorResult = getNumVarValue("BCOLOR", 0)
        if (config.backColor != bColorResult) {
            config.backColor = bColorResult
            changed = true
        }

        val fColorResult = getNumVarValue("FCOLOR", 0)
        if (config.fontColor != fColorResult) {
            config.fontColor = fColorResult
            changed = true
        }

        val lColorResult = getNumVarValue("LCOLOR", 0)
        if (config.linkColor != lColorResult) {
            config.linkColor = lColorResult
            changed = true
        }

        return changed
    }

    private val actionsList: ArrayList<LibListItem>
        get() {
            val actions = ArrayList<LibListItem>()
            val currGameDir = currGameDir

            for (element in getActions()) {
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

            for (element in getObjects()) {
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

    override fun startLibThread() {
        libThread = Thread({
            while (!Thread.currentThread().isInterrupted) {
                try {
                    init()
                    if (Looper.myLooper() == null) {
                        Looper.prepare()
                    }
                    libHandler = Handler(Looper.myLooper()!!)
                    libThreadInit = true
                    Looper.loop()
                    terminate()
                } catch (t: Throwable) {
                    Log.e(TAG, "lib thread has stopped exceptionally", t)
                    Thread.currentThread().interrupt()
                }
            }
        }, "libQSP")
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
            if (!restartGame(true)) {
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
        if (!openSavedGameFromData(gameData, true)) {
            showLastQspError()
        }
    }

    override fun saveGameState(uri: Uri) {
        if (!isSameThread(libHandler.looper.thread)) {
            runOnQspThread { saveGameState(uri) }
            return
        }

        gameInterface.requestPermFile(uri)
        val gameData = saveGameAsData(false) ?: return
        writeFileContents(context, uri, gameData)
    }

    override fun onActionClicked(index: Int) {
        runOnQspThread {
            if (!setSelActIndex(index, false)) {
                showLastQspError()
            }
            if (!execSelAction(true)) {
                showLastQspError()
            }
        }
    }

    override fun onObjectSelected(index: Int) {
        runOnQspThread {
            if (!setSelObjIndex(index, true)) {
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
            setInputStrText(input)
            if (!execUserInput(true)) {
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
            if (!execString(input, true)) {
                showLastQspError()
            }
        }
    }

    override fun execute(code: String?) {
        runOnQspThread {
            if (!execString(code, true)) {
                showLastQspError()
            }
        }
    }

    override fun executeCounter() {
        if (libLock.isLocked) return
        runOnQspThread {
            if (!execCounter(true)) {
                showLastQspError()
            }
        }
    }

    override fun setGameInterface(inter: GameInterface) {
        this.gameInterface = inter
    }

    override fun onRefreshInt(isForced: Boolean) {
        val request = LibRefIRequest()
        if (loadInterfaceConfiguration()) {
            request.isIConfigChanged = true
        }
        if (isMainDescChanged) {
            if (isNotEmptyOrBlank(gameState.mainDesc)) {
                if (gameState.mainDesc != mainDesc) {
                    gameState.mainDesc = mainDesc
                    request.isMainDescChanged = true
                }
            } else {
                gameState.mainDesc = mainDesc
                request.isMainDescChanged = true
            }
        }
        if (isActsChanged) {
            if (gameState.actionsList !== actionsList) {
                gameState.actionsList = actionsList
                request.isActionsChanged = true
            }
        }
        if (isObjsChanged) {
            if (gameState.objectsList !== objectsList) {
                gameState.objectsList = objectsList
                request.isObjectsChanged = true
            }
        }
        if (isVarsDescChanged) {
            if (isNotEmptyOrBlank(gameState.varsDesc)) {
                if (gameState.varsDesc != varsDesc) {
                    gameState.varsDesc = varsDesc
                    request.isVarsDescChanged = true
                }
            } else {
                gameState.varsDesc = varsDesc
                request.isVarsDescChanged = true
            }
        }

        gameInterface.doRefresh(request)
    }

    override fun onShowImage(file: String) {
        if (!isNotEmptyOrBlank(file)) return
        gameInterface.showLibDialog(LibTypeDialog.DIALOG_PICTURE, file)
    }

    override fun onSetTimer(msecs: Int) {
        gameInterface.setCountInter(msecs)
    }

    override fun onShowMessage(text: String) {
        gameInterface.showLibDialog(LibTypeDialog.DIALOG_MESSAGE, text)
    }

    override fun onPlayFile(file: String, volume: Int) {
        if (!isNotEmptyOrBlank(file)) return
        gameInterface.playFile(file, volume)
    }

    override fun onIsPlayingFile(file: String): Boolean {
        return isNotEmptyOrBlank(file) && gameInterface.isPlayingFile(file)
    }

    override fun onCloseFile(file: String) {
        if (isNotEmptyOrBlank(file)) {
            gameInterface.closeFile(file)
        } else {
            gameInterface.closeAllFiles()
        }
    }

    override fun onOpenGameStatus(file: String?) {
        if (file == null) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_POPUP_LOAD, null)
        } else {
            try {
                val saveFile = fromFullPath(context, file) ?: return
                gameInterface.requestPermFile(saveFile.uri)
                if (isWritableFile(context, saveFile)) {
                    gameInterface.doWithCounterDisabled { loadGameState(saveFile.uri) }
                } else {
                    gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, "Save file not found")
                    Log.e(TAG, "Save file not found")
                }
            } catch (e: Exception) {
                gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, e.toString())
                Log.e(TAG, "Error: ", e)
            }
        }
    }

    override fun onSaveGameStatus(file: String?) {
        if (file == null) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_POPUP_SAVE, null)
        } else {
            val currGameDir = currGameDir ?: return
            val saveFileUri = gameInterface.requestCreateFile(currGameDir.uri, file)
            if (saveFileUri != Uri.EMPTY) {
                saveGameState(saveFileUri)
            } else {
                gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, "Error access dir")
                Log.e(TAG, "Error access dir")
            }
        }
    }

    override fun onInputBox(text: String): String {
        val doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_INPUT, text) ?: return ""
        return doShow.outTextValue
    }

    override fun onGetMsCount(): Int {
        val now = SystemClock.elapsedRealtime()
        if (lastMsCountCallTime == 0L) {
            lastMsCountCallTime = gameStartTime
        }
        val dt = now - lastMsCountCallTime
        lastMsCountCallTime = now
        return dt.toInt()
    }

    override fun onShowMenu(items: Array<ListItem>): Int {
        val doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_MENU, null)
            ?: return super.onShowMenu(items)
        val result = doShow.outNumValue
        if (result != -1) {
            return result
        }
        return super.onShowMenu(items)
    }

    override fun onSleep(msecs: Int) {
        try {
            Thread.sleep(msecs.toLong())
        } catch (ex: InterruptedException) {
            Log.e(TAG, "Wait failed", ex)
        }
    }

    override fun onShowWindow(type: Int, toShow: Boolean) {
        val windowType = LibTypeWindow.entries[type]
        gameInterface.changeVisWindow(windowType, toShow)
    }

    override fun onOpenGame(file: String, isNewGame: Boolean) {
        val newGameDir = fromFullPath(context, file)
        if (newGameDir == null || !newGameDir.exists()) {
            Log.e(TAG, "Game directory not found: $file")
            return
        }
        val currGameDir = currGameDir ?: return
        val currGameDirUri = currGameDir.uri
        val newGameDirUri = newGameDir.uri
        if (currGameDirUri != newGameDirUri) {
            gameState.gameDirUri = newGameDirUri
            gameInterface.doChangeCurrGameDir(newGameDirUri)
        }
    }
}
