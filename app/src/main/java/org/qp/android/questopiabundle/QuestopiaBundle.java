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

import org.qp.android.questopiabundle.lib.LibGameState;
import org.qp.android.questopiabundle.lib.LibProxyImpl;
import org.qp.android.questopiabundle.lib.LibRefIRequest;
import org.qp.android.questopiabundle.lib.LibWindowType;

public class QuestopiaBundle extends Service implements GameInterface {

    private final LibProxyImpl libProxy = new LibProxyImpl(this);
    private AsyncCallbacks callbacks;

    private final Handler counterHandler = new Handler();
    private final Runnable counterTask = new Runnable() {
        @Override
        public void run() {
            libProxy.executeCounter();
            counterHandler.postDelayed(this, counterInterval);
        }
    };
    private int counterInterval = 500;


    @Override
    public AudioPlayer getAudioPlayer() {
        return new AudioPlayer() {
            @Override
            public boolean isPlayingFile(String filePath) {
                return true;
            }

            @Override
            public void closeAllFiles() {

            }

            @Override
            public void closeFile(String filePath) {

            }

            @Override
            public void playFile(String path, int volume) {

            }
        };
    }

    public void setCallback() {
        counterHandler.postDelayed(counterTask , counterInterval);
    }

    public void removeCallback() {
        counterHandler.removeCallbacks(counterTask);
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
            callbacks.sendLibRef(new LibResult<>(request, LibRefIRequest.class));
            callbacks.sendLibGameState(new LibResult<>(libProxy.getGameState(), LibGameState.class));
        } catch (Exception e) {
            Log.e("QuestopiaBundle", "Error", e);
        }
    }

    @Override
    public void showDialog(TypeDialog dialog, String inputMessage) {
        Log.d("QuestopiaBundle", String.valueOf(inputMessage));
    }

    @Override
    public void showWindow(LibWindowType type, boolean show) {

    }

    @Override
    public String showInputDialog(String prompt) {
        if (callbacks == null) return "0";
        try {
            return callbacks.doOnShowInputDialog(prompt);
        } catch (RemoteException e) {
            Log.e("QuestopiaBundle", "Error", e);
        }
        return "0";
    }

    @Override
    public String showExecutorDialog(String prompt) {
        return "";
    }

    @Override
    public int showMenu() {
        return -1;
    }

    @Override
    public void showLoadGamePopup() {

    }

    @Override
    public void showSaveGamePopup() {

    }

    @Override
    public void setCountInter(int delayMillis) {
        counterInterval = delayMillis;
    }

    @Override
    public void doWithCounterDisabled(Runnable runnable) {
        counterHandler.removeCallbacks(counterTask);
        runnable.run();
        counterHandler.postDelayed(counterTask , counterInterval);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        setCallback();

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
            public void startNativeLib() throws RemoteException {
                libProxy.setGameInterface(QuestopiaBundle.this);
                libProxy.startLibThread();
            }

            @Override
            public void stopNativeLib() throws RemoteException {
                libProxy.setGameInterface(null);
                libProxy.stopLibThread();
                stopSelf();
            }

            @Override
            public void runGameIntoLib(long gameId,
                                       String gameTitle,
                                       Uri gameDirUri,
                                       Uri gameFileUri) throws RemoteException {
                Log.i(this.getClass().getSimpleName(), String.valueOf(DocumentFileCompat.getAccessibleAbsolutePaths(getBaseContext())));
                Log.d(this.getClass().getSimpleName(), "Debug: "+"\nGameID|"+gameId+"\nGameTitle|"+gameTitle+"\nGameDirUri|"+gameDirUri+"\nGameFileUri|"+gameFileUri);
                libProxy.runGame(gameId, gameTitle, gameDirUri, gameFileUri);
            }

            @Override
            public void onActionClicked(int index) throws RemoteException {
                libProxy.onActionClicked(index);
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
        removeCallback();
    }
}
