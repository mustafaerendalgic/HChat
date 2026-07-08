package com.example.chatapp.bluetooth.util

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.UUID
import kotlin.uuid.Uuid

fun createUUID(): String{
    return UUID.randomUUID().toString()
}