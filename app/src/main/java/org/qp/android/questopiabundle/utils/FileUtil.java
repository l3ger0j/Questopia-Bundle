package org.qp.android.questopiabundle.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.anggrayudi.storage.FileWrapper;
import com.anggrayudi.storage.file.CreateMode;
import com.anggrayudi.storage.file.DocumentFileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class FileUtil {

    public static boolean isWritableFile(Context context, DocumentFile file) {
        if (file == null) return false;
        var canWrite = DocumentFileUtils.isWritable(file, context);
        return file.exists() && file.isFile() && canWrite;
    }

    @Nullable
    public static byte[] getFileContents(@NonNull Context context,
                                         @NonNull Uri uriContent) {
        var resolver = context.getContentResolver();
        try (var in = resolver.openInputStream(uriContent);
             var out = new ByteArrayOutputStream()) {
            if (in != null) {
                StreamUtil.copy(in, out);
            } else {
                throw new NullPointerException();
            }
            return out.toByteArray();
        } catch (Exception ex) {
            // TODO: 04.12.2024 Add logger
            Log.e("FileUtil", "Error reading file: " + uriContent, ex);
            return null;
        }
    }

    public static FileWrapper.Document documentWrap(DocumentFile inputFile) {
        return new FileWrapper.Document(inputFile);
    }

    public static void writeFileContents(@NonNull Context context,
                                         @NonNull Uri uriContent,
                                         byte[] dataToWrite) {
        var resolver = context.getContentResolver();
        try (var out = resolver.openOutputStream(uriContent, "w")) {
            if (out != null) {
                out.write(dataToWrite);
            } else {
                throw new IOException("Input is NULL!");
            }
        } catch (IOException ex) {
            // TODO: 04.12.2024 Add logger
            Log.e("FileUtil", "Error reading file: " + uriContent, ex);
        }
    }

    @Nullable
    public static DocumentFile findOrCreateFile(Context context,
                                                DocumentFile srcDir,
                                                String name,
                                                String mimeType) {
        return DocumentFileUtils.makeFile(srcDir, context, name, mimeType, CreateMode.REUSE);
    }

    public static DocumentFile fromRelPath(@NonNull Context context,
                                           @NonNull final String path,
                                           @NonNull DocumentFile parentDir,
                                           final boolean requiresWriteAccess) {
        return DocumentFileUtils.child(parentDir, context, path, requiresWriteAccess);
    }

    @Nullable
    public static DocumentFile fromFullPath(@NonNull Context context,
                                            @NonNull String fullPath,
                                            @NonNull DocumentFile rootDir) {
        var findDir = rootDir;
        var nameGameDir = rootDir.getName();

        try {
            var index = fullPath.indexOf(nameGameDir);
            var subString = fullPath.substring(index);
            var splitString = subString.replace(nameGameDir + "/", "");
            var pathToFileSegments = splitString.split("/");

            for (var segment : pathToFileSegments) {
                if (segment.isEmpty()) {
                    continue;
                }
                findDir = fromRelPath(context, segment, findDir, true);
                if (findDir == null) {
                    break;
                }
            }
        } catch (NullPointerException i) {
            return null;
        }

        return findDir;
    }
}
