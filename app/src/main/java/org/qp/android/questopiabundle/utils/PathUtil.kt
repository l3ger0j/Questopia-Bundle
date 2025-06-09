package org.qp.android.questopiabundle.utils

object PathUtil {
    fun getFilename(path: String): String {
        val idx = path.lastIndexOf('/')
        return if (idx == -1) path else path.substring(idx + 1)
    }

    /**
     * Leads to the normal form of the path to the game resource (melodies, images).
     *
     * @implNote Removes "./" from the beginning of the path, replaces all occurrences of "\" with "/".
     */
    fun normalizeContentPath(path: String?): String {
        if (path == null) return ""
        var result: String = path
        if (result.startsWith("./")) {
            result = result.substring(2)
        }
        return result.replace("\\", "/")
    }
}
