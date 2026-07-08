package com.example.chatapp.bluetooth.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.chatapp.bluetooth.data.entity.BluetoothDeviceListItem

@SuppressLint("MissingPermission")
fun createBluetoothItem(device: BluetoothDevice, nickName: String, psm: Int, role: Int, uuid: String): BluetoothDeviceListItem{
    val item = BluetoothDeviceListItem(device, nickName, null, "", 1, 0, null, false, role, psm, uuid)
    return item
}