package com.example.tp1

data class Block(val id: Int, var x: Int, var y: Int, val width: Int, val height: Int)

class Puzzle {
    val blocksPuzzle1 = mutableListOf(
        Block(0, 1, 3, 2, 1),
        Block(1, 1, 1, 3, 1),
        Block(2, 3, 2, 1, 3),
        Block(3, 1, 4, 1, 2),
        Block(4, 1, 6, 3, 1),
        Block(5, 6, 1, 1, 3),
        Block(6, 5, 4, 2, 1),
        Block(7, 5, 5, 1, 2),
    )

    val blocksPuzzle2 = mutableListOf(
        Block(0, 1, 3, 2, 1),
        Block(1, 1, 4, 2, 1),
        Block(2, 2, 5, 1, 2),
        Block(3, 3, 2, 1, 2),
        Block(4, 3, 4, 1, 2),
        Block(5, 3, 6, 2, 1),
        Block(6, 4, 2, 1, 3),
        Block(7, 5, 2, 1, 3),
    )

    val blocksPuzzle3 = mutableListOf(
        Block(0, 1, 3, 2, 1),
        Block(1, 1, 1, 1, 2),
        Block(2, 2, 1, 2, 1),
        Block(3, 4, 1, 2, 1),
        Block(4, 3, 2, 1, 2),
        Block(5, 1, 5, 3, 1),
        Block(6, 4, 3, 1, 3),
        Block(7, 5, 3, 1, 3),
    )
}