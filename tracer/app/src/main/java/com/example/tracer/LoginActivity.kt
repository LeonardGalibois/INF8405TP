package com.example.tracer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    private lateinit var authService: AuthService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        authService = AuthService(this)

        val signUpButton : Button = findViewById(R.id.sign_up_button)
        signUpButton.setOnClickListener { signUp() }

        val loginButton: Button = findViewById(R.id.login_button)
        loginButton.setOnClickListener { login() }
    }

    private fun signUp() {
        val intent = Intent(this, RegisterActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    private fun login() {
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
                // Toutes les validations passent, procéder à la connexion
                authService.logInUser(usernameInput, passwordInput)
            }
        }
    }
}