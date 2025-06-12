package org.qp.android.questopiabundle

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.qp.android.questopiabundle.dto.LibGameState
import org.qp.android.questopiabundle.lib.LibGameRequest
import org.qp.android.questopiabundle.lib.LibRefIRequest
import org.qp.android.questopiabundle.lib.LibTypeDialog
import org.qp.android.questopiabundle.lib.LibTypeWindow
import org.qp.android.questopiabundle.lib.impl.LibAlphaProxyImpl
import org.qp.android.questopiabundle.lib.impl.LibBravoProxyImpl
import org.qp.android.questopiabundle.lib.impl.LibCharlieProxyImpl
import kotlin.concurrent.Volatile

class QuestopiaBundle : Service(), GameInterface {
    private lateinit var counterMainJob: Job
    private lateinit var counterNDKJob: Job
    private lateinit var counterSNXJob: Job
    private val libHandlerScope = CoroutineScope(Dispatchers.Default)
    private val libAlphaProxy: LibAlphaProxyImpl = LibAlphaProxyImpl(this@QuestopiaBundle, this)
    private val libBravoProxy: LibBravoProxyImpl = LibBravoProxyImpl(this@QuestopiaBundle, this)
    private val libCharlieProxy: LibCharlieProxyImpl = LibCharlieProxyImpl(this@QuestopiaBundle, this)
    private lateinit var callbacks: AsyncCallbacks

    @Volatile private var counterInterval = 500L
    private val counterMainTask: suspend CoroutineScope.() -> Unit = {
        while (isActive) {
            libAlphaProxy.executeCounter()
            delay(counterInterval)
        }
    }
    private val counterNDKTask: suspend CoroutineScope.() -> Unit = {
        while (isActive) {
            libBravoProxy.executeCounter()
            delay(counterInterval)
        }
    }
    private val counterSNXTask: suspend CoroutineScope.() -> Unit = {
        while (isActive) {
            libCharlieProxy.executeCounter()
            delay(counterInterval)
        }
    }

    @Volatile private var mLibVersion = 570

    override fun requestReceiveFile(filePath: String): Uri {
        try {
            return callbacks.requestReceiveFile(filePath)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
            return Uri.EMPTY
        }
    }

    override fun requestCreateFile(dirUri: Uri, path: String): Uri {
        try {
            return callbacks.requestCreateFile(dirUri, path) ?: Uri.EMPTY
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
            return Uri.EMPTY
        }
    }

    override fun isPlayingFile(filePath: String): Boolean {
        try {
            return callbacks.isPlayingFile(filePath)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
            return false
        }
    }

    override fun closeAllFiles() {
        try {
            callbacks.closeAllFiles()
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
        }
    }

    override fun closeFile(filePath: String?) {
        try {
            callbacks.closeFile(filePath)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
        }
    }

    override fun playFile(path: String?, volume: Int) {
        try {
            callbacks.playFile(path, volume)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
        }
    }

    private fun removeCallback() {
        counterMainJob.cancel()
        counterNDKJob.cancel()
        counterSNXJob.cancel()
    }

    override fun doChangeCurrGameDir(newGameDirUri: Uri?) {
        try {
            callbacks.sendChangeCurrGameDir(newGameDirUri)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
        }
    }

    override fun doUpdateState(request: LibRefIRequest) {
        try {
            when (mLibVersion) {
                570 -> {
                    callbacks.updateState(LibResult<LibRefIRequest?>(request), LibResult<LibGameState?>(libBravoProxy.gameState))
                }

                575 -> {
                    callbacks.updateState(LibResult<LibRefIRequest?>(request), LibResult<LibGameState?>(libCharlieProxy.gameState))
                }

                592 -> {
                    callbacks.updateState(LibResult<LibRefIRequest?>(request), LibResult<LibGameState?>(libAlphaProxy.gameState))
                }
            }
        } catch (e: Exception) {
            Log.e("QuestopiaBundle", "Error", e)
        }
    }

    override fun showLibDialog(dialog: LibTypeDialog?, inputString: String?): LibDialogRetValue? {
        try {
            return callbacks.doOnShowDialog(LibResult(dialog), inputString)
        } catch (e: RemoteException) {
            Log.e("QuestopiaBundle", "Error", e)
            return LibDialogRetValue()
        }
    }

    override fun changeVisWindow(type: LibTypeWindow?, show: Boolean) {
        try {
            callbacks.doChangeVisWindow(LibResult<LibTypeWindow?>(type), show)
        } catch (e: RemoteException) {
            Log.e("QuestopiaBundle", "Error", e)
        }
    }

    override fun setCountInter(delayMillis: Int) {
        counterInterval = delayMillis.toLong()
    }

    override fun doWithCounterDisabled(runnable: Runnable?) {
        when (mLibVersion) {
            570 -> {
                counterNDKJob.cancel()
                runnable?.run()
                counterNDKJob = libHandlerScope.launch(block = counterNDKTask)
            }

            575 -> {
                counterSNXJob.cancel()
                runnable?.run()
                counterSNXJob = libHandlerScope.launch(block = counterSNXTask)
            }

            592 -> {
                counterMainJob.cancel()
                runnable?.run()
                counterMainJob = libHandlerScope.launch(block = counterMainTask)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = object : IQuestopiaBundle.Stub() {
        @Throws(RemoteException::class)
        override fun versionPlugin(): String {
            return BuildConfig.VERSION_NAME
        }

        @Throws(RemoteException::class)
        override fun titlePlugin(): String {
            return "Questopia Bundle"
        }

        @Throws(RemoteException::class)
        override fun authorPlugin(): String {
            return "l3ger0j"
        }

        @Throws(RemoteException::class)
        override fun startNativeLib(libVer: Int) {
            mLibVersion = libVer
            when (mLibVersion) {
                570 -> {
                    counterNDKJob = libHandlerScope.launch(block = counterNDKTask)
                    libBravoProxy.startLibThread()
                }

                575 -> {
                    counterSNXJob = libHandlerScope.launch(block = counterSNXTask)
                    libCharlieProxy.startLibThread()
                }

                592 -> {
                    counterMainJob = libHandlerScope.launch(block = counterMainTask)
                    libAlphaProxy.startLibThread()
                }
            }
        }

        @Throws(RemoteException::class)
        override fun stopNativeLib(libVer: Int) {
            mLibVersion = libVer
            when (mLibVersion) {
                570 -> libBravoProxy.stopLibThread()

                575 -> libCharlieProxy.stopLibThread()

                592 -> libAlphaProxy.stopLibThread()
            }
            removeCallback()
        }

        @Throws(RemoteException::class)
        override fun runGameIntoLib(
            gameId: Long,
            gameTitle: String,
            gameDirUri: Uri,
            gameFileUri: Uri
        ) {
            when (mLibVersion) {
                570 -> libBravoProxy.runGame(gameId, gameTitle, gameDirUri, gameFileUri)

                575 -> libCharlieProxy.runGame(gameId, gameTitle, gameDirUri, gameFileUri)

                592 -> libAlphaProxy.runGame(gameId, gameTitle, gameDirUri, gameFileUri)
            }
        }

        @Throws(RemoteException::class)
        override fun onActionClicked(index: Int) {
            when (mLibVersion) {
                570 -> libBravoProxy.onActionClicked(index)

                575 -> libCharlieProxy.onActionClicked(index)

                592 -> libAlphaProxy.onActionClicked(index)
            }
        }

        @Throws(RemoteException::class)
        override fun onObjectClicked(index: Int) {
            when (mLibVersion) {
                570 -> libBravoProxy.onObjectSelected(index)

                575 -> libCharlieProxy.onObjectSelected(index)

                592 -> libAlphaProxy.onObjectSelected(index)
            }
        }

        @Throws(RemoteException::class)
        override fun doLibRequest(gameRequest: LibResult<*>, codeToExec: String, fileUri: Uri) {
            when (mLibVersion) {
                570 -> {
                    val libGameReq = gameRequest.value as LibGameRequest
                    when (libGameReq) {
                        LibGameRequest.LOAD_FILE -> doWithCounterDisabled {
                            libBravoProxy.loadGameState(fileUri)
                        }

                        LibGameRequest.SAVE_FILE -> libBravoProxy.saveGameState(fileUri)
                        LibGameRequest.USE_EXECUTOR -> libBravoProxy.onUseExecutorString()
                        LibGameRequest.USE_INPUT -> libBravoProxy.onInputAreaClicked()
                        LibGameRequest.RESTART_GAME -> libBravoProxy.restartGame()
                        LibGameRequest.EXECUTE_CODE -> libBravoProxy.execute(codeToExec)
                    }
                }

                575 -> {
                    val libGameReq = gameRequest.value as LibGameRequest
                    when (libGameReq) {
                        LibGameRequest.LOAD_FILE -> doWithCounterDisabled {
                            libCharlieProxy.loadGameState(fileUri)
                        }

                        LibGameRequest.SAVE_FILE -> libCharlieProxy.saveGameState(fileUri)
                        LibGameRequest.USE_EXECUTOR -> libCharlieProxy.onUseExecutorString()
                        LibGameRequest.USE_INPUT -> libCharlieProxy.onInputAreaClicked()
                        LibGameRequest.RESTART_GAME -> libCharlieProxy.restartGame()
                        LibGameRequest.EXECUTE_CODE -> libCharlieProxy.execute(codeToExec)
                    }
                }

                592 -> {
                    val libGameReq = gameRequest.value as LibGameRequest
                    when (libGameReq) {
                        LibGameRequest.LOAD_FILE -> doWithCounterDisabled {
                            libAlphaProxy.loadGameState(fileUri)
                        }

                        LibGameRequest.SAVE_FILE -> libAlphaProxy.saveGameState(fileUri)
                        LibGameRequest.USE_EXECUTOR -> libAlphaProxy.onUseExecutorString()
                        LibGameRequest.USE_INPUT -> libAlphaProxy.onInputAreaClicked()
                        LibGameRequest.RESTART_GAME -> libAlphaProxy.restartGame()
                        LibGameRequest.EXECUTE_CODE -> libAlphaProxy.execute(codeToExec)
                    }
                }
            }
        }

        @Throws(RemoteException::class)
        override fun sendAsync(callbacks: AsyncCallbacks?) {
            this@QuestopiaBundle.callbacks = callbacks ?: return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeCallback()
    }
}
