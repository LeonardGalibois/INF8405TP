package com.example.tracer

import android.content.Context
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment(), SensorEventListener {
    private lateinit var toggleButton: ImageButton
    private var isStarted: Boolean = false
    private var sensorManager: SensorManager? = null
    private var walking = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private lateinit var stepsTextView: TextView
    private lateinit var speedTextView: TextView
    private lateinit var accelerationTextView: TextView

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
        resetSteps()
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
    }

    // Start walking/running session
    private fun start() {
        onResume()
    }

    // Stop walking/running session
    private fun stop() {
        resetSteps()
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
            sensorManager?.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
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
                if (walking) {
                    totalSteps = event.values[0]
                    val currentSteps = totalSteps.toInt() - previousTotalSteps
                    stepsTextView.text = "$currentSteps"
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val speed = calculateSpeed(x, y, z)
                speedTextView.text = "$speed"
                val acceleration = calculateAcceleration(x, y, z)
                accelerationTextView.text = "$acceleration"
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        TODO("Not yet implemented")
    }

    // Reset the total number of steps
    private fun resetSteps() {
        previousTotalSteps = totalSteps
        stepsTextView.text = "0"
        saveData()
    }

    private fun calculateSpeed(x: Float, y: Float, z: Float) {
        // TODO: Implement speed algorithm
    }

    private fun calculateAcceleration(x: Float, y: Float, z: Float) {
        // TODO: Implement acceleration algorithm
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
}