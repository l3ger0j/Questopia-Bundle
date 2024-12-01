package org.qp.android.questopiabundle;

public interface AudioPlayer {

    boolean isPlayingFile(String filePath);

    void closeAllFiles();

    void closeFile(String filePath);

    void playFile(String path, int volume);

}
