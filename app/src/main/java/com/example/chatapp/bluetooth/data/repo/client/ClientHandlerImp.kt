package com.example.chatapp.bluetooth.data.repo.client

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.chatapp.bluetooth.event.ClientBluetoothEvent
import com.example.chatapp.bluetooth.data.repo.BluetoothRepo
import javax.inject.Inject

class ClientHandlerImp @Inject constructor(val repo: BluetoothRepo): ClientHandler {
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend override fun handleClientEvent(event: ClientBluetoothEvent) {
        when(event){
            is ClientBluetoothEvent.ConnectToDevices -> repo.establishConnectionAsClient(event.device)
        }
    }

}