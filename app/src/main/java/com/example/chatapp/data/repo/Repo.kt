package com.example.chatapp.data.repo

import com.example.chatapp.data.entity.APICevap
import com.example.chatapp.retrofit.DAO
import com.example.chatapp.retrofit.UtilRetrofit
import retrofit2.create
import javax.inject.Inject

class Repo @Inject constructor(val dao: DAO) {

    suspend fun getTheWeatherData(latitude: Float, longitude: Float) : APICevap{
        return dao.getTheWeather(latitude, longitude)
    }

}