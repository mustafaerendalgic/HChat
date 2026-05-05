package com.example.chatapp.internet.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(date: String?) : String{
    return if(date != null)
        date.split(" ").get(1).toString()
    else
        "unknown"
}

fun formatDateBluetooth(timestamp: String?): String {
    return try {
        if (timestamp != null) {
            val date = Date(timestamp.toLong())
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            format.format(date)
        } else "unknown"
    } catch (e: Exception) {
        "unknown"
    }
}

