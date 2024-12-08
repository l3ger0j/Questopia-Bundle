package org.qp.android.questopiabundle.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public final class ThreadUtil {

    /**
     * @return <code>true</code> if the current thread is <code>thread</code>, otherwise <code>false</code>
     */
    public static boolean isSameThread(Thread thread) {
        return Thread.currentThread().equals(thread);
    }

    /**
     * Throw <code>IllegalStateException</code>, if the method is called in the main thread
     */
    public static void assertNonUiThread() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new IllegalStateException("This should not be run on the UI thread");
        }
    }

    /**
     * @return <code>true</code> if the current thread is the main one, otherwise <code>false</code>
     */
    public static boolean isMainThread() {
        Log.d("QSP", Thread.currentThread().getName());
//        Thread.currentThread().equals(Looper.getMainLooper().getThread());
        // TODO: 08.12.2024 Refactor this. The service doesn't have a main thread!
        return true;
    }

    /**
     * Throws a <code>RuntimeException</code> if the method is not called from the main thread.
     */
    public static void throwIfNotMainThread() {
        if (!isMainThread()) {
            throw new RuntimeException("Must be called from the main thread");
        }
    }

    public static void runOnUiThread(Runnable action) {
        if (!isMainThread()) {
            var mHandler = new Handler(Looper.getMainLooper());
            mHandler.post(action);
        } else {
            action.run();
        }
    }

}
