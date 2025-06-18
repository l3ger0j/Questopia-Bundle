package org.qp.android.questopiabundle

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat

data class LibReturnValue(
    val dialogTextValue: String = "",
    val dialogNumValue: Int = 0,
    val playFileState: Boolean = false,
    val fileUri: Uri = Uri.EMPTY
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readInt(),
        source.readInt() != 0,
        ParcelCompat.readParcelable(source, Uri::class.java.classLoader, Uri::class.java) ?: Uri.EMPTY
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(dialogTextValue)
        dest.writeInt(dialogNumValue)
        dest.writeInt(if (playFileState) 1 else 0)
        dest.writeParcelable(fileUri, flags)
    }

    companion object CREATOR : Parcelable.Creator<LibReturnValue> {
        override fun createFromParcel(source: Parcel): LibReturnValue {
            return LibReturnValue(source)
        }

        override fun newArray(size: Int): Array<LibReturnValue?> {
            return arrayOfNulls(size)
        }
    }
}
