package com.example.chatapp.bluetooth.data.repo.general

import android.bluetooth.BluetoothDevice
import android.util.Log
import com.example.chatapp.bluetooth.event.GeneralBluetoothEvent
import com.example.chatapp.bluetooth.data.repo.BluetoothRepo
import javax.inject.Inject

class GeneralHandlerImp @Inject constructor(val repo: BluetoothRepo): GeneralHandler {
    suspend override fun handleGeneralEvents(event: GeneralBluetoothEvent) {
        when(event){
            is GeneralBluetoothEvent.EndTheConnection -> repo.endConnection(event.device)
            GeneralBluetoothEvent.PerformScan -> repo.performScan()
            is GeneralBluetoothEvent.SetName -> repo.setName(event.name)
            is GeneralBluetoothEvent.TapToChat -> repo.tapToChat(event.deviceListItem)
            is GeneralBluetoothEvent.SendMessage -> repo.sendMessage(event.message, event.device)
        }
    }
}