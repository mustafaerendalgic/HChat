package com.example.chatapp.retrofit

import com.example.chatapp.data.entity.APICevap
import retrofit2.http.GET
import retrofit2.http.Query

interface DAO {

    @GET("forecast")
    suspend fun getTheWeather(
        @Query("latitude") latitude: Float,
        @Query("longitude") longitude: Float,
        @Query("current") current: String = "temperature_2m"
    ) : APICevap

}