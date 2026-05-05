package com.example.chatapp.bluetooth.data.entity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.example.chatapp.bluetooth.data.entity.ObjectConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import java.io.InputStream
import java.io.OutputStream

data class BluetoothConnection(
    val socket: BluetoothSocket,
    val inputStream: InputStream,
    val outputStream: OutputStream,
    val device: BluetoothDevice,
    val scope: CoroutineScope
)