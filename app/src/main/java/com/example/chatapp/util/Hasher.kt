package com.example.chatapp.util

import java.security.MessageDigest

fun hasher(word: String): String{
    val digest = MessageDigest.getInstance("MD5").digest(word.toByteArray())
    val sb = StringBuilder()
    for (b in digest){
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}