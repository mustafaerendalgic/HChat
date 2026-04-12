package com.example.chatapp.data.entity

import android.net.MacAddress
import android.net.Uri
import android.service.controls.DeviceTypes

data class BluetoothDeviceListItem(
    val deviceName: String = "undefined_device_name",
    val macAddress: MacAddress,
    val deviceType: DeviceTypes,
    val supportedBluetoothProfiles: ArrayList<String>,
    val listOfUUIDs: ArrayList<String>,
    val lastMessage: String? = null,
    val lastMessageDate: String,
    val lastMessageStatus: Int,
    val howManyUnseen: Int,
    val profilePicture: Uri? = null
)