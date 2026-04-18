package com.example.chatapp.data.entity

data class BluetoothMessage(
    val messageID: Int? = null,
    val mac: String? = null,
    val message: String? = null,
    val timestamp: String? = null,
    val nickname: String? = null,
    val isSentByMe: Boolean = false
)