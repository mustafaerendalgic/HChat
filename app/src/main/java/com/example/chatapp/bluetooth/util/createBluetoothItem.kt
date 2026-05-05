package com.example.chatapp.bluetooth.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.chatapp.bluetooth.data.entity.BluetoothDeviceListItem

@SuppressLint("MissingPermission")
fun createBluetoothItem(device: BluetoothDevice, nickName: String): BluetoothDeviceListItem{
    val item = BluetoothDeviceListItem(device.name ?: "undefined", device.address, device.type, device.uuids, "", "", 1, 0, null, device.bluetoothClass, false, null,  nickName)
    return item
}