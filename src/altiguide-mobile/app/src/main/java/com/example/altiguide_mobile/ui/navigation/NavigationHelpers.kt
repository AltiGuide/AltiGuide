package com.example.altiguide_mobile.ui.navigation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Color as AndroidColor
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.altiguide_mobile.R
import com.google.gson.JsonElement
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint

// ── Colors ──────────────────────────────────────────────────────────────────
internal val AltiDark      = Color(0xFF20341B)
internal val AltiMedium    = Color(0xFF859763)
internal val AltiLight     = Color(0xFFC3CB92)

internal val CustomOfflineTileSource = XYTileSource(
    "Mapnik",
    0, 19, 256, ".png",
    arrayOf(
        "https://a.tile.openstreetmap.org/",
        "https://b.tile.openstreetmap.org/",
        "https://c.tile.openstreetmap.org/"
    ),
    "© OpenStreetMap contributors",
    TileSourcePolicy(
        2,
        TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL or
        TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
    )
)

// ── Montserrat font family ──────────────────────────────────────────────────
internal val Montserrat = FontFamily(
    Font(R.font.montserrat_regular,  FontWeight.Normal),
    Font(R.font.montserrat_medium,   FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold,     FontWeight.Bold)
)

internal data class WeatherMock(val emoji: String, val tempMax: Int, val tempMin: Int)

internal data class HourlyMock(val time: String, val emoji: String, val temp: Int)

fun parseTrackCoordinates(element: JsonElement?): List<GeoPoint> {
    if (element == null || !element.isJsonArray) return emptyList()
    val list = mutableListOf<GeoPoint>()
    try {
        val array = element.asJsonArray
        for (i in 0 until array.size()) {
            val pt = array.get(i).asJsonArray
            val lat = pt.get(0).asDouble
            val lng = pt.get(1).asDouble
            list.add(GeoPoint(lat, lng))
        }
    } catch (e: Exception) {
        Log.e("TrackCoordinates", "Error parsing track coordinates", e)
    }
    return list
}

fun getOfflineMapDrawable(name: String): Int {
    return R.drawable.img_offlinemaps
}

fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "Cerah"
        1, 2, 3 -> "Cerah Berawan"
        45, 48 -> "Berkabut"
        51, 53, 55 -> "Gerimis"
        61, 63, 65 -> "Hujan Ringan"
        71, 73, 75 -> "Hujan Salju"
        80, 81, 82 -> "Hujan Showers"
        95, 96, 99 -> "Badai Petir"
        else -> "Berawan"
    }
}

internal fun getWeatherMock(mountainIndex: Int, dayIndex: Int): WeatherMock {
    val emojis = listOf("☀️", "⛅", "☁️", "🌧️", "⛈️")
    val hash = (mountainIndex * 7 + dayIndex) % 5
    val emoji = emojis[hash]

    val baseTempMax = when (mountainIndex) {
        0 -> 20 // Merbabu
        1 -> 24 // Andong
        2 -> 18 // Lawu
        3 -> 21 // Prau
        4 -> 17 // Sindoro
        5 -> 15 // Slamet
        6 -> 17 // Sumbing
        7 -> 22 // Ungaran
        else -> 20
    }

    val tempVarianceMax = (dayIndex % 3) - 1
    val max = baseTempMax + tempVarianceMax
    val min = max - 6 - (dayIndex % 3)
    return WeatherMock(emoji, max, min)
}

internal fun getHourlyMockList(dayIndex: Int): List<HourlyMock> {
    val times = listOf("05:00 AM", "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM")
    val emojis = listOf("☀️", "⛅", "☁️", "🌧️", "⛈️", "⛅")
    val temps = listOf(22, 18, 16, 19, 23, 25)
    return List(6) { index ->
        val emojiShift = (dayIndex + index) % emojis.size
        val tempShift = temps[index] + (dayIndex % 3) - 1
        HourlyMock(times[index], emojis[emojiShift], tempShift)
    }
}

fun formatHourlyTime(isoTime: String): String {
    val timePart = isoTime.substringAfter('T', "")
    if (timePart.isEmpty()) return isoTime
    try {
        val parts = timePart.split(":")
        val hour = parts[0].toInt()
        val minute = parts.getOrNull(1) ?: "00"
        val ampm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%02d:%s %s", displayHour, minute, ampm)
    } catch (e: Exception) {
        return timePart
    }
}

fun getMountainDrawable(name: String): Int {
    val key = name.lowercase().removePrefix("gunung ").trim()
    return when (key) {
        "merbabu"  -> R.drawable.merbabu
        "andong"   -> R.drawable.andong
        "lawu"     -> R.drawable.lawu
        "prau"     -> R.drawable.prau
        "sindoro"  -> R.drawable.sindoro
        "slamet"   -> R.drawable.slamet
        "sumbing"  -> R.drawable.sumbing
        "ungaran"  -> R.drawable.ungaran
        else       -> R.drawable.startjourney_img
    }
}

fun buildColoredMarkerIcon(context: Context, color: Int): Drawable {
    val size = 48 // px
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // White border
    paint.color = AndroidColor.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

    // Colored fill
    paint.color = color
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

    return BitmapDrawable(context.resources, bitmap)
}

fun buildDirectionArrowBitmap(context: Context): Bitmap {
    val size = 48 // px
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val path = AndroidPath()
    val width = size.toFloat()
    val height = size.toFloat()

    // Draw the white background/outline arrow slightly larger
    paint.color = AndroidColor.WHITE
    paint.style = Paint.Style.FILL_AND_STROKE
    paint.strokeWidth = 6f
    paint.strokeJoin = Paint.Join.ROUND
    paint.strokeCap = Paint.Cap.ROUND
    
    path.moveTo(width / 2f, 6f) // top tip
    path.lineTo(width - 8f, height - 8f) // bottom right
    path.lineTo(width / 2f, height * 0.72f) // indent center
    path.lineTo(8f, height - 8f) // bottom left
    path.close()
    canvas.drawPath(path, paint)

    // Draw the inner blue arrow
    paint.color = AndroidColor.parseColor("#4285F4") // Google Maps Blue
    paint.style = Paint.Style.FILL
    paint.strokeWidth = 0f
    
    path.reset()
    path.moveTo(width / 2f, 11f) // top tip
    path.lineTo(width - 12f, height - 12f) // bottom right
    path.lineTo(width / 2f, height * 0.72f) // indent center
    path.lineTo(12f, height - 12f) // bottom left
    path.close()
    canvas.drawPath(path, paint)

    return bitmap
}
