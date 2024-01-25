package com.example.unblockme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.example.unblockme.R

class  MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu)

        findViewById<Button>(R.id.play_button).setOnClickListener { play() }
        findViewById<Button>(R.id.about_button).setOnClickListener { about() }
        findViewById<Button>(R.id.exit_button).setOnClickListener { exit() }
    }

    private fun play()
    {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    private fun about()
    {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    private fun exit()
    {
        finishAndRemoveTask()
    }

}