package com.example.tracer

import android.graphics.Bitmap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.storage
import java.io.ByteArrayOutputStream
import java.util.UUID

// This class handles email/password authentication.
class AuthService(private val activity: AppCompatActivity) {
    /**
     * Function to sign up a user with email and password.
     * @param email The email address of the user.
     * @param password The password of the user.
     */
    fun signUpUser(email: String, password: String,imageBitmap: Bitmap) {
        // Check if email and password are not empty
        if (email.isNotEmpty() && password.isNotEmpty()) {
            // Create user with email and password using Firebase Auth
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // If sign-up is successful, navigate to ChooseDiscussionActivity
                        uploadImageToFirebaseStorage(imageBitmap)
                        NavigationUtils.startNewActivity(
                            activity,
                            MainActivity::class.java
                        )

                        // Show a toast indicating sign-up success
                        Toast.makeText(activity, "Sign Up Successful", Toast.LENGTH_SHORT).show()
                    } else {
                        // If sign-up fails, show an error message
                        Toast.makeText(activity, "Sign Up Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // If email or password is empty, show an error message
            Toast.makeText(activity, "Email and password must not be empty", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Function to log in a user with email and password.
     * @param email The email address of the user.
     * @param password The password of the user.
     */
    fun logInUser(email: String, password: String) {
        // Check if email and password are not empty
        if (email.isNotEmpty() && password.isNotEmpty()) {
            // Sign in user with email and password using Firebase Auth
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // If login is successful, navigate to ChooseDiscussionActivity
                        NavigationUtils.startNewActivity(
                            activity,
                            MainActivity::class.java
                       )

                    } else {
                        // If login fails, show an error message
                        Toast.makeText(activity, "Login Failed: Please check credentials", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // If email or password is empty, show an error message
            Toast.makeText(activity, "Email and password must not be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebaseStorage(imageBitmap: Bitmap) {
        val storageRef = Firebase.storage.reference
        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Image uploaded successfully, now get the download URL
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                saveImageUrlToFirebaseDatabase(imageUrl)

            }.addOnFailureListener {
                // Handle failure
            }
        }.addOnFailureListener { exception ->
            // Handle unsuccessful uploads
        }
    }

    private fun saveImageUrlToFirebaseDatabase(imageUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = user.uid
            val db = FirebaseDatabase.getInstance().reference
            val userRef = db.child("users").child(userId)

            userRef.child("profileImageUrl").setValue(imageUrl)
                .addOnSuccessListener {
                    // Image URL updated successfully in Realtime Database
                }
                .addOnFailureListener { e ->
                    // Handle failure
                }
        }
    }
}