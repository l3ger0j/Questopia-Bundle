package org.qp.android.questopiabundle.utils;

public final class ThreadUtil {

    /**
     * @return <code>true</code> if the current thread is <code>thread</code>, otherwise <code>false</code>
     */
    public static boolean isSameThread(Thread thread) {
        return Thread.currentThread().equals(thread);
    }

}
