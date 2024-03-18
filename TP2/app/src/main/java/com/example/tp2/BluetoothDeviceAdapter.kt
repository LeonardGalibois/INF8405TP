package com.example.tp2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class BluetoothDeviceAdapter(
    val devices: ArrayList<BluetoothDeviceEntry>,
    private val onItemClick: (BluetoothDeviceEntry) -> Unit,
    var permissionGranted: Boolean = false
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {
    private var showOnlyFavorites = false

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val deviceAddress: TextView = itemView.findViewById(R.id.device_address)
    }

    // Inject layout into ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.devices_list, parent, false)
        return ViewHolder(view)
    }

    // Bind text elements to view holder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = if (showOnlyFavorites) devices.filter { it.isFavorite }[position] else devices[position]

        if(permissionGranted)
        {
            holder.deviceName.text = "Name : " + entry.name
        }
        else
        {
            holder.deviceName.text = "Name : Unknown Device"
        }

        holder.deviceAddress.text = "MAC address : " + entry.macAddress


        holder.itemView.setOnClickListener {
            onItemClick.invoke(entry)
        }
    }

    // Get number of devices
    override fun getItemCount(): Int {
        return if (showOnlyFavorites) devices.filter { it.isFavorite }.size else devices.size
    }

    fun toggleFavoritesOnly(showOnlyFavorites: Boolean) {
        this.showOnlyFavorites = showOnlyFavorites
        notifyDataSetChanged()
    }
}
