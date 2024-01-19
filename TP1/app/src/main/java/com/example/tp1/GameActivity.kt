package com.example.tp1

import PuzzleBlockView
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity

class GameActivity : ComponentActivity() {
    private var puzzleNumber = 1
    private var movesCounter = 0

    private lateinit var puzzleGrid: GridLayout

    private lateinit var puzzleNumberText: TextView
    private lateinit var movesCounterText: TextView
    private lateinit var puzzleMinimumMovesText: TextView

    private lateinit var pauseButton: ImageButton
    private lateinit var previousPuzzleButton: ImageButton
    private lateinit var nextPuzzleButton: ImageButton
    private lateinit var cancelButton: ImageButton
    private lateinit var resetButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Go back to main menu
        pauseButton = findViewById(R.id.pause_button)
        pauseButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Go to previous puzzle
        previousPuzzleButton = findViewById(R.id.previous_puzzle_button)
        previousPuzzleButton.setOnClickListener {
            previousPuzzle()
        }

        // Go to next puzzle
        nextPuzzleButton = findViewById(R.id.next_puzzle_button)
        nextPuzzleButton.setOnClickListener {
            nextPuzzle()
        }

        // Cancel latest move
        cancelButton = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            cancel()
        }

        // Reset puzzle
        resetButton = findViewById(R.id.reset_button)
        resetButton.setOnClickListener {
            reset()
        }

        // Set initial states of the puzzle
        updatePuzzle()

        puzzleGrid = findViewById(R.id.puzzle_grid)
    }

    private fun addPuzzleBlocks(gridLayout: GridLayout, blocks: List<Block>) {
        for (block in blocks) {
            val blockView = createPuzzleBlock()
            val params = GridLayout.LayoutParams()
            params.rowSpec = GridLayout.spec(block.y, block.height)
            params.columnSpec = GridLayout.spec(block.x, block.width)
            blockView.layoutParams = params
            gridLayout.addView(blockView)
        }
    }

    private fun createPuzzleBlock(): PuzzleBlockView {
        val blockView = PuzzleBlockView(this)
        blockView.setBackgroundColor(Color.BLUE)
        return blockView
    }

    // Decrement puzzle number
    private fun previousPuzzle() {
        if (puzzleNumber <= 1) {
            return
        }
        puzzleNumber--
        updatePuzzle()
    }

    // Increment puzzle number
    private fun nextPuzzle() {
        if (puzzleNumber >= 3) {
            return
        }
        puzzleNumber++
        updatePuzzle()
    }

    // Shows the minimum number of moves necessary to complete the puzzle
    private fun updatePuzzleMinimumMoves() {
        puzzleMinimumMovesText = findViewById(R.id.puzzle_minimum_moves)
        if (puzzleNumber == 1 || puzzleNumber == 3) {
            puzzleMinimumMovesText.text = "/15"
        }
        if (puzzleNumber == 2) {
            puzzleMinimumMovesText.text = "/17"
        }
    }

    // Decrement moves counter when cancelling latest move
    private fun cancel() {
        movesCounter--
        movesCounterToString()
    }

    // Reset moves counter when resetting puzzle
    private fun reset() {
        movesCounter = 0
        movesCounterToString()
    }

    // Updates puzzle number text based on current puzzle
    private fun puzzleNumberToString() {
        puzzleNumberText = findViewById(R.id.puzzle_number)
        puzzleNumberText.text = puzzleNumber.toString()
    }

    // Updates moves counter text
    private fun movesCounterToString() {
        movesCounterText = findViewById(R.id.moves_counter)
        movesCounterText.text = movesCounter.toString()
    }

    // Updates visibility and interactability of buttons based on game state
    private fun updateButtonStates() {
        previousPuzzleButton.visibility = if (puzzleNumber > 1) View.VISIBLE else View.GONE
        nextPuzzleButton.visibility = if (puzzleNumber < 3) View.VISIBLE else View.GONE
        cancelButton.isEnabled = movesCounter > 0
        resetButton.isEnabled = movesCounter > 0
    }

    // Updates puzzle state
    private fun updatePuzzle() {
        reset()
        puzzleNumberToString()
        updateButtonStates()
        updatePuzzleMinimumMoves()
    }

    // Opens success window that fades out after 3 seconds when completing a puzzle
    private fun completePuzzle() {
        val successWindow = Dialog(this)
        successWindow.setContentView(R.layout.succes_window)
        successWindow.show()
        Handler(Looper.getMainLooper()).postDelayed({
            successWindow.dismiss()
            nextPuzzle()
        },3000)
    }
}