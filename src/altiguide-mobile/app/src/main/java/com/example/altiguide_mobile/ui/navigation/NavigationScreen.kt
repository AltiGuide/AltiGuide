package com.example.altiguide_mobile.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlin.OptIn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.altiguide_mobile.R
import com.example.altiguide_mobile.data.model.MountainModel
import com.example.altiguide_mobile.data.model.RouteModel
import com.example.altiguide_mobile.ui.home.HomeViewModel
import com.example.altiguide_mobile.util.UiState
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
// ── OSMDroid imports ──────────────────────────────────────────────────────────
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat
import android.graphics.Color as AndroidColor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// ── Design color tokens ─────────────────────────────────────────────────────
private val AltiDark      = Color(0xFF20341B)
private val AltiMedium    = Color(0xFF859763)
private val AltiLight     = Color(0xFFC3CB92)

// ── Montserrat font family ──────────────────────────────────────────────────
private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular,  FontWeight.Normal),
    Font(R.font.montserrat_medium,   FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold,     FontWeight.Bold)
)

// ── Custom Vector Icons ──────────────────────────────────────────────────────
private val IconChevronDown: ImageVector get() = ImageVector.Builder(
    name = "ChevronDown", defaultWidth = 14.dp, defaultHeight = 14.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
        moveTo(7.41f, 8.59f)
        lineTo(12f, 13.17f)
        lineTo(16.59f, 8.59f)
        lineTo(18f, 10f)
        lineTo(12f, 16f)
        lineTo(6f, 10f)
        close()
    }
}.build()

private val IconCloudAlert: ImageVector get() = ImageVector.Builder(
    name = "CloudAlert", defaultWidth = 80.dp, defaultHeight = 80.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color(0xFF8E9B7B))) {
        moveTo(19.35f, 10.04f)
        curveTo(18.67f, 6.59f, 15.64f, 4f, 12f, 4f)
        curveTo(9.11f, 4f, 6.6f, 5.64f, 5.35f, 8.04f)
        curveTo(2.34f, 8.36f, 0f, 10.91f, 0f, 14f)
        curveTo(0f, 17.31f, 2.69f, 20f, 6f, 20f)
        lineTo(19f, 20f)
        curveTo(21.76f, 20f, 24f, 17.76f, 24f, 15f)
        curveTo(24f, 12.36f, 21.95f, 10.22f, 19.35f, 10.04f)
        close()
        moveTo(19f, 18f)
        lineTo(6f, 18f)
        curveTo(3.79f, 18f, 2f, 16.21f, 2f, 14f)
        curveTo(2f, 11.95f, 3.53f, 10.24f, 5.56f, 10.03f)
        lineTo(6.63f, 9.92f)
        lineTo(7.13f, 8.97f)
        curveTo(8.08f, 7.14f, 9.94f, 6f, 12f, 6f)
        curveTo(14.89f, 6f, 17.4f, 7.86f, 17.85f, 10.72f)
        lineTo(18.06f, 12f)
        lineTo(19.3f, 12.08f)
        curveTo(20.81f, 12.18f, 22f, 13.45f, 22f, 15f)
        curveTo(22f, 16.65f, 20.65f, 18f, 19f, 18f)
        close()
    }
    path(fill = SolidColor(Color(0xFF8E9B7B))) {
        moveTo(11f, 9f)
        horizontalLineTo(13f)
        verticalLineTo(13f)
        horizontalLineTo(11f)
        close()
        moveTo(11f, 15f)
        horizontalLineTo(13f)
        verticalLineTo(17f)
        horizontalLineTo(11f)
        close()
    }
}.build()

private val IconBack: ImageVector get() = ImageVector.Builder(
    name = "Back", defaultWidth = 16.dp, defaultHeight = 16.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
        moveTo(20f, 11f)
        horizontalLineTo(7.83f)
        lineTo(13.42f, 5.41f)
        lineTo(12f, 4f)
        lineTo(4f, 12f)
        lineTo(12f, 20f)
        lineTo(13.41f, 18.59f)
        lineTo(7.83f, 13f)
        horizontalLineTo(20f)
        verticalLineTo(11f)
        close()
    }
}.build()

@Composable
fun NavigationScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val mountainsState by viewModel.mountainsState.collectAsState()
    val sharedRoute by viewModel.selectedRoute.collectAsState()
    
    // Remember expanded states for each mountain ID
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    var selectedComingSoonRoute by remember { mutableStateOf<RouteModel?>(null) }
    LaunchedEffect(sharedRoute) {
        selectedComingSoonRoute = sharedRoute
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
    ) {
        if (selectedComingSoonRoute != null) {
            // ── Header with Back option when route is selected ──────────────────
            val route = selectedComingSoonRoute!!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 52.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AltiDark)
                        .clickable { 
                            viewModel.selectRoute(null)
                            selectedComingSoonRoute = null 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "${route.mountain?.name ?: "Gunung"} via ${route.name}",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AltiDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Kembali",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = AltiDark.copy(alpha = 0.6f)
                    )
                }
            }

            RouteDetailNavigationScreen(
                route = route,
                viewModel = viewModel,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                onBackClick = { 
                    viewModel.selectRoute(null)
                    selectedComingSoonRoute = null 
                }
            )
        } else {
            // ── Header (stays constant on top) ──────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 52.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Navigation",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = AltiDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Navigate through detailed mountain trails, elevation data, and official basecamp locations.",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = AltiDark.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
            // ── Mountain Route List Screen ──────────────────────────────────
            when (val state = mountainsState) {
                is UiState.Loading, is UiState.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AltiDark)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Gagal memuat data gunung",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AltiDark
                            )
                            Text(
                                text = state.message,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = AltiDark.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.fetchMountains() },
                                colors = ButtonDefaults.buttonColors(containerColor = AltiDark)
                            ) {
                                Text("Coba Lagi", color = Color.White, fontFamily = Montserrat)
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    val mountains = state.data
                    
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(mountains, key = { it.id }) { mountain ->
                            val isExpanded = expandedStates[mountain.id] ?: false
                            MountainRouteCard(
                                mountain = mountain,
                                isExpanded = isExpanded,
                                onHeaderClick = {
                                    expandedStates[mountain.id] = !isExpanded
                                },
                                onRouteClick = { route ->
                                    selectedComingSoonRoute = route
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MountainRouteCard(
    mountain: MountainModel,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    onRouteClick: (RouteModel) -> Unit
) {
    val imageResId = getMountainDrawable(mountain.name)
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AltiDark.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
    ) {
        // Mountain Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = mountain.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Subtle dark overlay gradient on image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                AltiDark.copy(alpha = 0.4f)
                            )
                        )
                    )
            )
        }

        // Title and Route List Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onHeaderClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mountain.name,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "Route List",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            // Expand/Collapse Chevron Button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconChevronDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(rotationAngle)
                )
            }
        }

        // Routes list dropdown (Expanded Section)
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val routes = mountain.routes ?: emptyList()
                if (routes.isEmpty()) {
                    // Fallback when no routes exist in DB for this mountain
                    RoutePill(
                        name = "Jalur Coming Soon",
                        onClick = {}
                    )
                } else {
                    routes.forEach { route ->
                        RoutePill(
                            name = route.name,
                            onClick = { onRouteClick(route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutePill(
    name: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF859763).copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

private fun parseTrackCoordinates(element: com.google.gson.JsonElement?): List<GeoPoint> {
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
        android.util.Log.e("TrackCoordinates", "Error parsing track coordinates", e)
    }
    return list
}

private fun getOfflineMapDrawable(name: String): Int {
    return R.drawable.img_offlinemaps
}

private fun getWeatherDescription(code: Int): String {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteDetailNavigationScreen(
    route: RouteModel,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val weatherState by viewModel.activeWeatherState.collectAsState()
    var activeTab by remember { mutableStateOf(0) } // 0: Detail Jalur, 1: Analisis Cuaca
    var showOfflineMapDialog by remember { mutableStateOf(false) }

    // Active Navigation states
    var isNavigating by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var userAltitude by remember { mutableStateOf(0.0) }
    var currentWaypointIndex by remember { mutableStateOf(0) }
    var phoneAzimuth by remember { mutableStateOf(0f) }

    // MapView reference for imperative camera control
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    // Location manager
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val locationListener = remember {
        object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                userLocation = GeoPoint(location.latitude, location.longitude)
                userAltitude = location.altitude
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
    }

    // Sensor manager for Compass Azimuth
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magnetometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    val sensorEventListener = remember {
        object : SensorEventListener {
            private var lastGravity = FloatArray(3)
            private var lastGeomagnetic = FloatArray(3)
            private var hasGravity = false
            private var hasGeomagnetic = false

            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    System.arraycopy(event.values, 0, lastGravity, 0, event.values.size)
                    hasGravity = true
                } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    System.arraycopy(event.values, 0, lastGeomagnetic, 0, event.values.size)
                    hasGeomagnetic = true
                }
                if (hasGravity && hasGeomagnetic) {
                    val r = FloatArray(9)
                    val i = FloatArray(9)
                    if (SensorManager.getRotationMatrix(r, i, lastGravity, lastGeomagnetic)) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(r, orientation)
                        val azimuthRad = orientation[0]
                        var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                        azimuthDeg = (azimuthDeg + 360) % 360
                        phoneAzimuth = azimuthDeg
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    // Register Sensors
    DisposableEffect(isNavigating) {
        if (isNavigating) {
            accelerometer?.let { sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI) }
            magnetometer?.let { sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI) }
        }
        onDispose { sensorManager.unregisterListener(sensorEventListener) }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) isNavigating = true
        else Toast.makeText(context, "Izin lokasi diperlukan untuk navigasi offline.", Toast.LENGTH_SHORT).show()
    }

    // Register Location GPS
    DisposableEffect(isNavigating) {
        if (isNavigating) {
            val fine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (fine || coarse) {
                try {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 2f, locationListener)
                    val lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    lastKnown?.let {
                        userLocation = GeoPoint(it.latitude, it.longitude)
                        userAltitude = it.altitude
                    }
                } catch (e: SecurityException) {
                    android.util.Log.e("OfflineNav", "Permission error requesting updates", e)
                } catch (e: Exception) {
                    android.util.Log.e("OfflineNav", "Error starting GPS updates", e)
                }
            }
        }
        onDispose { locationManager.removeUpdates(locationListener) }
    }

    // Parse coordinates from mountains.json assets (fully offline)
    val trackPoints = remember(route) { parseTrackCoordinates(route.trackCoordinates) }
    val routeCenter = remember(trackPoints) {
        if (trackPoints.isNotEmpty()) trackPoints[trackPoints.size / 2]
        else GeoPoint(route.latitude ?: -7.4556, route.longitude ?: 110.4389)
    }

    // Auto-fit path bounds on map load via MapView controller
    LaunchedEffect(trackPoints) {
        if (!isNavigating && trackPoints.isNotEmpty()) {
            mapViewRef.value?.let { mv ->
                // Compute bounding box center and zoom to fit
                val minLat = trackPoints.minOf { it.latitude }
                val maxLat = trackPoints.maxOf { it.latitude }
                val minLon = trackPoints.minOf { it.longitude }
                val maxLon = trackPoints.maxOf { it.longitude }
                val centerLat = (minLat + maxLat) / 2.0
                val centerLon = (minLon + maxLon) / 2.0
                mv.controller.setCenter(GeoPoint(centerLat, centerLon))
                // Calculate zoom level to fit the bounding box
                val latSpan = maxLat - minLat
                val lonSpan = maxLon - minLon
                val span = maxOf(latSpan, lonSpan)
                val zoom = when {
                    span < 0.01 -> 15.0
                    span < 0.05 -> 13.0
                    span < 0.1  -> 12.0
                    span < 0.5  -> 10.0
                    else        -> 9.0
                }
                mv.controller.setZoom(zoom)
            }
        }
    }

    // Auto-center camera on user location when navigating
    LaunchedEffect(userLocation, isNavigating) {
        if (isNavigating && userLocation != null) {
            mapViewRef.value?.controller?.animateTo(userLocation)
            mapViewRef.value?.controller?.setZoom(15.0)
        }
    }

    // Calculate distance and bearing to current target waypoint
    val currentTargetWaypoint = route.waypoints?.getOrNull(currentWaypointIndex)
    val distanceToTarget = remember(userLocation, currentTargetWaypoint) {
        if (userLocation != null && currentTargetWaypoint != null) {
            val results = FloatArray(1)
            Location.distanceBetween(
                userLocation!!.latitude, userLocation!!.longitude,
                currentTargetWaypoint.latitude ?: 0.0, currentTargetWaypoint.longitude ?: 0.0,
                results
            )
            results[0]
        } else {
            0f
        }
    }

    val bearingToTarget = remember(userLocation, currentTargetWaypoint) {
        if (userLocation != null && currentTargetWaypoint != null) {
            val userLoc = Location("").apply {
                latitude = userLocation!!.latitude
                longitude = userLocation!!.longitude
            }
            val targetLoc = Location("").apply {
                latitude = currentTargetWaypoint.latitude ?: 0.0
                longitude = currentTargetWaypoint.longitude ?: 0.0
            }
            userLoc.bearingTo(targetLoc)
        } else {
            0f
        }
    }

    val arrowRotation = remember(bearingToTarget, phoneAzimuth) {
        (bearingToTarget - phoneAzimuth + 360) % 360
    }

    // Automatic target waypoint progression
    LaunchedEffect(distanceToTarget, isNavigating) {
        if (isNavigating && distanceToTarget > 0f && distanceToTarget < 15f) {
            val waypointsCount = route.waypoints?.size ?: 0
            if (currentWaypointIndex < waypointsCount - 1) {
                Toast.makeText(context, "Sampai di ${currentTargetWaypoint?.name ?: "Pos"}! Berlanjut ke pos berikutnya.", Toast.LENGTH_SHORT).show()
                currentWaypointIndex++
            } else {
                Toast.makeText(context, "Selamat! Anda telah sampai di puncak!", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Trigger Weather fetch if tab changes to 1 (Cuaca)
    LaunchedEffect(activeTab) {
        if (activeTab == 1) {
            viewModel.fetchWeatherForMountain(
                latitude = route.latitude ?: 0.0,
                longitude = route.longitude ?: 0.0,
                elevation = route.waypoints?.lastOrNull()?.altitude?.toDouble()
            )
        }
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = 220.dp,
        sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                color = AltiDark.copy(alpha = 0.4f)
            )
        },
        sheetContainerColor = Color(0xFFE3E9CD),
        containerColor = Color.Black,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFE3E9CD), Color(0xFFC3CB92))
                        )
                    )
                    .padding(horizontal = 24.dp)
            ) {

                // Tab Selector (Detail Jalur vs Analisis Cuaca) - Hidden when navigating
                if (!isNavigating) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(AltiDark.copy(alpha = 0.08f))
                            .border(1.dp, AltiDark.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (activeTab == 0) AltiDark else Color.Transparent)
                                .clickable { activeTab = 0 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Detail Jalur",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (activeTab == 0) Color.White else AltiDark
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (activeTab == 1) AltiDark else Color.Transparent)
                                .clickable { activeTab = 1 },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Analisis Cuaca",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (activeTab == 1) Color.White else AltiDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Scrollable tab content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp)
                ) {
                    if (isNavigating) {
                        // ── MODE NAVIGASI AKTIF IN-APP ──
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Header: Target Pos & Stop Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Navigasi Aktif",
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AltiDark.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = "Menuju: ${currentTargetWaypoint?.name ?: "Puncak"}",
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = AltiDark
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFEA4335).copy(alpha = 0.1f))
                                        .border(1.dp, Color(0xFFEA4335).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .clickable { 
                                            isNavigating = false
                                            userLocation = null
                                            currentWaypointIndex = 0
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Stop",
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFFEA4335)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Compass Dial
                            val distanceText = remember(distanceToTarget) {
                                if (distanceToTarget >= 1000f) {
                                    String.format("%.1f km", distanceToTarget / 1000f)
                                } else {
                                    "${distanceToTarget.toInt()} m"
                                }
                            }
                            OfflineCompassDial(
                                arrowRotation = arrowRotation,
                                distanceText = distanceText
                            )

                            // Target Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = AltiDark.copy(alpha = 0.05f)),
                                border = BorderStroke(1.dp, AltiDark.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Live Elevation Progress
                                    val targetAltitude = currentTargetWaypoint?.altitude?.toDouble() ?: 0.0
                                    val currentAltitude = userAltitude
                                    val altitudeText = if (currentAltitude > 0.0) "${currentAltitude.toInt()} mdpl" else "-"
                                    val targetAltitudeText = if (targetAltitude > 0.0) "${targetAltitude.toInt()} mdpl" else "-"
                                    
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Ketinggian Saya: $altitudeText",
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = AltiDark
                                            )
                                            Text(
                                                text = "Target: $targetAltitudeText",
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = AltiDark
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        val startAltitude = route.waypoints?.firstOrNull()?.altitude?.toDouble() ?: 1000.0
                                        val progress = remember(currentAltitude, startAltitude, targetAltitude) {
                                            if (targetAltitude > startAltitude && currentAltitude >= startAltitude) {
                                                ((currentAltitude - startAltitude) / (targetAltitude - startAltitude)).coerceIn(0.0, 1.0).toFloat()
                                            } else {
                                                0f
                                            }
                                        }
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = AltiMedium,
                                            trackColor = AltiDark.copy(alpha = 0.1f)
                                        )
                                    }

                                    // Waypoint description
                                    HorizontalDivider(color = AltiDark.copy(alpha = 0.1f), thickness = 0.5.dp)
                                    Text(
                                        text = currentTargetWaypoint?.description ?: "Menuju Pos target pendakian.",
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = AltiDark.copy(alpha = 0.7f),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    } else if (activeTab == 0) {
                        // ── TAB 1: DETAIL JALUR ──────────────────────────────────────
                        // Start Button
                        Button(
                            onClick = {
                                val fine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val coarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (fine || coarse) {
                                    isNavigating = true
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AltiDark),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("Start Navigation", fontSize = 13.sp, fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Description
                        Text("Description", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AltiDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = route.route_info?.logistics_description ?: "Jalur pendakian ini menyajikan pemandangan alam yang asri dan memukau.",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = AltiDark.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Legenda Pos
                        Text("Post Information", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AltiDark)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFD0E1FD)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("💧", fontSize = 12.sp)
                                }
                                Text("Water Source", fontFamily = Montserrat, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = AltiDark)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFE3E9CD)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⛺", fontSize = 12.sp)
                                }
                                Text("Campsite", fontFamily = Montserrat, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = AltiDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Waypoints Timeline List
                        val waypoints = route.waypoints ?: emptyList()
                        waypoints.forEachIndexed { idx, wp ->
                            WaypointTimelineItem(
                                waypoint = wp,
                                isFirst = idx == 0,
                                isLast = idx == (waypoints.size - 1)
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                    } else {
                        // ── TAB 2: ANALISIS CUACA ────────────────────────────────────
                        val lastWaypoint = route.waypoints?.lastOrNull()
                        val durationMinutes = route.waypoints?.sumOf { it.estimated_time_minutes ?: 0 } ?: 0
                        val durationHoursText = if (durationMinutes > 0) {
                            val h = durationMinutes / 60
                            val m = durationMinutes % 60
                            if (m > 0) "± $h Jam $m Menit" else "± $h Jam"
                        } else "± 7 - 8 Jam"

                        // Peak & Duration details
                        Text(
                            text = "📍 ${lastWaypoint?.name ?: "Puncak"} (${lastWaypoint?.altitude ?: 3000} mdpl)",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AltiDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⏳ Total Estimasi Perjalanan: $durationHoursText",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = AltiDark.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Weather", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AltiDark)
                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.background_frame_rekap_cuaca),
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = ContentScale.FillBounds
                            )

                            when (val weatherStateObj = weatherState) {
                                is UiState.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color.White)
                                    }
                                }
                                is UiState.Error -> {
                                    val calendar = java.util.Calendar.getInstance()
                                    val todayIndex = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
                                    val daysOfWeek = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                                    val daysList = List(7) { i ->
                                        daysOfWeek[(todayIndex + i) % 7]
                                    }
                                    val hourlyMocks = getHourlyMockList(todayIndex)

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        // ── Hourly Section ──
                                        Text(
                                            text = "Hourly Forecast",
                                            fontFamily = Montserrat,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            hourlyMocks.forEach { mock ->
                                                Column(
                                                    modifier = Modifier.width(60.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = mock.time,
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        fontFamily = Montserrat,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 9.sp
                                                    )
                                                    Text(
                                                        text = mock.emoji,
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        text = "${mock.temp}°",
                                                        fontFamily = Montserrat,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 10.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // ── Daily Section ──
                                        Text(
                                            text = "Weekly Forecast",
                                            fontFamily = Montserrat,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            daysList.forEachIndexed { dayIndex, dayName ->
                                                val mock = getWeatherMock(route.id, dayIndex)
                                                Column(
                                                    modifier = Modifier.width(60.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = dayName,
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        fontFamily = Montserrat,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 9.sp
                                                    )
                                                    Text(
                                                        text = mock.emoji,
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        text = "${mock.tempMax}° - ${mock.tempMin}°",
                                                        fontFamily = Montserrat,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 10.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                is UiState.Success -> {
                                    val weatherData = weatherStateObj.data.data
                                    val daily = weatherData?.daily
                                    val hourly = weatherData?.hourly

                                    val calendar = java.util.Calendar.getInstance()
                                    val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                                    val todayIndex = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
                                    val daysOfWeek = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                                    val daysList = List(7) { i ->
                                        daysOfWeek[(todayIndex + i) % 7]
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        // ── Hourly Section ──
                                        Text(
                                            text = "Hourly Forecast",
                                            fontFamily = Montserrat,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            for (i in 0..5) {
                                                val targetHourIndex = (currentHour + i) % 24
                                                val timeString = hourly?.time?.getOrNull(targetHourIndex) ?: ""
                                                val temp = hourly?.temperature_2m?.getOrNull(targetHourIndex)?.toInt() ?: 20
                                                val weatherCode = hourly?.weathercode?.getOrNull(targetHourIndex) ?: 0

                                                val emoji = when (weatherCode) {
                                                    0 -> "☀️"
                                                    1, 2, 3 -> "⛅"
                                                    45, 48 -> "🌫️"
                                                    51, 53, 55, 61, 63, 65, 80, 81, 82 -> "🌧️"
                                                    71, 73, 75 -> "❄️"
                                                    95, 96, 99 -> "⛈️"
                                                    else -> "⛅"
                                                }

                                                val displayTime = if (timeString.isNotEmpty()) formatHourlyTime(timeString) else "${targetHourIndex.toString().padStart(2, '0')}:00"

                                                Column(
                                                    modifier = Modifier.width(60.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = displayTime,
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        fontFamily = Montserrat,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 9.sp
                                                    )
                                                    Text(
                                                        text = emoji,
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        text = "${temp}°",
                                                        fontFamily = Montserrat,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 10.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // ── Daily Section ──
                                        Text(
                                            text = "Weekly Forecast",
                                            fontFamily = Montserrat,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            daysList.forEachIndexed { dayIndex, dayName ->
                                                val weatherCode = daily?.weathercode?.getOrNull(dayIndex) ?: 0
                                                val tempMax = daily?.temperature_2m_max?.getOrNull(dayIndex)?.toInt() ?: 20
                                                val tempMin = daily?.temperature_2m_min?.getOrNull(dayIndex)?.toInt() ?: 14

                                                val emoji = when (weatherCode) {
                                                    0 -> "☀️"
                                                    1, 2, 3 -> "⛅"
                                                    45, 48 -> "🌫️"
                                                    51, 53, 55, 61, 63, 65, 80, 81, 82 -> "🌧️"
                                                    71, 73, 75 -> "❄️"
                                                    95, 96, 99 -> "⛈️"
                                                    else -> "⛅"
                                                }

                                                Column(
                                                    modifier = Modifier.width(60.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Text(
                                                        text = dayName,
                                                        color = Color.White.copy(alpha = 0.9f),
                                                        fontFamily = Montserrat,
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 9.sp
                                                    )
                                                    Text(
                                                        text = emoji,
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        text = "${tempMax}° - ${tempMin}°",
                                                        fontFamily = Montserrat,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 10.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = AltiDark)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // 1. OSMDroid Map View (offline-capable OpenStreetMap)
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        setBuiltInZoomControls(false)
                        // Allow data connection for tile download (auto-cached after first load)
                        setUseDataConnection(true)
                        // Set initial zoom and center
                        controller.setZoom(13.0)
                        controller.setCenter(routeCenter)
                        mapViewRef.value = this
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    // ── Draw track polyline (from mountains.json, 100% offline) ──
                    if (trackPoints.isNotEmpty()) {
                        val polyline = Polyline(mapView).apply {
                            setPoints(trackPoints)
                            outlinePaint.color = AndroidColor.parseColor("#EA4335") // red trail
                            outlinePaint.strokeWidth = 10f
                            outlinePaint.isAntiAlias = true
                        }
                        mapView.overlays.add(polyline)
                    }

                    // ── Draw waypoint markers ──
                    val waypoints = route.waypoints ?: emptyList()
                    waypoints.forEachIndexed { index, pos ->
                        if (pos.latitude != null && pos.longitude != null && pos.latitude != 0.0) {
                            val markerColor = when {
                                index == 0                    -> AndroidColor.parseColor("#34A853") // green = start
                                index == waypoints.size - 1   -> AndroidColor.parseColor("#EA4335") // red = summit
                                else                          -> AndroidColor.parseColor("#4285F4") // blue = intermediate
                            }
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(pos.latitude, pos.longitude)
                                title = pos.name
                                snippet = "${pos.altitude ?: 0} mdpl"
                                // Build a tinted circle bitmap as marker icon
                                icon = buildColoredMarkerIcon(context, markerColor)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            mapView.overlays.add(marker)
                        }
                    }

                    // ── Live user location overlay (when navigating) ──
                    if (isNavigating) {
                        val myLocationOverlay = MyLocationNewOverlay(
                            GpsMyLocationProvider(context), mapView
                        ).apply {
                            enableMyLocation()
                            enableFollowLocation()
                        }
                        mapView.overlays.add(myLocationOverlay)
                    }

                    mapView.invalidate()
                }
            )

            // Lifecycle management for OSMDroid MapView
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

            // 2. Floating Zoom Controls
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable { mapViewRef.value?.controller?.zoomIn() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AltiDark)
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable { mapViewRef.value?.controller?.zoomOut() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AltiDark)
                }
            }
        }
    }

    // Offline Map Preview Dialog
    if (showOfflineMapDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineMapDialog = false },
            title = {
                Text(
                    text = "Peta Offline ${route.name}",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AltiDark
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = getOfflineMapDrawable(route.name)),
                        contentDescription = "Peta Offline",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Peta rute ini telah disimpan offline di perangkat Anda untuk navigasi tanpa koneksi internet.",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = AltiDark.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showOfflineMapDialog = false }) {
                    Text("Tutup", fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = AltiDark)
                }
            },
            containerColor = Color(0xFFE3E9CD)
        )
    }
}

@Composable
private fun WaypointTimelineItem(
    waypoint: com.example.altiguide_mobile.data.model.WaypointModel,
    isFirst: Boolean,
    isLast: Boolean
) {
    val context = LocalContext.current
    val hasCampsite = remember(waypoint) {
        waypoint.name.contains("camp", ignoreCase = true) ||
        waypoint.name.contains("pos 3", ignoreCase = true) ||
        waypoint.description?.contains("tenda", ignoreCase = true) == true ||
        waypoint.description?.contains("kemah", ignoreCase = true) == true ||
        waypoint.description?.contains("camp", ignoreCase = true) == true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Vertical Timeline line & Dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(16.dp)
                    .background(if (isFirst) Color.Transparent else AltiDark.copy(alpha = 0.5f))
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AltiDark)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(if (isLast) Color.Transparent else AltiDark.copy(alpha = 0.5f))
            )
        }

        // Pos details card
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${waypoint.name} (${waypoint.altitude ?: 0} mdpl)",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AltiDark
                )

                // Inline tags
                if (waypoint.has_water_source) {
                    Box(
                        modifier = Modifier.size(18.dp).clip(CircleShape).background(Color(0xFFD0E1FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💧", fontSize = 10.sp)
                    }
                }
                if (hasCampsite) {
                    Box(
                        modifier = Modifier.size(18.dp).clip(CircleShape).background(Color(0xFFE3E9CD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⛺", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = waypoint.description ?: "Trek pendakian Pos.",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = AltiDark.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private data class WeatherMock(val emoji: String, val tempMax: Int, val tempMin: Int)

private fun getWeatherMock(mountainIndex: Int, dayIndex: Int): WeatherMock {
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

private data class HourlyMock(val time: String, val emoji: String, val temp: Int)

private fun getHourlyMockList(dayIndex: Int): List<HourlyMock> {
    val times = listOf("05:00 AM", "06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM")
    val emojis = listOf("☀️", "⛅", "☁️", "🌧️", "⛈️", "⛅")
    val temps = listOf(22, 18, 16, 19, 23, 25)
    return List(6) { index ->
        val emojiShift = (dayIndex + index) % emojis.size
        val tempShift = temps[index] + (dayIndex % 3) - 1
        HourlyMock(times[index], emojis[emojiShift], tempShift)
    }
}

private fun formatHourlyTime(isoTime: String): String {
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

// ── Helpers ─────────────────────────────────────────────────────────────────
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

@Composable
private fun OfflineCompassDial(
    arrowRotation: Float,
    distanceText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(200.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow/shadow & background
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AltiDark.copy(alpha = 0.15f),
                            AltiDark.copy(alpha = 0.02f)
                        )
                    )
                )
                .border(2.dp, AltiDark.copy(alpha = 0.15f), CircleShape)
        )

        // The compass Canvas (outer ring, compass ticks, rotating needle)
        Canvas(
            modifier = Modifier
                .size(180.dp)
                .rotate(arrowRotation)
        ) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f

            // Draw small ticks/markings on the outer edge (every 30 degrees)
            for (angle in 0 until 360 step 30) {
                val isCardinal = angle % 90 == 0
                val tickLength = if (isCardinal) 12f else 6f
                val strokeWidth = if (isCardinal) 3f else 1.5f
                val tickColor = if (isCardinal) AltiDark else AltiDark.copy(alpha = 0.4f)
                
                val angleRad = Math.toRadians(angle.toDouble())
                val startX = (center.x + (radius - 12f) * Math.sin(angleRad)).toFloat()
                val startY = (center.y - (radius - 12f) * Math.cos(angleRad)).toFloat()
                val endX = (center.x + (radius - 12f - tickLength) * Math.sin(angleRad)).toFloat()
                val endY = (center.y - (radius - 12f - tickLength) * Math.cos(angleRad)).toFloat()
                
                drawLine(
                    color = tickColor,
                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = strokeWidth
                )
            }

            // Draw target needle (pointing North / top, since Canvas itself is rotated by arrowRotation)
            val needleWidth = 16f
            val needleLength = radius - 35f

            // Red half (North-facing pointer, points up)
            val northPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, center.y - needleLength) // Tip of needle
                lineTo(center.x + needleWidth / 2f, center.y) // Right joint
                lineTo(center.x, center.y - 4f) // Center indent
                close()
            }
            drawPath(
                path = northPath,
                color = Color(0xFFEA4335)
            )

            val northLeftPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, center.y - needleLength) // Tip of needle
                lineTo(center.x - needleWidth / 2f, center.y) // Left joint
                lineTo(center.x, center.y - 4f) // Center indent
                close()
            }
            drawPath(
                path = northLeftPath,
                color = Color(0xFFC5221F) // darker red for shadow effect
            )

            // South-facing pointer (points down, color coordinated to theme)
            val southPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, center.y + needleLength) // Tip of needle
                lineTo(center.x + needleWidth / 2f, center.y) // Right joint
                lineTo(center.x, center.y + 4f) // Center indent
                close()
            }
            drawPath(
                path = southPath,
                color = AltiMedium
            )

            val southLeftPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, center.y + needleLength) // Tip of needle
                lineTo(center.x - needleWidth / 2f, center.y) // Left joint
                lineTo(center.x, center.y + 4f) // Center indent
                close()
            }
            drawPath(
                path = southLeftPath,
                color = AltiDark
            )

            // Center pivot point
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = center
            )
            drawCircle(
                color = AltiDark,
                radius = 3f,
                center = center
            )
        }

        // Inner circle overlay for Distance text (unrotated, so text stays upright!)
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3E9CD))
                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Jarak",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = AltiDark.copy(alpha = 0.6f)
                )
                Text(
                    text = distanceText,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AltiDark
                )
            }
        }
    }
}

/**
 * Build a colored circle Drawable to use as an OSMDroid Marker icon.
 * Colors: green=start, red=summit, blue=intermediate waypoint.
 */
private fun buildColoredMarkerIcon(context: Context, color: Int): Drawable {
    val size = 48 // px
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    // White border
    paint.color = AndroidColor.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

    // Colored fill
    paint.color = color
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}
