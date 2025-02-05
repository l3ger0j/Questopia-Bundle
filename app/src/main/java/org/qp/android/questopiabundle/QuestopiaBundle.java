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
import org.qp.android.questopiabundle.lib.impl.LibBravoProxyImpl;
import org.qp.android.questopiabundle.lib.impl.LibAlphaProxyImpl;
import org.qp.android.questopiabundle.lib.LibRefIRequest;
import org.qp.android.questopiabundle.lib.LibTypeDialog;
import org.qp.android.questopiabundle.lib.LibTypeWindow;
import org.qp.android.questopiabundle.lib.impl.LibCharlieProxyImpl;

public class QuestopiaBundle extends Service implements GameInterface {

    private final Handler counterHandler = new Handler();
    private final Handler counterNDKHandler = new Handler();
    private final Handler counterSNXHandler = new Handler();
    private LibAlphaProxyImpl libAlphaProxy;
    private LibBravoProxyImpl libBravoProxy;
    private LibCharlieProxyImpl libCharlieProxy;
    private AsyncCallbacks callbacks;
    private volatile int counterInterval = 500;
    private final Runnable counterTask = new Runnable() {
        @Override
        public void run() {
            libAlphaProxy.executeCounter();
            counterHandler.postDelayed(this, counterInterval);
        }
    };
    private final Runnable counterNDKTask = new Runnable() {
        @Override
        public void run() {
            libBravoProxy.executeCounter();
            counterNDKHandler.postDelayed(this, counterInterval);
        }
    };
    private final Runnable counterSNXTask = new Runnable() {
        @Override
        public void run() {
            libCharlieProxy.executeCounter();
            counterSNXHandler.postDelayed(this, counterInterval);
        }
    };
    private volatile int mLibVersion;

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
        switch (mLibVersion) {
            case 570 -> counterNDKHandler.postDelayed(counterNDKTask, counterInterval);
            case 575 -> counterSNXHandler.postDelayed(counterSNXTask, counterInterval);
            case 592 -> counterHandler.postDelayed(counterTask, counterInterval);
        }
    }

    public void removeCallback() {
        counterHandler.removeCallbacks(counterTask);
        counterNDKHandler.removeCallbacks(counterNDKTask);
        counterSNXHandler.removeCallbacks(counterSNXTask);
    }

    @Override
    public void doChangeCurrGameDir(Uri newGameDirUri) {
        if (callbacks == null) return;

        try {
            callbacks.sendChangeCurrGameDir(newGameDirUri);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error", e);
        }
    }

    @Override
    public void doRefresh(LibRefIRequest request) {
        if (callbacks == null) return;

        try {
            switch (mLibVersion) {
                case 570 -> {
                    callbacks.sendLibRef(new LibResult<>(request));
                    callbacks.sendLibGameState(new LibResult<>(libBravoProxy.getGameState()));
                }
                case 575 -> {
                    callbacks.sendLibRef(new LibResult<>(request));
                    callbacks.sendLibGameState(new LibResult<>(libCharlieProxy.getGameState()));
                }
                case 592 -> {
                    callbacks.sendLibRef(new LibResult<>(request));
                    callbacks.sendLibGameState(new LibResult<>(libAlphaProxy.getGameState()));
                }
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
        switch (mLibVersion) {
            case 570 -> {
                counterNDKHandler.removeCallbacks(counterNDKTask);
                runnable.run();
                counterNDKHandler.postDelayed(counterNDKTask, counterInterval);
            }
            case 575 -> {
                counterSNXHandler.removeCallbacks(counterSNXTask);
                runnable.run();
                counterSNXHandler.postDelayed(counterSNXTask, counterInterval);
            }
            case 592 -> {
                counterHandler.removeCallbacks(counterTask);
                runnable.run();
                counterHandler.postDelayed(counterTask, counterInterval);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IQuestopiaBundle.Stub() {
            @Override
            public String versionPlugin() throws RemoteException {
                return BuildConfig.VERSION_NAME;
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
                switch (mLibVersion) {
                    case 570 -> {
                        libBravoProxy = new LibBravoProxyImpl(QuestopiaBundle.this);
                        setCallback();

                        libBravoProxy.setGameInterface(QuestopiaBundle.this);
                        libBravoProxy.startLibThread();
                    }
                    case 575 -> {
                        libCharlieProxy = new LibCharlieProxyImpl(QuestopiaBundle.this);
                        setCallback();

                        libCharlieProxy.setGameInterface(QuestopiaBundle.this);
                        libCharlieProxy.startLibThread();
                    }
                    case 592 -> {
                        libAlphaProxy = new LibAlphaProxyImpl(QuestopiaBundle.this);
                        setCallback();

                        libAlphaProxy.setGameInterface(QuestopiaBundle.this);
                        libAlphaProxy.startLibThread();
                    }
                }
            }

            @Override
            public void stopNativeLib(int libVer) throws RemoteException {
                mLibVersion = libVer;
                switch (mLibVersion) {
                    case 570 -> {
                        libBravoProxy.setGameInterface(null);
                        libBravoProxy.stopLibThread();
                        libBravoProxy = null;
                    }
                    case 575 -> {
                        libCharlieProxy.setGameInterface(null);
                        libCharlieProxy.stopLibThread();
                        libCharlieProxy = null;
                    }
                    case 592 -> {
                        libAlphaProxy.setGameInterface(null);
                        libAlphaProxy.stopLibThread();
                        libAlphaProxy = null;
                    }
                }
                stopSelf();
            }

            @Override
            public void runGameIntoLib(long gameId,
                                       String gameTitle,
                                       Uri gameDirUri,
                                       Uri gameFileUri) throws RemoteException {
                Log.i(this.getClass().getSimpleName(), String.valueOf(DocumentFileCompat.getAccessibleAbsolutePaths(getBaseContext())));
                Log.d(this.getClass().getSimpleName(), "Debug: " + "\nGameID|" + gameId + "\nGameTitle|" + gameTitle + "\nGameDirUri|" + gameDirUri + "\nGameFileUri|" + gameFileUri);
                switch (mLibVersion) {
                    case 570 -> libBravoProxy.runGame(gameId, gameTitle, gameDirUri, gameFileUri);
                    case 575 -> libCharlieProxy.runGame(gameId, gameTitle, gameDirUri, gameFileUri);
                    case 592 -> libAlphaProxy.runGame(gameId, gameTitle, gameDirUri, gameFileUri);
                }
            }

            @Override
            public void onActionClicked(int index) throws RemoteException {
                switch (mLibVersion) {
                    case 570 -> libBravoProxy.onActionClicked(index);
                    case 575 -> libCharlieProxy.onActionClicked(index);
                    case 592 -> libAlphaProxy.onActionClicked(index);
                }
            }

            @Override
            public void onObjectClicked(int index) throws RemoteException {
                switch (mLibVersion) {
                    case 570 -> libBravoProxy.onObjectSelected(index);
                    case 575 -> libCharlieProxy.onObjectSelected(index);
                    case 592 -> libAlphaProxy.onObjectSelected(index);
                }
            }

            @Override
            public void doLibRequest(LibResult gameRequest, String codeToExec, Uri fileUri) throws RemoteException {
                switch (mLibVersion) {
                    case 570 -> {
                        var libGameReq = (LibGameRequest) gameRequest.value;
                        switch (libGameReq) {
                            case LOAD_FILE -> doWithCounterDisabled(() -> libBravoProxy.loadGameState(fileUri));
                            case SAVE_FILE -> libBravoProxy.saveGameState(fileUri);
                            case USE_EXECUTOR -> libBravoProxy.onUseExecutorString();
                            case USE_INPUT -> libBravoProxy.onInputAreaClicked();
                            case RESTART_GAME -> libBravoProxy.restartGame();
                            case EXECUTE_CODE -> libBravoProxy.execute(codeToExec);
                        }
                    }
                    case 575 -> {
                        var libGameReq = (LibGameRequest) gameRequest.value;
                        switch (libGameReq) {
                            case LOAD_FILE -> doWithCounterDisabled(() -> libCharlieProxy.loadGameState(fileUri));
                            case SAVE_FILE -> libCharlieProxy.saveGameState(fileUri);
                            case USE_EXECUTOR -> libCharlieProxy.onUseExecutorString();
                            case USE_INPUT -> libCharlieProxy.onInputAreaClicked();
                            case RESTART_GAME -> libCharlieProxy.restartGame();
                            case EXECUTE_CODE -> libCharlieProxy.execute(codeToExec);
                        }
                    }
                    case 592 -> {
                        var libGameReq = (LibGameRequest) gameRequest.value;
                        switch (libGameReq) {
                            case LOAD_FILE -> doWithCounterDisabled(() -> libAlphaProxy.loadGameState(fileUri));
                            case SAVE_FILE -> libAlphaProxy.saveGameState(fileUri);
                            case USE_EXECUTOR -> libAlphaProxy.onUseExecutorString();
                            case USE_INPUT -> libAlphaProxy.onInputAreaClicked();
                            case RESTART_GAME -> libAlphaProxy.restartGame();
                            case EXECUTE_CODE -> libAlphaProxy.execute(codeToExec);
                        }
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
        libAlphaProxy = null;
        libBravoProxy = null;
        libCharlieProxy = null;
        removeCallback();
    }
}
