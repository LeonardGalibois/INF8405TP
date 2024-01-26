package com.example.unblockme

import android.util.Log
import androidx.lifecycle.ViewModel

class UnblockMeGameViewModel : ViewModel() {
    private var model: UnblockMeGame = UnblockMeGame()

    fun getWidth(): Int { return model.getBoarWidth() }

    fun getHeight(): Int { return model.getBoardHeight() }

    fun moveBlock(block: UnblockMeBlock, move: Int)
    {
        model.moveBlock(block, move)
    }

    fun getBlocks(): List<UnblockMeBlock> { return model.getBlocks()}

    fun undo() { model.undo() }

    fun restart() { model.restart() }

    fun previousPuzzle()
    {
        model.previousPuzzle()

        // TODO
    }

    fun nextPuzzle()
    {
        model.nextPuzzle()

        // TODO
    }

    fun getCurrentPuzzleNumber() : Int
    {
        return model.getCurrentPuzzleIndex() + 1
    }

    fun getNumberOfPuzzle(): Int {return model.getNumberOfPuzzle() }
}