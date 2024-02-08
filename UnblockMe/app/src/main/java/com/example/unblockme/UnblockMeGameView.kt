package com.example.unblockme

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import kotlin.math.min
import kotlin.math.max
import kotlin.math.roundToInt

data class BlockDrag(val block: UnblockMeBlock, val startX: Float, val startY: Float)
{
    var move: Int = 0
}

class UnblockMeGameView : View {
    private val fill = Paint()
    private val stroke = Paint()

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

    // Get block position on the board
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

        val left: Float = x * cellSize
        val top: Float = y * cellSize
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

    // Start dragging the block
    private fun onBlockDragBegin(block: UnblockMeBlock, x: Float, y: Float)
    {
        _ongoingDrag = BlockDrag(block, x, y)
    }

    // Stop dragging the block
    private fun onBlockDragEnded()
    {
        if (_ongoingDrag == null) return

        val block: UnblockMeBlock = _ongoingDrag!!.block
        val move: Int = _ongoingDrag!!.move

        if (move != 0)
        {
            viewModel.moveBlock(block, move)
            if (block.isWinner && block.x == 4)
            {
                viewModel.triggerSuccessWindow()
            }
            invalidate()
        }
        _ongoingDrag = null

    }

    // Handle block movement
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

    // Check vertical and horizontal collisions between blocks
    private fun CheckBlockCollisions(block: UnblockMeBlock, move: Int): Int
    {
        val displacement: Int

        if (block.direction == Direction.Vertical)
        {
            displacement = CheckVerticalCollisions(block, move)
        }
        else
        {
            displacement = CheckHorizontalCollisions(block, move)
        }

        return displacement
    }

    // Check vertical collisions between blocks
    private fun CheckVerticalCollisions(block: UnblockMeBlock, move: Int): Int
    {
        var displacement: Int = move

        // Block moves up and down

        displacement = min(displacement, viewModel.getHeight() - block.size - block.y)
        displacement = max(displacement, - block.y)

        for (other in viewModel.getBlocks())
        {
            if (other == block) continue

            if (other.direction == Direction.Vertical)
            {
                // Both blocks move in the same direction

                if (other.x != block.x) continue

                if (block.y < other.y)
                {
                    // Block is above other

                    if (displacement < 0) continue

                    displacement = min(displacement, other.y - block.y - block.size)
                }
                else
                {
                    // Block is below other

                    if (displacement > 0) continue

                    displacement = max(displacement, other.y - block.y + other.size)
                }
            }
            else
            {
                // Blocks move in different directions

                if (block.x < other.x || block.x >= other.x + other.size) continue

                if (block.y < other.y)
                {
                    // Block is above other

                    if (displacement < 0) continue

                    displacement = min(displacement, other.y - block.y - block.size)
                }
                else
                {
                    // Block is below other

                    if (displacement > 0) continue

                    displacement = max(displacement, other.y - block.y + 1)
                }
            }
        }

        return displacement
    }

    // Check horizontal collisions between blocks
    private fun CheckHorizontalCollisions(block: UnblockMeBlock, move: Int): Int
    {
        var displacement: Int = move

        // Block moves left and right

        displacement = min(displacement, viewModel.getWidth() - block.size - block.x)
        displacement = max(displacement, - block.x)

        for (other in viewModel.getBlocks())
        {
            if (other == block) continue

            if (other.direction == Direction.Vertical)
            {
                // Blocks move in different directions

                if (block.y < other.y || block.y >= other.y + other.size) continue

                if (block.x < other.x)
                {
                    // Block is to the left of other

                    if (displacement < 0) continue

                    displacement = min(displacement, other.x - block.x - block.size)
                }
                else
                {
                    // Block is to the right of other

                    if (displacement > 0) continue

                    displacement = max(displacement, other.x - block.x + 1)
                }
            }
            else
            {
                // Blocks both move in the same direction

                if (other.y != block.y) continue

                if (block.x < other.x)
                {
                    // Block is to the left of other

                    if (displacement < 0) continue

                    displacement = min(displacement, other.x - block.x - block.size)
                }
                else
                {
                    // Block is to the right of other

                    if (displacement > 0) continue

                    displacement = max(displacement, other.x - block.x + other.size)
                }
            }
        }

        return displacement
    }

    // Handle click on block
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null)
        {
            val x: Float = event.x
            val y: Float = event.y

            // Start holding block
            if (event.action == MotionEvent.ACTION_DOWN) {
                val blockTouched: UnblockMeBlock? = getBlockAt(x.toInt(), y.toInt())

                if (blockTouched != null) onBlockDragBegin(blockTouched, x, y)

                return true
            }
            // Stop holding block
            else if (event.action == MotionEvent.ACTION_UP) {
                onBlockDragEnded()
                return true
            }
            else {
                onBlockDragged(x, y)
                return true
            }
        }

        return false
    }

    // Draw and paint blocks on the board
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (block in viewModel.getBlocks())
        {
            if (block.isWinner) {
                fill.color = Color.RED
            }
            else {
                fill.color = Color.BLUE
            }
            fill.style = Paint.Style.FILL
            stroke.color = Color.BLACK
            stroke.style = Paint.Style.STROKE
            stroke.strokeWidth = 7f

            val rect = getBlockRect(block)

            canvas.apply { drawRect(rect, fill)  }
            canvas.apply { drawRect(rect, stroke)  }
        }
    }
}