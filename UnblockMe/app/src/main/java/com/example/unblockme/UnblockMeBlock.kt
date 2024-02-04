package com.example.unblockme

enum class Direction {
    Vertical,
    Horizontal
}

class UnblockMeBlock(
    var x: Int,
    var y: Int,
    val size: Int,
    val direction: Direction,
    val isWinner: Boolean = false
)