package org.qp.android.questopiabundle.dto

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class LibGameState(
    val interfaceConfig: LibIConfig = LibIConfig(),
    val gameRunning: Boolean = false,
    val gameId: Long = 0L,
    val gameTitle: String = "",
    val gameDirUri: Uri = Uri.EMPTY,
    val gameFileUri: Uri = Uri.EMPTY,
    val mainDesc: String = "",
    val varsDesc: String = "",
    val actionsList: List<LibGenItem> = listOf(),
    val objectsList: List<LibGenItem> = listOf(),
    val menuItemsList: List<LibGenItem> = listOf()
) : Parcelable {

    constructor(source: Parcel) : this(
        source.readParcelable(LibIConfig::class.java.classLoader) ?: LibIConfig(),
        source.readInt() != 0,
        source.readLong(),
        source.readString() ?: "",
        source.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
        source.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
        source.readString() ?: "",
        source.readString() ?: "",
    ) {
        source.readTypedList(actionsList, LibGenItem)
        source.readTypedList(objectsList, LibGenItem)
        source.readTypedList(menuItemsList, LibGenItem)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(interfaceConfig, flags)
        dest.writeInt(if (gameRunning) 1 else 0)
        dest.writeLong(gameId)
        dest.writeString(gameTitle)
        dest.writeParcelable(gameDirUri, flags)
        dest.writeParcelable(gameFileUri, flags)
        dest.writeString(mainDesc)
        dest.writeString(varsDesc)
        dest.writeTypedList(actionsList)
        dest.writeTypedList(objectsList)
        dest.writeTypedList(menuItemsList)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<LibGameState> {
        override fun createFromParcel(source: Parcel): LibGameState {
            return LibGameState(source)
        }

        override fun newArray(size: Int): Array<LibGameState?> {
            return arrayOfNulls(size)
        }
    }
}