package com.example.chatapp.bluetooth.util

import android.content.Context
import com.example.chatapp.bluetooth.data.entity.ObjectConstants

fun saveName(context: Context, name: String){
    val sp = context.getSharedPreferences(ObjectConstants.SP_NICK_NAME, Context.MODE_PRIVATE)
    val editor = sp.edit()
    editor.putString(ObjectConstants.SP_NICK_NAME, name)
    editor.apply()
}

fun getNameFromMemory(context: Context): String{
    val sp = context.getSharedPreferences(ObjectConstants.SP_NICK_NAME, Context.MODE_PRIVATE)
    val name = sp.getString(ObjectConstants.SP_NICK_NAME, "") ?: ""
    return name
}