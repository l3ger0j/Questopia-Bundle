package org.qp.android.questopiabundle.lib

import android.os.Parcel
import android.os.Parcelable

data class LibRefIRequest(
    val isIConfigChanged: Boolean = false,
    val isMainDescChanged: Boolean = false,
    val isActionsChanged: Boolean = false,
    val isObjectsChanged: Boolean = false,
    val isVarsDescChanged: Boolean = false
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readInt() != 0,
        source.readInt() != 0,
        source.readInt() != 0,
        source.readInt() != 0,
        source.readInt() != 0,
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(if (isIConfigChanged) 1 else 0)
        dest.writeInt(if (isMainDescChanged) 1 else 0)
        dest.writeInt(if (isActionsChanged) 1 else 0)
        dest.writeInt(if (isObjectsChanged) 1 else 0)
        dest.writeInt(if (isVarsDescChanged) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<LibRefIRequest> {
        override fun createFromParcel(source: Parcel): LibRefIRequest {
            return LibRefIRequest(source)
        }

        override fun newArray(size: Int): Array<LibRefIRequest?> {
            return arrayOfNulls(size)
        }
    }
}
