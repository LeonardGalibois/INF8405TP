package com.example.tracer

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date

data class LatLngData(val latitude: Double = 0.0, val longitude: Double = 0.0)

data class Hike(
    var date: Date? = null,
    var locations: List<LatLngData>? = null,
    var meanSpeed: Float? = null,
    var meanAcceleration: Float? = null,
    var stepsCount: Int? = null,
    var meanTemperature: Float? = null,
    var userId:String? = null
)

class HistoryViewModel : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val hikesRef: DatabaseReference = database.getReference("hikes")

    var history: MutableList<Hike> = mutableListOf()
    private val hikesListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            history.clear()
            for (hikeSnapshot in snapshot.children) {
                val hike = hikeSnapshot.getValue(Hike::class.java)
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (hike?.userId == userId) {
                    hike?.let { history.add(it) }
                }
            }
            // Notifier les observateurs que les données ont changé
            // Utilisation de LiveData ou d'autres méthodes de notification ici
        }

        override fun onCancelled(error: DatabaseError) {
            // Gérer les erreurs d'annulation
        }
    }

    fun addHikeToDatabase(hike: Hike) {
        val hikesRef = FirebaseDatabase.getInstance().getReference("hikes")
        val newHikeRef = hikesRef.push()

        // Obtenir l'ID de l'utilisateur actuel à partir de Firebase Auth
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Vérifier si l'ID de l'utilisateur est disponible
        userId?.let {
            // Ajouter l'ID de l'utilisateur à la randonnée
            hike.userId = userId

            // Ajouter la randonnée à la base de données
            newHikeRef.setValue(hike)
                .addOnSuccessListener {
                    Log.d(TAG, "Hike added successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add hike", e)
                }
        } ?: run {
            Log.e(TAG, "User ID is null")
        }
    }
    fun fetchHistory() {
        // listener pour get les données en temps réel
        hikesRef.addValueEventListener(hikesListener)
    }

    // remove listener quand on n'utilise plus
    override fun onCleared() {
        super.onCleared()
        hikesRef.removeEventListener(hikesListener)
    }
}
