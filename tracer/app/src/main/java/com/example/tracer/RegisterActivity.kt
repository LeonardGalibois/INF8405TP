package com.example.tracer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

class RegisterActivity : AppCompatActivity() {
    lateinit var authService: AuthService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val signInButton: Button = findViewById(R.id.sign_in_button)
        authService = AuthService(this)
        signInButton.setOnClickListener { signIn() }

        val registerButton: Button = findViewById(R.id.register_button)
        registerButton.setOnClickListener { register() }

        val profilePictureButton: ImageButton = findViewById(R.id.profile_picture_button)
        profilePictureButton.setOnClickListener { selectProfilePicture() }
    }

    private fun selectProfilePicture() {
        // TODO: Ask for camera permission

        // TODO: Open camera app and let user take picture

        // TODO: Change src of image button
    }

    private fun register() {
        val usernameInput = findViewById<EditText>(R.id.user_input).text.toString()
        val passwordInput = findViewById<EditText>(R.id.password_input).text.toString()

        val emailValidationResult = ValidationUtils.validateEmail(usernameInput)
        val passwordValidationResult = ValidationUtils.validatePassword(passwordInput)

        when {
            emailValidationResult != 0 -> {
                // Afficher un message d'erreur pour un email invalide
                Toast.makeText(this, "Adresse email invalide.", Toast.LENGTH_SHORT).show()
            }
            passwordValidationResult == ValidationUtils.ERROR_SHORT_PASSWORD -> {
                // Afficher un message d'erreur pour un mot de passe trop court
                Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères.", Toast.LENGTH_SHORT).show()
            }
            passwordValidationResult == ValidationUtils.ERROR_INVALID_PASSWORD_FORMAT -> {
                // Afficher un message d'erreur pour un format de mot de passe invalide
                Toast.makeText(this, "Le mot de passe doit contenir uniquement des lettres et des chiffres.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Toutes les validations passent, procéder à l'inscription
                authService.signUpUser(usernameInput, passwordInput)
            }
        }
    }

    private fun signIn() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }
}