package org.qp.android.questopiabundle.utils

import org.jetbrains.annotations.Contract

object StringUtil {

    @JvmStatic
    fun isNotEmptyOrBlank(str: String?): Boolean {
        return !str.isNullOrEmpty() && str.isNotBlank()
    }

    @JvmStatic
    fun isNullOrEmpty(str: String?): Boolean {
        return str.isNullOrEmpty()
    }

    @JvmStatic
    @Contract(value = "!null -> param1", pure = true)
    fun getStringOrEmpty(str: String?): String {
        return str ?: ""
    }
}
