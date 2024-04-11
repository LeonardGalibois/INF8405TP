package com.example.tracer

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

const val API_KEY: String ="76607276e0904255ae01732525f51507"


data class WeatherData(
    val lat: Double,
    val lon: Double,
    val timezone:String,
    val current: CurrentWeather
)

data class CurrentWeather(
    val temp: Double,
    val feels_like:Double
)

interface OpenWeatherMapService {
    @GET("onecall")
    fun getCurrentWeatherData(@Query("lat") lat: String, @Query("lon")lon:String, @Query("appid") apiKey:String = API_KEY, @Query("units") unit:String = "metric") : Call<WeatherData>
}
object WeatherService {
    private const val BASE_URL: String = "https://api.openweathermap.org/data/3.0/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
    }

    val apiService: OpenWeatherMapService by lazy {
        retrofit.create(OpenWeatherMapService::class.java)
    }
}