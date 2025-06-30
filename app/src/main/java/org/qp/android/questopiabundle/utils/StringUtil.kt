package org.qp.android.questopiabundle.utils

import org.jetbrains.annotations.Contract
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object StringUtil {
    @ExperimentalContracts
    fun isNotEmptyOrBlank(str: String?): Boolean {
        contract {
            returns() implies (str != null)
        }
        return !str.isNullOrEmpty() && str.isNotBlank()
    }

    @Contract(value = "!null -> param1", pure = true)
    fun getStringOrEmpty(str: String?): String {
        return str ?: ""
    }
}
