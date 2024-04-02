package com.example.tracer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var signUpButton : Button = findViewById<Button>(R.id.sign_up_button)
        signUpButton.setOnClickListener { signUp() }

        var loginButton: Button = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener { login() }
    }

    private fun signUp() {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    private fun login() {
        // TODO: Verify credentials

        // Lines below should only execute if login is successful
        finish()
        val intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }
}