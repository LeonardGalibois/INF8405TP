package com.example.tracer

object ValidationUtils {

    const val ERROR_INVALID_EMAIL = 1
    const val ERROR_SHORT_PASSWORD = 2
    const val ERROR_INVALID_PASSWORD_FORMAT = 3

    fun validateEmail(email: String): Int {
        return if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            0 // Email valide
        } else {
            ERROR_INVALID_EMAIL
        }
    }

    fun validatePassword(password: String): Int {
        return when {
            password.length < 6 -> ERROR_SHORT_PASSWORD
            !password.matches(Regex("[a-zA-Z0-9]+")) -> ERROR_INVALID_PASSWORD_FORMAT
            else -> 0 // Mot de passe valide
        }
    }
}

