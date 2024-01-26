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
                UnblockMeBlock(0,2, 2, Direction.Horizontal, true),
                UnblockMeBlock(0,0,3, Direction.Horizontal),
                UnblockMeBlock(5,0,3, Direction.Vertical),
                UnblockMeBlock(2,1,3, Direction.Vertical),
                UnblockMeBlock(0,3,2, Direction.Vertical),
                UnblockMeBlock(4,3,2, Direction.Horizontal),
                UnblockMeBlock(4,4,2, Direction.Vertical),
                UnblockMeBlock(0,5,3, Direction.Horizontal)
            ), 0, 15
        ),
        Puzzle(6,6,
            listOf(
                UnblockMeBlock(0,2, 2, Direction.Horizontal, true),
                UnblockMeBlock(2,1,2, Direction.Vertical),
                UnblockMeBlock(3,1,3, Direction.Vertical),
                UnblockMeBlock(4,1,3, Direction.Vertical),
                UnblockMeBlock(0,3,2, Direction.Horizontal),
                UnblockMeBlock(2,3,2, Direction.Vertical),
                UnblockMeBlock(1,4,2, Direction.Vertical),
                UnblockMeBlock(2,5,2, Direction.Horizontal)
            ), 0, 17
        ),
        Puzzle(6,6,
            listOf(
                UnblockMeBlock(0,2, 2, Direction.Horizontal, true),
                UnblockMeBlock(0,0,2, Direction.Vertical),
                UnblockMeBlock(1,0,2, Direction.Horizontal),
                UnblockMeBlock(3,0,2, Direction.Horizontal),
                UnblockMeBlock(2,1,2, Direction.Vertical),
                UnblockMeBlock(3,2,3, Direction.Vertical),
                UnblockMeBlock(4,2,3, Direction.Vertical),
                UnblockMeBlock(0,4,3, Direction.Horizontal)
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
        if (currentPuzzleIndex < puzzles.size - 1)
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