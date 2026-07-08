package com.example.chatapp.scan.viewmodel

sealed interface NetworkEvent {
    object PerformScan: NetworkEvent
    object StopScan: NetworkEvent
}