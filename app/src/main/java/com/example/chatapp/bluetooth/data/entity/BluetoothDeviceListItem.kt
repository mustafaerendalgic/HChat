package com.example.chatapp.bluetooth.data.entity

import android.bluetooth.BluetoothClass
import android.net.Uri
import android.os.ParcelUuid
import com.example.chatapp.bluetooth.event.DeviceRole

data class BluetoothDeviceListItem(
    val deviceName: String = "undefined_device_name",
    val macAddress: String,
    val deviceType: Int,
    val listOfUUIDs: Array<ParcelUuid>?,
    val lastMessage: String? = null,
    val lastMessageDate: String,
    val lastMessageStatus: Int,
    val howManyUnseen: Int,
    val profilePicture: Uri? = null,
    val bluetoothClass: BluetoothClass,
    var isConnected: Boolean = false,
    var role: DeviceRole? = null,
    val nick: String
)