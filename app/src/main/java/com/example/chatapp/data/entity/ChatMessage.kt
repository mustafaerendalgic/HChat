package com.example.chatapp.data.entity

import com.google.firebase.Timestamp

data class ChatMessage(
    val messageID: String? = null,
    val uid: String? = null,
    val message: String? = null,
    val timestamp: String? = null,
    val deliverStatus: String? = null,
    val nickname: String? = null,
    var seen: Boolean = false
)