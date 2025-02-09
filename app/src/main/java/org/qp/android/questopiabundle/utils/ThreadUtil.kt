package org.qp.android.questopiabundle.utils

object ThreadUtil {

    /**
     * @return `true` if the current thread is `thread`, otherwise `false`
     */
    @JvmStatic
    fun isSameThread(thread: Thread): Boolean {
        return Thread.currentThread() == thread
    }
}
