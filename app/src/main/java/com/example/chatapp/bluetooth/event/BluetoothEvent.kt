package com.example.chatapp.bluetooth.event

import android.bluetooth.BluetoothDevice
import com.example.chatapp.bluetooth.data.entity.BluetoothDeviceListItem

sealed interface BluetoothEvent {

}

sealed interface GeneralBluetoothEvent: BluetoothEvent{
    object PerformScan: GeneralBluetoothEvent
    data class TapToChat(val deviceListItem: BluetoothDeviceListItem): GeneralBluetoothEvent
    data class EndTheConnection(val device: BluetoothDeviceListItem?): GeneralBluetoothEvent
    data class SetName(val name: String): GeneralBluetoothEvent
    data class Error(val message: String)
    data class SendMessage(val message: String, val device: BluetoothDevice): GeneralBluetoothEvent
}

sealed interface ClientBluetoothEvent: BluetoothEvent{
    data class ConnectToDevices(val device: BluetoothDevice): ClientBluetoothEvent
}