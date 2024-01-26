package com.example.unblockme

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UnblockMeGameViewModel : ViewModel() {
    private var model: UnblockMeGame = UnblockMeGame()
    var moveNumber: MutableLiveData<Int> = MutableLiveData<Int>(0)

    fun getWidth(): Int { return model.getBoarWidth() }

    fun getHeight(): Int { return model.getBoardHeight() }

    fun moveBlock(block: UnblockMeBlock, move: Int)
    {
        model.moveBlock(block, move)
        moveNumber.value = model.getNumberOfMoves()
    }

    fun getBlocks(): List<UnblockMeBlock> { return model.getBlocks()}

    fun undo()
    {
        model.undo()
        moveNumber.value = model.getNumberOfMoves()
    }

    fun restart()
    {
        model.restart()
        moveNumber.value = model.getNumberOfMoves()
    }

    fun previousPuzzle()
    {
        model.previousPuzzle()
        moveNumber.value = model.getNumberOfMoves()
        // TODO
    }

    fun nextPuzzle()
    {
        model.nextPuzzle()
        moveNumber.value = model.getNumberOfMoves()
        // TODO
    }

    fun getCurrentPuzzleNumber() : Int
    {
        return model.getCurrentPuzzleIndex() + 1
    }

    fun getNumberOfPuzzle(): Int { return model.getNumberOfPuzzle() }
}