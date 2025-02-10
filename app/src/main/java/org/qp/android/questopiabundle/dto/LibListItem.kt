package org.qp.android.questopiabundle.dto

import android.os.Parcel
import android.os.Parcelable
import com.libqsp.jni.QSPLib
import org.libndkqsp.jni.NDKLib
import org.libsnxqsp.jni.SNXLib

data class LibListItem(
    var text: String = "",
    var pathToImage: String = ""
) : Parcelable {

    constructor(item: QSPLib.ListItem) : this(item.name, item.image)

    constructor(item: NDKLib.ListItem) : this(item.text, item.image)

    constructor(item: SNXLib.ListItem) : this(item.text, item.image)

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
