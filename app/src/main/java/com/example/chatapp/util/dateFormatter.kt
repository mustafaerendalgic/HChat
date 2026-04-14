package com.example.chatapp.util

fun formatDate(date: String?) : String{
    return if(date != null)
        date.split(" ").get(1).toString()
    else
        "unknown"
}