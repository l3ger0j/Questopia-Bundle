package org.qp.android.questopiabundle;

import android.net.Uri;
import org.qp.android.questopiabundle.AsyncCallbacks;

interface IQuestopiaBundle {
    // API
    String versionPlugin();
    String titlePlugin();
    String authorPlugin();

    // Plugin part
    void startNativeLib();
    void stopNativeLib();
    void runGameIntoLib(long gameId, String gameTitle, in Uri gameDirUri, in Uri gameFileUri);
    void onActionClicked(int index);

    void sendAsync(AsyncCallbacks callbacks);
}