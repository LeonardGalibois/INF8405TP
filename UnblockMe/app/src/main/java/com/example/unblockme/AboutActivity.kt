package com.example.unblockme
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class AboutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)

        findViewById<Button>(R.id.menu_button).setOnClickListener { back() }
    }

    // Go back to main menu
    private fun back()
    {
        finish()
    }

}