package org.qp.android.questopiabundle;

import android.net.Uri;

import org.qp.android.questopiabundle.lib.LibRefIRequest;
import org.qp.android.questopiabundle.lib.LibTypeDialog;
import org.qp.android.questopiabundle.lib.LibTypeWindow;

public interface GameInterface {

    boolean isPlayingFile(String filePath);

    void closeAllFiles();

    void closeFile(String filePath);

    void playFile(String path, int volume);

    void doChangeCurrGameDir(Uri newGameDirUri);

    void doRefresh(LibRefIRequest request);

    LibDialogRetValue showLibDialog(LibTypeDialog dialog, String inputString);

    void changeVisWindow(LibTypeWindow type, boolean show);

    /**
     * Set the counter location processing interval to <code>delayMillis</code> milliseconds.
     */
    void setCountInter(int delayMillis);

    /**
     * Execute <code>runnable</code> without processing the location counter.
     */
    void doWithCounterDisabled(Runnable runnable);

}
