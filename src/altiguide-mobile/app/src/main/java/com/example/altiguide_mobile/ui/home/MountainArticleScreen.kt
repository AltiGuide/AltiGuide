package com.example.altiguide_mobile.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.altiguide_mobile.R
import com.example.altiguide_mobile.data.model.MountainModel
// ── OSMDroid ──
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// ── Montserrat font family ──────────────────────────────────────────────────
private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular,  FontWeight.Normal),
    Font(R.font.montserrat_medium,   FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold,     FontWeight.Bold)
)

// ── Colors ──────────────────────────────────────────────────────────────────
private val AltiDark      = Color(0xFF374426)
private val AltiMedium    = Color(0xFF859763)
private val AltiLight     = Color(0xFFE3E9CD)

// ── Vector Icons ────────────────────────────────────────────────────────────
private val IconClock: ImageVector get() = ImageVector.Builder(
    name = "Clock", defaultWidth = 16.dp, defaultHeight = 16.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiDark)) {
        moveTo(11.99f, 2f)
        curveTo(6.47f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.47f, 22f, 11.99f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 11.99f, 2f); close()
        moveTo(12f, 20f)
        curveTo(7.58f, 20f, 4f, 16.42f, 4f, 12f)
        curveTo(4f, 7.58f, 7.58f, 4f, 12f, 4f)
        curveTo(16.42f, 4f, 20f, 7.58f, 20f, 12f)
        curveTo(20f, 16.42f, 16.42f, 20f, 12f, 20f); close()
        moveTo(12.5f, 7f); lineTo(11f, 7f); lineTo(11f, 13f)
        lineTo(16.25f, 16.15f); lineTo(17f, 14.92f)
        lineTo(12.5f, 12.25f); close()
    }
}.build()

private val IconRoute: ImageVector get() = ImageVector.Builder(
    name = "Route", defaultWidth = 16.dp, defaultHeight = 16.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiDark)) {
        moveTo(13.49f, 5.48f)
        curveTo(13.49f, 4.12f, 12.37f, 3f, 11f, 3f)
        curveTo(9.63f, 3f, 8.51f, 4.12f, 8.51f, 5.48f)
        curveTo(8.51f, 6.47f, 9.09f, 7.32f, 9.93f, 7.74f)
        lineTo(9.93f, 12f)
        curveTo(8.73f, 12.42f, 8f, 13.41f, 8f, 14.5f)
        curveTo(8f, 16.43f, 9.79f, 18f, 12f, 18f)
        curveTo(14.21f, 18f, 16f, 16.43f, 16f, 14.5f)
        curveTo(16f, 13.41f, 15.27f, 12.42f, 14.07f, 12f)
        lineTo(14.07f, 7.74f)
        curveTo(14.91f, 7.32f, 13.49f, 6.47f, 13.49f, 5.48f); close()
    }
}.build()

private val IconPin: ImageVector get() = ImageVector.Builder(
    name = "Pin", defaultWidth = 16.dp, defaultHeight = 16.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiDark)) {
        moveTo(12f, 2f)
        curveTo(8.13f, 2f, 5f, 5.13f, 5f, 9f)
        curveTo(5f, 14.25f, 12f, 22f, 12f, 22f)
        curveTo(12f, 22f, 19f, 14.25f, 19f, 9f)
        curveTo(19f, 5.13f, 15.87f, 2f, 12f, 2f)
        close()
        moveTo(12f, 11.5f)
        curveTo(10.62f, 11.5f, 9.5f, 10.38f, 9.5f, 9f)
        curveTo(9.5f, 7.62f, 10.62f, 6.5f, 12f, 6.5f)
        curveTo(13.38f, 6.5f, 14.5f, 7.62f, 14.5f, 9f)
        curveTo(14.5f, 10.38f, 13.38f, 11.5f, 12f, 11.5f)
        close()
    }
}.build()

private val IconStar: ImageVector get() = ImageVector.Builder(
    name = "Star", defaultWidth = 16.dp, defaultHeight = 16.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.Transparent), stroke = SolidColor(AltiDark), strokeLineWidth = 2f) {
        moveTo(12f, 2f)
        lineTo(15.09f, 8.26f); lineTo(22f, 9.27f); lineTo(17f, 14.14f)
        lineTo(18.18f, 21.02f); lineTo(12f, 17.77f); lineTo(5.82f, 21.02f)
        lineTo(7f, 14.14f); lineTo(2f, 9.27f); lineTo(8.91f, 8.26f); close()
    }
}.build()

private val IconBack: ImageVector get() = ImageVector.Builder(
    name = "Back", defaultWidth = 24.dp, defaultHeight = 24.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
        moveTo(20f, 11f)
        lineTo(7.83f, 11f)
        lineTo(13.41f, 5.41f)
        lineTo(12f, 4f)
        lineTo(4f, 12f)
        lineTo(12f, 20f)
        lineTo(13.41f, 18.59f)
        lineTo(7.83f, 13f)
        lineTo(20f, 13f)
        close()
    }
}.build()

@Composable
fun MountainArticleScreen(
    mountain: MountainModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Format altitude
    val altitudeText = mountain.altitude?.let { "${formatNumber(it)} mdpl" } ?: ""

    // Format route info values
    val firstRoute = mountain.routes?.firstOrNull()
    val durationText = firstRoute?.duration_hours?.let {
        val h = (it / 60).toInt()
        val hEnd = h + 2
        "$h-$hEnd hr"
    } ?: "7-9 hr"

    val distanceText = firstRoute?.distance_km?.let {
        if (it == it.toLong().toDouble()) "${it.toLong()} km"
        else "$it km"
    } ?: "8 km"

    val locationText = mountain.location ?: mountain.province ?: "Central Java"

    val difficultyRaw = firstRoute?.difficulty?.lowercase() ?: "moderate"
    val levelText = when (difficultyRaw) {
        "hard"     -> "Expert"
        "moderate" -> "Intermediate"
        "easy"     -> "Beginner"
        else       -> difficultyRaw.replaceFirstChar { it.uppercaseChar() }
    }

    val imageResId = getMountainDrawable(mountain.name)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Underlay
    ) {
        // 1. Cover Photo
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = mountain.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            contentScale = ContentScale.Crop
        )

        // Subtle top dark overlay for back button visibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent)
                    )
                )
        )

        // 2. Scrollable Detail Content Sheet
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Spacer to scroll past the cover image area
            Spacer(modifier = Modifier.height(290.dp))

            // The overlapping sheet
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                    .background(AltiLight)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                // Drag handle indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(44.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(AltiDark.copy(alpha = 0.15f))
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Title
                Text(
                    text = mountain.name,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = AltiDark,
                    lineHeight = 28.sp
                )

                // Elevation
                Text(
                    text = altitudeText,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = AltiDark.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2x2 Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        InfoItem(icon = IconClock, label = "Duration", value = durationText)
                        Spacer(modifier = Modifier.height(18.dp))
                        InfoItem(icon = IconPin, label = "Location", value = locationText)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        InfoItem(icon = IconRoute, label = "Distance", value = distanceText)
                        Spacer(modifier = Modifier.height(18.dp))
                        InfoItem(icon = IconStar, label = "Level", value = levelText)
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Description section
                Text(
                    text = "Description",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AltiDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                val paragraphs = parseContentToParagraphs(mountain.content ?: mountain.contentJson)
                    ?: mountain.description?.split("\n\n")?.map { it.trim() }?.filter { it.isNotEmpty() }
                    ?: listOf("Tidak ada informasi artikel untuk gunung ini.")

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    paragraphs.forEachIndexed { index, paragraphText ->
                        val header = when (index) {
                            1 -> "Persiapan Fisik & Logistik"
                            2 -> "Keindahan Puncak"
                            3 -> "Konservasi & Etika Pendakian"
                            4 -> "Puncak Tertinggi: Puncak Triangulasi"
                            else -> null
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (header != null) {
                                Text(
                                    text = header,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AltiDark
                                )
                            }
                            Text(
                                text = paragraphText,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                color = AltiDark.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Location section
                Text(
                    text = "Location",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AltiDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Map Window with live dynamic WebView (or redirects to google maps app)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    ArticleOsmMapView(
                        latitude = mountain.latitude ?: -7.4497,
                        longitude = mountain.longitude ?: 110.4381,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Transparent overlay — tap to open maps app
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { launchMaps(context, mountain) }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Open on Maps Pill Button
                Button(
                    onClick = { launchMaps(context, mountain) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AltiDark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Open on Maps",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                // Bottom padding to avoid navigation bar clash
                Spacer(modifier = Modifier.height(76.dp))
            }
        }

        // 3. Floating Back Button
        Box(
            modifier = Modifier
                .padding(start = 20.dp, top = 52.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f))
                .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                .clickable { onBack() }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AltiDark,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = value,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = AltiDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = AltiDark.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
private fun ArticleOsmMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    val center = GeoPoint(latitude, longitude)
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef.value?.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapViewRef.value?.onPause()
                else                       -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef.value?.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(false)
                setBuiltInZoomControls(false)
                setUseDataConnection(true)
                controller.setZoom(13.0)
                controller.setCenter(center)
                // Add a marker for the mountain summit
                val marker = Marker(this).apply {
                    position = center
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                overlays.add(marker)
                mapViewRef.value = this
            }
        },
        update = { mapView ->
            mapView.invalidate()
        }
    )
}


private fun launchMaps(context: Context, mountain: MountainModel) {
    val lat = mountain.latitude ?: -7.4497
    val lon = mountain.longitude ?: 110.4381
    val encodedName = Uri.encode(mountain.name)

    // Strategy 1: Try Google Maps app via geo: URI
    val geoUri = Uri.parse("geo:$lat,$lon?q=$encodedName")
    val geoIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val mapsAppInstalled = context.packageManager
        .queryIntentActivities(geoIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        .any { it.activityInfo.packageName == "com.google.android.apps.maps" }

    if (mapsAppInstalled) {
        geoIntent.setPackage("com.google.android.apps.maps")
        try { context.startActivity(geoIntent); return } catch (_: Exception) {}
    }

    // Strategy 2: OpenStreetMap in browser (always works without Google)
    val osmUrl = "https://www.openstreetmap.org/?mlat=$lat&mlon=$lon&zoom=14"
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(osmUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (_: Exception) {
        android.widget.Toast.makeText(
            context,
            "Tidak dapat membuka peta.",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}

private fun formatNumber(n: Int): String {
    return String.format("%,d", n).replace(',', '.')
}

private fun getMountainDrawable(name: String): Int {
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

private fun parseContentToParagraphs(element: com.google.gson.JsonElement?): List<String>? {
    if (element == null || element.isJsonNull) return null
    val list = mutableListOf<String>()
    
    try {
        if (element.isJsonPrimitive) {
            val rawText = element.asString
            return rawText.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
        }
        
        if (element.isJsonArray) {
            val arr = element.asJsonArray
            for (i in 0 until arr.size()) {
                val item = arr.get(i)
                if (item.isJsonPrimitive) {
                    list.add(item.asString.trim())
                } else if (item.isJsonObject) {
                    val obj = item.asJsonObject
                    val text = obj.get("text")?.asString 
                        ?: obj.get("body")?.asString 
                        ?: obj.get("content")?.asString
                        ?: obj.get("paragraph")?.asString
                    if (text != null) {
                        list.add(text.trim())
                    }
                }
            }
            return if (list.isNotEmpty()) list else null
        }
        
        if (element.isJsonObject) {
            val obj = element.asJsonObject
            val text = obj.get("text")?.asString 
                ?: obj.get("body")?.asString 
                ?: obj.get("content")?.asString
                ?: obj.get("paragraph")?.asString
            if (text != null) {
                return text.split("\n\n").map { it.trim() }.filter { it.isNotEmpty() }
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    return null
}
