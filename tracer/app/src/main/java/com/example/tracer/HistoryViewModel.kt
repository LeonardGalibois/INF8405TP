package com.example.tracer

import android.location.Location
import androidx.lifecycle.ViewModel
import java.io.Serializable
import java.util.Date

data class Hike(
    val date: Date,
    val locations: Array<Location>,
    val meanSpeed: Float,
    val meanAcceleration: Float,
    val stepsCount: Int,
    val meanTemperature: Float) : Serializable
{

}

class HistoryViewModel : ViewModel() {

    var history: MutableList<Hike> = mutableListOf()
    fun fetchHistory()
    {
        // Clear existing history
        history.clear()

        //TODO: fetch history
        history.add(Hike(Date(), arrayOf(), 10.0f, 1.2f, 500, 12.1f))
    }


}