package com.example.unblockme

import android.util.Log

data class Move(val block: UnblockMeBlock, val amount: Int)

data class Puzzle(val width: Int, val height: Int, val blocks: List<UnblockMeBlock>, val highScore: Int, val bestScore: Int)
{
    var moves: ArrayDeque<Move> = ArrayDeque()
}
class UnblockMeGame {
    val puzzles: List<Puzzle> = listOf(
        Puzzle(6, 6,
            listOf(
                UnblockMeBlock(1,2, 2, Direction.Horizontal, true),
                UnblockMeBlock(4,1,3, Direction.Vertical)
            ), 0, 15
        )
    )

    private var currentPuzzleIndex: Int

    constructor()
    {
        currentPuzzleIndex = 0
    }

    fun getBoarWidth(): Int { return getCurrentPuzzle().width }

    fun getBoardHeight(): Int { return getCurrentPuzzle().height }

    private fun getCurrentPuzzle(): Puzzle { return puzzles.get(currentPuzzleIndex) }

    fun getNumberOfMoves(): Int { return getCurrentPuzzle().moves.size }

    fun moveBlockNoRegister(block: UnblockMeBlock, amount: Int)
    {
        when (block.direction)
        {
            Direction.Vertical      -> block.y += amount
            Direction.Horizontal    -> block.x += amount
        }
    }

    fun moveBlock(block: UnblockMeBlock, amount: Int)
    {
        moveBlockNoRegister(block, amount)

        getCurrentPuzzle().moves.addLast(Move(block, amount))
    }

    fun getBlocks(): List<UnblockMeBlock> { return getCurrentPuzzle().blocks }

    fun getCurrentPuzzleIndex(): Int { return currentPuzzleIndex }

    fun nextPuzzle()
    {
        if (currentPuzzleIndex < puzzles.size)
        {
            currentPuzzleIndex++
        }
    }

    fun previousPuzzle()
    {
        if (currentPuzzleIndex > 0)
        {
            currentPuzzleIndex--
        }
    }
    fun undo()
    {
        if (getCurrentPuzzle().moves.isEmpty()) return

        val move: Move = getCurrentPuzzle().moves.removeLast()

        for (block in getCurrentPuzzle().blocks)
        {
            if (block != move.block) continue

            moveBlockNoRegister(block, -move.amount)
            break
        }
    }
    fun restart()
    {
        while (getCurrentPuzzle().moves.isNotEmpty()) undo()
    }
}