package com.example.tp1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity

class GameActivity : ComponentActivity() {
    private var puzzleNumber = 1
    private var movesCounter = 0

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
    }

    // Decrement puzzle number
    private fun previousPuzzle() {
        puzzleNumber--
        updatePuzzle()
    }

    // Increment puzzle number
    private fun nextPuzzle() {
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

    private fun puzzleNumberToString() {
        puzzleNumberText = findViewById(R.id.puzzle_number)
        puzzleNumberText.text = puzzleNumber.toString()
    }

    private fun movesCounterToString() {
        movesCounterText = findViewById(R.id.moves_counter)
        movesCounterText.text = movesCounter.toString()
    }

    private fun updateButtonStates() {
        previousPuzzleButton.visibility = if (puzzleNumber > 1) View.VISIBLE else View.GONE
        nextPuzzleButton.visibility = if (puzzleNumber < 3) View.VISIBLE else View.GONE
        cancelButton.isEnabled = movesCounter > 0
        resetButton.isEnabled = movesCounter > 0
    }

    private fun updatePuzzle() {
        reset()
        puzzleNumberToString()
        updateButtonStates()
        updatePuzzleMinimumMoves()
    }
}