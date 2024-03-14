package com.example.tp2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class BluetoothDeviceAdapter(
    private val devices: List<BluetoothDeviceEntry>,
    private val onItemClick: (BluetoothDeviceEntry) -> Unit,
    var permissionGranted: Boolean = false
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

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
        val entry = devices[position]

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
        return devices.size
    }
}
