package com.example.chatapp.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UtilRetrofit {
    companion object{
        val baseURL = "https://api.open-meteo.com/v1/"

        fun getRetrofit() : Retrofit{
            return Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create()).build()
        }

    }
}