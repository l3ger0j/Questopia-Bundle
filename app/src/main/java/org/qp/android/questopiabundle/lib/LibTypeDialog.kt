package org.qp.android.questopiabundle.lib

import android.os.Parcel
import android.os.Parcelable

enum class LibTypeDialog : Parcelable {
    DIALOG_ERROR,
    DIALOG_PICTURE,
    DIALOG_MESSAGE,
    DIALOG_INPUT,
    DIALOG_EXECUTOR,
    DIALOG_MENU,
    DIALOG_POPUP_SAVE,
    DIALOG_POPUP_LOAD,
    ;

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
    }

    companion object CREATOR : Parcelable.Creator<LibTypeDialog> {
        override fun createFromParcel(source: Parcel): LibTypeDialog {
            return valueOf(source.readString()!!)
        }

        override fun newArray(size: Int): Array<LibTypeDialog?> {
            return arrayOfNulls(size)
        }
    }
}
