package org.qp.android.questopiabundle.dto

import android.os.Parcel
import android.os.Parcelable

data class LibIConfig(
    val useHtml: Boolean = false,
    val fontSize: Long = 0L,
    val backColor: Long = 0L,
    val fontColor: Long = 0L,
    val linkColor: Long = 0L
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readInt() != 0,
        source.readLong(),
        source.readLong(),
        source.readLong(),
        source.readLong()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(if (useHtml) 1 else 0)
        dest.writeLong(fontSize)
        dest.writeLong(backColor)
        dest.writeLong(fontColor)
        dest.writeLong(linkColor)
    }

    companion object CREATOR : Parcelable.Creator<LibIConfig> {
        override fun createFromParcel(source: Parcel): LibIConfig {
            return LibIConfig(source)
        }

        override fun newArray(size: Int): Array<LibIConfig?> {
            return arrayOfNulls(size)
        }
    }
}
