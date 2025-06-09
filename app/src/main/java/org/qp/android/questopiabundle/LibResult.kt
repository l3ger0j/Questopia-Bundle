package org.qp.android.questopiabundle

import android.os.Parcel
import android.os.Parcelable

data class LibResult<T : Parcelable?>(
    var value: T?
) : Parcelable {

    constructor(source: Parcel) : this(null) {
        this.value = source.readParcelable(javaClass.classLoader)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(value, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LibResult<Parcelable>> {
        override fun createFromParcel(source: Parcel): LibResult<Parcelable> {
            return LibResult(source)
        }

        override fun newArray(size: Int): Array<LibResult<Parcelable>?> {
            return arrayOfNulls(size)
        }
    }
}
