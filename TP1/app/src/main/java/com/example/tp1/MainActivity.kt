package com.example.tp1

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var playButton: Button
    private lateinit var aboutButton: Button
    private lateinit var exitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Play the game
        playButton = findViewById(R.id.play_button)
        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Open team members popup
        aboutButton = findViewById(R.id.about_button)
        aboutButton.setOnClickListener {
            val popup = Dialog(this)
            popup.setContentView(R.layout.team_members)
            popup.show()
        }

        // Close the game
        exitButton = findViewById(R.id.exit_button)
        exitButton.setOnClickListener {
            finish()
        }
    }
}