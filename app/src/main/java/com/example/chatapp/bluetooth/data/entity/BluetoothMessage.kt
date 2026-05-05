package com.example.chatapp.bluetooth.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message_table", primaryKeys = ["messageID", "isSentByMe", "chatFileName"])
data class BluetoothMessage(
    val messageID: Int,
    val mac: String? = null,
    val message: String,
    val timestamp: String,
    val deviceName: String? = null,
    val nickname: String,
    val isSentByMe: Boolean = false,
    val chatFileName: String
)