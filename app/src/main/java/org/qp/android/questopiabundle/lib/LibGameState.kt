package org.qp.android.questopiabundle.lib

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import org.qp.android.questopiabundle.dto.LibListItem
import org.qp.android.questopiabundle.dto.LibMenuItem
import org.qp.android.questopiabundle.lib.LibIConfig

data class LibGameState(
    var interfaceConfig: LibIConfig = LibIConfig(),
    var gameRunning: Boolean = false,
    var gameId: Long = 0L,
    var gameTitle: String = "",
    var gameDirUri: Uri = Uri.EMPTY,
    var gameFileUri: Uri = Uri.EMPTY,
    var mainDesc: String = "",
    var varsDesc: String = "",
    var actionsList: MutableList<LibListItem> = mutableListOf(),
    var objectsList: MutableList<LibListItem> = mutableListOf(),
    var menuItemsList: MutableList<LibMenuItem> = mutableListOf()
) : Parcelable {

    constructor(source: Parcel) : this (
        source.readParcelable(LibIConfig::class.java.classLoader) ?: LibIConfig(),
        source.readByte().toInt() != 0,
        source.readLong(),
        source.readString() ?: "",
        source.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
        source.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
        source.readString() ?: "",
        source.readString() ?: "",
    ) {
        source.readTypedList(actionsList, LibListItem)
        source.readTypedList(objectsList, LibListItem)
        source.readTypedList(menuItemsList, LibMenuItem)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(interfaceConfig, flags)
        dest.writeByte((if (gameRunning) 1 else 0).toByte())
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

    fun reset() {
        interfaceConfig.reset()
        gameRunning = false
        gameId = 0L
        gameTitle = ""
        gameDirUri = Uri.EMPTY
        gameFileUri = Uri.EMPTY
        mainDesc = ""
        varsDesc = ""
        actionsList.clear()
        objectsList.clear()
        menuItemsList.clear()
    }

    companion object CREATOR : Parcelable.Creator<LibGameState> {
        override fun createFromParcel(source: Parcel): LibGameState {
            return LibGameState(source)
        }

        override fun newArray(size: Int): Array<LibGameState?> {
            return arrayOfNulls(size)
        }
    }
}
