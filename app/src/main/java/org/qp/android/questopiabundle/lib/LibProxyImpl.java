package org.qp.android.questopiabundle.lib;

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
import static org.qp.android.questopiabundle.utils.StringUtil.isNotEmptyOrBlank;
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
import com.libqsp.jni.QSPLib;

import org.qp.android.questopiabundle.GameInterface;
import org.qp.android.questopiabundle.dto.LibListItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class LibProxyImpl extends QSPLib implements LibIProxy {
    private final String TAG = "LibProxyImpl";

    private final ReentrantLock libLock = new ReentrantLock();
    private final LibGameState gameState = new LibGameState();
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
        var gameFileUri = gameState.gameFileUri;
        var gameData = getFileContents(context, gameFileUri);
        if (gameData == null) return false;

        if (!loadGameWorldFromData(gameData, true)) {
            showLastQspError();
            Log.d("QSP", "World is not loaded!");
            return false;
        }
        Log.d("QSP", "World is loaded!");
        return true;
    }

    private void showLastQspError() {
        var errorData = getLastErrorData();
        var locName = getStringOrEmpty(errorData.locName);
        var desc = getStringOrEmpty(getErrorDesc(errorData.errorNum));
        final var message = String.format(
                Locale.getDefault(),
                "Location: %s\nAction: %d\nLine: %d\nError number: %d\nDescription: %s",
                locName,
                errorData.actIndex,
                errorData.intLineNum,
                errorData.errorNum,
                desc);
        Log.e(TAG, errorData.toString());
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

        var htmlResult = getNumVarValue("USEHTML", 0);
        var useHtml = htmlResult != 0;
        if (config.useHtml != useHtml) {
            config.useHtml = useHtml;
            changed = true;
        }

        var fSizeResult = getNumVarValue("FSIZE", 0);
        if (config.fontSize != fSizeResult) {
            config.fontSize = fSizeResult;
            changed = true;
        }

        var bColorResult = getNumVarValue("BCOLOR", 0);
        if (config.backColor != bColorResult) {
            config.backColor = bColorResult;
            changed = true;
        }

        var fColorResult = getNumVarValue("FCOLOR", 0);
        if (config.fontColor != fColorResult) {
            config.fontColor = fColorResult;
            changed = true;
        }

        var lColorResult = getNumVarValue("LCOLOR", 0);
        if (config.linkColor != lColorResult) {
            config.linkColor = lColorResult;
            changed = true;
        }

        return changed;
    }

    @NonNull
    private ArrayList<LibListItem> getActionsList() {
        var actions = new ArrayList<LibListItem>();
        var curGameDir = getCurGameDir();
        for (var element : getActions()) {
            var newElement = new LibListItem(element);
            if (isNotEmptyOrBlank(newElement.pathToImage)) {
                var tempPath = normalizeContentPath(getFilename(newElement.pathToImage));
                var fileFromPath = fromRelPath(context, tempPath, curGameDir, false);
                if (fileFromPath != null) {
                    newElement.pathToImage = String.valueOf(fileFromPath.getUri());
                } else {
                    newElement.pathToImage = null;
                }
            }
            newElement.text = gameState.interfaceConfig.useHtml
                    ? removeHtmlTags(newElement.text)
                    : newElement.text;
            actions.add(newElement);
        }
        return actions;
    }

    @NonNull
    private ArrayList<LibListItem> getObjectsList() {
        var objects = new ArrayList<LibListItem>();
        var curGameDir = getCurGameDir();

        for (var element : getObjects()) {
            var newElement = new LibListItem(element);
            if (newElement.text.contains("<img")) {
                if (isContainsHtmlTags(newElement.text)) {
                    var tempPath = getSrcDir(newElement.text);
                    var fileFromPath = fromRelPath(context, tempPath, curGameDir, false);
                    newElement.pathToImage = String.valueOf(fileFromPath);
                } else {
                    var fileFromPath = fromRelPath(context, newElement.text, curGameDir, false);
                    newElement.pathToImage = String.valueOf(fileFromPath);
                }
            } else {
                newElement.text = gameState.interfaceConfig.useHtml
                        ? removeHtmlTags(newElement.text)
                        : newElement.text;
            }
            objects.add(newElement);
        }

        return objects;
    }

    // region LibQpProxy

    public void startLibThread() {
        libThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    init();
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                    }
                    libHandler = new Handler(Looper.myLooper());
                    libThreadInit = true;
                    Looper.loop();
                    terminate();
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
        runOnQspThread(() -> enableDebugMode(isDebug));
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
            if (!restartGame(true)) {
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
        if (!openSavedGameFromData(gameData, true)) {
            showLastQspError();
        }
    }

    @Override
    public void saveGameState(final Uri uri) {
        if (!isSameThread(libHandler.getLooper().getThread())) {
            runOnQspThread(() -> saveGameState(uri));
            return;
        }
        final var gameData = saveGameAsData(false);
        if (gameData == null) return;
        writeFileContents(context, uri, gameData);
    }

    @Override
    public void onActionClicked(final int index) {
        runOnQspThread(() -> {
            if (!setSelActIndex(index, false)) {
                showLastQspError();
            }
            if (!execSelAction(true)) {
                showLastQspError();
            }
        });
    }

    @Override
    public void onObjectSelected(final int index) {
        runOnQspThread(() -> {
            if (!setSelObjIndex(index, true)) {
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
            setInputStrText(input);
            if (!execUserInput(true)) {
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
            if (!execString(input, true)) {
                showLastQspError();
            }
        });
    }

    @Override
    public void execute(final String code) {
        runOnQspThread(() -> {
            if (!execString(code, true)) {
                showLastQspError();
            }
        });
    }

    @Override
    public void executeCounter() {
        if (libLock.isLocked()) return;
        runOnQspThread(() -> {
            if (!execCounter(true)) {
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
    public void onRefreshInt(boolean isForced) {
        var request = new LibRefIRequest();

        if (loadInterfaceConfiguration()) {
            request.isIConfigChanged = true;
        }
        if (isMainDescChanged()) {
            if (isNotEmptyOrBlank(gameState.mainDesc)) {
                if (!gameState.mainDesc.equals(getMainDesc())) {
                    gameState.mainDesc = getMainDesc();
                    request.isMainDescChanged = true;
                }
            } else {
                gameState.mainDesc = getMainDesc();
                request.isMainDescChanged = true;
            }
        }
        if (isActsChanged()) {
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
        if (isObjsChanged()) {
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
        if (isVarsDescChanged()) {
            if (isNotEmptyOrBlank(gameState.varsDesc)) {
                if (!gameState.varsDesc.equals(getVarsDesc())) {
                    gameState.varsDesc = getVarsDesc();
                    request.isVarsDescChanged = true;
                }
            } else {
                gameState.varsDesc = getVarsDesc();
                request.isVarsDescChanged = true;
            }
        }

        var inter = gameInterface;
        if (inter != null) {
            inter.doRefresh(request);
        }
    }

    @Override
    public void onShowImage(String file) {
        var inter = gameInterface;
        if (inter == null) return;
        if (isNotEmptyOrBlank(file)) {
            var picFile = fromFullPath(context, file, getCurGameDir());
            if (picFile == null) return;
            var pathToPic = String.valueOf(picFile.getUri());
            inter.showLibDialog(LibTypeDialog.DIALOG_PICTURE, pathToPic);
        }
    }

    @Override
    public void onSetTimer(int msecs) {
        var inter = gameInterface;
        if (inter == null) return;
        inter.setCountInter(msecs);
    }

    @Override
    public void onShowMessage(String text) {
        var inter = gameInterface;
        if (inter == null) return;
        inter.showLibDialog(LibTypeDialog.DIALOG_MESSAGE, text);
    }

    @Override
    public void onPlayFile(String file, int volume) {
        if (gameInterface == null) return;
        if (isNotEmptyOrBlank(file)) {
            gameInterface.playFile(file, volume);
        }
    }

    @Override
    public boolean onIsPlayingFile(String file) {
        if (gameInterface == null) return false;
        return isNotEmptyOrBlank(file) && gameInterface.isPlayingFile(file);
    }

    @Override
    public void onCloseFile(String file) {
        if (gameInterface == null) return;
        if (isNotEmptyOrBlank(file)) {
            gameInterface.closeFile(file);
        } else {
            gameInterface.closeAllFiles();
        }
    }

    @Override
    public void onOpenGameStatus(String file) {
        if (file == null) {
            if (gameInterface == null) return;
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_POPUP_LOAD, null);
        } else {
            try {
                var saveFile = fromFullPath(context, file, getCurGameDir());
                if (!isWritableFile(context, saveFile)) {
                    if (gameInterface != null) {
                        gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, "Save file not found");
                    }
                    Log.e(TAG, "Save file not found");
                    return;
                }
                if (gameInterface != null) {
                    gameInterface.doWithCounterDisabled(() -> loadGameState(saveFile.getUri()));
                }
            } catch (Exception e) {
                if (gameInterface != null) {
                    gameInterface.showLibDialog(LibTypeDialog.DIALOG_ERROR, e.toString());
                }
                Log.e(TAG, "Error: ", e);
            }
        }
    }

    @Override
    public void onSaveGameStatus(String file) {
        if (file == null) {
            if (gameInterface == null) return;
            gameInterface.showLibDialog(LibTypeDialog.DIALOG_POPUP_SAVE, null);
        } else {
            var save = new File(file);
            var saveFile = findOrCreateFile(context, getCurGameDir(), save.getName(), MimeType.TEXT);
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
    public String onInputBox(String text) {
        if (gameInterface == null) return "";
        var doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_INPUT, text);
        if (doShow == null) return "";
        return doShow.outTextValue;
    }

    @Override
    public int onGetMsCount() {
        var now = SystemClock.elapsedRealtime();
        if (lastMsCountCallTime == 0) {
            lastMsCountCallTime = gameStartTime;
        }
        var dt = now - lastMsCountCallTime;
        lastMsCountCallTime = now;
        return (int) dt;
    }

    @Override
    public int onShowMenu(ListItem[] items) {
        if (gameInterface == null) return super.onShowMenu(items);
        var doShow = gameInterface.showLibDialog(LibTypeDialog.DIALOG_MENU, null);
        if (doShow == null) return super.onShowMenu(items);
        var result = doShow.outNumValue;
        if (result != -1) {
            return result;
        }
        return super.onShowMenu(items);
    }

    @Override
    public void onSleep(int msecs) {
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException ex) {
            Log.e(TAG, "Wait failed", ex);
        }
    }

    @Override
    public void onShowWindow(int type, boolean toShow) {
        if (gameInterface == null) return;
        var windowType = LibTypeWindow.values()[type];
        gameInterface.changeVisWindow(windowType, toShow);
    }

    @Override
    public void onOpenGame(String file, boolean isNewGame) {
        var newGameDir = fromFullPath(context, file, getCurGameDir());
        if (newGameDir == null || !newGameDir.exists()) {
            Log.e(TAG, "Game directory not found: " + file);
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
