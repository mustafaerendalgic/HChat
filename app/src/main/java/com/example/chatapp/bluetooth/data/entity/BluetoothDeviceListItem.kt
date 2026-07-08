package com.example.chatapp.bluetooth.data.entity

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.net.Uri
import android.os.ParcelUuid

data class BluetoothDeviceListItem(
    val device: BluetoothDevice,
    val nick: String = "",
    val lastMessage: String? = null,
    val lastMessageDate: String,
    val lastMessageStatus: Int,
    val howManyUnseen: Int,
    val profilePicture: Uri? = null,
    var isConnected: Boolean = false,
    var role: Int,
    val psm: Int = 0,
    val deviceUUID: String
)