package com.example.chatapp.retrofit

import com.google.firebase.firestore.util.Util

class DAOAccessFunction{
    companion object{

        fun getTheDao(): DAO{
            return UtilRetrofit.getRetrofit().create(DAO::class.java)
        }

    }
}