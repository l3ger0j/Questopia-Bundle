package org.qp.android.questopiabundle.dto

import android.os.Parcel
import android.os.Parcelable

data class LibGenItem(
    val text: String = "",
    val imagePath: String = ""
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readString() ?: ""
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(text)
        dest.writeString(imagePath)
    }

    companion object CREATOR : Parcelable.Creator<LibGenItem> {
        override fun createFromParcel(source: Parcel): LibGenItem {
            return LibGenItem(source)
        }

        override fun newArray(size: Int): Array<LibGenItem?> {
            return arrayOfNulls(size)
        }
    }
}