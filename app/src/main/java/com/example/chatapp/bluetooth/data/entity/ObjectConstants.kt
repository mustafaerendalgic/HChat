package com.example.chatapp.bluetooth.data.entity

import android.os.ParcelUuid
import java.util.UUID

object ObjectConstants {
    val PERM_ANSWER_KEY = "perm_request"
    val DONT_ASK_AGAIN_KEY = "dont_ask_again"
    val NOT_SEEN_DIALOGUE = 0
    val SEEN_DIALOGUE_REFUSE = 1
    val SEEN_DIALOGUE_ACCEPT = 2
    val SP_ANSWER_FILE_NAME = "permissions"
    val DATABASE_NAME = "message_database"
    val BLUETOOTH_BUFFER_SIZE = 1024
    val _uuid = UUID.fromString("4a202ae9-4a5b-4ed9-9df5-31d3b58c3b88")
    val appName = "Bchat"
    val SERVER_CODE = 1
    val CLINET_CODE = 0
    val IDLE_CODE = 2
    val SP_NICK_NAME = "nickname"
    val PARCEL_UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB")
}