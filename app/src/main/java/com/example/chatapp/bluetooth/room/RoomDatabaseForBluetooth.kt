package com.example.chatapp.bluetooth.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chatapp.bluetooth.data.entity.BluetoothMessage

@Database(entities = [BluetoothMessage::class], version = 4)
abstract class RoomDatabaseForBluetooth: RoomDatabase() {
    abstract fun bluetoothMessageDao() : BluetoothDao
}