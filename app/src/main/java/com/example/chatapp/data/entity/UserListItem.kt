package com.example.chatapp.data.entity

import android.widget.ImageView

data class UserListItem(
    val nick: String = "",
    val uid: String,
    val profilePicture: String? = null,
    val howManyUnseenMessage: Int = 0,
    val lastMessageDate: String = "13.33",
    var lastMessage: String? = null,
    var lastMessageBy: String? = null,
    val lastMessageStatus: Int = 0
)