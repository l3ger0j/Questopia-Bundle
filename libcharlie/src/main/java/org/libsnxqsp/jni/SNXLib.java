package org.libsnxqsp.jni;

public abstract class SNXLib {

    public record ListItem(String image, String text) { }

    public record VarValResp(boolean isSuccess, String stringValue, int intValue) { }

    public record ExecutionState(String loc, int actIndex, int lineNum) { }

    public record ErrorData(String locName, int errorNum, int index, int line) { }

    static {
        System.loadLibrary("snxqsp");
    }

    public native void init();
    public native void terminate();
    public native boolean QSPIsInCallBack();
    public native void QSPEnableDebugMode(boolean isDebug);
    public native Object QSPGetCurStateData();//!!!STUB
    public native String QSPGetVersion();
    public native String QSPGetCompiledDateTime();
    public native int QSPGetFullRefreshCount();
    public native String QSPGetQstFullPath();
    public native String QSPGetCurLoc();
    public native Object QSPGetExprValue();//!!!STUB
    /* Main desc */
    public native String getMainDesc();
    public native boolean isMainDescChanged();
    /* Vars desc */
    public native String getVarsDesc();
    public native boolean isVarsDescChanged();
    public native int getVarValuesCount(String name);
    public native Object QSPGetVarValues(String name, int ind);//!!!STUB
    public native int QSPGetMaxVarsCount();
    public native Object QSPGetVarNameByIndex(int index);//!!!STUB
    /* Input string */
    public native void QSPSetInputStrText(String val);
    /* Actions */
    public native int getActionsCount();
    public native ListItem[] getActions();
    public native boolean executeSelActionCode(boolean isRefresh);
    public native boolean setSelActionIndex(int ind, boolean isRefresh);
    public native int getSelActionIndex();
    public native boolean isActionsChanged();
    /* Objects */
    public native ListItem[] getObjects();
    public native int getObjectsCount();
    public native boolean setSelObjectIndex(int ind, boolean isRefresh);
    public native int getSelObjectIndex();
    public native boolean isObjectsChanged();
    /* Code execution */
    public native boolean execString(String s, boolean isRefresh);
    public native boolean execLocationCode(String name, boolean isRefresh);
    public native boolean execCounter(boolean isRefresh);
    public native boolean execUserInput(boolean isRefresh);
    /* Errors */
    public native ErrorData getLastErrorData();
    public native String getErrorDesc(int errorNum);
    /* Game */
    public native boolean QSPLoadGameWorld(String fileName);
    public native boolean loadGameWorldFromData(byte[] data , int dataSize, String fileName);
    public native boolean QSPSaveGame(String fileName, boolean isRefresh);
    public native byte[] QSPSaveGameAsData(boolean isRefresh);
    public native boolean QSPOpenSavedGame(String fileName, boolean isRefresh);
    public native boolean QSPOpenSavedGameFromData(byte[] data , int dataSize, boolean isRefresh);
    public native boolean QSPRestartGame(boolean isRefresh);

    public void onSystem(String code) { }
    public void onDebug(String str) { }
    public void onRefreshInt() { }
    public void onShowImage(String path) { }
    public void onSetTimer(int msecs) { }
    public void onSetInputStrText(String text) { }
    public void onShowMessage(String message) { }
    public void onPlayFile(String path, int volume) { }
    public boolean onIsPlayingFile(final String path) { return false; }
    public void onCloseFile(String path) { }
    public void onOpenGame(String filename) { }
    public void onSaveGame(String filename) { }
    public String onInputBox(String prompt) { return null; }
    public int onGetMsCount() { return 0; }
    public void onAddMenuItem(String name, String imgPath) { }
    public int onShowMenu() { return -1; }
    public void onDeleteMenu() { }
    public void onSleep(int msecs) { }
    public void onShowWindow(int type, boolean isShow) { }
}