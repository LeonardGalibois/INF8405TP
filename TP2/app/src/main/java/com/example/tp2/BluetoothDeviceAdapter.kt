package com.example.tp2

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BluetoothDeviceAdapter(
    private val devices: List<BluetoothDevice>,
    private val onItemClick: (BluetoothDevice) -> Unit,
    private val onFavoriteClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {
    var favoritesList: MutableList<BluetoothDevice> = mutableListOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val deviceAddress: TextView = itemView.findViewById(R.id.device_address)
        val favoriteIcon: ImageView = itemView.findViewById(R.id.favorite_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.devices_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = devices[position]
        holder.deviceName.text = device.name ?: "Unknown Device"
        holder.deviceAddress.text = device.address

        holder.itemView.setOnClickListener {
            onItemClick.invoke(device)
        }

        val isFavorite = favoritesList.contains(device)
        holder.favoriteIcon.setImageResource(if (isFavorite) R.drawable.filled_star else R.drawable.empty_star)
        holder.favoriteIcon.setOnClickListener {
            onFavoriteClick.invoke(device)
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }
}
