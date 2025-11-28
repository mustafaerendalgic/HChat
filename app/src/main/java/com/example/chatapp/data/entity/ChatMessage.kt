package com.example.chatapp.data.entity

import com.google.firebase.Timestamp

data class ChatMessage(
    val messageID: String,
    val uid: String,
    val profilePicture: String,
    val message: String,
    val timestamp: String,
    val deliverStatus: String,
    val nickname: String
)