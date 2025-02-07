package org.libsnxqsp.jni;

public abstract class SNXLib {

    public record ListItem(String image , String text) { }

    public record VarValResp(boolean isSuccess , String stringValue , int intValue) { }

    public record ExecutionState(String loc, int actIndex, int lineNum) { }

    public record ErrorData(String locName , int errorNum , int index , int line) { }

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
    public native boolean QSPIsMainDescChanged();
    /* Vars desc */
    public native String QSPGetVarsDesc();
    public native boolean QSPIsVarsDescChanged();
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
    public void CallDebug(String str) { }
    public void RefreshInt() { }
    public void ShowPicture(String path) { }
    public void SetTimer(int msecs) { }
    public void setInputStrText(String text) { }
    public void ShowMessage(String message) { }
    public void PlayFile(String path, int volume) { }
    public boolean IsPlayingFile(final String path) { return false; }
    public void CloseFile(String path) { }
    public void OpenGame(String filename) { }
    public void SaveGame(String filename) { }
    public String InputBox(String prompt) { return null; }
    public int GetMSCount() { return 0; }
    public void addMenuItem(String name, String imgPath) { }
    public int showMenu() { return -1; }
    public void deleteMenu() { }
    public void Wait(int msecs) { }
    public void ShowWindow(int type, boolean isShow) { }
}