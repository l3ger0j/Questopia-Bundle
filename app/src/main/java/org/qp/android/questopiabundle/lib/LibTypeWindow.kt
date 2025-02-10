package org.qp.android.questopiabundle.lib

import android.os.Parcel
import android.os.Parcelable

enum class LibTypeWindow : Parcelable {
    ACTIONS,
    OBJECTS,
    VARIABLES,
    INPUT
    ;

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
    }

    companion object CREATOR : Parcelable.Creator<LibTypeWindow> {
        override fun createFromParcel(source: Parcel): LibTypeWindow {
            return valueOf(source.readString()!!)
        }

        override fun newArray(size: Int): Array<LibTypeWindow?> {
            return arrayOfNulls(size)
        }
    }
}
