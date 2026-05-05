package com.example.chatapp.bluetooth.data.repo.client

import com.example.chatapp.bluetooth.event.ClientBluetoothEvent
import com.example.chatapp.bluetooth.data.repo.BluetoothRepo
import javax.inject.Inject

class ClientHandlerImp @Inject constructor(val repo: BluetoothRepo): ClientHandler {
    suspend override fun handleClientEvent(event: ClientBluetoothEvent) {
        when(event){
            is ClientBluetoothEvent.ConnectToDevices -> repo.ManageHostingAndConnecting().establishConnectionAsClient(event.device)
        }
    }

}