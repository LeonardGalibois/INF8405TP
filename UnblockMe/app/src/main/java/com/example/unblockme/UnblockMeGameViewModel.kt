package com.example.unblockme

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UnblockMeGameViewModel : ViewModel() {
    private var model: UnblockMeGame = UnblockMeGame()
    var moveNumber: MutableLiveData<Int> = MutableLiveData<Int>(0)

    // Return board width
    fun getWidth(): Int { return model.getBoardWidth() }

    // Return board height
    fun getHeight(): Int { return model.getBoardHeight() }

    // Move block and update moves counter
    fun moveBlock(block: UnblockMeBlock, move: Int)
    {
        model.moveBlock(block, move)
        moveNumber.value = model.getNumberOfMoves()
    }

    // Return all blocks for current puzzle
    fun getBlocks(): List<UnblockMeBlock> { return model.getBlocks()}

    // Undo latest move and update number of moves
    fun undo()
    {
        model.undo()
        moveNumber.value = model.getNumberOfMoves()
    }

    // Restart puzzle and update number of moves
    fun restart()
    {
        model.restart()
        moveNumber.value = model.getNumberOfMoves()
    }

    // Decrease puzzle index and reset moves counter
    fun previousPuzzle()
    {
        model.previousPuzzle()
        restart()
        // TODO
    }

    // Increase puzzle index and reset moves counter
    fun nextPuzzle()
    {
        model.nextPuzzle()
        restart()
        // TODO
    }

    // Return current puzzle index
    fun getCurrentPuzzleNumber() : Int
    {
        return model.getCurrentPuzzleIndex() + 1
    }

    // Return number of puzzles
    fun getNumberOfPuzzle(): Int { return model.getNumberOfPuzzle() }
}