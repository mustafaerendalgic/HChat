package com.example.chatapp.bluetooth.util

fun getChatFileName(remoteUuid: String): String {
    return transformToMD5(remoteUuid.trim().lowercase())
}