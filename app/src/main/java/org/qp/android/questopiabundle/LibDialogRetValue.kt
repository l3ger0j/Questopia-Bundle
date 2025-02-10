package org.qp.android.questopiabundle

import android.os.Parcel
import android.os.Parcelable

data class LibDialogRetValue(
    var outTextValue: String = "",
    var outNumValue: Int = 0
) : Parcelable {

    constructor(source: Parcel) : this() {
        outTextValue = source.readString() ?: ""
        outNumValue = source.readInt()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(outTextValue)
        dest.writeInt(outNumValue)
    }

    companion object CREATOR : Parcelable.Creator<LibDialogRetValue> {
        override fun createFromParcel(source: Parcel): LibDialogRetValue {
            return LibDialogRetValue(source)
        }

        override fun newArray(size: Int): Array<LibDialogRetValue?> {
            return arrayOfNulls(size)
        }
    }
}
