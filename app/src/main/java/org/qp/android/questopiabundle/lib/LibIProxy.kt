package org.qp.android.questopiabundle.lib

import android.net.Uri
import org.qp.android.questopiabundle.GameInterface

interface LibIProxy {
    /**
     * Starts the library thread.
     */
    fun startLibThread()
    /**
     * Stops the library thread.
     */
    fun stopLibThread()
    fun enableDebugMode(isDebug: Boolean)
    fun runGame(gameId: Long, gameTitle: String, gameDirUri: Uri, gameFileUri: Uri)
    fun restartGame()
    fun loadGameState(uri: Uri)
    fun saveGameState(uri: Uri)
    fun onActionClicked(index: Int)
    fun onObjectSelected(index: Int)
    fun onInputAreaClicked()
    fun onUseExecutorString()
    /**
     * Starts execution of the specified line of code in the library.
     */
    fun execute(code: String?)
    /**
     * Starts processing the location counter in the library.
     */
    fun executeCounter()
    val gameState: LibGameState
    fun setGameInterface(inter: GameInterface)
}
