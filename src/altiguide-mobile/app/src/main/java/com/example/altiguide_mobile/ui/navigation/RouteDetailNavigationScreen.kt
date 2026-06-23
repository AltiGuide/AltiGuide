package com.example.altiguide_mobile.ui.navigation

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.altiguide_mobile.R
import com.example.altiguide_mobile.data.model.RouteModel
import com.example.altiguide_mobile.ui.home.HomeViewModel
import com.example.altiguide_mobile.util.UiState
import kotlinx.coroutines.delay
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailNavigationScreen(
    route: RouteModel,
    viewModel: HomeViewModel,
    downloadedRouteIds: MutableList<String>,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val weatherState by viewModel.activeWeatherState.collectAsState()
    var activeTab by remember { mutableStateOf(0) } // 0: Detail Jalur, 1: Analisis Cuaca
    var showOfflineMapDialog by remember { mutableStateOf(false) }

    // Map download states
    var isDownloadingMap by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var downloadMaxProgress by remember { mutableStateOf(100) }
    var currentDownloadJob by remember { mutableStateOf<CacheManager.CacheManagerTask?>(null) }

    // Active Navigation states
    var isNavigating by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var userAltitude by remember { mutableStateOf(0.0) }
    var currentWaypointIndex by remember { mutableStateOf(0) }
    var phoneAzimuth by remember { mutableStateOf(0f) }
    var lastLocationUpdateTime by remember { mutableStateOf(0L) }

    // MapView reference for imperative camera control
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }

    val myLocationOverlay = remember(mapViewRef.value) {
        mapViewRef.value?.let { mv ->
            MyLocationNewOverlay(GpsMyLocationProvider(context), mv).apply {
                val arrowBitmap = buildDirectionArrowBitmap(context)
                setPersonIcon(arrowBitmap)
                setDirectionIcon(arrowBitmap)
                setPersonAnchor(0.5f, 0.5f)
                setDirectionAnchor(0.5f, 0.5f)
            }
        }
    }

    var hasCenteredOnStart by remember { mutableStateOf(false) }

    // Location manager
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val locationListener = remember {
        object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                if (location.latitude != 0.0 && location.longitude != 0.0) {
                    userLocation = GeoPoint(location.latitude, location.longitude)
                    userAltitude = location.altitude
                    lastLocationUpdateTime = System.currentTimeMillis()
                }
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
                        if (it.latitude != 0.0 && it.longitude != 0.0) {
                            userLocation = GeoPoint(it.latitude, it.longitude)
                            userAltitude = it.altitude
                        }
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

    // Auto-center camera ONCE when navigation starts and a location is acquired
    LaunchedEffect(isNavigating) {
        if (!isNavigating) {
            hasCenteredOnStart = false
        }
    }

    LaunchedEffect(userLocation, isNavigating) {
        if (isNavigating && userLocation != null && !hasCenteredOnStart) {
            mapViewRef.value?.controller?.animateTo(userLocation)
            mapViewRef.value?.controller?.setZoom(16.0)
            hasCenteredOnStart = true
        }
    }

    LaunchedEffect(isNavigating, myLocationOverlay) {
        val overlay = myLocationOverlay
        if (overlay != null) {
            if (isNavigating) {
                overlay.enableMyLocation()
                overlay.enableFollowLocation()
            } else {
                overlay.disableMyLocation()
                overlay.disableFollowLocation()
            }
        }
    }

    // Calculate distance and bearing to current target waypoint
    val currentTargetWaypoint = route.waypoints?.getOrNull(currentWaypointIndex)
    val distanceToTarget = remember(userLocation, currentTargetWaypoint) {
        if (userLocation != null && userLocation!!.latitude != 0.0 && userLocation!!.longitude != 0.0 && currentTargetWaypoint != null) {
            val results = FloatArray(1)
            val targetLat = currentTargetWaypoint.latitude ?: route.latitude ?: trackPoints.firstOrNull()?.latitude ?: 0.0
            val targetLon = currentTargetWaypoint.longitude ?: route.longitude ?: trackPoints.firstOrNull()?.longitude ?: 0.0
            Location.distanceBetween(
                userLocation!!.latitude, userLocation!!.longitude,
                targetLat, targetLon,
                results
            )
            results[0]
        } else {
            -1f
        }
    }

    val bearingToTarget = remember(userLocation, currentTargetWaypoint) {
        if (userLocation != null && userLocation!!.latitude != 0.0 && userLocation!!.longitude != 0.0 && currentTargetWaypoint != null) {
            val userLoc = Location("").apply {
                latitude = userLocation!!.latitude
                longitude = userLocation!!.longitude
            }
            val targetLat = currentTargetWaypoint.latitude ?: route.latitude ?: trackPoints.firstOrNull()?.latitude ?: 0.0
            val targetLon = currentTargetWaypoint.longitude ?: route.longitude ?: trackPoints.firstOrNull()?.longitude ?: 0.0
            val targetLoc = Location("").apply {
                latitude = targetLat
                longitude = targetLon
            }
            userLoc.bearingTo(targetLoc)
        } else {
            0f
        }
    }

    var accumulatedRotation by remember { mutableStateOf(0f) }
    LaunchedEffect(bearingToTarget, phoneAzimuth) {
        val target = (bearingToTarget - phoneAzimuth + 360) % 360
        var diff = target - (accumulatedRotation % 360)
        if (diff > 180) {
            diff -= 360
        } else if (diff < -180) {
            diff += 360
        }
        accumulatedRotation += diff
    }

    val arrowRotation by animateFloatAsState(
        targetValue = accumulatedRotation,
        animationSpec = androidx.compose.animation.core.spring(
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "smooth_compass"
    )

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

    var isSearchingGPS by remember { mutableStateOf(true) }
    LaunchedEffect(isNavigating, userLocation, lastLocationUpdateTime) {
        if (!isNavigating) {
            isSearchingGPS = true
            return@LaunchedEffect
        }
        while (isNavigating) {
            isSearchingGPS = (userLocation == null) || (System.currentTimeMillis() - lastLocationUpdateTime > 8000L)
            delay(1000L)
        }
    }

    val cacheManager = remember(mapViewRef.value) {
        mapViewRef.value?.let { CacheManager(it) }
    }

    val onDownloadClick = {
        val mv = mapViewRef.value
        if (mv == null) {
            Toast.makeText(context, "Harap tunggu sampai peta selesai dimuat.", Toast.LENGTH_SHORT).show()
        } else if (cacheManager != null) {
            val bbox = if (trackPoints.isNotEmpty()) {
                val minLat = trackPoints.minOf { it.latitude }
                val maxLat = trackPoints.maxOf { it.latitude }
                val minLon = trackPoints.minOf { it.longitude }
                val maxLon = trackPoints.maxOf { it.longitude }
                BoundingBox(maxLat + 0.01, maxLon + 0.01, minLat - 0.01, minLon - 0.01)
            } else {
                val lat = route.latitude ?: -7.4556
                val lon = route.longitude ?: 110.4389
                BoundingBox(lat + 0.03, lon + 0.03, lat - 0.03, lon - 0.03)
            }

            downloadProgress = 0
            downloadMaxProgress = 100
            isDownloadingMap = true

            currentDownloadJob = cacheManager.downloadAreaAsync(
                context,
                bbox,
                11,
                16,
                object : CacheManager.CacheManagerCallback {
                    override fun onTaskComplete() {
                        isDownloadingMap = false
                        currentDownloadJob = null
                        Toast.makeText(context, "Peta rute ${route.name} berhasil diunduh offline!", Toast.LENGTH_LONG).show()
                        
                        // Mark route as offline downloaded in local storage
                        val prefs = context.getSharedPreferences("altiguide_offline", Context.MODE_PRIVATE)
                        val downloadedSet = prefs.getStringSet("downloaded_routes", emptySet())?.toMutableSet() ?: mutableSetOf()
                        downloadedSet.add(route.id.toString())
                        prefs.edit().putStringSet("downloaded_routes", downloadedSet).apply()

                        // Update Compose state so it updates UI instantly
                        if (!downloadedRouteIds.contains(route.id.toString())) {
                            downloadedRouteIds.add(route.id.toString())
                        }
                    }

                    override fun onTaskFailed(errors: Int) {
                        isDownloadingMap = false
                        currentDownloadJob = null
                        if (errors > 0) {
                            Toast.makeText(context, "Gagal mengunduh peta ($errors error). Silakan coba lagi.", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun updateProgress(progress: Int, currentZoomLevel: Int, zoomMin: Int, zoomMax: Int) {
                        downloadProgress = progress
                    }

                    override fun downloadStarted() {}

                    override fun setPossibleTilesInArea(total: Int) {
                        downloadMaxProgress = if (total > 0) total else 100
                    }
                }
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
                            if (isSearchingGPS) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF9AB00).copy(alpha = 0.15f))
                                        .border(1.dp, Color(0xFFF9AB00).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⚠️ Mencari sinyal GPS... Pastikan berada di bawah langit terbuka.",
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFE37400),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            // Header: Target Pos & Stop Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
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
                                        color = AltiDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
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
                                if (distanceToTarget < 0f) {
                                    "-"
                                } else if (distanceToTarget >= 1000f) {
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
                                            Column {
                                                Text(
                                                    text = "Ketinggian Saya: $altitudeText",
                                                    fontFamily = Montserrat,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = AltiDark
                                                )
                                                Text(
                                                    text = "*Akurasi ketinggian GPS ±50m",
                                                    fontFamily = Montserrat,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 9.sp,
                                                    color = AltiDark.copy(alpha = 0.5f)
                                                )
                                            }
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

                                    // Manual Pos Selector Row
                                    HorizontalDivider(color = AltiDark.copy(alpha = 0.1f), thickness = 0.5.dp)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = {
                                                if (currentWaypointIndex > 0) {
                                                    currentWaypointIndex--
                                                }
                                            },
                                            enabled = currentWaypointIndex > 0
                                        ) {
                                            Text(
                                                text = "◀ Pos Seb",
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (currentWaypointIndex > 0) AltiDark else AltiDark.copy(alpha = 0.3f)
                                            )
                                        }

                                        Text(
                                            text = "Pos ${currentWaypointIndex + 1} dari ${route.waypoints?.size ?: 0}",
                                            fontFamily = Montserrat,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = AltiDark
                                        )

                                        TextButton(
                                            onClick = {
                                                val waypointsCount = route.waypoints?.size ?: 0
                                                if (currentWaypointIndex < waypointsCount - 1) {
                                                    currentWaypointIndex++
                                                }
                                            },
                                            enabled = currentWaypointIndex < (route.waypoints?.size ?: 0) - 1
                                        ) {
                                            Text(
                                                text = "Pos Next ▶",
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (currentWaypointIndex < (route.waypoints?.size ?: 0) - 1) AltiDark else AltiDark.copy(alpha = 0.3f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (activeTab == 0) {
                        // ── TAB 1: DETAIL JALUR ──────────────────────────────────────
                        // Navigation & Download Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
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
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("Start Navigation", fontSize = 13.sp, fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    showOfflineMapDialog = true
                                },
                                shape = RoundedCornerShape(22.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AltiMedium),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("Unduh Peta", fontSize = 13.sp, fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color.White)
                            }
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
                                    val calendar = Calendar.getInstance()
                                    val todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
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

                                    val calendar = Calendar.getInstance()
                                    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                                    val todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1
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
        Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(innerPadding)) {
            // 1. OSMDroid Map View (offline-capable OpenStreetMap)
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(CustomOfflineTileSource)
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
                    val overlay = myLocationOverlay
                    if (isNavigating && overlay != null) {
                        if (!mapView.overlays.contains(overlay)) {
                            mapView.overlays.add(overlay)
                        }
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

    // Offline Map Preview Dialog (Confirmation to Download)
    if (showOfflineMapDialog) {
        AlertDialog(
            onDismissRequest = { showOfflineMapDialog = false },
            title = {
                Text(
                    text = "Unduh Peta Offline ${route.name}",
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
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Apakah Anda ingin mengunduh peta rute ini untuk penggunaan offline? Setelah diunduh, Anda dapat memperbesar dan menggeser peta rute secara lengkap tanpa koneksi internet sama sekali saat di gunung.",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = AltiDark.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showOfflineMapDialog = false
                        onDownloadClick()
                    }
                ) {
                    Text("Unduh", fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = AltiDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOfflineMapDialog = false }) {
                    Text("Batal", fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                }
            },
            containerColor = Color(0xFFE3E9CD)
        )
    }

    // Download Progress Dialog
    if (isDownloadingMap) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss by touching outside */ },
            title = {
                Text(
                    text = "Mengunduh Peta Offline",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AltiDark
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    val percentage = if (downloadMaxProgress > 0) (downloadProgress * 100 / downloadMaxProgress).coerceIn(0, 100) else 0
                    Text(
                        text = "Mengunduh petak peta... $percentage% ($downloadProgress/$downloadMaxProgress)",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = AltiDark.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val progressFloat = if (downloadMaxProgress > 0) downloadProgress.toFloat() / downloadMaxProgress.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = { progressFloat.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = AltiMedium,
                        trackColor = AltiDark.copy(alpha = 0.1f)
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = {
                        currentDownloadJob?.cancel(true)
                        isDownloadingMap = false
                        currentDownloadJob = null
                        Toast.makeText(context, "Unduhan peta dibatalkan.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Batal", fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
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
