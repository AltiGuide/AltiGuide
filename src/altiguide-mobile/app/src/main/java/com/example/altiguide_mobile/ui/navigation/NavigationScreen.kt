package com.example.altiguide_mobile.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

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

private fun parseTrackCoordinates(element: com.google.gson.JsonElement?): List<LatLng> {
    if (element == null || !element.isJsonArray) return emptyList()
    val list = mutableListOf<LatLng>()
    try {
        val array = element.asJsonArray
        for (i in 0 until array.size()) {
            val pt = array.get(i).asJsonArray
            val lat = pt.get(0).asDouble
            val lng = pt.get(1).asDouble
            list.add(LatLng(lat, lng))
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

    // Parse coordinates and define camera state
    val trackPoints = remember(route) { parseTrackCoordinates(route.trackCoordinates) }
    val routeCenter = remember(trackPoints) {
        if (trackPoints.isNotEmpty()) trackPoints[trackPoints.size / 2]
        else LatLng(route.latitude ?: -7.4556, route.longitude ?: 110.4389)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(routeCenter, 13f)
    }

    // Auto-fit path bounds on map load
    LaunchedEffect(trackPoints) {
        if (trackPoints.isNotEmpty()) {
            try {
                val builder = LatLngBounds.Builder()
                trackPoints.forEach { builder.include(it) }
                val bounds = builder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 80),
                    1000
                )
            } catch (e: Exception) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(routeCenter, 13f)
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

                // Tab Selector (Detail Jalur vs Analisis Cuaca)
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

                // Scrollable tab content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (activeTab == 0) {
                        // ── TAB 1: DETAIL JALUR ──────────────────────────────────────
                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Direction Button
                            Button(
                                onClick = {
                                    try {
                                        val gmmIntentUri = Uri.parse("google.navigation:q=${route.latitude},${route.longitude}")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Google Maps tidak terpasang di perangkat.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AltiDark),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("Direction", fontSize = 11.sp, fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            // Start Button
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Navigasi pendakian dimulai untuk ${route.name}!", Toast.LENGTH_LONG).show()
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AltiDark),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Text("Start", fontSize = 11.sp, fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            // Download Offline Maps Button
                            Button(
                                onClick = { showOfflineMapDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AltiDark),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier.weight(1.2f).height(44.dp)
                            ) {
                                Text("Offline Maps", fontSize = 10.sp, fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Color.White)
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

                        when (val weatherStateObj = weatherState) {
                            is UiState.Loading -> {
                                // Shimmer Loader Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(AltiDark.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AltiDark)
                                }
                            }
                            is UiState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.Red.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Gagal mengambil data cuaca",
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            is UiState.Success -> {
                                val weatherData = weatherStateObj.data.data
                                val currentWeather = weatherData?.current_weather
                                val tempVal = currentWeather?.temperature ?: 24.0
                                val windspeed = currentWeather?.windspeed ?: 6.0
                                val code = currentWeather?.weathercode ?: 0

                                // Weather main gradient card
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color(0xFF5E6DF8), Color(0xFF3B24D9))
                                            )
                                        )
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Hari Ini",
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = getWeatherDescription(code),
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(
                                                text = "${tempVal.toInt()}°C",
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 38.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Real feel ${(tempVal - 2).toInt()}°C",
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }

                                        // Custom Sun/Cloud illustration
                                        WeatherIllustration(modifier = Modifier.size(90.dp))
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Sub-chips row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        WeatherDetailMiniChip(label = "Wind", value = "${windspeed.toInt()} km/h", emoji = "🍃", modifier = Modifier.weight(1f))
                                        WeatherDetailMiniChip(label = "Temp", value = "${tempVal.toInt()}°C", emoji = "🌡️", modifier = Modifier.weight(1f))
                                        WeatherDetailMiniChip(label = "Humidity", value = "51%", emoji = "💧", modifier = Modifier.weight(1.1f))
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Hourly forecast
                                Text("Hourly Forecast", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AltiDark)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val hourly = weatherData?.hourly
                                    val limit = minOf(hourly?.temperature_2m?.size ?: 0, 5)
                                    for (i in 0 until limit) {
                                        val temp = hourly?.temperature_2m?.get(i) ?: 20.0
                                        val hCode = hourly?.weathercode?.get(i) ?: 0
                                        val hourLabel = when (i) {
                                            0 -> "05:00 AM"
                                            1 -> "06:00 AM"
                                            2 -> "07:00 AM"
                                            3 -> "08:00 AM"
                                            else -> "09:00 AM"
                                        }
                                        HourlyForecastItem(
                                            time = hourLabel,
                                            temp = "${temp.toInt()}°",
                                            code = hCode,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Tomorrow card
                                Text("Besok", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AltiDark)
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = AltiDark.copy(alpha = 0.95f))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("⛈️", fontSize = 16.sp)
                                            }
                                            Column {
                                                Text("Tomorrow", fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                                val tmrCode = weatherData?.daily?.weathercode?.firstOrNull() ?: 0
                                                Text(getWeatherDescription(tmrCode), fontFamily = Montserrat, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                            }
                                        }

                                        val maxTemp = weatherData?.daily?.temperature_2m_max?.firstOrNull() ?: 24.0
                                        val minTemp = weatherData?.daily?.temperature_2m_min?.firstOrNull() ?: 16.0
                                        Text(
                                            text = "^ ${maxTemp.toInt()}° . v ${minTemp.toInt()}°",
                                            fontFamily = Montserrat,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(30.dp))
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
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // 1. Google Maps View
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = true
                )
            ) {
                if (trackPoints.isNotEmpty()) {
                    Polyline(
                        points = trackPoints,
                        color = Color(0xFFEA4335), // Red line for trail path
                        width = 8f
                    )
                }

                // Waypoint Markers
                val waypoints = route.waypoints ?: emptyList()
                waypoints.forEachIndexed { index, pos ->
                    val posLatLng = LatLng(pos.latitude ?: 0.0, pos.longitude ?: 0.0)
                    if (pos.latitude != null && pos.longitude != null && pos.latitude != 0.0) {
                        Marker(
                            state = MarkerState(position = posLatLng),
                            title = pos.name,
                            snippet = "${pos.altitude ?: 0} mdpl",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                if (index == 0) BitmapDescriptorFactory.HUE_GREEN
                                else if (index == (waypoints.size - 1)) BitmapDescriptorFactory.HUE_RED
                                else BitmapDescriptorFactory.HUE_AZURE
                            )
                        )
                    }
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
                        .clickable {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                cameraPositionState.position.target,
                                cameraPositionState.position.zoom + 1f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AltiDark)
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                                cameraPositionState.position.target,
                                cameraPositionState.position.zoom - 1f
                            )
                        },
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

@Composable
private fun WeatherDetailMiniChip(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFD0E1FD).copy(alpha = 0.25f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White, maxLines = 1)
            Text(label, fontFamily = Montserrat, fontWeight = FontWeight.Medium, fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 1)
        }
    }
}

@Composable
private fun HourlyForecastItem(
    time: String,
    temp: String,
    code: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AltiDark.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, AltiDark.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Little dynamic mini canvas icon
            MiniWeatherIcon(code = code, modifier = Modifier.size(24.dp))
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(temp, fontFamily = Montserrat, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AltiDark)
                Text(time, fontFamily = Montserrat, fontWeight = FontWeight.Medium, fontSize = 8.sp, color = AltiDark.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun MiniWeatherIcon(code: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        when (code) {
            0 -> { // Cerah
                drawCircle(
                    color = Color(0xFFFBC02D),
                    radius = size.minDimension / 3f
                )
            }
            1, 2, 3 -> { // Berawan
                drawCircle(
                    color = Color(0xFFFBC02D),
                    radius = size.minDimension / 5f,
                    center = center.copy(x = center.x + 3f, y = center.y - 3f)
                )
                drawCircle(
                    color = Color.White,
                    radius = size.minDimension / 3.5f,
                    center = center.copy(x = center.x - 2f, y = center.y + 2f)
                )
            }
            45, 48 -> { // Kabut
                val w = size.width
                val h = size.height
                drawLine(Color.Gray, start = center.copy(x = w * 0.2f, y = h * 0.3f), end = center.copy(x = w * 0.8f, y = h * 0.3f), strokeWidth = 2f)
                drawLine(Color.Gray, start = center.copy(x = w * 0.15f, y = h * 0.5f), end = center.copy(x = w * 0.85f, y = h * 0.5f), strokeWidth = 2f)
                drawLine(Color.Gray, start = center.copy(x = w * 0.2f, y = h * 0.7f), end = center.copy(x = w * 0.8f, y = h * 0.7f), strokeWidth = 2f)
            }
            else -> { // Awan / Hujan / Overcast
                drawCircle(
                    color = Color.White,
                    radius = size.minDimension / 3.5f
                )
            }
        }
    }
}

@Composable
private fun WeatherIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Golden Sun
        Canvas(modifier = Modifier.size(54.dp).align(Alignment.TopEnd).offset(x = (-8).dp, y = 8.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF176), Color(0xFFFBC02D))
                ),
                radius = size.minDimension / 2
            )
        }
        // White Cloud
        Canvas(modifier = Modifier.size(72.dp).align(Alignment.BottomStart)) {
            val path = android.graphics.Path().apply {
                val w = size.width
                val h = size.height
                moveTo(w * 0.2f, h * 0.75f)
                cubicTo(w * 0.05f, h * 0.75f, w * 0.05f, h * 0.5f, w * 0.2f, h * 0.5f)
                cubicTo(w * 0.2f, h * 0.25f, w * 0.5f, h * 0.25f, w * 0.5f, h * 0.4f)
                cubicTo(w * 0.65f, h * 0.2f, w * 0.9f, h * 0.3f, w * 0.85f, h * 0.5f)
                cubicTo(w * 0.98f, h * 0.5f, w * 0.98f, h * 0.75f, w * 0.85f, h * 0.75f)
                close()
            }
            drawPath(
                path = path.asComposePath(),
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFCFD8DC))
                )
            )
        }
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
