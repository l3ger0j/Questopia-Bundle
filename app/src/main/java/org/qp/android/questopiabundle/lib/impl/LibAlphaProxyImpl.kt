package org.qp.android.questopiabundle.lib.impl

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.os.HandlerCompat
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.child
import com.libqsp.jni.QSPLib
import org.qp.android.questopiabundle.GameInterface
import org.qp.android.questopiabundle.dto.LibGameState
import org.qp.android.questopiabundle.dto.LibGenItem
import org.qp.android.questopiabundle.lib.LibIProxy
import org.qp.android.questopiabundle.lib.LibRefIRequest
import org.qp.android.questopiabundle.lib.LibTypeDialog
import org.qp.android.questopiabundle.lib.LibTypeWindow
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
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts

class LibAlphaProxyImpl(
    private val context: Context,
    override var gameInterface: GameInterface,
    override var gameState: LibGameState = LibGameState(),
    private var gameRequest: LibRefIRequest = LibRefIRequest()
) : QSPLib(), LibIProxy {

    private val libLock = ReentrantLock()
    private lateinit var libThread: Thread
    @Volatile private lateinit var libHandler: Handler
    @Volatile private var libThreadInit = false
    @Volatile private var gameStartTime: Long = 0L
    @Volatile private var lastMsCountCallTime: Long = 0L
    private val currGameDir: DocumentFile?
        get() = fromUri(context, gameState.gameDirUri)

    private fun runOnQspThread(runnable: Runnable) {
        if (libThreadInit) {
            libHandler.post {
                libLock.withLock { runnable.run() }
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

        gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, """
            Location: $locName
            Action: ${errorData.actIndex}
            Line: ${errorData.intLineNum}
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
        val htmlResult = getNumVarValue("USEHTML", 0)
        val fSizeResult = getNumVarValue("FSIZE", 0)
        val bColorResult = getNumVarValue("BCOLOR", 0)
        val fColorResult = getNumVarValue("FCOLOR", 0)
        val lColorResult = getNumVarValue("LCOLOR", 0)

        val useHtml = htmlResult != 0L
        val newConfig = gameState.interfaceConfig.copy(
            useHtml = useHtml,
            fontSize = fSizeResult,
            backColor = bColorResult,
            fontColor = fColorResult,
            linkColor = lColorResult
        )

        return when {
            newConfig != gameState.interfaceConfig -> {
                gameState = gameState.copy(interfaceConfig = newConfig)
                true
            }
            else -> false
        }
    }

    @OptIn(ExperimentalContracts::class)
    private val actionsList: List<LibGenItem>
        get() {
            if (!isWritableDir(context, currGameDir)) return emptyList()
            val actions = mutableListOf<LibGenItem>()
            val gameDir = currGameDir

            for (element in getActions()) {
                if (element == null) continue
                var tempImagePath = element.image
                val tempText = element.name

                if (isNotEmptyOrBlank(tempImagePath)) {
                    val tempPath = normalizeContentPath(getFilename(tempImagePath))
                    val fileFromPath = gameDir?.child(context, tempPath)
                    if (isWritableFile(context, fileFromPath)) {
                        tempImagePath = fileFromPath.uri.toString()
                    }
                }

                actions.add(LibGenItem(tempText, tempImagePath))
            }

            return actions
        }

    @OptIn(ExperimentalContracts::class)
    private val objectsList: List<LibGenItem>
        get() {
            if (!isWritableDir(context, currGameDir)) return emptyList()
            val objects = mutableListOf<LibGenItem>()
            val gameDir = currGameDir

            for (element in getObjects()) {
                if (element == null) continue
                var tempImagePath = element.image ?: ""
                val tempText = element.name ?: ""

                if (tempText.contains("<img")) {
                    if (!isContainsHtmlTags(tempText)) {
                        val fileFromPath = gameDir?.child(context, tempText)
                        if (isWritableFile(context, fileFromPath)) {
                            tempImagePath = fileFromPath.uri.toString()
                        }
                    } else {
                        val tempPath = getSrcDir(tempText)
                        val fileFromPath = gameDir?.child(context, tempPath)
                        if (isWritableFile(context, fileFromPath)) {
                            tempImagePath = fileFromPath.uri.toString()
                        }
                    }
                }

                objects.add(LibGenItem(tempText, tempImagePath))
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
                    libHandler = HandlerCompat.createAsync(Looper.myLooper()!!)
                    libThreadInit = true
                    Looper.loop()
                    terminate()
                } catch (t: Throwable) {
                    Thread.currentThread().interrupt()
                }
            }
        }, "libQSP")
        libThread.start()
    }

    override fun stopLibThread() {
        if (libThreadInit) {
            val handler = libHandler
            handler.looper.quitSafely()
            libThreadInit = false
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

    override fun onRefreshInt(isForced: Boolean) {
        gameState = if (isForced) {
            gameState.copy(
                mainDesc = mainDesc,
                varsDesc = varsDesc,
                actionsList = actionsList,
                objectsList = objectsList
            )
        } else {
            val newState = gameState.copy(
                mainDesc = if (isMainDescChanged) mainDesc else gameState.mainDesc,
                varsDesc = if (isVarsDescChanged) varsDesc else gameState.varsDesc,
                actionsList = if (isActsChanged) actionsList else gameState.actionsList,
                objectsList = if (isObjsChanged) objectsList else gameState.objectsList
            )

            when (newState != gameState) {
                true -> newState
                false -> gameState
            }
        }

        val newRequest = gameRequest.copy(
            isIConfigChanged = loadInterfaceConfiguration(),
            isMainDescChanged = isMainDescChanged,
            isVarsDescChanged = isVarsDescChanged,
            isActionsChanged = isActsChanged,
            isObjectsChanged = isObjsChanged
        )

        if (newRequest != gameRequest) {
            gameRequest = newRequest
            gameInterface.doUpdateState(newRequest)
        }
    }

    @OptIn(ExperimentalContracts::class)
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

    @OptIn(ExperimentalContracts::class)
    override fun onPlayFile(file: String, volume: Int) {
        if (!isNotEmptyOrBlank(file)) return
        gameInterface.playFile(file, volume)
    }

    @OptIn(ExperimentalContracts::class)
    override fun onIsPlayingFile(file: String): Boolean {
        return isNotEmptyOrBlank(file) && gameInterface.isPlayingFile(file)
    }

    @OptIn(ExperimentalContracts::class)
    override fun onCloseFile(file: String) {
        if (isNotEmptyOrBlank(file)) {
            gameInterface.closeFile(file)
        } else {
            gameInterface.closeAllFiles()
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun onOpenGameStatus(file: String?) {
        if (file == null) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_POPUP_LOAD, null)
        } else {
            try {
                val saveFile = gameInterface.requestReceiveFile(file).toDocumentFile(context)
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
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, ex.toString())
        }
    }

    override fun onShowWindow(type: Int, toShow: Boolean) {
        val windowType = LibTypeWindow.entries[type]
        gameInterface.changeVisWindow(windowType, toShow)
    }

    @OptIn(ExperimentalContracts::class)
    override fun onOpenGame(file: String, isNewGame: Boolean) {
        if (!isNotEmptyOrBlank(file)) return
        val currGameDirUri = currGameDir?.uri ?: return
        val newGameDirUri = gameInterface.requestReceiveFile(file)
        if (newGameDirUri == Uri.EMPTY) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, "Game directory not found: $file")
            return
        }
        if (currGameDirUri != newGameDirUri) {
            gameState = gameState.copy(gameDirUri = newGameDirUri)
            gameInterface.doChangeCurrGameDir(newGameDirUri)
        }
    }
}
