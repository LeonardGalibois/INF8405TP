package com.example.tp2

import android.bluetooth.BluetoothDevice
import android.location.Location
import android.net.MacAddress
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity
data class BluetoothDeviceEntry(
    @PrimaryKey val macAddress: String,
    @ColumnInfo(name = "Favorite") var isFavorite: Boolean,
    @ColumnInfo(name = "Name") var name: String,
    @ColumnInfo(name = "Class") var majorClass: Int,
    @ColumnInfo(name = "Latitude") var latitude: Double?,
    @ColumnInfo(name = "Longitude") var longitude: Double?
)

@Dao
interface BluetoothDao {
    @Query("SELECT * FROM BluetoothDeviceEntry")
    fun getAll(): List<BluetoothDeviceEntry>

    @Query("SELECT * FROM BluetoothDeviceEntry WHERE macAddress IN (:mac)")
    fun loadAllByIds(mac: String): List<BluetoothDeviceEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg entry: BluetoothDeviceEntry)

    @Delete
    fun delete(user: BluetoothDeviceEntry)
}

@Database(entities = [BluetoothDeviceEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun bluetoothDao(): BluetoothDao
}