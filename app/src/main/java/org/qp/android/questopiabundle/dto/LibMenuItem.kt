package org.qp.android.questopiabundle.dto

import android.os.Parcel
import android.os.Parcelable

data class LibMenuItem(
    var name: String = "",
    var pathToImage: String = ""
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readString() ?: ""
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(pathToImage)
    }

    companion object CREATOR : Parcelable.Creator<LibMenuItem> {
        override fun createFromParcel(source: Parcel): LibMenuItem {
            return LibMenuItem(source)
        }

        override fun newArray(size: Int): Array<LibMenuItem?> {
            return arrayOfNulls(size)
        }
    }
}
