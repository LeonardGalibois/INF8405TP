package com.example.unblockme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels

class GameActivity : AppCompatActivity() {
    private val gameViewModel: UnblockMeGameViewModel by viewModels()
    private var board: UnblockMeGameView? = null
    private var puzzleNumber: TextView? = null

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
        puzzleNumber?.text = gameViewModel.getCurrentPuzzleNumber().toString()
        board!!.invalidate()
    }

    private fun nextPuzzle()
    {
        gameViewModel.nextPuzzle()
        puzzleNumber?.text = gameViewModel.getCurrentPuzzleNumber().toString()
        board!!.invalidate()
    }
}