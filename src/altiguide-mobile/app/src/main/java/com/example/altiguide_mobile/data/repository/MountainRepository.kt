package com.example.altiguide_mobile.data.repository

import android.content.Context
import android.util.Log
import com.example.altiguide_mobile.data.model.MountainModel
import com.example.altiguide_mobile.data.model.WeatherResponse
import com.example.altiguide_mobile.data.network.AltiGuideApiService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MountainRepository @Inject constructor(
    private val apiService: AltiGuideApiService,
    @ApplicationContext private val context: Context
) {
    // In-memory cache to prevent reading/parsing the JSON file repeatedly
    private var cachedMountains: List<MountainModel>? = null

    private fun loadMountainsFromAssets(): List<MountainModel> {
        // If data is already in memory, return it instantly
        cachedMountains?.let { return it }

        return try {
            val jsonString = context.assets.open("mountains.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<MountainModel>>() {}.type
            val list = Gson().fromJson<List<MountainModel>>(jsonString, listType) ?: emptyList()
            cachedMountains = list // Save to cache
            list
        } catch (e: Exception) {
            Log.e("MountainRepository", "Error loading mountains from assets", e)
            emptyList()
        }
    }

    suspend fun getMountains(): List<MountainModel> {
        return loadMountainsFromAssets()
    }

    suspend fun getMountainDetail(id: Int): MountainModel {
        val mountains = loadMountainsFromAssets()
        return mountains.firstOrNull { it.id == id }
            ?: throw IOException("Mountain with id $id not found locally in assets")
    }

    suspend fun getMountainWeather(id: Int): WeatherResponse {
        return WeatherResponse(
            status = "success",
            mountain_name = "Mock Mountain",
            route_name = null,
            basecamp_altitude = 1500,
            data = com.example.altiguide_mobile.data.model.WeatherData(
                current_weather = com.example.altiguide_mobile.data.model.CurrentWeather(22.0, 5.0, 0),
                daily = com.example.altiguide_mobile.data.model.DailyWeather(
                    temperature_2m_max = listOf(22.0, 23.0, 24.0, 22.0, 21.0, 23.0, 24.0),
                    temperature_2m_min = listOf(14.0, 15.0, 14.0, 13.0, 12.0, 14.0, 15.0),
                    weathercode = listOf(0, 1, 2, 3, 45, 51, 0)
                )
            )
        )
    }

    suspend fun getMountainWeatherDirect(latitude: Double, longitude: Double, elevation: Double? = null): WeatherResponse {
        val client = okhttp3.OkHttpClient()
        val gson = com.google.gson.Gson()
        val elevationParam = if (elevation != null) "&elevation=$elevation" else ""
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude$elevationParam" +
            "&current_weather=true" +
            "&hourly=temperature_2m,weathercode" +
            "&daily=weathercode,temperature_2m_max,temperature_2m_min" +
            "&timezone=Asia%2FJakarta"
        val request = okhttp3.Request.Builder().url(url).build()

        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw java.io.IOException("Unexpected code $response")
                val body = response.body?.string() ?: throw java.io.IOException("Empty response body")

                val openMeteo = gson.fromJson(body, OpenMeteoResponse::class.java)
                val cw = openMeteo.current_weather
                WeatherResponse(
                    status = "success",
                    mountain_name = null,
                    route_name = null,
                    basecamp_altitude = null,
                    data = com.example.altiguide_mobile.data.model.WeatherData(
                        current_weather = com.example.altiguide_mobile.data.model.CurrentWeather(
                            temperature = cw?.temperature ?: 20.0,
                            windspeed = cw?.windspeed ?: 5.0,
                            weathercode = cw?.weathercode ?: 0
                        ),
                        daily = openMeteo.daily,
                        hourly = openMeteo.hourly
                    )
                )
            }
        }
    }
}

private data class OpenMeteoCurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int
)

private data class OpenMeteoResponse(
    val current_weather: OpenMeteoCurrentWeather? = null,
    val daily: com.example.altiguide_mobile.data.model.DailyWeather,
    val hourly: com.example.altiguide_mobile.data.model.HourlyWeather? = null
)

