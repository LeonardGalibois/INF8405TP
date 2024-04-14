package com.example.tracer

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.Date

data class LatLngData(val latitude: Double = 0.0, val longitude: Double = 0.0) {
    constructor() : this(0.0, 0.0)
}

data class Hike(
    val date: Date,
    val locations: Array<LatLngData>,
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
        history.add(Hike(Date(), arrayOf(LatLngData(-34.399, 150.646), LatLngData(-34.395, 150.642)), 10.0f, 1.2f, 500, 12.1f))
    }


}