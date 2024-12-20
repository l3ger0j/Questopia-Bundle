package org.qp.android.questopiabundle.lib;

import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import org.qp.android.questopiabundle.GameInterface;

public interface LibIProxy {
    /**
     * Starts the library thread.
     */
    void startLibThread();

    /**
     * Stops the library thread.
     */
    void stopLibThread();

    void enableDebugMode(boolean isDebug);

    void runGame(long gameId, String gameTitle, Uri gameDirUri, Uri gameFileUri);

    void restartGame();

    void loadGameState(Uri uri);

    void saveGameState(Uri uri);

    void onActionClicked(int index);

    void onObjectSelected(int index);

    void onInputAreaClicked();

    void onUseExecutorString();

    /**
     * Starts execution of the specified line of code in the library.
     */
    void execute(String code);

    /**
     * Starts processing the location counter in the library.
     */
    void executeCounter();

    LibGameState getGameState();

    void setGameInterface(GameInterface view);

}
