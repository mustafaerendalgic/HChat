package com.example.chatapp.data.entity

import android.widget.ImageView

data class UserListItem(
    val profilePicture: ImageView,
    val lastMessage: String,
    val lastMessageStatus: Int,
    val nick: String,
)