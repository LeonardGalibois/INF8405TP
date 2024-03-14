package com.example.tp2

import android.bluetooth.BluetoothDevice
import android.location.Location
import android.net.MacAddress
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.google.android.gms.maps.model.Marker

@Entity
data class BluetoothDeviceEntry(
    @PrimaryKey val macAddress: String,
    @ColumnInfo(name = "Favorite") var isFavorite: Boolean,
    @ColumnInfo(name = "Name") var name: String,
    @ColumnInfo(name = "Class") var majorClass: Int,
    @ColumnInfo(name = "Latitude") var latitude: Double?,
    @ColumnInfo(name = "Longitude") var longitude: Double?,
    @Ignore var marker: Marker? = null
)
{
    constructor(macAddress: String, isFavorite: Boolean, name: String, majorClass: Int, latitude: Double?, longitude: Double?)
            :this(macAddress,isFavorite,name,majorClass,latitude,longitude, null)
}

@Dao
interface BluetoothDao {
    @Query("SELECT * FROM BluetoothDeviceEntry")
    fun getAll(): List<BluetoothDeviceEntry>

    @Query("SELECT * FROM BluetoothDeviceEntry WHERE macAddress IN (:mac)")
    fun loadAllByIds(mac: String): List<BluetoothDeviceEntry>

    @Insert
    fun insertAll(vararg entry: BluetoothDeviceEntry)

    @Query("UPDATE BluetoothDeviceEntry SET Favorite=:isFavorite WHERE macAddress=:mac")
    fun updateFavorite(mac: String, isFavorite: Boolean)
    @Query("UPDATE BluetoothDeviceEntry SET Latitude=:newlatitude WHERE macAddress=:mac")
    fun updateLatitude(mac: String, newlatitude: Double?)
    @Query("UPDATE BluetoothDeviceEntry SET Longitude=:newlongitude WHERE macAddress=:mac")
    fun updateLongitude(mac: String, newlongitude: Double?)

    @Delete
    fun delete(user: BluetoothDeviceEntry)
}

@Database(entities = [BluetoothDeviceEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun bluetoothDao(): BluetoothDao
}