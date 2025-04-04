package org.qp.android.questopiabundle

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import com.anggrayudi.storage.file.DocumentFileCompat.getAccessibleAbsolutePaths
import org.qp.android.questopiabundle.lib.LibGameRequest
import org.qp.android.questopiabundle.lib.LibGameState
import org.qp.android.questopiabundle.lib.LibRefIRequest
import org.qp.android.questopiabundle.lib.LibTypeDialog
import org.qp.android.questopiabundle.lib.LibTypeWindow
import org.qp.android.questopiabundle.lib.impl.LibAlphaProxyImpl
import org.qp.android.questopiabundle.lib.impl.LibBravoProxyImpl
import org.qp.android.questopiabundle.lib.impl.LibCharlieProxyImpl
import kotlin.concurrent.Volatile

class QuestopiaBundle : Service(), GameInterface {
    private val counterHandler = Handler(Looper.myLooper() ?: Looper.getMainLooper())
    private val counterNDKHandler = Handler(Looper.myLooper() ?: Looper.getMainLooper())
    private val counterSNXHandler = Handler(Looper.myLooper() ?: Looper.getMainLooper())
    private val libAlphaProxy: LibAlphaProxyImpl = LibAlphaProxyImpl(this@QuestopiaBundle)
    private val libBravoProxy: LibBravoProxyImpl = LibBravoProxyImpl(this@QuestopiaBundle)
    private val libCharlieProxy: LibCharlieProxyImpl = LibCharlieProxyImpl(this@QuestopiaBundle)
    private var callbacks: AsyncCallbacks? = null

    @Volatile
    private var counterInterval = 500
    private val counterTask: Runnable = object : Runnable {
        override fun run() {
            libAlphaProxy.executeCounter()
            counterHandler.postDelayed(this, counterInterval.toLong())
        }
    }
    private val counterNDKTask: Runnable = object : Runnable {
        override fun run() {
            libBravoProxy.executeCounter()
            counterNDKHandler.postDelayed(this, counterInterval.toLong())
        }
    }
    private val counterSNXTask: Runnable = object : Runnable {
        override fun run() {
            libCharlieProxy.executeCounter()
            counterSNXHandler.postDelayed(this, counterInterval.toLong())
        }
    }

    @Volatile
    private var mLibVersion = 0

    override fun requestPermFile(pathUri: Uri) {
        try {
            callbacks?.requestPermOnFile(pathUri)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
        }
    }

    override fun requestCreateFile(dirUri: Uri, path: String): Uri {
        try {
            return callbacks?.requestCreateFile(dirUri, path) ?: Uri.EMPTY
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
            return Uri.EMPTY
        }
    }

    override fun isPlayingFile(filePath: String): Boolean {
        try {
            return callbacks?.isPlayingFile(filePath) ?: false
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
            return false
        }
    }

    override fun closeAllFiles() {
        try {
            callbacks?.closeAllFiles()
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
        }
    }

    override fun closeFile(filePath: String?) {
        try {
            callbacks?.closeFile(filePath)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
        }
    }

    override fun playFile(path: String?, volume: Int) {
        try {
            callbacks?.playFile(path, volume)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
        }
    }

    private fun removeCallback() {
        counterHandler.removeCallbacks(counterTask)
        counterNDKHandler.removeCallbacks(counterNDKTask)
        counterSNXHandler.removeCallbacks(counterSNXTask)
    }

    override fun doChangeCurrGameDir(newGameDirUri: Uri?) {
        try {
            callbacks?.sendChangeCurrGameDir(newGameDirUri)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Error", e)
        }
    }

    override fun doRefresh(request: LibRefIRequest?) {
        try {
            when (mLibVersion) {
                570 -> {
                    callbacks?.sendLibRef(LibResult<LibRefIRequest?>(request))
                    callbacks?.sendLibGameState(LibResult<LibGameState?>(libBravoProxy.gameState))
                }

                575 -> {
                    callbacks?.sendLibRef(LibResult<LibRefIRequest?>(request))
                    callbacks?.sendLibGameState(LibResult<LibGameState?>(libCharlieProxy.gameState))
                }

                592 -> {
                    callbacks?.sendLibRef(LibResult<LibRefIRequest?>(request))
                    callbacks?.sendLibGameState(LibResult<LibGameState?>(libAlphaProxy.gameState))
                }
            }
        } catch (e: Exception) {
            Log.e("QuestopiaBundle", "Error", e)
        }
    }

    override fun showLibDialog(dialog: LibTypeDialog?, inputString: String?): LibDialogRetValue? {
        try {
            return callbacks?.doOnShowDialog(LibResult(dialog), inputString)
        } catch (e: RemoteException) {
            Log.e("QuestopiaBundle", "Error", e)
            return LibDialogRetValue()
        }
    }

    override fun changeVisWindow(type: LibTypeWindow?, show: Boolean) {
        try {
            callbacks?.doChangeVisWindow(LibResult<LibTypeWindow?>(type), show)
        } catch (e: RemoteException) {
            Log.e("QuestopiaBundle", "Error", e)
        }
    }

    override fun setCountInter(delayMillis: Int) {
        counterInterval = delayMillis
    }

    override fun doWithCounterDisabled(runnable: Runnable?) {
        when (mLibVersion) {
            570 -> {
                counterNDKHandler.removeCallbacks(counterNDKTask)
                runnable?.run()
                counterNDKHandler.postDelayed(counterNDKTask, counterInterval.toLong())
            }

            575 -> {
                counterSNXHandler.removeCallbacks(counterSNXTask)
                runnable?.run()
                counterSNXHandler.postDelayed(counterSNXTask, counterInterval.toLong())
            }

            592 -> {
                counterHandler.removeCallbacks(counterTask)
                runnable?.run()
                counterHandler.postDelayed(counterTask, counterInterval.toLong())
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
                    counterNDKHandler.postDelayed(counterNDKTask, counterInterval.toLong())

                    libBravoProxy.setGameInterface(this@QuestopiaBundle)
                    libBravoProxy.startLibThread()
                }

                575 -> {
                    counterSNXHandler.postDelayed(counterSNXTask, counterInterval.toLong())

                    libCharlieProxy.setGameInterface(this@QuestopiaBundle)
                    libCharlieProxy.startLibThread()
                }

                592 -> {
                    counterHandler.postDelayed(counterTask, counterInterval.toLong())

                    libAlphaProxy.setGameInterface(this@QuestopiaBundle)
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
            stopSelf()
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
            this@QuestopiaBundle.callbacks = callbacks
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeCallback()
    }
}
