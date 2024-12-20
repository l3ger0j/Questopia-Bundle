package org.qp.android.questopiabundle.lib;

import java.util.Objects;

/**
 * The methods of this class are defined in <code>androidqspwrapper.c</code>.
 */
public class LibNativeMethods {

    static {
        System.loadLibrary("ndkqsp");
    }

    private final LibCallbacks callbacks;

    public LibNativeMethods(LibCallbacks callbacks) {
        this.callbacks = Objects.requireNonNull(callbacks, "callbacks must not be null");
    }

    public native void QSPInit();

    public native void QSPDeInit();

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
    public native String QSPGetMainDesc();

    public native boolean QSPIsMainDescChanged();

    /* Vars desc */
    public native String QSPGetVarsDesc();

    public native boolean QSPIsVarsDescChanged();

    public native Object QSPGetVarValuesCount(String name);

    public native Object QSPGetVarValues(String name, int ind);//!!!STUB

    public native int QSPGetMaxVarsCount();

    public native Object QSPGetVarNameByIndex(int index);//!!!STUB

    /* Input string */
    public native void QSPSetInputStrText(String val);

    /* Actions */
    public native int QSPGetActionsCount();

    public native Object QSPGetActionData(int ind);//!!!STUB

    public native boolean QSPExecuteSelActionCode(boolean isRefresh);

    public native boolean QSPSetSelActionIndex(int ind, boolean isRefresh);

    public native int QSPGetSelActionIndex();

    public native boolean QSPIsActionsChanged();

    /* Objects */
    public native Object QSPGetObjectData(int ind);//!!!STUB

    public native int QSPGetObjectsCount();

    public native boolean QSPSetSelObjectIndex(int ind, boolean isRefresh);

    public native int QSPGetSelObjectIndex();

    public native boolean QSPIsObjectsChanged();

    /* Code execution */
    public native boolean QSPExecString(String s, boolean isRefresh);

    public native boolean QSPExecLocationCode(String name, boolean isRefresh);

    public native boolean QSPExecCounter(boolean isRefresh);

    public native boolean QSPExecUserInput(boolean isRefresh);

    /* Errors */
    public native Object QSPGetLastErrorData();

    public native String QSPGetErrorDesc(int errorNum);

    /* Game */
    public native boolean QSPLoadGameWorld(String fileName);

    public native boolean QSPLoadGameWorldFromData(byte[] data, int dataSize, String fileName);

    public native boolean QSPSaveGame(String fileName, boolean isRefresh);

    public native byte[] QSPSaveGameAsData(boolean isRefresh);

    public native boolean QSPOpenSavedGame(String fileName, boolean isRefresh);

    public native boolean QSPOpenSavedGameFromData(byte[] data, int dataSize, boolean isRefresh);

    public native boolean QSPRestartGame(boolean isRefresh);

    public native void QSPSelectMenuItem(int index);
    //public native void QSPSetCallBack(int type, QSP_CALLBACK func)

}
