package org.libndkqsp.jni

abstract class NDKLib {
    @JvmRecord
    data class ListItem(val image: String = "", val text: String = "")

    @JvmRecord
    data class ExecutionState(val loc: String, val actIndex: Int, val lineNum: Int)

    @JvmRecord
    data class VarValResp(val isSuccess: Boolean, val stringValue: String, val intValue: Int)

    @JvmRecord
    data class ErrorData(val locName: String, val errorNum: Int, val index: Int, val line: Int)

    external fun QSPInit()
    external fun QSPDeInit()
    external fun QSPIsInCallBack(): Boolean
    external fun QSPEnableDebugMode(isDebug: Boolean)
    external fun QSPGetCurStateData(): Any? //!!!STUB
    external fun QSPGetVersion(): String?
    external fun QSPGetCompiledDateTime(): String?
    external fun QSPGetFullRefreshCount(): Int
    external fun QSPGetQstFullPath(): String?
    external fun QSPGetCurLoc(): String?
    external fun QSPGetExprValue(): Any? //!!!STUB

    /* Main desc */
    external fun QSPGetMainDesc(): String?
    external fun QSPIsMainDescChanged(): Boolean

    /* Vars desc */
    external fun QSPGetVarsDesc(): String?
    external fun QSPIsVarsDescChanged(): Boolean
    external fun QSPGetVarValuesCount(name: String?): Any?
    external fun QSPGetVarValues(name: String?, ind: Int): Any? //!!!STUB
    external fun QSPGetMaxVarsCount(): Int
    external fun QSPGetVarNameByIndex(index: Int): Any? //!!!STUB

    /* Input string */
    external fun QSPSetInputStrText(`val`: String?)

    /* Actions */
    external fun QSPGetActionsCount(): Int
    external fun QSPGetActionData(): List<ListItem?>?
    external fun QSPExecuteSelActionCode(isRefresh: Boolean): Boolean
    external fun QSPSetSelActionIndex(ind: Int, isRefresh: Boolean): Boolean
    external fun QSPGetSelActionIndex(): Int
    external fun QSPIsActionsChanged(): Boolean

    /* Objects */
    external fun QSPGetObjectData(): List<ListItem?>?
    external fun QSPGetObjectsCount(): Int
    external fun QSPSetSelObjectIndex(ind: Int, isRefresh: Boolean): Boolean
    external fun QSPGetSelObjectIndex(): Int
    external fun QSPIsObjectsChanged(): Boolean

    /* Code execution */
    external fun QSPExecString(s: String?, isRefresh: Boolean): Boolean
    external fun QSPExecLocationCode(name: String?, isRefresh: Boolean): Boolean
    external fun QSPExecCounter(isRefresh: Boolean): Boolean
    external fun QSPExecUserInput(isRefresh: Boolean): Boolean

    /* Errors */
    external fun QSPGetLastErrorData(): ErrorData?
    external fun QSPGetErrorDesc(errorNum: Int): String?

    /* Game */
    external fun QSPLoadGameWorld(fileName: String?): Boolean
    external fun QSPLoadGameWorldFromData(data: ByteArray?, fileName: String?): Boolean
    external fun QSPSaveGame(fileName: String?, isRefresh: Boolean): Boolean
    external fun QSPSaveGameAsData(isRefresh: Boolean): ByteArray?
    external fun QSPOpenSavedGame(fileName: String?, isRefresh: Boolean): Boolean
    external fun QSPOpenSavedGameFromData(
        data: ByteArray?,
        dataSize: Int,
        isRefresh: Boolean
    ): Boolean

    external fun QSPRestartGame(isRefresh: Boolean): Boolean
    external fun QSPSelectMenuItem(index: Int)

    //public native void QSPSetCallBack(int type, QSP_CALLBACK func)
    fun CallDebug(str: String?) {}
    open fun RefreshInt() {}
    open fun ShowPicture(path: String?) {}
    open fun SetTimer(msecs: Int) {}
    open fun ShowMessage(message: String?) {}
    open fun PlayFile(path: String?, volume: Int) {}
    open fun IsPlayingFile(path: String?): Boolean {
        return false
    }

    open fun CloseFile(path: String?) {}
    open fun OpenGame(filename: String?) {}
    open fun SaveGame(filename: String?) {}
    open fun InputBox(prompt: String?): String? {
        return null
    }

    open fun GetMSCount(): Int {
        return 0
    }

    open fun AddMenuItem(name: String?, imgPath: String?) {}
    open fun ShowMenu() {}
    open fun DeleteMenu() {}
    open fun Wait(msecs: Int) {}
    open fun ShowWindow(type: Int, isShow: Boolean) {}
    open fun GetFileContents(path: String?): ByteArray? {
        return null
    }

    open fun ChangeQuestPath(path: String?) {}

    companion object {
        init {
            System.loadLibrary("ndkqsp")
        }
    }
}
