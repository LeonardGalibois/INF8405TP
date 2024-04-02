package com.example.tracer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        var signInButton: Button = findViewById<Button>(R.id.sign_in_button)
        signInButton.setOnClickListener { signIn() }

        var registerButton: Button = findViewById<Button>(R.id.register_button)
        registerButton.setOnClickListener { register() }

        var profilePictureButton: ImageButton = findViewById<ImageButton>(R.id.profile_picture_button)
        profilePictureButton.setOnClickListener { selectProfilePicture() }
    }

    private fun selectProfilePicture() {
        // TODO: Ask for camera permission

        // TODO: Open camera app and let user take picture

        // TODO: Change src of image button
    }

    private fun register() {
        // TODO: Complete registration

        // Lines below should only execute if registration is complete
        finish()
        val intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    private fun signIn() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }
}