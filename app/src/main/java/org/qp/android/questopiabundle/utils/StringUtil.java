package org.qp.android.questopiabundle.utils;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

public final class StringUtil {

    public static boolean isNotEmptyOrBlank(String str) {
        return str != null && !str.isEmpty() && !str.isBlank();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    @NonNull
    @Contract(value = "!null -> param1", pure = true)
    public static String getStringOrEmpty(String str) {
        return str != null ? str : "";
    }
}
