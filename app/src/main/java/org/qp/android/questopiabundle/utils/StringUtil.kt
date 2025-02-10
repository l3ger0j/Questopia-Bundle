package org.qp.android.questopiabundle.utils

import org.jetbrains.annotations.Contract

object StringUtil {
    fun isNotEmptyOrBlank(str: String?): Boolean {
        return !str.isNullOrEmpty() && str.isNotBlank()
    }

    fun isNullOrEmpty(str: String?): Boolean {
        return str.isNullOrEmpty()
    }

    @Contract(value = "!null -> param1", pure = true)
    fun getStringOrEmpty(str: String?): String {
        return str ?: ""
    }
}
