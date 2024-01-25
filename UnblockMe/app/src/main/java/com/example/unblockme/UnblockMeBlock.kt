package com.example.unblockme

import android.graphics.Bitmap

enum class Direction {
    Vertical,
    Horizontal
}

class UnblockMeBlock
{
    var x: Int
    var y: Int
    val size: Int
    val direction: Direction
    val isWinner: Boolean

    constructor(x: Int, y: Int, size: Int, direction: Direction, isWinner: Boolean = false)
    {
        this.x = x
        this.y = y
        this.size = size
        this.direction = direction
        this.isWinner = isWinner
    }
}