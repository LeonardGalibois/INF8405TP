package com.example.tracer

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    lateinit var authService: AuthService
    private lateinit var profilePictureButton: ImageButton
    private var currentPhotoPath: String? = null
    private var capturedImageBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val signInButton: Button = findViewById(R.id.sign_in_button)
        authService = AuthService(this)
        signInButton.setOnClickListener { signIn() }

        val registerButton: Button = findViewById(R.id.register_button)
        registerButton.setOnClickListener { register() }

        profilePictureButton = findViewById(R.id.profile_picture_button)
        profilePictureButton.setOnClickListener { selectProfilePicture() }
    }

    private fun selectProfilePicture() {
        // Demander la permission d'accès à la caméra
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
            return
        }
        // Ouvrir l'application de l'appareil photo et laisser l'utilisateur prendre une photo
        dispatchTakePictureIntent()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.tracer.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Image capturée et enregistrée dans le fichier spécifié dans l'Intent
            val file = File(currentPhotoPath)
            val uri = Uri.fromFile(file)
            // Convertir l'URI en Bitmap
            capturedImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            // Changer la source de l'image du bouton de la photo de profil
            profilePictureButton.setImageBitmap(capturedImageBitmap)
        }
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
            capturedImageBitmap == null -> {
                // Afficher un message d'erreur indiquant que l'utilisateur doit sélectionner une photo de profil
                Toast.makeText(this, "Veuillez sélectionner une photo de profil.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Toutes les validations passent et une photo de profil a été sélectionnée, procéder à l'inscription
                capturedImageBitmap?.let {
                    authService.signUpUser(usernameInput, passwordInput, it)
                }
            }
        }
    }


    private fun signIn() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 10
        private const val REQUEST_IMAGE_CAPTURE = 11
    }

}