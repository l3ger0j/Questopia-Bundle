package org.qp.android.questopiabundle.lib

import android.os.Parcel
import android.os.Parcelable

data class LibRefIRequest(
    var isIConfigChanged: Boolean = false,
    var isMainDescChanged: Boolean = false,
    var isActionsChanged: Boolean = false,
    var isObjectsChanged: Boolean = false,
    var isVarsDescChanged: Boolean = false
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
