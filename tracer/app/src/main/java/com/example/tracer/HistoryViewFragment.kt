package com.example.tracer

import android.Manifest
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val DEFAULT_ZOOM = 20.0f
private const val DEFAULT_WIDTH = 10.0f

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryViewFragment : Fragment(), OnMapReadyCallback {
    val historyViewModel: HistoryViewModel by activityViewModels()

    private var map: GoogleMap? = null
    private var polyline: Polyline? = null
    private var locations: MutableList<LatLng>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getPermision()
        initializeMap()

        val index: Int? = arguments?.getInt("Index")

        if (index != null) displayHike(historyViewModel.history[index])


    }

    private fun displayHike(hike: Hike)
    {
        // Display Mean Speed
        view?.findViewById<TextView>(R.id.speed_text_view)?.text = hike.meanSpeed.toString()

        // Display Mean Acceleration
        view?.findViewById<TextView>(R.id.acceleration_text_view)?.text = hike.meanAcceleration.toString()

        // Display Steps Count
        view?.findViewById<TextView>(R.id.steps_text_view)?.text = hike.stepsCount.toString()

        // Display Average Temperature
        view?.findViewById<TextView>(R.id.temperature_text_view)?.text = hike.meanTemperature.toString()


        map?.clear()
        polyline = map?.addPolyline(PolylineOptions())
        polyline?.width = DEFAULT_WIDTH
        polyline?.points = hike.locations.toMutableList()
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(hike.locations[0], DEFAULT_ZOOM))
        locations = hike.locations.toMutableList()
    }

    private fun getPermision()
    {
        this.activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0) }
    }

    private fun initializeMap()
    {
        if (map == null)
        {
            val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

            mapFragment.getMapAsync(this)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map?.uiSettings?.isScrollGesturesEnabled = true
        map?.uiSettings?.isMyLocationButtonEnabled = true
        map?.uiSettings?.isCompassEnabled = true

        map?.clear()
        polyline = map?.addPolyline(PolylineOptions())
        polyline?.width = DEFAULT_WIDTH
        polyline?.points = locations?: listOf(LatLng(0.0,0.0))
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(locations?.get(0) ?:LatLng(0.0,0.0), DEFAULT_ZOOM))
    }
}