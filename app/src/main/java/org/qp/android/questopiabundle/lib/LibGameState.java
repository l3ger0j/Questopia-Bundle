package org.qp.android.questopiabundle.lib;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import org.qp.android.questopiabundle.dto.lib.LibListItem;
import org.qp.android.questopiabundle.dto.lib.LibMenuItem;

import java.util.ArrayList;
import java.util.List;

public class LibGameState implements Parcelable {

    public static final Creator<LibGameState> CREATOR = new Creator<>() {
        @Override
        public LibGameState createFromParcel(Parcel in) {
            return new LibGameState(in);
        }

        @Override
        public LibGameState[] newArray(int size) {
            return new LibGameState[size];
        }
    };

    public LibIConfig interfaceConfig;
    public boolean gameRunning;
    public long gameId;
    public String gameTitle;
    public Uri gameDir;
    public Uri gameFile;
    public String gameFileFullPath;
    public String mainDesc;
    public String varsDesc;
    public List<LibListItem> actionsList = new ArrayList<>();
    public List<LibListItem> objectsList = new ArrayList<>();
    public List<LibMenuItem> menuItemsList = new ArrayList<>();

    public LibGameState() {
        interfaceConfig = new LibIConfig();
        mainDesc = "";
        varsDesc = "";
    }

    protected LibGameState(Parcel in) {
        interfaceConfig = in.readParcelable(LibIConfig.class.getClassLoader());
        gameRunning = in.readByte() != 0;
        gameId = in.readLong();
        gameTitle = in.readString();
        gameDir = in.readParcelable(Uri.class.getClassLoader());
        gameFile = in.readParcelable(Uri.class.getClassLoader());
        gameFileFullPath = in.readString();
        mainDesc = in.readString();
        varsDesc = in.readString();
        in.readTypedList(actionsList, LibListItem.CREATOR);
        in.readTypedList(objectsList, LibListItem.CREATOR);
        in.readTypedList(menuItemsList, LibMenuItem.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(interfaceConfig, flags);
        dest.writeByte((byte) (gameRunning ? 1 : 0));
        dest.writeLong(gameId);
        dest.writeString(gameTitle);
        dest.writeParcelable(gameDir, flags);
        dest.writeParcelable(gameFile, flags);
        dest.writeString(gameFileFullPath);
        dest.writeString(mainDesc);
        dest.writeString(varsDesc);
        dest.writeTypedList(actionsList);
        dest.writeTypedList(objectsList);
        dest.writeTypedList(menuItemsList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void reset() {
        interfaceConfig.reset();
        gameRunning = false;
        gameId = 0L;
        gameTitle = "";
        gameDir = Uri.EMPTY;
        gameFile = Uri.EMPTY;
        gameFileFullPath = "";
        mainDesc = "";
        varsDesc = "";
        actionsList = new ArrayList<>();
        objectsList = new ArrayList<>();
        menuItemsList = new ArrayList<>();
    }

}
