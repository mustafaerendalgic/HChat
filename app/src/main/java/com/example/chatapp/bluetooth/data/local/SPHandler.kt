package com.example.chatapp.bluetooth.data.local

import android.content.Context
import com.example.chatapp.bluetooth.data.entity.ObjectConstants
import com.example.chatapp.bluetooth.data.repo.general.SPHandler

class SPDataHandlers(val context: Context): SPHandler{

    private val sp = context.getSharedPreferences(ObjectConstants.SP_ANSWER_FILE_NAME, Context.MODE_PRIVATE)

    override fun saveTheAnswer(key: String, answerToSave: Int){
        sp.edit().putInt(key, answerToSave).apply()
        //Log.d("answer_check", "saveTheAnswer - the value is set, it is ${fetchTheAnswer(PERM_ANSWER_KEY, context)}")
    }

    override fun fetchTheAnswer(key: String): Int{
        return sp.getInt(key, 0)
        //Log.d("answer_check", "fetchTheAnswer - answer is fetched: $answer")

    }

    override fun howManyRefused(){
        val keyDontAskAgain = ObjectConstants.DONT_ASK_AGAIN_KEY
        val dontAskAgainNumber = fetchTheAnswer(keyDontAskAgain)
        val numberToPut = dontAskAgainNumber + 1
        sp.edit().putInt(keyDontAskAgain, numberToPut).apply()
    }

    override fun fetchUUIDRecord(): String?{
        return sp.getString(ObjectConstants.UUID_KEY_NAME, null)

    }

    override fun saveUUIDRecord(uuid: String){
        sp.edit().putString(ObjectConstants.UUID_KEY_NAME, uuid).apply()
    }
}



