package org.qp.android.questopiabundle

import android.os.Parcel
import android.os.Parcelable

class LibException(
    private val errorMessage: String?,
    private val errorCode: Int = RUNTIME_EXCEPTION
) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString(), parcel.readInt())

    fun toException(): Exception {
        return when (errorCode) {
            RUNTIME_EXCEPTION -> RuntimeException(errorMessage)
            else -> RuntimeException(errorMessage)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(errorMessage)
        parcel.writeInt(errorCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LibException> {
        const val RUNTIME_EXCEPTION: Int = 1000
        override fun createFromParcel(parcel: Parcel): LibException {
            return LibException(parcel)
        }

        override fun newArray(size: Int): Array<LibException?> {
            return arrayOfNulls(size)
        }
    }
}
