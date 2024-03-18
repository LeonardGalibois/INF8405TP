package com.example.tp2

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION  = 1
private const val PERMISSIONS_REQUEST_BLUETOOTH_SCAN  = 2
private const val PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 3
private const val DEFAULT_ZOOM = 15
private const val BLUETOOTH_MARKER_ICON_HEIGHT: Int  = 100
private const val BLUETOOTH_MARKER_ICON_WIDTH: Int   = 100
private const val LOCATION_UPDATE_FREQUENCY_MS: Long = 1000


class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener, OnMarkerClickListener {
    private lateinit var database: AppDatabase
    private lateinit var deviceAdapter: BluetoothDeviceAdapter
    private var bluetoothBroadcastReceiver: BroadcastReceiver? = null
    private var bluetoothScannerLauncher: BroadcastReceiver? = null

    private var map: GoogleMap? = null
    private var currentLocation: Location? = null

    var bluetoothDevices: ArrayList<BluetoothDeviceEntry> = ArrayList()

    // Permissions
    private var locationPermissionGranted: Boolean = false
    private var bluetoothPermissionGranted: Boolean = false
    private var bluetoothConnectPermissionGranted: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "bluetoothDevices").allowMainThreadQueries().build()
        bluetoothDevices.addAll(database.bluetoothDao().getAll())

        val recyclerView: RecyclerView = findViewById(R.id.devices_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        deviceAdapter = BluetoothDeviceAdapter(bluetoothDevices,
            { device -> showDeviceDetails(device) }
        )
        recyclerView.adapter = deviceAdapter

        // Handle theme swap (light and dark modes)
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

        val toggleFavoritesButton: ToggleButton = findViewById(R.id.toggle_favorites_button)
        toggleFavoritesButton.text = getString(R.string.favorites_off)
        toggleFavoritesButton.textOff = getString(R.string.favorites_off)
        toggleFavoritesButton.textOn = getString(R.string.favorites_on)
        toggleFavoritesButton.setOnCheckedChangeListener { _, isChecked ->
            deviceAdapter.toggleFavoritesOnly(isChecked)
        }

        getPermission()

        initializeMap()
        initializeLocationService()
        initializeBluetooth()
    }

    // Scan for Bluetooth devices
    private fun initializeBluetooth()
    {
        if(!bluetoothPermissionGranted || !bluetoothConnectPermissionGranted) return

        val bluetoothManager: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Get the information about a bluetooth device
        bluetoothBroadcastReceiver = object: BroadcastReceiver()
        {
            override fun onReceive(context: Context, intent: Intent) {
                if(intent.action == BluetoothDevice.ACTION_FOUND)
                {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if(device != null){
                        var entry = BluetoothDeviceEntry(
                            device.address,
                            false,
                            (device.name ?: "Unknown Device"),
                            device.bluetoothClass.majorDeviceClass,
                            currentLocation?.latitude,
                            currentLocation?.longitude
                        )


                        if(bluetoothDevices.all { bluetoothDeviceEntry -> bluetoothDeviceEntry.macAddress != device.address })
                        {
                            database.bluetoothDao().insertAll(entry)
                            bluetoothDevices.add(entry)
                        }
                        else
                        {
                           entry = bluetoothDevices.first { bluetoothDeviceEntry -> bluetoothDeviceEntry.macAddress == device.address }
                            database.bluetoothDao().updateLatitude(entry.macAddress, entry.latitude)
                            database.bluetoothDao().updateLongitude(entry.macAddress, entry.longitude)
                        }

                        entry.marker = addMarkerAtLocation(entry.name, entry.latitude, entry.longitude)
                        deviceAdapter.notifyDataSetChanged()
                    }
                }
            }
        }

        // Relaunch the bluetooth scan
        bluetoothScannerLauncher = object: BroadcastReceiver()
        {
            override fun onReceive(context: Context, intent: Intent) {
                if(intent.action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                {
                    bluetoothAdapter.startDiscovery()
                }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(bluetoothBroadcastReceiver, filter)

        val scannerfilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(bluetoothScannerLauncher, scannerfilter)

        if(!bluetoothPermissionGranted) return
        bluetoothAdapter.startDiscovery()
    }

    // Initialize Google Map
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

    private fun getPermission()
    {

        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationPermissionGranted = true
        }

        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)
        {
            bluetoothPermissionGranted = true
        }

        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
        {
            bluetoothConnectPermissionGranted = true
            deviceAdapter.permissionGranted = true
        }

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), 0)
    }


    // Handle permission requests
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty())
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                locationPermissionGranted = true
                initializeLocationService()
                if (map != null) enableLocationOnMap()
            }

            if(grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                bluetoothPermissionGranted = true
                initializeBluetooth()
            }

            if(grantResults[1] == PackageManager.PERMISSION_GRANTED)
            {
                bluetoothConnectPermissionGranted = true
                deviceAdapter.permissionGranted = true
            }
        }
        else super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun enableLocationOnMap()
    {
        map?.isMyLocationEnabled = true
        map?.uiSettings?.isMyLocationButtonEnabled = true
    }

    // Initialize map settings
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Set map style to prevent some default pointers such as businesses from appearing
        map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))

        // Prevents user from being able to move the map around
        map?.uiSettings?.isScrollGesturesEnabled = false

        if (locationPermissionGranted)
        {
            enableLocationOnMap()
        }


        // Add marker clicked listener
        map?.setOnMarkerClickListener(this)

        for (entry : BluetoothDeviceEntry in bluetoothDevices)
        {
            entry.marker = addMarkerAtLocation(entry.name, entry.latitude, entry.longitude)
        }
    }

    // Move camera to current location on the map
    override fun onLocationChanged(location: Location) {
        // Snapping Google Maps' camera to the current location
        currentLocation = location

        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), DEFAULT_ZOOM.toFloat()))
    }


    // Show device details and functionalities when clicking on a marker on the map
    override fun onMarkerClick(marker: Marker): Boolean {
        for (device in bluetoothDevices)
        {
            if (device.marker != marker) continue

            showDeviceDetails(device)
            return true
        }

        return false
    }

    // Add marker for device on the map
    fun addMarkerAtLocation(title: String, latitude: Double?, longitude: Double?): Marker?
    {
        // Make sure there is a current location available
        if (latitude == null || longitude == null) return null

        val marker = MarkerOptions()

        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bluetooth_icon)
        val resizedIcon: Bitmap = Bitmap.createScaledBitmap(icon, BLUETOOTH_MARKER_ICON_WIDTH, BLUETOOTH_MARKER_ICON_HEIGHT, false)

        marker.position(LatLng(latitude, longitude))
        marker.title(title)
        marker.icon(BitmapDescriptorFactory.fromBitmap(resizedIcon))

        return map?.addMarker(marker)
    }

    fun addMarkerAtLocation(title: String): Marker?
    {
        return addMarkerAtLocation(title, currentLocation?.latitude, currentLocation?.longitude)
    }

    // Show device details and features (favorite, share, GPS) when clicking on device
    private fun showDeviceDetails(entry: BluetoothDeviceEntry) {
        val deviceDetails = Dialog(this)
        deviceDetails.setContentView(R.layout.device_details)

        val deviceName: TextView = deviceDetails.findViewById(R.id.device_name)
        val deviceAddress: TextView = deviceDetails.findViewById(R.id.device_address)
        val deviceClass: TextView = deviceDetails.findViewById(R.id.device_class)
        val deviceLocation: TextView = deviceDetails.findViewById(R.id.device_location)
        
        deviceName.text = "Name : " + entry.name
        deviceAddress.text = "MAC Address : " + entry.macAddress
        deviceClass.text = "Class : " + getBluetoothClass(entry)
        deviceLocation.text = "Latitude : " + entry.latitude + "\nLongitude : " + entry.longitude

        val pairedDevices: TextView = deviceDetails.findViewById(R.id.paired_devices)
        val pairedDevicesStringBuilder = StringBuilder()
        for (otherDevice in bluetoothDevices) {
            if (otherDevice != entry) {
                pairedDevicesStringBuilder.append(
                    "Name: ${otherDevice.name}\n" +
                    "MAC Address: ${otherDevice.macAddress}\n" +
                    "Class: ${getBluetoothClass(otherDevice)}\n\n"
                )
            }
        }
        pairedDevices.text = pairedDevicesStringBuilder.toString()

        val favoriteIcon = deviceDetails.findViewById<ImageView>(R.id.favorite_icon)
        favoriteIcon.setImageResource(if (entry.isFavorite) R.drawable.filled_star else R.drawable.empty_star)
        favoriteIcon.setOnClickListener {
            toggleFavorite(entry)
            favoriteIcon.setImageResource(if (entry.isFavorite) R.drawable.filled_star else R.drawable.empty_star)
            deviceAdapter.notifyDataSetChanged()
        }

        val itineraryButton = deviceDetails.findViewById<Button>(R.id.itinerary_btn)
        itineraryButton.setOnClickListener {
            val gmmIntentUri: Uri = Uri.parse("google.navigation:q=${currentLocation?.latitude},${currentLocation?.longitude}&mode=w")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
            deviceDetails.dismiss()
        }

        val shareIcon = deviceDetails.findViewById<ImageView>(R.id.share_icon)
        shareIcon.setOnClickListener {
            shareDevice(entry)
        }

        deviceDetails.show()
    }

    // Add or remove device from favorites
    private fun toggleFavorite(entry: BluetoothDeviceEntry) {
        entry.isFavorite = !entry.isFavorite
        database.bluetoothDao().updateFavorite(entry.macAddress, entry.isFavorite)
    }

    // Open menu to share device
    private fun shareDevice(device: BluetoothDeviceEntry) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "Check out this Bluetooth device: ${device.name}, ${device.macAddress}")
        startActivity(Intent.createChooser(intent, "Share Device Details"))
    }

    // Assign text to Bluetooth major classes
    private fun getBluetoothClass(entry: BluetoothDeviceEntry) : String
    {
        var result = ""

        when(entry.majorClass)
        {
            BluetoothClass.Device.Major.AUDIO_VIDEO -> result = "Audio video"
            BluetoothClass.Device.Major.COMPUTER -> result = "Computer"
            BluetoothClass.Device.Major.HEALTH -> result = "Health"
            BluetoothClass.Device.Major.MISC -> result = "Misc"
            BluetoothClass.Device.Major.IMAGING -> result = "Imaging"
            BluetoothClass.Device.Major.NETWORKING -> result = "Networking"
            BluetoothClass.Device.Major.PERIPHERAL -> result = "Peripheral"
            BluetoothClass.Device.Major.PHONE -> result = "Phone"
            BluetoothClass.Device.Major.TOY -> result = "Toy"
            BluetoothClass.Device.Major.UNCATEGORIZED -> result = "Uncategorized"
            BluetoothClass.Device.Major.WEARABLE -> result = "Wearable"
        }

        return result
    }

    // Unregister Bluetooth broadcast receiver and scanner
    override fun onDestroy() {
        super.onDestroy()

        if (bluetoothBroadcastReceiver != null) unregisterReceiver(bluetoothBroadcastReceiver)
        if (bluetoothScannerLauncher != null) unregisterReceiver(bluetoothScannerLauncher)
    }
}