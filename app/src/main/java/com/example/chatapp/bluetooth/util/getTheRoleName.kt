package com.example.chatapp.bluetooth.util

import com.example.chatapp.bluetooth.data.entity.DeviceRole
import com.example.chatapp.bluetooth.data.entity.ObjectConstants

fun getTheRoleName(i: Int): String{
    return when(i){
        DeviceRole.CLIENT -> "CLIENT"
        DeviceRole.IDLE -> "IDLE"
        DeviceRole.SERVER -> "SERVER"
        else -> "IDLE"
    }
}