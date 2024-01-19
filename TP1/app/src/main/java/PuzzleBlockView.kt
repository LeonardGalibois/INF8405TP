import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.example.tp1.Puzzle

class PuzzleBlockView(context: Context) : View(context) {
    private val paint: Paint = Paint()
    val puzzle: Puzzle = Puzzle()

    init {
        paint.color = Color.BLUE
        paint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
//        for (block in puzzle.blocksPuzzle1) {
//            canvas.drawRect(
//                block.x.toFloat(),
//                block.y.toFloat(),
//                (block.x + block.width).toFloat(),
//                (block.y + block.height).toFloat(),
//                paint
//            )
//        }
    }
}
