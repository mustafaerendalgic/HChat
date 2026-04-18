package com.example.chatapp.data.entity

import android.bluetooth.BluetoothClass
import android.net.MacAddress
import android.net.Uri
import android.os.ParcelUuid
import android.service.controls.DeviceTypes
import java.util.UUID

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
    val isConnected: Boolean = false
)