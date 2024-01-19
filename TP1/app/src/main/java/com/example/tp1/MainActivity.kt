package com.example.tp1

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Play the game
        val playButton: Button = findViewById(R.id.play_button)
        playButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Open team members popup
        val aboutButton: Button = findViewById(R.id.about_button)
        aboutButton.setOnClickListener {
            val popup = Dialog(this)
            popup.setContentView(R.layout.team_members)
            popup.show()
        }

        // Close the game
        val exitButton: Button = findViewById(R.id.exit_button)
        exitButton.setOnClickListener {
            finish()
        }
    }
}