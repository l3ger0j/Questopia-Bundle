package org.qp.android.questopiabundle

import org.qp.android.questopiabundle.lib.LibRefIRequest
import org.qp.android.questopiabundle.lib.LibTypeDialog
import org.qp.android.questopiabundle.lib.LibTypeWindow

interface GameInterface {
    fun requestReceiveFile(filePath: String)
    fun requestCreateFile(path: String, mimeType: String)
    fun isPlayingFile(filePath: String)
    fun closeAllFiles()
    fun closeFile(filePath: String?)
    fun playFile(path: String?, volume: Int)
    fun changeGameDir(filePath: String)
    fun doUpdateState(request: LibRefIRequest)
    fun showLibDialog(dialog: LibTypeDialog?, inputString: String?)
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
