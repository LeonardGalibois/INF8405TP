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
import android.os.Build
import android.os.Bundle
import android.util.Log
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
    private lateinit var deviceAdapter: BluetoothDeviceAdapter
    private var bluetoothBroadcastReceiver: BroadcastReceiver? = null
    private var bluetoothScannerLauncher: BroadcastReceiver? = null

    private var map: GoogleMap? = null
    private var currentLocation: Location? = null

    val bluetoothDevices: ArrayList<BluetoothDeviceEntry> = ArrayList<BluetoothDeviceEntry>()

    // Permissions
    private var locationPermissionGranted: Boolean = false
    private var bluetoothPermissionGranted: Boolean = false
    private var bluetoothConnectPermissionGranted: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.devices_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        deviceAdapter = BluetoothDeviceAdapter(bluetoothDevices,
            { device -> showDeviceDetails(device) },
            { device -> toggleFavorite(device) }
        )
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
        getBluetoothPermission()
        getBluetoothConnectPermission()

        initializeMap()
        initializeLocationService()
        initializeBluetooth()
    }

    private fun initializeBluetooth()
    {
        val bluetoothManager: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        bluetoothBroadcastReceiver = object: BroadcastReceiver()
        {
            override fun onReceive(context: Context, intent: Intent) {
                if(intent.action == BluetoothDevice.ACTION_FOUND)
                {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if(device != null
                        && bluetoothDevices.all { bluetoothDeviceEntry -> bluetoothDeviceEntry.device.address != device.address  }){
                        bluetoothDevices.add(
                            BluetoothDeviceEntry(
                                device,
                                false,
                                currentLocation
                            ))

                        addMarkerAtLocation((device.name ?: "Unknown Device"), currentLocation)
                        deviceAdapter.notifyItemChanged(bluetoothDevices.count() - 1)
                    }
                }
            }
        }

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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getBluetoothPermission()
    {
        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)
        {
            bluetoothPermissionGranted = true
        }
        else
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN), PERMISSIONS_REQUEST_BLUETOOTH_SCAN)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getBluetoothConnectPermission()
    {
        if (ContextCompat.checkSelfPermission(this.applicationContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
        {
            bluetoothConnectPermissionGranted = true
            deviceAdapter.permissionGranted = true
        }
        else
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), PERMISSIONS_REQUEST_BLUETOOTH_CONNECT)
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
                PERMISSIONS_REQUEST_BLUETOOTH_SCAN ->
                {
                    bluetoothPermissionGranted = true
                    initializeBluetooth()
                }
                PERMISSIONS_REQUEST_BLUETOOTH_CONNECT ->
                {
                    bluetoothConnectPermissionGranted = true
                    deviceAdapter.permissionGranted = true
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

        // Add marker clicked listener
        map?.setOnMarkerClickListener(this)
    }

    override fun onLocationChanged(location: Location) {
        // Snapping Google Maps' camera to the current location
        currentLocation = location

        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), DEFAULT_ZOOM.toFloat()))
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        for (device in bluetoothDevices)
        {
            if (device.marker != marker) continue

            showDeviceDetails(device)
            return true
        }

        return false
    }

    fun addMarkerAtLocation(title: String, location: Location?): Marker?
    {
        // Make sure there is a current location available
        if (location == null) return null

        val marker = MarkerOptions()

        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.bluetooth_icon)
        val resizedIcon: Bitmap = Bitmap.createScaledBitmap(icon, BLUETOOTH_MARKER_ICON_WIDTH, BLUETOOTH_MARKER_ICON_HEIGHT, false)

        marker.position(LatLng(location!!.latitude, location!!.longitude))
        marker.title(title)
        marker.icon(BitmapDescriptorFactory.fromBitmap(resizedIcon))

        return map?.addMarker(marker)
    }

    fun addMarkerAtLocation(title: String): Marker?
    {
        return addMarkerAtLocation(title, currentLocation)
    }

    private fun showDeviceDetails(entry: BluetoothDeviceEntry) {
        // TODO: Implement device details
        val deviceDetails = Dialog(this)
        deviceDetails.setContentView(R.layout.device_details)

        val deviceName: TextView = deviceDetails.findViewById(R.id.device_name)
        val deviceAddress: TextView = deviceDetails.findViewById(R.id.device_address)
        val deviceClass: TextView = deviceDetails.findViewById(R.id.device_class)
        val deviceLocation: TextView = deviceDetails.findViewById(R.id.device_location)
        val pairedDevices: TextView = deviceDetails.findViewById(R.id.paired_devices)
        
        deviceName.text = "Name : " + (entry.device.name ?: "Unknown Device")
        deviceAddress.text = "Mac Address : " + entry.device.address
        deviceClass.text = "Class : " + getBluetoothClass(entry)
        deviceLocation.text = "Latitude : " + entry.location?.latitude + "\nLongitude : " + entry.location?.longitude
        
        val pairedDevicesInfo = getPairedDevicesInfo(entry)
        pairedDevices.text = pairedDevicesInfo

        val favoriteIcon = deviceDetails.findViewById<ImageView>(R.id.favorite_icon)
        favoriteIcon.setOnClickListener {
            toggleFavorite(entry)
            favoriteIcon.setImageResource(if (entry.isFavorite) R.drawable.filled_star else R.drawable.empty_star)
        }

        val shareIcon = deviceDetails.findViewById<ImageView>(R.id.share_icon)
        shareIcon.setOnClickListener {
            shareDevice(entry)
        }

        deviceDetails.show()
    }

    private fun getPairedDevicesInfo(device: BluetoothDeviceEntry): String {
        // TODO: Implement paired devices info
        return ""
    }

    private fun toggleFavorite(entry: BluetoothDeviceEntry) {
        entry.isFavorite = !entry.isFavorite
        deviceAdapter.notifyDataSetChanged()
    }

    private fun shareDevice(entry: BluetoothDeviceEntry) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, "Check out this Bluetooth device: ${entry.device.name}, ${entry.device.address}")
        startActivity(Intent.createChooser(intent, "Share Device Details"))
    }

    private fun getBluetoothClass(entry: BluetoothDeviceEntry) : String
    {
        var result : String = ""

        when(entry.device.bluetoothClass.majorDeviceClass)
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

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(bluetoothBroadcastReceiver)
        unregisterReceiver(bluetoothScannerLauncher)
    }
}