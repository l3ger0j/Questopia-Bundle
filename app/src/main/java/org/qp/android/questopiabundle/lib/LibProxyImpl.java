package org.qp.android.questopiabundle.lib;

import static org.qp.android.questopiabundle.utils.FileUtil.documentWrap;
import static org.qp.android.questopiabundle.utils.FileUtil.findOrCreateFile;
import static org.qp.android.questopiabundle.utils.FileUtil.fromFullPath;
import static org.qp.android.questopiabundle.utils.FileUtil.fromRelPath;
import static org.qp.android.questopiabundle.utils.FileUtil.getFileContents;
import static org.qp.android.questopiabundle.utils.FileUtil.isWritableFile;
import static org.qp.android.questopiabundle.utils.FileUtil.writeFileContents;
import static org.qp.android.questopiabundle.utils.HtmlUtil.getSrcDir;
import static org.qp.android.questopiabundle.utils.HtmlUtil.isContainsHtmlTags;
import static org.qp.android.questopiabundle.utils.HtmlUtil.removeHtmlTags;
import static org.qp.android.questopiabundle.utils.PathUtil.getFilename;
import static org.qp.android.questopiabundle.utils.PathUtil.normalizeContentPath;
import static org.qp.android.questopiabundle.utils.StringUtil.getStringOrEmpty;
import static org.qp.android.questopiabundle.utils.StringUtil.isNotEmpty;
import static org.qp.android.questopiabundle.utils.ThreadUtil.isSameThread;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.anggrayudi.storage.file.DocumentFileCompat;
import com.anggrayudi.storage.file.MimeType;

import org.qp.android.questopiabundle.GameInterface;
import org.qp.android.questopiabundle.dto.LibActionData;
import org.qp.android.questopiabundle.dto.LibErrorData;
import org.qp.android.questopiabundle.dto.LibListItem;
import org.qp.android.questopiabundle.dto.LibMenuItem;
import org.qp.android.questopiabundle.dto.LibObjectData;
import org.qp.android.questopiabundle.dto.LibVarValResp;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class LibProxyImpl implements LibIProxy, LibCallbacks {
    private final String TAG = "LibProxyImpl";

    private final ReentrantLock libLock = new ReentrantLock();
    private final LibGameState gameState = new LibGameState();
    private final LibNativeMethods nativeMethods = new LibNativeMethods(this);
    private final Context context;
    private Thread libThread;
    private volatile Handler libHandler;
    private volatile boolean libThreadInit;
    private volatile long gameStartTime;
    private volatile long lastMsCountCallTime;
    private GameInterface gameInterface;

    public LibProxyImpl(Context context) {
        this.context = context;
    }

    private DocumentFile getCurGameDir() {
        return DocumentFileCompat.fromUri(context, gameState.gameDirUri);
    }

    private void runOnQspThread(final Runnable runnable) {
//        throwIfNotMainThread();
        if (libThread == null) {
            Log.w(TAG, "Lib thread has not been started!");
            return;
        }
        if (!libThreadInit) {
            Log.w(TAG, "Lib thread has been started, but not initialized!");
            return;
        }
        var mLibHandler = libHandler;
        if (mLibHandler == null) return;
        mLibHandler.post(() -> {
            libLock.lock();
            try {
                runnable.run();
            } finally {
                libLock.unlock();
            }
        });
    }

    private boolean loadGameWorld() {
        final var gameFileUri = gameState.gameFileUri;
        var gameFileFullPath = "";
        if (String.valueOf(gameFileUri).startsWith("file:///")) {
            gameFileFullPath = new File(String.valueOf(gameFileUri)).getAbsolutePath();
        } else {
            gameFileFullPath = documentWrap(DocumentFileCompat.fromUri(context, gameFileUri)).getAbsolutePath(context);
        }
        final var gameData = getFileContents(context, gameFileUri);
        if (gameData == null) return false;

        if (!nativeMethods.QSPLoadGameWorldFromData(gameData, gameData.length, gameFileFullPath)) {
            showLastQspError();
            Log.d("QSP", "World is not loaded!");
            return false;
        }
        Log.d("QSP", "World is loaded!");
        return true;
    }

    private void showLastQspError() {
        var errorData = (LibErrorData) nativeMethods.QSPGetLastErrorData();
        var locName = getStringOrEmpty(errorData.locName());
        var desc = getStringOrEmpty(nativeMethods.QSPGetErrorDesc(errorData.errorNum()));
        final var message = String.format(
                Locale.getDefault(),
                "Location: %s\nAction: %d\nLine: %d\nError number: %d\nDescription: %s",
                locName,
                errorData.index(),
                errorData.line(),
                errorData.errorNum(),
                desc);
        Log.e(TAG, message);
        if (gameInterface != null) {
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, message);
        }
    }

    /**
     * Loads the interface configuration - using HTML, font and colors - from the library.
     *
     * @return <code>true</code> if the configuration has changed, otherwise <code>false</code>
     */
    private boolean loadInterfaceConfiguration() {
        var config = gameState.interfaceConfig;
        boolean changed = false;

        var htmlResult = (LibVarValResp) nativeMethods.QSPGetVarValues("USEHTML", 0);
        if (htmlResult.isSuccess()) {
            var useHtml = htmlResult.intValue() != 0;
            if (config.useHtml != useHtml) {
                config.useHtml = useHtml;
                changed = true;
            }
        }
        var fSizeResult = (LibVarValResp) nativeMethods.QSPGetVarValues("FSIZE", 0);
        if (fSizeResult.isSuccess() && config.fontSize != fSizeResult.intValue()) {
            config.fontSize = fSizeResult.intValue();
            changed = true;
        }
        var bColorResult = (LibVarValResp) nativeMethods.QSPGetVarValues("BCOLOR", 0);
        if (bColorResult.isSuccess() && config.backColor != bColorResult.intValue()) {
            config.backColor = bColorResult.intValue();
            changed = true;
        }
        var fColorResult = (LibVarValResp) nativeMethods.QSPGetVarValues("FCOLOR", 0);
        if (fColorResult.isSuccess() && config.fontColor != fColorResult.intValue()) {
            config.fontColor = fColorResult.intValue();
            changed = true;
        }
        var lColorResult = (LibVarValResp) nativeMethods.QSPGetVarValues("LCOLOR", 0);
        if (lColorResult.isSuccess() && config.linkColor != lColorResult.intValue()) {
            config.linkColor = lColorResult.intValue();
            changed = true;
        }

        return changed;
    }

    @NonNull
    private ArrayList<LibListItem> getActionsList() {
        var actions = new ArrayList<LibListItem>();
        var count = nativeMethods.QSPGetActionsCount();

        for (int i = 0; i < count; ++i) {
            var action = new LibListItem();
            var actionResult = (LibActionData) nativeMethods.QSPGetActionData(i);
            var curGameDir = getCurGameDir();

            if (actionResult.image() == null) {
                action.pathToImage = null;
            } else {
                var tempPath = normalizeContentPath(getFilename(actionResult.image()));
                var fileFromPath = fromRelPath(context, tempPath, curGameDir, false);
                if (fileFromPath != null) {
                    action.pathToImage = String.valueOf(fileFromPath.getUri());
                } else {
                    action.pathToImage = null;
                }
            }

            action.text = gameState.interfaceConfig.useHtml
                    ? removeHtmlTags(actionResult.name())
                    : actionResult.name();

            actions.add(action);

        }

        return actions;
    }

    @NonNull
    private ArrayList<LibListItem> getObjectsList() {
        var objects = new ArrayList<LibListItem>();
        var count = nativeMethods.QSPGetObjectsCount();

        for (int i = 0; i < count; i++) {
            var object = new LibListItem();
            var objectResult = (LibObjectData) nativeMethods.QSPGetObjectData(i);
            var curGameDir = getCurGameDir();

            if (objectResult.name().contains("<img")) {
                if (isContainsHtmlTags(objectResult.name())) {
                    var tempPath = getSrcDir(objectResult.name());
                    var fileFromPath = fromRelPath(context, tempPath, curGameDir, false);
                    object.pathToImage = String.valueOf(fileFromPath);
                } else {
                    var fileFromPath = fromRelPath(context, objectResult.name(), curGameDir, false);
                    object.pathToImage = String.valueOf(fileFromPath);
                }
            } else {
                object.pathToImage = objectResult.image();
                object.text = gameState.interfaceConfig.useHtml
                        ? removeHtmlTags(objectResult.name())
                        : objectResult.name();
            }
            objects.add(object);
        }

        return objects;
    }

    // region LibQpProxy

    public void startLibThread() {
        libThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    nativeMethods.QSPInit();
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    libHandler = new Handler(Looper.myLooper());
                    libThreadInit = true;
                    Looper.loop();
                    nativeMethods.QSPDeInit();
                } catch (Throwable t) {
                    Log.e(TAG, "lib thread has stopped exceptionally", t);
                    Thread.currentThread().interrupt();
                }
            }
        }, "libQSP");
        libThread.start();
    }

    public void stopLibThread() {
//        throwIfNotMainThread();
        if (libThread == null) return;
        if (libThreadInit) {
            var handler = libHandler;
            if (handler != null) {
                handler.getLooper().quitSafely();
            }
            libThreadInit = false;
        } else {
            Log.w(TAG, "libqsp thread has been started, but not initialized");
        }
        libThread.interrupt();
    }

    public void enableDebugMode(boolean isDebug) {
        runOnQspThread(() -> nativeMethods.QSPEnableDebugMode(isDebug));
    }

    @Override
    public void runGame(long gameId,
                        String gameTitle,
                        Uri gameDirUri,
                        Uri gameFileUri) {
        runOnQspThread(() -> doRunGame(gameId, gameTitle, gameDirUri, gameFileUri));
    }

    @Override
    public void restartGame() {
        runOnQspThread(() -> doRunGame(gameState.gameId, gameState.gameTitle, gameState.gameDirUri, gameState.gameFileUri));
    }

    private void doRunGame(final long id, final String title, final Uri dir, final Uri file) {
        gameInterface.doWithCounterDisabled(() -> {
            gameInterface.closeAllFiles();
            gameState.reset();
            gameState.gameRunning = true;
            gameState.gameId = id;
            gameState.gameTitle = title;
            gameState.gameDirUri = dir;
            gameState.gameFileUri = file;
            gameInterface.doChangeCurrGameDir(dir);
            if (!loadGameWorld()) return;
            gameStartTime = SystemClock.elapsedRealtime();
            lastMsCountCallTime = 0;
            if (!nativeMethods.QSPRestartGame(true)) {
                showLastQspError();
            }
        });
    }

    @Override
    public void loadGameState(final Uri uri) {
        if (!isSameThread(libHandler.getLooper().getThread())) {
            runOnQspThread(() -> loadGameState(uri));
            return;
        }
        final var gameData = getFileContents(context, uri);
        if (gameData == null) return;
        if (!nativeMethods.QSPOpenSavedGameFromData(gameData, gameData.length, true)) {
            showLastQspError();
        }
    }

    @Override
    public void saveGameState(final Uri uri) {
        if (!isSameThread(libHandler.getLooper().getThread())) {
            runOnQspThread(() -> saveGameState(uri));
            return;
        }
        final var gameData = nativeMethods.QSPSaveGameAsData(false);
        if (gameData == null) return;
        writeFileContents(context, uri, gameData);
    }

    @Override
    public void onActionClicked(final int index) {
        runOnQspThread(() -> {
            if (!nativeMethods.QSPSetSelActionIndex(index, false)) {
                showLastQspError();
            }
            if (!nativeMethods.QSPExecuteSelActionCode(true)) {
                showLastQspError();
            }
        });
    }

    @Override
    public void onObjectSelected(final int index) {
        runOnQspThread(() -> {
            if (!nativeMethods.QSPSetSelObjectIndex(index, true)) {
                showLastQspError();
            }
        });
    }

    @Override
    public void onInputAreaClicked() {
        if (gameInterface == null) return;
        runOnQspThread(() -> {
            var doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_INPUT, "userInputTitle");
            if (doShow == null) return;
            var input = doShow.outTextValue;
            nativeMethods.QSPSetInputStrText(input);
            if (!nativeMethods.QSPExecUserInput(true)) {
                showLastQspError();
            }
        });
    }

    @Override
    public void onUseExecutorString() {
        if (gameInterface == null) return;
        runOnQspThread(() -> {
            var doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_EXECUTOR, "execStringTitle");
            if (doShow == null) return;
            var input = doShow.outTextValue;
            if (!nativeMethods.QSPExecString(input, true)) {
                showLastQspError();
            }
        });
    }

    @Override
    public void execute(final String code) {
        runOnQspThread(() -> {
            if (!nativeMethods.QSPExecString(code, true)) {
                showLastQspError();
            }
        });
    }

    @Override
    public void executeCounter() {
        if (libLock.isLocked()) return;
        runOnQspThread(() -> {
            if (!nativeMethods.QSPExecCounter(true)) {
                showLastQspError();
            }
        });
    }

    @Override
    public LibGameState getGameState() {
        return gameState;
    }

    @Override
    public void setGameInterface(GameInterface inter) {
        this.gameInterface = inter;
    }

    // endregion LibQpProxy

    // region LibQpCallbacks

    @Override
    public void RefreshInt() {
        var request = new LibRefIRequest();
        var configChanged = loadInterfaceConfiguration();

        if (configChanged) {
            request.isIConfigChanged = true;
        }
        if (nativeMethods.QSPIsMainDescChanged()) {
            if (gameState.mainDesc != null) {
                if (!gameState.mainDesc.equals(nativeMethods.QSPGetMainDesc())) {
                    gameState.mainDesc = nativeMethods.QSPGetMainDesc();
                    request.isMainDescChanged = true;
                }
            } else {
                gameState.mainDesc = nativeMethods.QSPGetMainDesc();
                request.isMainDescChanged = true;
            }
        }
        if (nativeMethods.QSPIsActionsChanged()) {
            if (gameState.actionsList != null) {
                if (gameState.actionsList != getActionsList()) {
                    gameState.actionsList = getActionsList();
                    request.isActionsChanged = true;
                }
            } else {
                gameState.actionsList = getActionsList();
                request.isActionsChanged = true;
            }
        }
        if (nativeMethods.QSPIsObjectsChanged()) {
            if (gameState.objectsList != null) {
                if (gameState.objectsList != getObjectsList()) {
                    gameState.objectsList = getObjectsList();
                    request.isObjectsChanged = true;
                }
            } else {
                gameState.objectsList = getObjectsList();
                request.isObjectsChanged = true;
            }
        }
        if (nativeMethods.QSPIsVarsDescChanged()) {
            if (gameState.varsDesc != null) {
                if (!gameState.varsDesc.equals(nativeMethods.QSPGetVarsDesc())) {
                    gameState.varsDesc = nativeMethods.QSPGetVarsDesc();
                    request.isVarsDescChanged = true;
                }
            } else {
                gameState.varsDesc = nativeMethods.QSPGetVarsDesc();
                request.isVarsDescChanged = true;
            }
        }

        var inter = gameInterface;
        if (inter != null) {
            inter.doRefresh(request);
        }
    }

    @Override
    public void ShowPicture(String path) {
        var inter = gameInterface;
        if (inter == null) return;
        if (isNotEmpty(path)) {
            var picFile = fromFullPath(context, path, getCurGameDir());
            if (picFile == null) return;
            var pathToPic = String.valueOf(picFile.getUri());
            inter.showLibDialog(LibTypeDialog.DIALOG_PICTURE, pathToPic);
        }
    }

    @Override
    public void SetTimer(int delayMillis) {
        var inter = gameInterface;
        if (inter == null) return;
        inter.setCountInter(delayMillis);
    }

    @Override
    public void ShowMessage(String message) {
        var inter = gameInterface;
        if (inter == null) return;
        inter.showLibDialog(LibTypeDialog.DIALOG_MESSAGE, message);
    }

    @Override
    public void PlayFile(String path, int volume) {
        if (gameInterface == null) return;
        if (isNotEmpty(path)) {
            gameInterface.playFile(path, volume);
        }
    }

    @Override
    public boolean IsPlayingFile(final String path) {
        if (gameInterface == null) return false;
        return isNotEmpty(path) && gameInterface.isPlayingFile(path);
    }

    @Override
    public void CloseFile(String path) {
        if (gameInterface == null) return;
        if (isNotEmpty(path)) {
            gameInterface.closeFile(path);
        } else {
            gameInterface.closeAllFiles();
        }
    }

    @Override
    public void OpenGame(String filename) {
        if (filename == null) {
            if (gameInterface == null) return;
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_POPUP_LOAD, null);
        } else {
            try {
                var saveFile = fromFullPath(context, filename, getCurGameDir());
                if (!isWritableFile(context, saveFile)) {
                    if (gameInterface != null) {
                        gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, "Save file not found");
                    }
                    Log.e(TAG , "Save file not found");
                    return;
                }
                if (gameInterface != null) {
                    gameInterface.doWithCounterDisabled(() -> loadGameState(saveFile.getUri()));
                }
            } catch (Exception e) {
                if (gameInterface != null) {
                    gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, e.toString());
                }
                Log.e(TAG , "Error: ", e);
            }
        }
    }

    @Override
    public void SaveGame(String filename) {
        if (filename == null) {
            if (gameInterface == null) return;
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_POPUP_SAVE, null);
        } else {
            var file = new File(filename);
            var saveFile = findOrCreateFile(context, getCurGameDir(), file.getName(), MimeType.TEXT);
            if (isWritableFile(context, saveFile)) {
                saveGameState(saveFile.getUri());
            } else {
                if (gameInterface != null) {
                    gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, "Error access dir");
                }
                Log.e(TAG, "Error access dir");
            }
        }
    }

    @Override
    public String InputBox(String prompt) {
        if (gameInterface == null) return "";
        var doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_INPUT, prompt);
        if (doShow == null) return "";
        return doShow.outTextValue;
    }

    @Override
    public int GetMSCount() {
        var now = SystemClock.elapsedRealtime();
        if (lastMsCountCallTime == 0) {
            lastMsCountCallTime = gameStartTime;
        }
        var dt = (int) (now - lastMsCountCallTime);
        lastMsCountCallTime = now;
        return dt;
    }

    @Override
    public void AddMenuItem(String name, String imgPath) {
        var item = new LibMenuItem();
        item.name = name;
        item.pathToImage = imgPath;
        gameState.menuItemsList.add(item);
    }

    @Override
    public void ShowMenu() {
        if (gameInterface == null) return;
        var doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_MENU, null);
        if (doShow == null) return;
        int result = doShow.outNumValue;
        if (result != -1) {
            nativeMethods.QSPSelectMenuItem(result);
        }
    }

    @Override
    public void DeleteMenu() {
        gameState.menuItemsList.clear();
    }

    @Override
    public void Wait(int msecs) {
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException ex) {
            Log.e(TAG, "Wait failed", ex);
        }
    }

    @Override
    public void ShowWindow(int type, boolean isShow) {
        if (gameInterface == null) return;
        var windowType = LibTypeWindow.values()[type];
        gameInterface.changeVisWindow(windowType, isShow);
    }

    @Override
    public byte[] GetFileContents(String path) {
        var targetFile = fromFullPath(context, path, getCurGameDir());
        if (targetFile == null) return null;
        var targetFileUri = targetFile.getUri();
        return getFileContents(context , targetFileUri);
    }

    @Override
    public void ChangeQuestPath(String path) {
        var newGameDir = fromFullPath(context, path , getCurGameDir());
        if (newGameDir == null || !newGameDir.exists()) {
            Log.e(TAG,"Game directory not found: " + path);
            return;
        }
        var currGameDirUri = getCurGameDir().getUri();
        var newGameDirUri = newGameDir.getUri();
        if (!Objects.equals(currGameDirUri, newGameDirUri)) {
            gameState.gameDirUri = newGameDirUri;
            gameInterface.doChangeCurrGameDir(newGameDirUri);
        }
    }

    // endregion LibQpCallbacks
}
