package org.qp.android.questopiabundle;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import org.qp.android.questopiabundle.lib.LibGameState;
import org.qp.android.questopiabundle.lib.LibProxyImpl;
import org.qp.android.questopiabundle.lib.LibRefIRequest;
import org.qp.android.questopiabundle.lib.LibWindowType;

public class QuestopiaBundle extends Service implements GameInterface {

    private final LibProxyImpl libProxy = new LibProxyImpl(this);
    private LibRefIRequest request = new LibRefIRequest();

    @Override
    public AudioPlayer getAudioPlayer() {
        return new AudioPlayer() {
            @Override
            public boolean isPlayingFile(String filePath) {
                return false;
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

    @Override
    public HostApplication getHostApplication() {
        return gameDirUri -> {
            Log.d("getHostApplication", " "+gameDirUri);
        };
    }

    private void startLib() {
        libProxy.setGameInterface(this);
        libProxy.startLibThread();
    }

    @Override
    public void doRefresh(LibRefIRequest request) {
        this.request = request;
    }

    @Override
    public void showDialog(TypeDialog dialog, String inputMessage) {

    }

    @Override
    public void showWindow(LibWindowType type, boolean show) {

    }

    @Override
    public String showInputDialog(String prompt) {
        return "";
    }

    @Override
    public String showExecutorDialog(String prompt) {
        return "";
    }

    @Override
    public int showMenu() {
        return 0;
    }

    @Override
    public void showLoadGamePopup() {

    }

    @Override
    public void showSaveGamePopup() {

    }

    @Override
    public void setCountInter(int delayMillis) {

    }

    @Override
    public void doWithCounterDisabled(Runnable runnable) {

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
            public void startNativeLib() throws RemoteException {
                startLib();
            }

            @Override
            public void stopNativeLib() throws RemoteException {
                libProxy.setGameInterface(null);
                libProxy.stopLibThread();
            }

            @Override
            public void runGameIntoLib(long gameId,
                                       String gameTitle,
                                       Uri gameDirUri,
                                       Uri gameFileUri,
                                       String gameFullPath) throws RemoteException {
                libProxy.runGame(gameId, gameTitle, gameDirUri, gameFileUri, gameFullPath);
            }

            @Override
            public void sendAsync(AsyncCallbacks callbacks) throws RemoteException {
                callbacks.sendLibGameState(new LibResult<>(libProxy.getGameState(), LibGameState.class));
                callbacks.sendLibRef(new LibResult<>(request, LibRefIRequest.class));
            }
        };
    }
}
