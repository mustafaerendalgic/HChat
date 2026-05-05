package com.example.chatapp.bluetooth.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.chatapp.bluetooth.data.entity.BluetoothMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface BluetoothDao {

@Query ("SELECT * FROM message_table WHERE chatFileName IS :chatName")
fun getTheChatHistory(chatName: String) : Flow<List<BluetoothMessage>>

@Query ("SELECT * FROM message_table WHERE messageID = :id")
suspend fun checkIfMessageWasSent(id: Int): List<BluetoothMessage>

@Query ("SELECT MAX(messageID) FROM message_table WHERE chatFileName = :fileName")
suspend fun getTheLastSentMessageID(fileName: String): Int?

@Insert
suspend fun addMessageToHistory(message: BluetoothMessage)

@Insert
suspend fun addMessageListToHistory(messages: List<BluetoothMessage>)

@Delete
suspend fun deleteMessage(message: BluetoothMessage)

@Upsert
suspend fun updateMessage(message: BluetoothMessage)

}

