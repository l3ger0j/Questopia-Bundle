package org.qp.android.questopiabundle.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.NonNull
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.FileWrapper
import com.anggrayudi.storage.file.CreateMode
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.child
import com.anggrayudi.storage.file.isWritable
import com.anggrayudi.storage.file.makeFile
import org.qp.android.questopiabundle.utils.StreamUtil.copy
import java.io.ByteArrayOutputStream
import java.io.IOException

object FileUtil {

    @JvmStatic
    fun isWritableFile(context: Context, file: DocumentFile?): Boolean {
        if (file == null) return false
        val canWrite = file.isWritable(context)
        return file.exists() && file.isFile && canWrite
    }

    @JvmStatic
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

    @JvmStatic
    fun documentWrap(inputFile: DocumentFile): FileWrapper.Document {
        return FileWrapper.Document(inputFile)
    }

    @JvmStatic
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

    @JvmStatic
    fun findOrCreateFile(
        context: Context,
        srcDir: DocumentFile,
        name: String,
        mimeType: String?
    ): DocumentFile? {
        return srcDir.makeFile(context, name, mimeType, CreateMode.REUSE)
    }

    @JvmStatic
    fun fromRelPath(
        context: Context,
        path: String,
        parentDir: DocumentFile,
        requiresWriteAccess: Boolean
    ): DocumentFile? {
        return parentDir.child(context, path, requiresWriteAccess)
    }

    @JvmStatic
    fun fromFullPath(
        context: Context,
        fullPath: String
    ): DocumentFile? {
        return DocumentFileCompat.fromFullPath(context, fullPath, requiresWriteAccess = true)
    }
}
