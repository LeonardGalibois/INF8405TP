package com.example.tp2

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION  = 1
private const val DEFAULT_ZOOM = 15
private const val BLUETOOTH_MARKER_ICON_HEIGHT: Int  = 100
private const val BLUETOOTH_MARKER_ICON_WIDTH: Int   = 100
private const val LOCATION_UPDATE_FREQUENCY_MS: Long = 1000


class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    private lateinit var deviceAdapter: BluetoothDeviceAdapter

    private var map: GoogleMap? = null
    private var currentLocation: Location? = null

    // Permissions
    private var locationPermissionGranted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        val bluetoothDevices = listOf<BluetoothDevice>()

        val recyclerView: RecyclerView = findViewById(R.id.devices_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        deviceAdapter = BluetoothDeviceAdapter(bluetoothDevices) { device -> showDeviceDetails(device) }
        recyclerView.adapter = deviceAdapter

        val themeButton: ToggleButton = findViewById(R.id.theme_button)
        themeButton.text = getString(R.string.swap_theme)
        themeButton.textOff = getString(R.string.swap_theme)
        themeButton.textOn = getString(R.string.swap_theme)

        val sharedPreferences: SharedPreferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val darkMode: Boolean = sharedPreferences.getBoolean("dark", false)

        if (darkMode) {
            themeButton.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        themeButton.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                editor.putBoolean("dark", false)
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                editor.putBoolean("dark", true)
            }
            editor.apply()
        }

        getLocationPermission()

        initializeMap()
        initializeLocationService()
    }

    private fun initializeMap()
    {
        Log.d("MainActivity", "Intializing Map")

        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    private fun initializeLocationService()
    {
        if (!locationPermissionGranted) return

        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, LOCATION_UPDATE_FREQUENCY_MS, 0f, this)
    }

    private fun getLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationPermissionGranted = true
        }
        else
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            when (requestCode)
            {
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION    ->
                {
                    locationPermissionGranted = true
                    initializeLocationService()
                }
            }
        }
        else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Set map style to prevent some default pointers such as businesses from appearing
        map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        // Prevents user from being able to move the map around
        map?.uiSettings?.isScrollGesturesEnabled = false

        map?.isMyLocationEnabled = true
        map?.uiSettings?.isMyLocationButtonEnabled = true
    }

    override fun onLocationChanged(location: Location) {
        // Snapping Google Maps' camera to the current location
        currentLocation = location

        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), DEFAULT_ZOOM.toFloat()))
    }

    fun addMarkerAtLocation(title: String): Marker?
    {
        // Make sure there is a current location available
        if (currentLocation == null) return null

        val marker = MarkerOptions()

        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bluetooth_icon)
        val resizedIcon: Bitmap = Bitmap.createScaledBitmap(icon, BLUETOOTH_MARKER_ICON_WIDTH, BLUETOOTH_MARKER_ICON_HEIGHT, false)

        marker.position(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
        marker.title(title)
        marker.icon(BitmapDescriptorFactory.fromBitmap(resizedIcon))

        return map?.addMarker(marker)
    }

    private fun showDeviceDetails(device: BluetoothDevice) {
        // TODO: Implement device details
        val deviceDetails = Dialog(this)
        deviceDetails.setContentView(R.layout.device_details)
        deviceDetails.show()
    }
}