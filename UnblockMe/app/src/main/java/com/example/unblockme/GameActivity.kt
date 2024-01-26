package com.example.unblockme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels

class GameActivity : AppCompatActivity() {
    private val gameViewModel: UnblockMeGameViewModel by viewModels()
    private var board: UnblockMeGameView? = null
    private var puzzleNumber: TextView? = null
    private var nextLevelButton: ImageButton? = null
    private var previousLevelButton: ImageButton? = null
    private var movesCounter: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game)

        Log.d("Test", "Entered game!")

        findViewById<Button>(R.id.menu_button).setOnClickListener { back() }
        findViewById<ImageButton>(R.id.undo_button).setOnClickListener { undo() }
        findViewById<ImageButton>(R.id.restart_button).setOnClickListener { restart() }
        findViewById<ImageButton>(R.id.previous_button).setOnClickListener { previousPuzzle() }
        findViewById<ImageButton>(R.id.next_button).setOnClickListener { nextPuzzle() }

        board = findViewById<UnblockMeGameView>(R.id.board)
        puzzleNumber = findViewById<TextView>(R.id.puzzle_number) as TextView
        nextLevelButton = findViewById<ImageButton>(R.id.next_button)
        previousLevelButton = findViewById<ImageButton>(R.id.previous_button)
        movesCounter = findViewById<TextView>(R.id.moves_counter) as TextView

        gameViewModel.moveNumber.observe(this, { nbMoves: Int ->  movesCounter?.text = nbMoves.toString() })
        updatePuzzleSelection()
    }

    private fun back()
    {
        finish()
    }

    private fun undo()
    {
        gameViewModel.undo()

        board!!.invalidate()
    }

    private fun restart()
    {
        gameViewModel.restart()

        board!!.invalidate()
    }

    private fun previousPuzzle()
    {
        gameViewModel.previousPuzzle()
        updatePuzzleSelection()
        board!!.invalidate()
    }

    private fun nextPuzzle()
    {
        gameViewModel.nextPuzzle()
        updatePuzzleSelection()
        board!!.invalidate()
    }

    private fun updatePuzzleSelection()
    {
        val currentPuzzleNumber: Int = gameViewModel.getCurrentPuzzleNumber()
        puzzleNumber?.text = currentPuzzleNumber.toString()

        previousLevelButton?.visibility = if(currentPuzzleNumber == 1) View.INVISIBLE else View.VISIBLE
        nextLevelButton?.visibility = if(currentPuzzleNumber == gameViewModel.getNumberOfPuzzle()) View.INVISIBLE else View.VISIBLE
    }
}