package org.qp.android.questopiabundle.utils;

import androidx.annotation.NonNull;

public final class PathUtil {

    @NonNull
    public static String getFilename(@NonNull String path) {
        var idx = path.lastIndexOf('/');
        return idx == -1 ? path : path.substring(idx + 1);
    }

    /**
     * Leads to the normal form of the path to the game resource (melodies, images).
     *
     * @implNote Removes "./" from the beginning of the path, replaces all occurrences of "\" with "/".
     */
    public static String normalizeContentPath(String path) {
        if (path == null) return null;
        var result = path;
        if (result.startsWith("./")) {
            result = result.substring(2);
        }
        return result.replace("\\", "/");
    }
}
