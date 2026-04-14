package com.example.chatapp.util

import android.content.Context
import android.util.Log
import com.example.chatapp.data.entity.ObjectConstants

fun saveTheAnswer(key: String, context: Context, answerToSave: Int){
    /*Log.d("answer_check", "saveTheAnswer - key is $key")
    Log.d("answer_check", "saveTheAnswer - answer to save is $answerToSave")
    val SEEN_DIALOGUE_REFUSE = ObjectConstants.SEEN_DIALOGUE_REFUSE*/
    val sp = context.getSharedPreferences("permissions", Context.MODE_PRIVATE)
    val editor = sp.edit()
    editor.putInt(key, answerToSave)
    editor.apply()
    //Log.d("answer_check", "saveTheAnswer - the value is set, it is ${fetchTheAnswer(PERM_ANSWER_KEY, context)}")
}

fun fetchTheAnswer(key: String, context: Context): Int{
    var sp = context.getSharedPreferences(ObjectConstants.SP_ANSWER_FILE_NAME, Context.MODE_PRIVATE)
    val answer = sp.getInt(key, 0)
    //Log.d("answer_check", "fetchTheAnswer - answer is fetched: $answer")
    return answer
}

fun howManyRefused(context: Context){
    val keyDontAskAgain = ObjectConstants.DONT_ASK_AGAIN_KEY
    val dontAskAgainNumber = fetchTheAnswer(keyDontAskAgain, context)
    val sp = context.getSharedPreferences(ObjectConstants.SP_ANSWER_FILE_NAME, Context.MODE_PRIVATE)
    val editor = sp.edit()
    val numberToPut = dontAskAgainNumber + 1
    editor.putInt(keyDontAskAgain, numberToPut)
    Log.d("answer_check", "don't ask again: #$dontAskAgainNumber, number to put: $numberToPut")
    editor.apply()
}

