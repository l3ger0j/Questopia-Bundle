package org.qp.android.questopiabundle

import android.net.Uri
import org.qp.android.questopiabundle.lib.LibRefIRequest
import org.qp.android.questopiabundle.lib.LibTypeDialog
import org.qp.android.questopiabundle.lib.LibTypeWindow

interface GameInterface {
    fun requestReceiveFile(filePath: String): Uri
    fun requestCreateFile(path: String): Uri
    fun isPlayingFile(filePath: String): Boolean
    fun closeAllFiles()
    fun closeFile(filePath: String?)
    fun playFile(path: String?, volume: Int)
    fun changeGameDir(filePath: String)
    fun doUpdateState(request: LibRefIRequest)
    fun showLibDialog(dialog: LibTypeDialog?, inputString: String?): LibDialogRetValue?
    fun changeVisWindow(type: LibTypeWindow?, show: Boolean)
    /**
     * Set the counter location processing interval to `delayMillis` milliseconds.
     */
    fun setCountInter(delayMillis: Int)
    /**
     * Execute `runnable` without processing the location counter.
     */
    fun doWithCounterDisabled(runnable: Runnable?)
}
