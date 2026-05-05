package com.example.chatapp.bluetooth.data.repo.general

import android.bluetooth.BluetoothDevice
import com.example.chatapp.bluetooth.event.GeneralBluetoothEvent

interface GeneralHandler {
    suspend fun handleGeneralEvents(event: GeneralBluetoothEvent)
}