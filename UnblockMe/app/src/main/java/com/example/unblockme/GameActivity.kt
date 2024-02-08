package com.example.unblockme

import DataStoreManager
import android.app.Dialog
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import kotlinx.coroutines.runBlocking

class GameActivity : AppCompatActivity() {
    private val gameViewModel: UnblockMeGameViewModel by viewModels()
    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var highScoreDisplay: TextView
    private var board: UnblockMeGameView? = null
    private var puzzleNumber: TextView? = null
    private var nextLevelButton: ImageButton? = null
    private var previousLevelButton: ImageButton? = null
    private var movesCounter: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game)
        dataStoreManager = DataStoreManager(this)

        highScoreDisplay = findViewById(R.id.high_score_display)

        fun updateHighscore() : Int {
            return runBlocking { dataStoreManager.getHighscore(gameViewModel.getCurrentPuzzleNumber()) }
        }

        val nbMinimalObserver = Observer<Int>{nbMinimal ->
            highScoreDisplay.text = "Record : ${updateHighscore().takeUnless { it == 0 }?: "--"}/$nbMinimal"
        }
        gameViewModel.nbrMinimal.observe(this,nbMinimalObserver)

        val undoBtn: ImageButton = findViewById<ImageButton>(R.id.undo_button)
        val restartBtn: ImageButton = findViewById<ImageButton>(R.id.restart_button)

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

        gameViewModel.moveNumber.observe(this) { nbMoves: Int ->
            movesCounter?.text = nbMoves.toString()
            updateBtn(restartBtn, nbMoves)
            updateBtn(undoBtn,nbMoves)
        }

        gameViewModel.successWindow.observe(this) {
            gameViewModel.moveNumber.value?.let { score ->
                runBlocking {
                    dataStoreManager.setHighscore(gameViewModel.getCurrentPuzzleNumber(), score)
                }
            }
            openSuccessWindow()
        }

        updatePuzzleSelection()

    }

    private fun updateBtn(imgBtn: ImageButton,nbMoves:Int) {
        val moveCondition = nbMoves > 0
        imgBtn.isEnabled = moveCondition
        imgBtn.alpha = if(moveCondition) 1.0f else 0.5f
    }


    // Go back to main menu
    private fun back()
    {
        finish()
    }

    // Cancel latest move
    private fun undo()
    {
        gameViewModel.undo()
        board!!.invalidate()
    }

    // Reset puzzle
    private fun restart()
    {
        gameViewModel.restart()
        board!!.invalidate()
    }

    // Go to previous puzzle
    private fun previousPuzzle()
    {
        gameViewModel.previousPuzzle()
        updatePuzzleSelection()
        board!!.invalidate()
    }

    // Go to next puzzle
    private fun nextPuzzle()
    {
        gameViewModel.nextPuzzle()
        updatePuzzleSelection()
        board!!.invalidate()
    }

    // Updates puzzle number and arrow visibility
    private fun updatePuzzleSelection()
    {
        val currentPuzzleNumber: Int = gameViewModel.getCurrentPuzzleNumber()
        puzzleNumber?.text = currentPuzzleNumber.toString()

        previousLevelButton?.visibility = if(currentPuzzleNumber == 1) View.INVISIBLE else View.VISIBLE
        nextLevelButton?.visibility = if(currentPuzzleNumber == gameViewModel.getNumberOfPuzzle()) View.INVISIBLE else View.VISIBLE
    }

    // Show success window when completing a puzzle
    private fun openSuccessWindow()
    {
        // Open success window
        val successWindow = Dialog(this)
        successWindow.setContentView(R.layout.success_window)
        successWindow.show()

        // Play sound
        val successSound: MediaPlayer = MediaPlayer.create(this, R.raw.success_sound)
        successSound.start()

        // Close window after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            successWindow.dismiss()
            nextPuzzle()
        },3000)
    }
}