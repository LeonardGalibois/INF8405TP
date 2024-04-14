package com.example.tracer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import kotlin.math.pow
import kotlin.math.sqrt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val DEFAULT_ZOOM = 20.0f
private const val DEFAULT_WIDTH = 10.0f
private const val LOCATION_UPDATE_FREQUENCY_MS: Long = 100
private const val WEATHER_UPDATE_FREQUENCY_MS: Long = 120000
/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), OnMapReadyCallback, LocationListener, SensorEventListener {
    private lateinit var toggleButton: ImageButton
    private var isStarted: Boolean = false
    private var sensorManager: SensorManager? = null
    private var walking = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private lateinit var stepsTextView: TextView
    private lateinit var speedTextView: TextView
    private lateinit var accelerationTextView: TextView
    val historyViewModel: HistoryViewModel by viewModels()

    private var gravity = FloatArray(3)
    private var linearAcceleration = FloatArray(3)

    lateinit var weatherTextView: TextView

    private val weatherTimer: Timer = Timer("weather")
    private var needToUpdateWeather: Boolean = false

    private var map: GoogleMap? = null
    private var currentLocation: Location? = null
    private var polyline: Polyline? = null

    private var locations: MutableList<LatLng>? = null
    private var lastTemperature: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        loadData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        stepsTextView = view.findViewById(R.id.steps_text_view)
        speedTextView = view.findViewById(R.id.speed_text_view)
        accelerationTextView = view.findViewById(R.id.acceleration_text_view)
        reset()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isStarted = false

        toggleButton = view.findViewById(R.id.toggle_button)
        toggleButton.setOnClickListener {
            if (!isStarted)
            {
                toggleButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red))
                toggleButton.setImageResource(R.drawable.stop_icon)
                start()
            }
            else
            {
                toggleButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green))
                toggleButton.setImageResource(R.drawable.start_icon)
                stop()
            }

            isStarted = !isStarted
        }

        weatherTextView = view.findViewById(R.id.temperature_text_view)

        val task: TimerTask = object: TimerTask()
        {
            override fun run() {
                if(currentLocation != null)
                {
                    val position: LatLng = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
                    updateWeather(position)
                }
                else
                {
                    needToUpdateWeather = true
                }
            }
        }

        weatherTimer.schedule(task, 1000, WEATHER_UPDATE_FREQUENCY_MS)

        getPermision()
        initializeMap()
        initializeLocationService()
    }

    // Start walking/running session
    private fun start() {
        locations = mutableListOf()
        polyline = map?.addPolyline(PolylineOptions())
        polyline?.width = DEFAULT_WIDTH
        onResume()
    }

    // Stop walking/running session
    private fun stop() {

        val locationsData : MutableList<LatLngData> = mutableListOf()

        if(locations != null)
        {
            for(location in locations!!)
            {
                locationsData.add(LatLngData(location.latitude, location.longitude))
            }
        }

        val hike = Hike(
            Date(),
            locationsData,
            speedTextView.text.toString().toFloat(),
            accelerationTextView.text.toString().toFloat(),
            totalSteps.toInt(),
            lastTemperature.toFloat()
        )

        historyViewModel.addHikeToDatabase(hike)
        map?.clear()
        reset()
    }

    // Listen to sensors if they are present on the device
    override fun onResume() {
        super.onResume()
        walking = true
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(requireContext(), "No step sensor detected on this device", Toast.LENGTH_SHORT).show()
        }
        else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

        val accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor == null) {
            Toast.makeText(requireContext(), "No accelerometer sensor detected on this device", Toast.LENGTH_SHORT).show()
        }
        else {
            sensorManager?.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // Stop listening to the sensors when fragment is inactive
    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    // Update the values captured by the sensors
    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                if (!isStarted) return
                if (walking) {
                    totalSteps = event.values[0]
                    val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
                    stepsTextView.text = "$currentSteps"
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                if (!isStarted) return
                val speed = currentLocation?.let { calculateSpeed(it) }
                speedTextView.text = speed

                val alpha = 0.8f

                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                // Remove the gravity contribution with the high-pass filter.
                linearAcceleration[0] = event.values[0] - gravity[0]
                linearAcceleration[1] = event.values[1] - gravity[1]
                linearAcceleration[2] = event.values[2] - gravity[2]

                // Calculate acceleration module
                val acceleration = sqrt(linearAcceleration[0].pow(2) + linearAcceleration[1].pow(2) + linearAcceleration[2].pow(2))
                accelerationTextView.text = String.format("%.2f", acceleration)
            }
        }
    }

    // Not used, but must be implemented by SensorEventListener
    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    // Reset steps, speed and acceleration
    private fun reset() {
        previousTotalSteps = totalSteps
        stepsTextView.text = "0"
        speedTextView.text = "0"
        accelerationTextView.text = "0"
        saveData()
    }

    // Calculate walking speed
    private fun calculateSpeed(location: Location): String {
        return "%.2f".format(location.speed)
    }

    private fun saveData() {
        val sharedPreferences = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("steps", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = requireContext().getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val savedSteps = sharedPreferences.getFloat("steps", 0f)
        previousTotalSteps = savedSteps
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TraceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
            }
    }

    private fun updateWeather(position: LatLng)
    {
        val callback: Callback<WeatherData> = object: Callback<WeatherData>
        {
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                if(response.isSuccessful)
                {
                    lastTemperature = response.body()?.current?.temp ?: 0.0

                    Log.w("UPDATE WEATHER" , "weather: " + lastTemperature)
                    weatherTextView.text = String.format("%.0f", lastTemperature)
                    weatherTextView.invalidate()
                }
            }

            override fun onFailure(call: Call<WeatherData>, t: Throwable)
            {
                needToUpdateWeather = true
                Log.w("Failed", t)
            }
        }

        WeatherService.apiService.getCurrentWeatherData(position.latitude.toString(), position.longitude.toString()).enqueue(callback)
    }

    private fun getPermision()
    {
        this.activity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.BODY_SENSORS), 0) }
    }

    private fun initializeMap()
    {
        if (map == null)
        {
            val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

            mapFragment.getMapAsync(this)
        }
    }

    private fun initializeLocationService()
    {
        val locationManager: LocationManager = activity?.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        if (this.activity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED && this.activity?.let {
                ActivityCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, LOCATION_UPDATE_FREQUENCY_MS, 0f, this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map?.uiSettings?.isScrollGesturesEnabled = false
        map?.isMyLocationEnabled = true
        map?.uiSettings?.isMyLocationButtonEnabled = true
        map?.uiSettings?.isCompassEnabled = true
        val position: LatLng = map?.cameraPosition?.target ?: LatLng(0.0,0.0)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM))
        updateWeather(position)
    }
    override fun onLocationChanged(location: Location) {
        currentLocation = location
        val position = LatLng(location.latitude, location.longitude)
        val zoomLevel: Float = map?.cameraPosition?.zoom ?: DEFAULT_ZOOM
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoomLevel))

        if(isStarted)
        {
            locations?.add(position)
            polyline?.points = locations!!
        }

        if(needToUpdateWeather)
        {
            needToUpdateWeather = false
            updateWeather(position)
        }
    }
}