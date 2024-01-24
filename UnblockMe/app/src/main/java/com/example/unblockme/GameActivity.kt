package com.example.unblockme

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels

class GameActivity : AppCompatActivity() {
    private val gameViewModel: UnblockMeGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game)

        Log.d("Test", "Entered game!")

        gameViewModel.setBoardDimensions(6, 6)

        gameViewModel.addBlock(UnblockMeBlock(1,2, 2, Direction.Horizontal))
        gameViewModel.addBlock(UnblockMeBlock(4,1,3, Direction.Vertical))

        findViewById<Button>(R.id.menu_button).setOnClickListener { back() }
    }

    private fun back()
    {
        finish()
    }
}