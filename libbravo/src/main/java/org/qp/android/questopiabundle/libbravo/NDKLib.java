package org.qp.android.questopiabundle.libbravo;

import java.util.Objects;

public abstract class NDKLib {

    public class ListItem {
        public String pathToImage;
        public String text;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            var that = (ListItem) o;
            return Objects.equals(text, that.text)
                    && Objects.equals(pathToImage, that.pathToImage);
        }
    }

    public record VarValResp(boolean isSuccess , String stringValue , int intValue) { }

    public record ErrorData(String locName , int errorNum , int index , int line) { }

    static {
        System.loadLibrary("ndkqsp");
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
    public native boolean QSPLoadGameWorldFromData(byte[] data , int dataSize, String fileName);
    public native boolean QSPSaveGame(String fileName, boolean isRefresh);
    public native byte[] QSPSaveGameAsData(boolean isRefresh);
    public native boolean QSPOpenSavedGame(String fileName, boolean isRefresh);
    public native boolean QSPOpenSavedGameFromData(byte[] data , int dataSize, boolean isRefresh);
    public native boolean QSPRestartGame(boolean isRefresh);
    public native void QSPSelectMenuItem(int index);
    //public native void QSPSetCallBack(int type, QSP_CALLBACK func)

    public void CallDebug(String str) {}
    public void RefreshInt() { }
    public void ShowPicture(String path) { }
    public void SetTimer(int msecs) { }
    public void ShowMessage(String message) { }
    public void PlayFile(String path, int volume) { }
    public boolean IsPlayingFile(final String path) { return false; }
    public void CloseFile(String path) { }
    public void OpenGame(String filename) { }
    public void SaveGame(String filename) { }
    public String InputBox(String prompt) { return null; }
    public int GetMSCount() { return 0; }
    public void AddMenuItem(String name, String imgPath) { }
    public void ShowMenu() { }
    public void DeleteMenu() { }
    public void Wait(int msecs) { }
    public void ShowWindow(int type, boolean isShow) { }
    public byte[] GetFileContents(String path) { return null; }
    public void ChangeQuestPath(String path) { }
}
