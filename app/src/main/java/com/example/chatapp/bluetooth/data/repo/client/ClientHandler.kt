package com.example.chatapp.bluetooth.data.repo.client

import com.example.chatapp.bluetooth.event.ClientBluetoothEvent

interface ClientHandler {
    suspend fun handleClientEvent(event: ClientBluetoothEvent)
}