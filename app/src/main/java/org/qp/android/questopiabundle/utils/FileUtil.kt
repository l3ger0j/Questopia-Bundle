package org.qp.android.questopiabundle.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.FileWrapper
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.child
import com.anggrayudi.storage.file.isWritable
import org.qp.android.questopiabundle.utils.StreamUtil.copy
import java.io.ByteArrayOutputStream
import java.io.IOException

object FileUtil {
    fun isWritableFile(context: Context, file: DocumentFile?): Boolean {
        if (file == null) return false
        val canWrite = file.isWritable(context)
        return file.exists() && file.isFile && canWrite
    }

    fun getFileContents(
        context: Context,
        uriContent: Uri
    ): ByteArray? {
        val resolver = context.contentResolver
        try {
            resolver.openInputStream(uriContent).use { `in` ->
                ByteArrayOutputStream().use { out ->
                    if (`in` != null) {
                        copy(`in`, out)
                    } else {
                        throw NullPointerException()
                    }
                    return out.toByteArray()
                }
            }
        } catch (ex: Exception) {
            // TODO: 04.12.2024 Add logger
            Log.e("FileUtil", "Error reading file: $uriContent", ex)
            return null
        }
    }

    fun documentWrap(inputFile: DocumentFile): FileWrapper.Document {
        return FileWrapper.Document(inputFile)
    }

    fun writeFileContents(
        context: Context,
        uriContent: Uri,
        dataToWrite: ByteArray?
    ) {
        val resolver = context.contentResolver
        try {
            resolver.openOutputStream(uriContent, "w").use { out ->
                if (out != null) {
                    out.write(dataToWrite)
                } else {
                    throw IOException("Input is NULL!")
                }
            }
        } catch (ex: IOException) {
            // TODO: 04.12.2024 Add logger
            Log.e("FileUtil", "Error reading file: $uriContent", ex)
        }
    }

    fun fromRelPath(
        context: Context,
        path: String,
        parentDir: DocumentFile,
        requiresWriteAccess: Boolean
    ): DocumentFile? {
        return parentDir.child(context, path, requiresWriteAccess)
    }

    fun fromFullPath(
        context: Context,
        fullPath: String
    ): DocumentFile? {
        return DocumentFileCompat.fromFullPath(context, fullPath, requiresWriteAccess = true)
    }
}
