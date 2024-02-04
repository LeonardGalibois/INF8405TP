package com.example.unblockme

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import kotlin.math.roundToInt

/**
 * TODO: document your custom view class.
 */

data class BlockDrag(val block: UnblockMeBlock, val startX: Float, val startY: Float)
{
    var move: Int = 0
}

class UnblockMeGameView : View {

    private var _ongoingDrag: BlockDrag? = null

    private val viewModel by lazy {
        ViewModelProvider(findViewTreeViewModelStoreOwner()!!).get(UnblockMeGameViewModel::class.java)
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.UnblockMeView, defStyle, 0
        )

        a.recycle()
    }

    private fun getBlockRect(block: UnblockMeBlock): Rect
    {
        val cellSize: Float = width.toFloat() / viewModel.getWidth().toFloat()

        var x: Int = block.x
        var y: Int = block.y

        if (_ongoingDrag != null && _ongoingDrag!!.block == block)
        {
            val block: UnblockMeBlock = _ongoingDrag!!.block

            if (block.direction == Direction.Vertical)
            {
                // Block moves up and down
                y += _ongoingDrag!!.move
            }
            else
            {
                // Block moves left and right
                x += _ongoingDrag!!.move
            }
        }

        var left: Float = x * cellSize
        var top: Float = y * cellSize
        var right: Float = (x + 1) * cellSize
        var bottom: Float = (y + 1) * cellSize

        if (block.size > 1)
        {
            when (block.direction)
            {
                Direction.Vertical      -> bottom   += cellSize * (block.size - 1)
                Direction.Horizontal    -> right    += cellSize * (block.size - 1)
            }
        }

        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    private fun getBlockAt(x: Int, y: Int): UnblockMeBlock?
    {
        for (block in viewModel.getBlocks())
        {
            val rect: Rect = getBlockRect(block)

            if (!rect.contains(x, y)) continue

            return block
        }

        return null
    }

    private fun onBlockDragBegin(block: UnblockMeBlock, x: Float, y: Float)
    {
        _ongoingDrag = BlockDrag(block, x, y)

        Log.d("UnblockMeGameView","Started dragging block that was at (${block.x}, ${block.y})")
    }

    private fun onBlockDragEnded(x: Float, y: Float)
    {
        if (_ongoingDrag == null) return

        val block: UnblockMeBlock = _ongoingDrag!!.block
        val move: Int = _ongoingDrag!!.move

        Log.d("UnblockMeGameView", "Stopped dragging block that was at (${block.x}, ${block.y})")

        if (move != 0)
        {
            Log.d("UnblockMeGameView", "Moved block by $move units")
            viewModel.moveBlock(block, move)
            invalidate()
        }
        _ongoingDrag = null

    }

    private fun onBlockDragged(x: Float, y: Float)
    {
        if (_ongoingDrag == null) return

        val block: UnblockMeBlock = _ongoingDrag!!.block
        val cellSize: Float = measuredWidth.toFloat() / viewModel.getWidth().toFloat()

        var move: Int

        if (block.direction == Direction.Vertical)
        {
            // Block moves up and down

            move = ((y - _ongoingDrag!!.startY) / cellSize).roundToInt()
        }
        else {
            // Block moves left and right

            move = ((x - _ongoingDrag!!.startX) / cellSize).roundToInt()
        }

        move = CheckBlockCollisions(block, move)

        if (move != _ongoingDrag!!.move)
        {
            _ongoingDrag!!.move = move
            invalidate()
        }
    }

    private fun CheckBlockMutualCollisions(self: UnblockMeBlock, move: Int): Int
    {
        var finalMove: Int = move

        var grid: MutableList<MutableList<Int>> = mutableListOf()

        for (y in 0..viewModel.getHeight())
        {
            grid[y] = mutableListOf()

            for (x in 0..viewModel.getWidth()) grid[y][x] = 0
        }

        return finalMove
    }

    private fun CheckBlockBoundsCollisions(block: UnblockMeBlock, move: Int): Int
    {
        var finalMove: Int = move

        if (block.direction == Direction.Vertical)
        {
            // Block moves up and down, so we must check upper and lower bounds

            var newY: Int = block.y + move

            if (newY < 0) newY = 0
            else if (newY > viewModel.getHeight() - block.size) newY = viewModel.getHeight() - block.size

            finalMove = newY - block.y
        }
        else
        {
            // Block moves left and right, so we must check side bounds

            var newX: Int = block.x + move

            if (newX < 0) newX = 0
            else if (newX > viewModel.getWidth() - block.size) newX = viewModel.getHeight() - block.size

            finalMove = newX - block.x
        }

        return finalMove
    }

    private fun CheckBlockCollisions(block: UnblockMeBlock, move: Int): Int
    {
        var finalMove: Int = move

        if (block.direction == Direction.Vertical)
        {
            // Block moves up and down

            var minY: Int = 0
            var maxY: Int = viewModel.getHeight() - block.size

            for (other in viewModel.getBlocks())
            {
                if (block == other || block.x < other.x || block.x >= other.x + other.size) continue

                if (other.y < block.y && other.y >= minY) minY = other.y + 1
                if (other.y > block.y && other.y <= maxY) maxY = other.y - block.size
            }

            if (finalMove > maxY - block.y) finalMove = maxY - block.y
            else if (finalMove < minY - block.y) finalMove = minY - block.y
        }
        else
        {
            // Block moves left and right

            var minX: Int = 0
            var maxX: Int = viewModel.getWidth() - block.size

            for (other in viewModel.getBlocks())
            {
                if (block == other || block.y < other.y || block.y >= other.y + other.size) continue

                if (other.x < block.x && other.x >= minX) minX = other.x + 1
                if (other.x > block.x && other.x <= maxX) maxX = other.x - block.size
            }

            if (finalMove > maxX - block.x) finalMove = maxX - block.x
            else if (finalMove < minX - block.x) finalMove = minX - block.x
        }

        return finalMove
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null)
        {
            val x: Float = event.x
            val y: Float = event.y

            if (event.action == MotionEvent.ACTION_DOWN) {
                val blockTouched: UnblockMeBlock? = getBlockAt(x.toInt(), y.toInt())

                if (blockTouched != null) onBlockDragBegin(blockTouched, x, y)

                return true
            }
            else if (event.action == MotionEvent.ACTION_UP) {
                onBlockDragEnded(x, y)
                return true
            }
            else {
                onBlockDragged(x, y)
                return true
            }
        }

        return false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (block in viewModel.getBlocks())
        {
            var brush: Paint = Paint()

            if (block.isWinner) brush.setARGB(255, 255, 0, 0)
            else brush.setARGB(255, 0, 0, 255)

            val rect = getBlockRect(block)
            
            canvas.apply { drawRect(rect, brush)  }
        }
    }
}