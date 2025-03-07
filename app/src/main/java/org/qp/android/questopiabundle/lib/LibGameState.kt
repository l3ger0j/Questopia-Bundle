package org.qp.android.questopiabundle.lib

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import org.qp.android.questopiabundle.dto.LibListItem
import org.qp.android.questopiabundle.dto.LibMenuItem
import org.qp.android.questopiabundle.lib.LibIConfig

data class LibGameState(
    val interfaceConfig: LibIConfig = LibIConfig(),
    val gameRunning: Boolean = false,
    val gameId: Long = 0L,
    val gameTitle: String = "",
    val gameDirUri: Uri = Uri.EMPTY,
    val gameFileUri: Uri = Uri.EMPTY,
    val mainDesc: String = "",
    val varsDesc: String = "",
    val actionsList: List<LibListItem> = listOf(),
    val objectsList: List<LibListItem> = listOf(),
    val menuItemsList: List<LibMenuItem> = listOf()
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
        source.readTypedList(actionsList, LibListItem)
        source.readTypedList(objectsList, LibListItem)
        source.readTypedList(menuItemsList, LibMenuItem)
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
