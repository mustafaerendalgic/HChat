package com.example.chatapp.scan.data.entity

data class ScanResultObject(
    val ipAddress: String,
    val hostName: String,
    val macAddress: String = "Unavailable"
)