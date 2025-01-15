package com.libqsp.jni;

public abstract class QSPLib {
    public enum Error {
        DIVBYZERO, /* = 10 */
        TYPEMISMATCH,
        STACKOVERFLOW,
        TOOMANYITEMS,
        CANTLOADFILE,
        GAMENOTLOADED,
        COLONNOTFOUND,
        CANTINCFILE,
        CANTADDACTION,
        EQNOTFOUND,
        LOCNOTFOUND,
        ENDNOTFOUND,
        LABELNOTFOUND,
        INCORRECTNAME,
        QUOTNOTFOUND,
        BRACKNOTFOUND,
        BRACKSNOTFOUND,
        SYNTAX,
        UNKNOWNACTION,
        ARGSCOUNT,
        CANTADDOBJECT,
        CANTADDMENUITEM,
        TOOMANYVARS,
        INCORRECTREGEXP,
        CODENOTFOUND,
        LOOPWHILENOTFOUND
    }

    public enum Window {
        ACTS,
        OBJS,
        VARS,
        INPUT
    }

    public class ListItem {
        public String image;
        public String name;
    }

    public class ExecutionState {
        public String loc;
        public int actIndex;
        public int lineNum;
    }

    public class ErrorInfo {
        public int errorNum; /* Error.ordinal() */
        public String errorDesc;
        public String locName; /* location name */
        public int actIndex; /* index of the base action */
        public int topLineNum; /* top-level line within the game code */
        public int intLineNum; /* line number of the actual code */
        public String intLine; /* line of the actual code */
    }

    static {
        System.loadLibrary("qsp");
        // System.load("libqsp.so");
    }

    // Main API
    public native void init();
    public native void terminate();

    public native void enableDebugMode(boolean isDebug);
    public native ExecutionState getCurrentState();
    public native String getVersion();
    public native String getCompiledDateTime();
    public native int getFullRefreshCount();
    public native String getMainDesc();
    public native boolean isMainDescChanged();
    public native String getVarsDesc();
    public native boolean isVarsDescChanged();
    public native void setInputStrText(String value);
    public native ListItem[] getActions();
    public native boolean setSelActIndex(int index, boolean toRefreshUI);
    public native boolean execSelAction(boolean toRefreshUI);
    public native int getSelActIndex();
    public native boolean isActsChanged();
    public native ListItem[] getObjects();
    public native boolean setSelObjIndex(int index, boolean toRefreshUI);
    public native int getSelObjIndex();
    public native boolean isObjsChanged();
    public native void showWindow(int type /* Window.ordinal() */, boolean toShow);
    public native int getVarValuesCount(String name);
    public native int getVarIndexByString(String name, String str);
    /*
    public native QSPVariant getVarValue(String name, int index);
    public native String convertValueToString(QSPVariant value);
    */
    public native long getNumVarValue(String name, int index);
    public native String getStrVarValue(String name, int index);
    public native boolean execString(String code, boolean toRefreshUI);
    public native String calculateStrExpr(String expression, boolean toRefreshUI);
    public native long calculateNumExpr(String expression, boolean toRefreshUI);
    public native boolean execLocationCode(String name, boolean toRefreshUI);
    public native boolean execCounter(boolean toRefreshUI);
    public native boolean execUserInput(boolean toRefreshUI);
    public native ErrorInfo getLastErrorData();
    public native String getErrorDesc(int errorNum);
    public native boolean loadGameWorldFromData(byte[] data, boolean isNewGame);
    public native byte[] saveGameAsData(boolean toRefreshUI);
    public native boolean openSavedGameFromData(byte[] data, boolean toRefreshUI);
    public native boolean restartGame(boolean toRefreshUI);

    // Callbacks
    public void onDebug(String str) {}
    public boolean onIsPlayingFile(String file) { return false; }
    public void onPlayFile(String file, int volume) {}
    public void onCloseFile(String file) {}
    public void onShowImage(String file) {}
    public void onShowWindow(int type /* Window.ordinal() */, boolean toShow) {}
    public int onShowMenu(ListItem[] items) { return -1; }
    public void onShowMessage(String text) {}
    public void onRefreshInt(boolean isForced) {}
    public void onSetTimer(int msecs) {}
    public void onSetInputStrText(String text) {}
    public void onSystem(String cmd) {}
    public void onOpenGame(String file, boolean isNewGame) {}
    public void onOpenGameStatus(String file) {}
    public void onSaveGameStatus(String file) {}
    public void onSleep(int msecs) {}
    public int onGetMsCount() { return 0; }
    public String onInputBox(String text) { return ""; }
    public String onVersion(String param) { return ""; }
}
