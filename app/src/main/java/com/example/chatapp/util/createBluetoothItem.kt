package com.example.chatapp.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.chatapp.data.entity.BluetoothDeviceListItem

@SuppressLint("MissingPermission")
fun createBluetoothItem(device: BluetoothDevice): BluetoothDeviceListItem{
    val item = BluetoothDeviceListItem(deviceName = device.name ?: "undefined", macAddress = device.address, deviceType =  device.type, listOfUUIDs = device.uuids, lastMessageDate = "", lastMessage = "", lastMessageStatus = 1, howManyUnseen = 0, bluetoothClass = device.bluetoothClass)
    return item
}