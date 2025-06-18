package org.qp.android.questopiabundle.lib.impl

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.os.HandlerCompat
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.child
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.libndkqsp.jni.NDKLib
import org.qp.android.questopiabundle.GameInterface
import org.qp.android.questopiabundle.LibReturnValue
import org.qp.android.questopiabundle.dto.LibGameState
import org.qp.android.questopiabundle.dto.LibGenItem
import org.qp.android.questopiabundle.lib.LibIProxy
import org.qp.android.questopiabundle.lib.LibRefIRequest
import org.qp.android.questopiabundle.lib.LibTypeDialog
import org.qp.android.questopiabundle.lib.LibTypeWindow
import org.qp.android.questopiabundle.utils.FileUtil.documentWrap
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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.Volatile
import kotlin.concurrent.withLock
import kotlin.contracts.ExperimentalContracts

class LibBravoProxyImpl(
    private val context: Context,
    override var gameInterface: GameInterface,
    override var gameState: LibGameState = LibGameState(),
    override val returnValueFlow: MutableSharedFlow<LibReturnValue> = MutableSharedFlow(),
    private var gameRequest: LibRefIRequest = LibRefIRequest(),
) : NDKLib(), LibIProxy {

    private val libLock = ReentrantLock()
    private lateinit var libThread: Thread
    @Volatile private lateinit var libHandler: Handler
    @Volatile private var libThreadInit = false
    @Volatile private var gameStartTime: Long = 0L
    @Volatile private var lastMsCountCallTime: Long = 0L
    private val currGameDir: DocumentFile?
        get() = fromUri(context, gameState.gameDirUri)

    private val mutableMenuItemList: MutableList<LibGenItem> = mutableListOf()

    private fun runOnQspThread(runnable: Runnable) {
        if (libThreadInit) {
            libHandler.post {
                libLock.withLock { runnable.run() }
            }
        }
    }

    private fun loadGameWorld(): Boolean {
        val gameFileUri = gameState.gameFileUri
        val gameFile = fromUri(context, gameState.gameFileUri) ?: return false
        val gameFileFullPath = documentWrap(gameFile).getAbsolutePath(context)
        val gameData = getFileContents(context, gameFileUri) ?: return false

        if (!QSPLoadGameWorldFromData(gameData, gameFileFullPath)) {
            showLastQspError()
            return false
        }
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

        val htmlResult = QSPGetVarValues("USEHTML", 0)
        val fSizeResult = QSPGetVarValues("FSIZE", 0)
        val bColorResult = QSPGetVarValues("BCOLOR", 0)
        val fColorResult = QSPGetVarValues("FCOLOR", 0)
        val lColorResult = QSPGetVarValues("LCOLOR", 0)

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
                if (element == null) continue
                var tempImagePath = element.image ?: ""
                val tempText = element.text ?: ""

                if (tempImagePath.isEmpty() && tempText.isEmpty()) continue

                if (isNotEmptyOrBlank(tempImagePath)) {
                    val tempPath = normalizeContentPath(getFilename(tempImagePath))
                    val fileFromPath = gameDir.child(context, tempPath)
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
            val gameDir = currGameDir
            if (!isWritableDir(context, gameDir)) return emptyList()

            val objects = mutableListOf<LibGenItem>()
            for (element in QSPGetObjectData() ?: return emptyList()) {
                if (element == null) continue
                var tempImagePath = element.image ?: ""
                val tempText = element.text ?: ""

                if (tempImagePath.isEmpty() && tempText.isEmpty()) continue

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

                objects.add(LibGenItem(tempText, tempImagePath))
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
                    libHandler = HandlerCompat.createAsync(Looper.myLooper()!!)
                    libThreadInit = true
                    Looper.loop()
                    QSPDeInit()
                } catch (t: Throwable) {
                    Thread.currentThread().interrupt()
                }
            }
        }, "libNDKQSP")
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

        val gameData = getFileContents(context, uri) ?: return
        if (!QSPOpenSavedGameFromData(gameData, true)) {
            showLastQspError()
        }
    }

    override fun saveGameState(uri: Uri) {
        if (!isSameThread(libHandler.looper.thread)) {
            runOnQspThread { saveGameState(uri) }
            return
        }

        val gameData = QSPSaveGameAsData(false) ?: return
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
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_INPUT, "userInputTitle")
            val dialogValue = try {
                runBlocking { returnValueFlow.first() }.dialogTextValue
            } catch (e: Exception) {
                ""
            }
            QSPSetInputStrText(dialogValue)
            if (!QSPExecUserInput(true)) {
                showLastQspError()
            }
        }
    }

    override fun onUseExecutorString() {
        runOnQspThread {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_EXECUTOR, "execStringTitle")
            val dialogValue = try {
                runBlocking { returnValueFlow.first() }.dialogTextValue
            } catch (e: Exception) {
                ""
            }
            if (!QSPExecString(dialogValue, true)) {
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
        if (isNotEmptyOrBlank(path)) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_PICTURE, path)
        }
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
        if (!isNotEmptyOrBlank(path)) {
            return false
        } else {
            gameInterface.isPlayingFile(path)
            return try {
                runBlocking { returnValueFlow.first() }.playFileState
            } catch (e: Exception) {
                false
            }
        }
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
            CompletableFuture
                .supplyAsync {
                    gameInterface.requestReceiveFile(filename)
                    runBlocking { returnValueFlow.first() }.fileUri
                }
                .thenAccept {
                    if (it != Uri.EMPTY) {
                        gameInterface.doWithCounterDisabled { loadGameState(it) }
                    } else {
                        gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, "Save file not found")
                    }
                }
                .exceptionally {
                    gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, it.toString())
                    null
                }
        }
    }

    @OptIn(ExperimentalContracts::class)
    override fun SaveGame(filename: String?) {
        if (!isNotEmptyOrBlank(filename)) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_POPUP_SAVE, null)
        } else {
            CompletableFuture
                .supplyAsync {
                    gameInterface.requestCreateFile(filename, MimeType.BINARY_FILE)
                    runBlocking { returnValueFlow.first() }.fileUri
                }
                .thenAccept {
                    if (it != null && it != Uri.EMPTY) {
                        saveGameState(it)
                    } else {
                        gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, "Error access dir")
                    }
                }
                .exceptionally {
                    gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, it.toString())
                    null
                }
        }
    }

    override fun InputBox(prompt: String?): String {
        gameInterface.showLibDialog(LibTypeDialog.DIALOG_INPUT, prompt)
        return try {
            runBlocking { returnValueFlow.first() }.dialogTextValue
        } catch (e: Exception) {
            ""
        }
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
        mutableMenuItemList.add(LibGenItem(name ?: "", imgPath ?: ""))
    }

    override fun ShowMenu() {
        gameState = gameState.copy(menuItemsList = mutableMenuItemList)
        gameInterface.showLibDialog(LibTypeDialog.DIALOG_MENU, null)
        val dialogValue = try {
            runBlocking { returnValueFlow.first() }.dialogNumValue
        } catch (e: Exception) {
            -1
        }
        if (dialogValue != -1) {
            QSPSelectMenuItem(dialogValue)
        }
    }

    override fun DeleteMenu() {
        gameState = gameState.copy(menuItemsList = emptyList())
    }

    override fun Wait(msecs: Int) {
        try {
            Thread.sleep(msecs.toLong())
        } catch (ex: InterruptedException) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, ex.toString())
        }
    }

    override fun ShowWindow(type: Int, isShow: Boolean) {
        val windowType = LibTypeWindow.entries[type]
        gameInterface.changeVisWindow(windowType, isShow)
    }

    @OptIn(ExperimentalContracts::class)
    override fun GetFileContents(path: String?): ByteArray? {
        if (!isNotEmptyOrBlank(path)) return byteArrayOf()

        gameInterface.requestReceiveFile(path)
        val targetFileUri = runBlocking { returnValueFlow.first() }.fileUri
        if (targetFileUri == Uri.EMPTY) return byteArrayOf()

        return getFileContents(context, targetFileUri)
    }

    @OptIn(ExperimentalContracts::class)
    override fun ChangeQuestPath(path: String?) {
        if (isNotEmptyOrBlank(path)) {
            gameInterface.changeGameDir(path)
        }
    }
}
