package com.example.tp2

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView

class BluetoothDeviceEntry(
    var device: BluetoothDevice,
    var isFavorite: Boolean,
    var location: Location?)
{

}
class BluetoothDeviceAdapter(
    private val devices: List<BluetoothDeviceEntry>,
    private val onItemClick: (BluetoothDeviceEntry) -> Unit,
    private val onFavoriteClick: (BluetoothDeviceEntry) -> Unit,
    var permissionGranted: Boolean = false
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val deviceAddress: TextView = itemView.findViewById(R.id.device_address)
        //val favoriteIcon: ImageView = itemView.findViewById(R.id.favorite_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.devices_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = devices[position]

        if(permissionGranted)
        {
            holder.deviceName.text = entry.device.name ?: "Unknown Device"
        }
        else
        {
            holder.deviceName.text = "Unknown Device"
        }

        holder.deviceAddress.text = entry.device.address

        holder.itemView.setOnClickListener {
            onItemClick.invoke(entry)
        }

        //holder.favoriteIcon.setImageResource(if (entry.isFavorite) R.drawable.filled_star else R.drawable.empty_star)
        //holder.favoriteIcon.setOnClickListener {
        //    onFavoriteClick.invoke(entry)
        //}
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    fun contains(address : String) : Boolean
    {
        for (entry: BluetoothDeviceEntry in devices)
        {
            if(address == entry.device.address) return true
        }
        return false
    }
}
