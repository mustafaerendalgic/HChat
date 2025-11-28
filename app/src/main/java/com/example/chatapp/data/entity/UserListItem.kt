package com.example.chatapp.data.entity

import android.widget.ImageView

data class UserListItem(
    val nick: String = "",
    val profilePicture: String,
    val lastMessage: String = "Say hi to ${nick}",
    val lastMessageStatus: Int = 0
)