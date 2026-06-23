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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
// ── OSMDroid imports ──────────────────────────────────────────────────────────
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.util.BoundingBox
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
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
    val context = LocalContext.current
    val mountainsState by viewModel.mountainsState.collectAsState()
    val sharedRoute by viewModel.selectedRoute.collectAsState()
    
    // Remember expanded states for each mountain ID
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }
    // Persist LazyColumn scroll state across route detail navigation
    val listState = rememberLazyListState()

    // Persistent set of downloaded route IDs for offline indicators
    val downloadedRouteIds = remember {
        val prefs = context.getSharedPreferences("altiguide_offline", Context.MODE_PRIVATE)
        val set = prefs.getStringSet("downloaded_routes", emptySet()) ?: emptySet()
        mutableStateListOf<String>().apply { addAll(set) }
    }

    var selectedComingSoonRoute by remember { mutableStateOf<RouteModel?>(null) }
    LaunchedEffect(sharedRoute) {
        selectedComingSoonRoute = sharedRoute
    }

    if (selectedComingSoonRoute != null) {
        BackHandler {
            viewModel.selectRoute(null)
            selectedComingSoonRoute = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = if (selectedComingSoonRoute != null) 0.dp else 90.dp)
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
                downloadedRouteIds = downloadedRouteIds,
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
                        state = listState,
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
                                downloadedRouteIds = downloadedRouteIds,
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
    downloadedRouteIds: List<String>,
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
                        isDownloaded = false,
                        onClick = {}
                    )
                } else {
                    // Sort so that downloaded routes are at the top of the list
                    val sortedRoutes = routes.sortedByDescending { downloadedRouteIds.contains(it.id.toString()) }
                    sortedRoutes.forEach { route ->
                        RoutePill(
                            name = route.name,
                            isDownloaded = downloadedRouteIds.contains(route.id.toString()),
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
    isDownloaded: Boolean,
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
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        if (isDownloaded) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF34A853).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF34A853), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Downloaded",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = Color(0xFF34A853)
                )
            }
        }
    }
}

