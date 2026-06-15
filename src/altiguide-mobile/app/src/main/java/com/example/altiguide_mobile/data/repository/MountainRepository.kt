package com.example.altiguide_mobile.data.repository

import com.example.altiguide_mobile.data.model.MountainModel
import com.example.altiguide_mobile.data.model.WeatherResponse
import com.example.altiguide_mobile.data.network.AltiGuideApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MountainRepository @Inject constructor(
    private val apiService: AltiGuideApiService
) {
    suspend fun getMountains(): List<MountainModel> {
        return apiService.getMountains()
    }

    suspend fun getMountainDetail(id: Int): MountainModel {
        return apiService.getMountainDetail(id)
    }

    suspend fun getMountainWeather(id: Int): WeatherResponse {
        return apiService.getMountainWeather(id)
    }

    suspend fun getMountainWeatherDirect(latitude: Double, longitude: Double, elevation: Double? = null): WeatherResponse {
        val client = okhttp3.OkHttpClient()
        val gson = com.google.gson.Gson()
        val elevationParam = if (elevation != null) "&elevation=$elevation" else ""
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude$elevationParam&hourly=temperature_2m,weathercode&daily=weathercode,temperature_2m_max,temperature_2m_min&timezone=Asia/Jakarta"
        val request = okhttp3.Request.Builder().url(url).build()

        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw java.io.IOException("Unexpected code $response")
                val body = response.body?.string() ?: throw java.io.IOException("Empty response body")
                
                val openMeteo = gson.fromJson(body, OpenMeteoResponse::class.java)
                WeatherResponse(
                    status = "success",
                    mountain_name = null,
                    route_name = null,
                    basecamp_altitude = null,
                    data = com.example.altiguide_mobile.data.model.WeatherData(
                        current_weather = com.example.altiguide_mobile.data.model.CurrentWeather(0.0, 0.0, 0),
                        daily = openMeteo.daily,
                        hourly = openMeteo.hourly
                    )
                )
            }
        }
    }
}

private data class OpenMeteoResponse(
    val daily: com.example.altiguide_mobile.data.model.DailyWeather,
    val hourly: com.example.altiguide_mobile.data.model.HourlyWeather? = null
)

