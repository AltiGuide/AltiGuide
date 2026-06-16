package com.example.altiguide_mobile.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class MountainModel(
    val id: Int,
    val name: String,
    val description: String?,
    val altitude: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val province: String?,
    @SerializedName("image")
    val image_url: String?,
    val routes: List<RouteModel>?,
    val location: String? = null,
    val content: com.google.gson.JsonElement? = null,
    @SerializedName("content_json")
    val contentJson: com.google.gson.JsonElement? = null
)

data class RouteModel(
    val id: Int,
    val mountain_id: Int,
    val name: String,
    val difficulty: String?,
    @SerializedName("distance")
    val distance_km: Double?,
    @SerializedName("estimated_time")
    val duration_hours: Double?,
    val daily_quota: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val image: String? = null,
    val mountain: MountainModel?,
    val route_info: RouteInfoModel?,
    val waypoints: List<WaypointModel>?,
    @SerializedName("track_coordinates")
    val trackCoordinates: JsonElement? = null
)

data class RouteInfoModel(
    val id: Int,
    val route_id: Int,
    val basecamp_name: String? = null,
    val basecamp_address: String? = null,
    val basecamp_altitude: Int? = null,
    val simaksi_price: String? = null,
    val facilities: String? = null,
    val facilities_description: String? = null,
    val logistics_description: String? = null,
    val contact_person: String? = null,
    val notes: String? = null,
    val ojek_price: String? = null,
    val ojek_description: String? = null
)

data class WaypointModel(
    val id: Int,
    val route_id: Int,
    val name: String,
    val altitude: Int?,
    val order_index: Int,
    val distance_from_prev: Double?,
    val estimated_time_minutes: Int?,
    val description: String?,
    val has_water_source: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class WeatherResponse(
    val status: String,
    val mountain_name: String?,
    val route_name: String?,
    val basecamp_altitude: Int?,
    val data: WeatherData?
)

data class WeatherData(
    val current_weather: CurrentWeather,
    val daily: DailyWeather,
    val hourly: HourlyWeather? = null
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int
)

data class DailyWeather(
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val weathercode: List<Int>
)

data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weathercode: List<Int>
)
