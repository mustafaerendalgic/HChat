package com.example.chatapp.bluetooth.data.repo

import android.annotation.SuppressLint
import com.example.chatapp.bluetooth.data.entity.BluetoothConnection
import com.example.chatapp.bluetooth.data.entity.BluetoothMessage
import com.example.chatapp.bluetooth.util.getChatFileName
import java.nio.ByteBuffer
import javax.inject.Inject

class BluetoothMessageParser @Inject constructor() {

    @SuppressLint("MissingPermission")
    fun organizeBytesIntoMessageItem(byteArrays: List<ByteArray>, connection: BluetoothConnection): BluetoothMessage{
        val sortedByteArray = byteArrays.sortedBy { it[4].toInt() and 0xFF}
        val messageID = getMessageID(byteArrays.last())
        val nickname = getNick(byteArrays.last())
        val sb = StringBuilder()
        sortedByteArray.forEach { byteArray ->
            sb.append(getMessage(byteArray))
        }
        val message = sb.toString()
        val item = BluetoothMessage(messageID, connection.device.address, message, System.currentTimeMillis().toString(), connection.device.name, nickname, false,
            getChatFileName(connection.uuid)
        )
        return item
    }

    fun getMessageID(packet: ByteArray): Int{
        return ByteBuffer.wrap(packet, 0, 2).short.toInt()
    }

    fun getNick(packet: ByteArray): String{
        val nickSize = packet.get(3).toInt()
        val nickBytes = packet.copyOfRange(5, 5 + nickSize)
        val nick = String(nickBytes)
        return nick
    }

    fun getMessage(packet: ByteArray): String{
        val nickSize = packet.get(3).toInt()
        return String(packet.copyOfRange(5 + nickSize, packet.size))
    }

}