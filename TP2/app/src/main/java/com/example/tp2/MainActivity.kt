package com.example.tp2

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {
    private lateinit var deviceAdapter: BluetoothDeviceAdapter

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
    }

    private fun showDeviceDetails(device: BluetoothDevice) {
        // TODO: Implement device details
        val deviceDetails = Dialog(this)
        deviceDetails.setContentView(R.layout.device_details)
        deviceDetails.show()
    }
}