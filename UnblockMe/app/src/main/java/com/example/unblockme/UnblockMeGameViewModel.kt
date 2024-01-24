package com.example.unblockme

import android.util.Log
import androidx.lifecycle.ViewModel

class UnblockMeGameViewModel : ViewModel() {
    private var blocks: MutableList<UnblockMeBlock> = mutableListOf<UnblockMeBlock>()

    var _width: Int = 0
    var _height: Int = 0

    fun getWidth(): Int { return _width }

    fun getHeight(): Int { return _height }

    fun setBoardDimensions(width: Int, height: Int)
    {
        _width = width
        _height = height
    }

    fun moveBlock(block: UnblockMeBlock, move: Int)
    {
        Log.d("UnblockMeGameViewModel", "Moved block from (${block.x}, ${block.y})")
        when (block.direction)
        {
            Direction.Vertical      -> block.y += move
            Direction.Horizontal    -> block.x += move
        }
        Log.d("UnblockMeGameViewModel", "to (${block.x}, ${block.y})")
    }

    fun addBlock(block: UnblockMeBlock) { blocks.add(block) }

    fun removeBlock(block: UnblockMeBlock) { blocks.remove(block) }

    fun removeAllBlocks() { blocks.clear() }

    fun getBlocks(): List<UnblockMeBlock> { return blocks.toList() }

}