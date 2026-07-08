package com.example.chatapp.bluetooth.data.repo.general

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.example.chatapp.bluetooth.event.GeneralBluetoothEvent
import com.example.chatapp.bluetooth.data.repo.BluetoothRepo
import javax.inject.Inject

class GeneralHandlerImp @Inject constructor(val repo: BluetoothRepo): GeneralHandler {
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend override fun handleGeneralEvents(event: GeneralBluetoothEvent) {
        when(event){
            is GeneralBluetoothEvent.EndTheConnection -> repo.endConnection(event.device)
            GeneralBluetoothEvent.PerformScan -> repo.performScan()
            is GeneralBluetoothEvent.SetName -> repo.setName(event.name)
            is GeneralBluetoothEvent.TapToChat -> repo.tapToChat(event.deviceListItem)
            is GeneralBluetoothEvent.SendMessage -> repo.sendMessage(event.message, event.device)
            GeneralBluetoothEvent.ClearCache -> repo.clearCache()
        }
    }
}