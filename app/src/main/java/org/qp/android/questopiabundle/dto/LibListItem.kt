package org.qp.android.questopiabundle.dto

import android.os.Parcel
import android.os.Parcelable

data class LibListItem(
    val text: String = "",
    val pathToImage: String = ""
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readString() ?: ""
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(text)
        dest.writeString(pathToImage)
    }

    companion object CREATOR : Parcelable.Creator<LibListItem> {
        override fun createFromParcel(source: Parcel): LibListItem {
            return LibListItem(source)
        }

        override fun newArray(size: Int): Array<LibListItem?> {
            return arrayOfNulls(size)
        }
    }
}
