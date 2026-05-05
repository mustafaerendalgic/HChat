package com.example.chatapp.bluetooth.util

import java.security.MessageDigest

fun transformToMD5(string: String): String{
    val md5 = MessageDigest.getInstance("MD5")
    val digest = md5.digest(string.toByteArray())
    return digest.toHexString()
}