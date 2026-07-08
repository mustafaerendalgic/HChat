package com.example.chatapp.bluetooth.util

import java.nio.ByteBuffer
import java.util.UUID
import kotlin.uuid.Uuid

fun convertUuidToByteArray(uuidString: String): ByteArray{
    val uuid = UUID.fromString(uuidString)
    val buffer = ByteBuffer.wrap(ByteArray(16))
    buffer.putLong(uuid.mostSignificantBits)
    buffer.putLong(uuid.leastSignificantBits)
    return buffer.array()
}

fun byteArrayToUuidString(bytes: ByteArray): String {
    val byteBuffer = ByteBuffer.wrap(bytes)
    val high = byteBuffer.long
    val low = byteBuffer.long
    return UUID(high, low).toString()
}