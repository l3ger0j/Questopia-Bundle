package org.qp.android.questopiabundle;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.anggrayudi.storage.file.DocumentFileCompat;

import org.qp.android.questopiabundle.lib.LibGameRequest;
import org.qp.android.questopiabundle.lib.LibNDKProxyImpl;
import org.qp.android.questopiabundle.lib.LibProxyImpl;
import org.qp.android.questopiabundle.lib.LibRefIRequest;
import org.qp.android.questopiabundle.lib.LibTypeDialog;
import org.qp.android.questopiabundle.lib.LibTypeWindow;

public class QuestopiaBundle extends Service implements GameInterface {

    private final Handler counterHandler = new Handler();
    private final Handler counterNDKHandler = new Handler();
    private LibProxyImpl libProxy;
    private LibNDKProxyImpl libNDKProxy;
    private AsyncCallbacks callbacks;
    private volatile int counterInterval = 500;
    private final Runnable counterTask = new Runnable() {
        @Override
        public void run() {
            libProxy.executeCounter();
            counterHandler.postDelayed(this, counterInterval);
        }
    };
    private final Runnable counterNDKTask = new Runnable() {
        @Override
        public void run() {
            libNDKProxy.executeCounter();
            counterNDKHandler.postDelayed(this, counterInterval);
        }
    };
    private int mLibVersion;

    @Override
    public boolean isPlayingFile(String filePath) {
        try {
            if (callbacks == null) return false;
            return callbacks.isPlayingFile(filePath);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error", e);
            return false;
        }
    }

    @Override
    public void closeAllFiles() {
        try {
            if (callbacks == null) return;
            callbacks.closeAllFiles();
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error", e);
        }
    }

    @Override
    public void closeFile(String filePath) {
        try {
            if (callbacks == null) return;
            callbacks.closeFile(filePath);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error", e);
        }
    }

    @Override
    public void playFile(String path, int volume) {
        try {
            if (callbacks == null) return;
            callbacks.playFile(path, volume);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error", e);
        }
    }

    public void setCallback() {
        if (mLibVersion == 570) {
            counterNDKHandler.postDelayed(counterNDKTask, counterInterval);
        } else if (mLibVersion == 592) {
            counterHandler.postDelayed(counterTask, counterInterval);
        }
    }

    public void removeCallback() {
        counterHandler.removeCallbacks(counterTask);
        counterNDKHandler.removeCallbacks(counterNDKTask);
    }

    @Override
    public void doChangeCurrGameDir(Uri newGameDirUri) {
        try {
            if (callbacks == null) return;
            callbacks.sendChangeCurrGameDir(newGameDirUri);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error", e);
        }
    }

    @Override
    public void doRefresh(LibRefIRequest request) {
        try {
            if (callbacks == null) return;
            if (mLibVersion == 570) {
                callbacks.sendLibRef(new LibResult<>(request));
                callbacks.sendLibGameState(new LibResult<>(libNDKProxy.getGameState()));
            } else if (mLibVersion == 592) {
                callbacks.sendLibRef(new LibResult<>(request));
                callbacks.sendLibGameState(new LibResult<>(libProxy.getGameState()));
            }
        } catch (Exception e) {
            Log.e("QuestopiaBundle", "Error", e);
        }
    }

    @Override
    public LibDialogRetValue showLibDialog(LibTypeDialog dialog, String inputMessage) {
        var wrap = new LibDialogRetValue();

        if (callbacks == null) {
            if (dialog == LibTypeDialog.DIALOG_MENU) {
                wrap.outNumValue = -1;
            } else {
                wrap.outTextValue = "";
            }
            return wrap;
        }

        try {
            return callbacks.doOnShowDialog(new LibResult<>(dialog), inputMessage);
        } catch (RemoteException e) {
            Log.e("QuestopiaBundle", "Error", e);
        }

        if (dialog == LibTypeDialog.DIALOG_MENU) {
            wrap.outNumValue = -1;
        } else {
            wrap.outTextValue = "";
        }
        return wrap;
    }

    @Override
    public void changeVisWindow(LibTypeWindow type, boolean show) {
        if (callbacks == null) return;
        try {
            callbacks.doChangeVisWindow(new LibResult<>(type), show);
        } catch (RemoteException e) {
            Log.e("QuestopiaBundle", "Error", e);
        }
    }

    @Override
    public void setCountInter(int delayMillis) {
        counterInterval = delayMillis;
    }

    @Override
    public void doWithCounterDisabled(Runnable runnable) {
        if (mLibVersion == 570) {
            counterNDKHandler.removeCallbacks(counterNDKTask);
            runnable.run();
            counterNDKHandler.postDelayed(counterNDKTask, counterInterval);
        } else if (mLibVersion == 592) {
            counterHandler.removeCallbacks(counterTask);
            runnable.run();
            counterHandler.postDelayed(counterTask, counterInterval);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IQuestopiaBundle.Stub() {
            @Override
            public String versionPlugin() throws RemoteException {
                return "0.0.1";
            }

            @Override
            public String titlePlugin() throws RemoteException {
                return "Questopia Bundle";
            }

            @Override
            public String authorPlugin() throws RemoteException {
                return "l3ger0j";
            }

            @Override
            public void startNativeLib(int libVer) throws RemoteException {
                mLibVersion = libVer;
                if (mLibVersion == 570) {
                    libNDKProxy = new LibNDKProxyImpl(QuestopiaBundle.this);
                    setCallback();

                    libNDKProxy.setGameInterface(QuestopiaBundle.this);
                    libNDKProxy.startLibThread();
                } else if (mLibVersion == 592) {
                    libProxy = new LibProxyImpl(QuestopiaBundle.this);
                    setCallback();

                    libProxy.setGameInterface(QuestopiaBundle.this);
                    libProxy.startLibThread();
                }
            }

            @Override
            public void stopNativeLib(int libVer) throws RemoteException {
                mLibVersion = libVer;
                if (mLibVersion == 570) {
                    libNDKProxy.setGameInterface(null);
                    libNDKProxy.stopLibThread();
                    libNDKProxy = null;

                    stopSelf();
                } else if (mLibVersion == 592) {
                    libProxy.setGameInterface(null);
                    libProxy.stopLibThread();
                    libProxy = null;

                    stopSelf();
                }
            }

            @Override
            public void runGameIntoLib(long gameId,
                                       String gameTitle,
                                       Uri gameDirUri,
                                       Uri gameFileUri) throws RemoteException {
                Log.i(this.getClass().getSimpleName(), String.valueOf(DocumentFileCompat.getAccessibleAbsolutePaths(getBaseContext())));
                Log.d(this.getClass().getSimpleName(), "Debug: " + "\nGameID|" + gameId + "\nGameTitle|" + gameTitle + "\nGameDirUri|" + gameDirUri + "\nGameFileUri|" + gameFileUri);
                if (mLibVersion == 570) {
                    libNDKProxy.runGame(gameId, gameTitle, gameDirUri, gameFileUri);
                } else if (mLibVersion == 592) {
                    libProxy.runGame(gameId, gameTitle, gameDirUri, gameFileUri);
                }
            }

            @Override
            public void onActionClicked(int index) throws RemoteException {
                if (mLibVersion == 570) {
                    libNDKProxy.onActionClicked(index);
                } else if (mLibVersion == 592) {
                    libProxy.onActionClicked(index);
                }
            }

            @Override
            public void onObjectClicked(int index) throws RemoteException {
                if (mLibVersion == 570) {
                    libNDKProxy.onObjectSelected(index);
                } else if (mLibVersion == 592) {
                    libProxy.onObjectSelected(index);
                }
            }

            @Override
            public void doLibRequest(LibResult gameRequest, String codeToExec, Uri fileUri) throws RemoteException {
                if (mLibVersion == 570) {
                    var libGameReq = (LibGameRequest) gameRequest.value;
                    switch (libGameReq) {
                        case LOAD_FILE -> doWithCounterDisabled(() -> libNDKProxy.loadGameState(fileUri));
                        case SAVE_FILE -> libNDKProxy.saveGameState(fileUri);
                        case USE_EXECUTOR -> libNDKProxy.onUseExecutorString();
                        case USE_INPUT -> libNDKProxy.onInputAreaClicked();
                        case RESTART_GAME -> libNDKProxy.restartGame();
                        case EXECUTE_CODE -> libNDKProxy.execute(codeToExec);
                    }
                } else if (mLibVersion == 592) {
                    var libGameReq = (LibGameRequest) gameRequest.value;
                    switch (libGameReq) {
                        case LOAD_FILE -> doWithCounterDisabled(() -> libProxy.loadGameState(fileUri));
                        case SAVE_FILE -> libProxy.saveGameState(fileUri);
                        case USE_EXECUTOR -> libProxy.onUseExecutorString();
                        case USE_INPUT -> libProxy.onInputAreaClicked();
                        case RESTART_GAME -> libProxy.restartGame();
                        case EXECUTE_CODE -> libProxy.execute(codeToExec);
                    }
                }
            }

            @Override
            public void sendAsync(AsyncCallbacks callbacks) throws RemoteException {
                QuestopiaBundle.this.callbacks = callbacks;
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        libProxy = null;
        libNDKProxy = null;
        removeCallback();
    }
}
