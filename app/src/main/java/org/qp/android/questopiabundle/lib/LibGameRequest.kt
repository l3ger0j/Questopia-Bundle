package org.qp.android.questopiabundle.lib

import android.os.Parcel
import android.os.Parcelable

enum class LibGameRequest : Parcelable {
    LOAD_FILE,
    SAVE_FILE,
    USE_EXECUTOR,
    USE_INPUT,
    RESTART_GAME,
    EXECUTE_CODE,
    ;

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
    }

    companion object CREATOR : Parcelable.Creator<LibGameRequest> {
        override fun createFromParcel(source: Parcel): LibGameRequest {
            return LibGameRequest.valueOf(source.readString()!!)
        }

        override fun newArray(size: Int): Array<LibGameRequest?> {
            return arrayOfNulls(size)
        }
    }
}
