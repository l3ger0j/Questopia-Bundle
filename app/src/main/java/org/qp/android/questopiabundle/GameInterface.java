package org.qp.android.questopiabundle;

import org.qp.android.questopiabundle.lib.LibRefIRequest;
import org.qp.android.questopiabundle.lib.LibWindowType;

public interface GameInterface {

    AudioPlayer getAudioPlayer();

    HostApplication getHostApplication();

    void doRefresh(LibRefIRequest request);

    void showDialog(TypeDialog dialog, String inputMessage);

    void showWindow(LibWindowType type, boolean show);

    String showInputDialog(String prompt);

    String showExecutorDialog(String prompt);

    int showMenu();

    void showLoadGamePopup();

    void showSaveGamePopup();

    /**
     * Set the counter location processing interval to <code>delayMillis</code> milliseconds.
     */
    void setCountInter(int delayMillis);

    /**
     * Execute <code>runnable</code> without processing the location counter.
     */
    void doWithCounterDisabled(Runnable runnable);

    enum TypeDialog {
        DIALOG_ERROR,
        DIALOG_PICTURE,
        DIALOG_MESSAGE
    }
}
