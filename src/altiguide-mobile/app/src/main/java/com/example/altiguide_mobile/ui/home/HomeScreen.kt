package com.example.altiguide_mobile.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.horizontalScroll
import kotlinx.coroutines.launch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.altiguide_mobile.R
import androidx.compose.foundation.lazy.LazyColumn
import com.example.altiguide_mobile.data.model.MountainModel
import com.example.altiguide_mobile.data.model.TransactionModel
import com.example.altiguide_mobile.data.model.UserModel
import com.example.altiguide_mobile.ui.profile.EditProfileScreen
import com.example.altiguide_mobile.ui.profile.ProfileScreen
import com.example.altiguide_mobile.ui.profile.ProfileViewModel
import com.example.altiguide_mobile.ui.navigation.NavigationScreen
import com.example.altiguide_mobile.util.UiState
import androidx.compose.foundation.BorderStroke
import coil.compose.SubcomposeAsyncImage
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.text.SimpleDateFormat
import java.util.Locale

// ── Montserrat font family ──────────────────────────────────────────────────
private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular,  FontWeight.Normal),
    Font(R.font.montserrat_medium,   FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold,     FontWeight.Bold)
)

// ── Design color tokens ─────────────────────────────────────────────────────
private val AltiDark      = Color(0xFF20341B)
private val AltiMedium    = Color(0xFF859763)
private val AltiLight     = Color(0xFFC3CB92)
private val SearchBg      = Color(0xFFEAF0D8)

// ── Vector icons ────────────────────────────────────────────────────────────
private val IconSearch: ImageVector get() = ImageVector.Builder(
    name = "Search", defaultWidth = 18.dp, defaultHeight = 18.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color(0xFF859763))) {
        moveTo(15.5f, 14f); lineTo(14.71f, 14f); lineTo(14.43f, 13.73f)
        curveTo(15.63f, 12.33f, 16.25f, 10.42f, 15.91f, 8.39f)
        curveTo(15.44f, 5.61f, 13.12f, 3.39f, 10.32f, 3.05f)
        curveTo(6.09f, 2.53f, 2.53f, 6.09f, 3.05f, 10.32f)
        curveTo(3.39f, 13.12f, 5.61f, 15.44f, 8.39f, 15.91f)
        curveTo(10.42f, 16.25f, 12.33f, 15.63f, 13.73f, 14.43f)
        lineTo(14f, 14.71f); lineTo(14f, 15.5f)
        lineTo(18.25f, 19.75f)
        curveTo(18.66f, 20.16f, 19.33f, 20.16f, 19.74f, 19.75f)
        curveTo(20.15f, 19.34f, 20.15f, 18.67f, 19.74f, 18.26f); close()
        moveTo(9.5f, 14f)
        curveTo(7.01f, 14f, 5f, 11.99f, 5f, 9.5f)
        curveTo(5f, 7.01f, 7.01f, 5f, 9.5f, 5f)
        curveTo(11.99f, 5f, 14f, 7.01f, 14f, 9.5f)
        curveTo(14f, 11.99f, 11.99f, 14f, 9.5f, 14f); close()
    }
}.build()

private val IconArrow: ImageVector get() = ImageVector.Builder(
    name = "Arrow", defaultWidth = 16.dp, defaultHeight = 16.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
        moveTo(7f, 17f); lineTo(15.59f, 8.41f); lineTo(15.59f, 15f)
        lineTo(17f, 15f); lineTo(17f, 6f); lineTo(8f, 6f)
        lineTo(8f, 7.41f); lineTo(14.59f, 7.41f); lineTo(6f, 16f); close()
    }
}.build()

private val IconClock: ImageVector get() = ImageVector.Builder(
    name = "Clock", defaultWidth = 13.dp, defaultHeight = 13.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
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
    name = "Route", defaultWidth = 13.dp, defaultHeight = 13.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
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

private val IconStar: ImageVector get() = ImageVector.Builder(
    name = "Star", defaultWidth = 13.dp, defaultHeight = 13.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.Transparent), stroke = SolidColor(Color.White), strokeLineWidth = 2f) {
        moveTo(12f, 2f)
        lineTo(15.09f, 8.26f); lineTo(22f, 9.27f); lineTo(17f, 14.14f)
        lineTo(18.18f, 21.02f); lineTo(12f, 17.77f); lineTo(5.82f, 21.02f)
        lineTo(7f, 14.14f); lineTo(2f, 9.27f); lineTo(8.91f, 8.26f); close()
    }
}.build()

// Bottom nav icons
private val IconHome: ImageVector get() = ImageVector.Builder(
    name = "Home", defaultWidth = 22.dp, defaultHeight = 22.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
        moveTo(10f, 20f); lineTo(10f, 14f); lineTo(14f, 14f); lineTo(14f, 20f)
        lineTo(19f, 20f); lineTo(19f, 12f); lineTo(22f, 12f); lineTo(12f, 3f)
        lineTo(2f, 12f); lineTo(5f, 12f); lineTo(5f, 20f); close()
    }
}.build()

private val IconNav: ImageVector get() = ImageVector.Builder(
    name = "Nav", defaultWidth = 22.dp, defaultHeight = 22.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiLight)) {
        moveTo(12f, 2f)
        lineTo(4.5f, 20.29f); lineTo(5.21f, 21f); lineTo(12f, 18f)
        lineTo(18.79f, 21f); lineTo(19.5f, 20.29f); close()
    }
}.build()

private val IconBook: ImageVector get() = ImageVector.Builder(
    name = "Book", defaultWidth = 22.dp, defaultHeight = 22.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiLight)) {
        moveTo(19f, 3f); lineTo(14.82f, 3f)
        curveTo(14.4f, 1.84f, 13.3f, 1f, 12f, 1f)
        curveTo(10.7f, 1f, 9.6f, 1.84f, 9.18f, 3f)
        lineTo(5f, 3f)
        curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
        lineTo(3f, 19f)
        curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
        lineTo(19f, 21f)
        curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
        lineTo(21f, 5f)
        curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f); close()
        moveTo(12f, 3f)
        curveTo(12.55f, 3f, 13f, 3.45f, 13f, 4f)
        curveTo(13f, 4.55f, 12.55f, 5f, 12f, 5f)
        curveTo(11.45f, 5f, 11f, 4.55f, 11f, 4f)
        curveTo(11f, 3.45f, 11.45f, 3f, 12f, 3f); close()
        moveTo(7f, 7f); lineTo(17f, 7f); lineTo(17f, 9f); lineTo(7f, 9f); close()
        moveTo(7f, 11f); lineTo(17f, 11f); lineTo(17f, 13f); lineTo(7f, 13f); close()
        moveTo(7f, 15f); lineTo(14f, 15f); lineTo(14f, 17f); lineTo(7f, 17f); close()
    }
}.build()

private val IconBack: ImageVector get() = ImageVector.Builder(
    name = "Back", defaultWidth = 16.dp, defaultHeight = 16.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
        moveTo(20f, 11f); horizontalLineTo(7.83f)
        lineTo(13.42f, 5.41f); lineTo(12f, 4f)
        lineTo(4f, 12f); lineTo(12f, 20f)
        lineTo(13.41f, 18.59f); lineTo(7.83f, 13f)
        horizontalLineTo(20f); verticalLineTo(11f); close()
    }
}.build()

private val IconPerson: ImageVector get() = ImageVector.Builder(
    name = "Person", defaultWidth = 22.dp, defaultHeight = 22.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiLight)) {
        moveTo(12f, 12f)
        curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f)
        curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f)
        curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f)
        curveTo(8f, 10.21f, 9.79f, 12f, 12f, 12f); close()
        moveTo(12f, 14f)
        curveTo(9.33f, 14f, 4f, 15.34f, 4f, 18f)
        lineTo(4f, 20f); lineTo(20f, 20f); lineTo(20f, 18f)
        curveTo(20f, 15.34f, 14.67f, 14f, 12f, 14f); close()
    }
}.build()

private val IconCalendar: ImageVector get() = ImageVector.Builder(
    name = "Calendar", defaultWidth = 14.dp, defaultHeight = 14.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiDark)) {
        moveTo(19f, 4f)
        horizontalLineToRelative(-1f)
        verticalLineTo(2f)
        horizontalLineToRelative(-2f)
        verticalLineToRelative(2f)
        horizontalLineTo(8f)
        verticalLineTo(2f)
        horizontalLineTo(6f)
        verticalLineToRelative(2f)
        horizontalLineTo(5f)
        curveTo(3.89f, 4f, 3f, 4.9f, 3f, 6f)
        verticalLineToRelative(14f)
        curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f)
        horizontalLineToRelative(14f)
        curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f)
        verticalLineTo(6f)
        curveTo(21f, 4.9f, 20.1f, 4f, 19f, 4f)
        close()
        moveTo(19f, 20f)
        horizontalLineTo(5f)
        verticalLineTo(10f)
        horizontalLineToRelative(14f)
        verticalLineTo(20f)
        close()
        moveTo(19f, 8f)
        horizontalLineTo(5f)
        verticalLineTo(6f)
        horizontalLineToRelative(14f)
        verticalLineTo(8f)
        close()
    }
}.build()

private val IconPin: ImageVector get() = ImageVector.Builder(
    name = "Pin", defaultWidth = 14.dp, defaultHeight = 14.dp,
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

// ── HomeScreen composable ───────────────────────────────────────────────────
@Composable
fun HomeScreen(
    userName: String = "Diva",
    onLogout: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val mountainsState by viewModel.mountainsState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showEditProfile by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = {
        (mountainsState as? UiState.Success<List<MountainModel>>)?.data?.size ?: 0
    })
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileState by profileViewModel.profileState.collectAsState()
    val updateState by profileViewModel.updateState.collectAsState()
    val activeWeatherState by viewModel.activeWeatherState.collectAsState()
    var selectedMountainForArticle by remember { mutableStateOf<MountainModel?>(null) }
    var selectedTicket by remember { mutableStateOf<TransactionModel?>(null) }
    val mountainDetailState by viewModel.mountainDetailState.collectAsState()

    LaunchedEffect(pagerState.currentPage, mountainsState) {
        val state = mountainsState
        if (state is UiState.Success<List<MountainModel>>) {
            val mountains = state.data
            if (pagerState.currentPage in mountains.indices) {
                val activeMountain = mountains[pagerState.currentPage]
                val lat = activeMountain.latitude ?: -7.4497
                val lon = activeMountain.longitude ?: 110.4381
                val elevation = activeMountain.altitude?.toDouble()
                viewModel.fetchWeatherForMountain(lat, lon, elevation)
            }
        }
    }

    val displayName = remember(profileState) {
        val state = profileState
        if (state is UiState.Success<UserModel>) {
            state.data.name.trim().split("\\s+".toRegex()).firstOrNull() ?: userName
        } else {
            userName
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_main),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        when (selectedTab) {
            0 -> {
                if (selectedMountainForArticle != null) {
                    val selectedId = selectedMountainForArticle!!.id
                    LaunchedEffect(selectedId) {
                        viewModel.fetchMountainDetail(selectedId)
                    }

                    when (val detailState = mountainDetailState) {
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AltiDark)
                            }
                        }
                        is UiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Gagal memuat detail: ${detailState.message}", color = AltiDark, fontFamily = Montserrat)
                                    Button(
                                        onClick = { viewModel.fetchMountainDetail(selectedId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AltiDark)
                                    ) {
                                        Text("Coba Lagi", color = Color.White, fontFamily = Montserrat)
                                    }
                                    TextButton(onClick = {
                                        selectedMountainForArticle = null
                                        viewModel.clearMountainDetail()
                                    }) {
                                        Text("Kembali", color = AltiDark, fontFamily = Montserrat)
                                    }
                                }
                            }
                        }
                        is UiState.Success<MountainModel> -> {
                            MountainArticleScreen(
                                mountain = detailState.data,
                                onBack = {
                                    selectedMountainForArticle = null
                                    viewModel.clearMountainDetail()
                                }
                            )
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AltiDark)
                            }
                        }
                    }
                } else {
                    ExploreTabContent(
                        displayName = displayName,
                        profileState = profileState,
                        mountainsState = mountainsState,
                        activeWeatherState = activeWeatherState,
                        pagerState = pagerState,
                        scope = scope,
                        onAvatarClick = {
                            selectedTab = 3
                            showEditProfile = true
                        },
                        onMountainSelected = { mountain ->
                            selectedMountainForArticle = mountain
                        },
                        onNavigateToRoute = { route ->
                            viewModel.selectRoute(route)
                            selectedTab = 1
                        },
                        viewModel = viewModel
                    )
                }
            }
            1 -> {
                NavigationScreen(viewModel = viewModel)
            }
            2 -> {
                val bookingsState by viewModel.bookingsState.collectAsState()

                LaunchedEffect(Unit) {
                    viewModel.fetchBookings()
                }

                if (selectedTicket != null) {
                    TicketDetailScreen(
                        transaction = selectedTicket!!,
                        onBack = { selectedTicket = null }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 90.dp)
                    ) {
                        Text(
                            text = "My Bookings",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = AltiDark,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 12.dp)
                        )

                        when (val state = bookingsState) {
                            is UiState.Loading -> {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AltiDark)
                                }
                            }
                            is UiState.Error -> {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                                        Text(state.message, color = AltiDark, fontFamily = Montserrat, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { viewModel.fetchBookings() },
                                            colors = ButtonDefaults.buttonColors(containerColor = AltiDark)
                                        ) {
                                            Text("Coba Lagi", color = Color.White, fontFamily = Montserrat)
                                        }
                                    }
                                }
                            }
                            is UiState.Success<List<TransactionModel>> -> {
                                val transactions = state.data
                                if (transactions.isEmpty()) {
                                    EmptyBookingsState()
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(transactions.size) { index ->
                                            val transaction = transactions[index]
                                            TicketCard(
                                                transaction = transaction,
                                                onClick = { selectedTicket = transaction }
                                            )
                                        }
                                    }
                                }
                            }
                            else -> {
                                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AltiDark)
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                if (showEditProfile) {
                    val user = (profileState as? UiState.Success<UserModel>)?.data
                    if (user != null) {
                        EditProfileScreen(
                            user = user,
                            updateState = updateState,
                            onBack = { showEditProfile = false },
                            onSave = { name, email, phone, age, address, emergencyContact, nik, password ->
                                profileViewModel.updateProfile(
                                    name = name,
                                    email = email,
                                    phone_number = phone,
                                    age = age,
                                    address = address,
                                    emergency_contact = emergencyContact,
                                    nik = nik,
                                    password = password
                                )
                            },
                            onResetUpdate = { profileViewModel.resetUpdateState() }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AltiDark)
                        }
                    }
                } else {
                    ProfileScreen(
                        profileState = profileState,
                        onEditProfile = { showEditProfile = true },
                        onLogout = onLogout,
                        onRetry = { profileViewModel.fetchProfile() }
                    )
                }
            }
        }

        // ── Bottom Navigation ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(66.dp)
                .clip(RoundedCornerShape(33.dp))
                .background(AltiDark.copy(alpha = 0.93f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(IconHome,   "Explore",    selectedTab == 0) { selectedTab = 0 }
                NavItem(IconNav,    "Navigation", selectedTab == 1) { selectedTab = 1 }
                NavItem(IconBook,   "Bookings",   selectedTab == 2) { selectedTab = 2 }
                NavItem(IconPerson, "Profile",    selectedTab == 3) { selectedTab = 3 }
            }
        }
    }
}

// ── Mountain Card ───────────────────────────────────────────────────────────
@Composable
internal fun MountainCard(
    mountain: MountainModel,
    cardWidth: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    // Format altitude
    val altitudeText = mountain.altitude?.let { "${formatNumber(it)} mdpl" } ?: ""
    // Format from first route (data langsung dari API)
    val firstRoute = mountain.routes?.firstOrNull()
    val durationText = firstRoute?.duration_hours?.let {
        val h = (it / 60).toInt()
        val hEnd = h + 1.5
        "$h-$hEnd hr"
    } ?: "-"
    val distanceText = firstRoute?.distance_km?.let {
        if (it == it.toLong().toDouble()) "${it.toLong()} km"
        else "$it km"
    } ?: "-"
    val levelText = firstRoute?.difficulty?.replaceFirstChar { it.uppercaseChar() } ?: "-"
    val imageResId = getMountainDrawable(mountain.name)

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(310.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = mountain.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay (top & bottom, clear in middle)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f  to AltiDark.copy(alpha = 0.60f),
                            0.40f to Color.Transparent,
                            0.65f to Color.Transparent,
                            1.0f  to AltiDark.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Top: Name + Altitude + Arrow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(start = 16.dp, end = 14.dp, top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mountain.name,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = altitudeText,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.90f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconArrow,
                    contentDescription = "Detail",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Bottom: Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatChip(icon = IconClock, value = durationText, label = "Duration")
            StatChip(icon = IconRoute, value = distanceText, label = "Distance")
            StatChip(icon = IconStar,  value = levelText,    label = "Level")
        }
    }
}

// ── Stat Chip ───────────────────────────────────────────────────────────────
@Composable
private fun StatChip(icon: ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = value,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.White
            )
            Text(
                text = label,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }
    }
}

// ── Bottom nav item ─────────────────────────────────────────────────────────
@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else AltiLight,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = if (isSelected) Color.White else AltiLight,
            maxLines = 1,
            softWrap = false
        )
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────
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

internal data class WeatherMock(val emoji: String, val tempMax: Int, val tempMin: Int)

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

internal data class HourlyMock(val time: String, val emoji: String, val temp: Int)

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

internal fun formatHourlyTime(isoTime: String): String {
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

private fun formatIndonesianDate(dateStr: String?): String {
    if (dateStr == null) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateStr.substring(0, 10))
        if (date != null) {
            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
            outputFormat.format(date)
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

private fun calculateDurationDays(startDateStr: String?, endDateStr: String?): String {
    if (startDateStr == null || endDateStr == null) return "2 - 3 Days"
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = LocalDate.parse(startDateStr.substring(0, 10), formatter)
        val endDate = LocalDate.parse(endDateStr.substring(0, 10), formatter)
        val days = ChronoUnit.DAYS.between(startDate, endDate) + 1
        if (days <= 1) "1 Day" else "$days Days"
    } catch (e: Exception) {
        "2 - 3 Days"
    }
}

@Composable
private fun TicketDetailRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AltiDark,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = AltiDark.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TicketCard(
    transaction: TransactionModel,
    onClick: () -> Unit
) {
    val session = transaction.hikingSession
    val route = session?.route
    val mountainName = route?.mountain?.name ?: "Unknown Mountain"
    val routeName = route?.name ?: "Unknown Route"
    val shortRouteName = remember(routeName) {
        if (routeName.contains(" via ")) {
            "Jalur " + routeName.substringAfter(" via ")
        } else {
            routeName
        }
    }
    val rawDate = session?.start_date
    val dateText = remember(rawDate) { formatIndonesianDate(rawDate) }
    val durationText = remember(session) {
        calculateDurationDays(session?.start_date, session?.end_date)
    }
    val imageRes = getMountainDrawable(mountainName)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        // Background Mountain Image
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = mountainName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay layout matching Figma dimensions and linear gradient stops
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(167.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White.copy(alpha = 0.85f),
                            0.35f to Color.White.copy(alpha = 0.60f),
                            0.65f to Color.White.copy(alpha = 0.35f),
                            1.0f to Color.White.copy(alpha = 0.00f)
                        )
                    ),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        bottomStart = 20.dp,
                        topEnd = 30.dp,
                        bottomEnd = 30.dp
                    )
                )
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Mountain Title
                Text(
                    text = mountainName,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = AltiDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Info Rows (Calendar, Pin, Clock)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TicketDetailRow(icon = IconCalendar, text = dateText)
                    TicketDetailRow(icon = IconPin, text = shortRouteName)
                    TicketDetailRow(icon = IconClock, text = durationText)
                }
            }
        }
    }
}

@Composable
private fun TicketDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(40) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(AltiDark.copy(alpha = 0.15f))
            )
        }
    }
}

@Composable
private fun TicketInfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = AltiDark.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = AltiDark
        )
    }
}

@Composable
private fun EmptyBookingsState() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AltiDark.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎫",
                    fontSize = 40.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Belum Ada Tiket",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AltiDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tiket yang sudah dibayar dan diverifikasi\nakan muncul di sini.",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = AltiDark.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun TicketDetailScreen(
    transaction: TransactionModel,
    onBack: () -> Unit
) {
    val session = transaction.hikingSession
    val route = session?.route
    val mountainName = route?.mountain?.name ?: "Unknown Mountain"
    val routeName = route?.name ?: "Unknown Route"
    val dateText = session?.start_date ?: "-"
    val groupName = session?.group_name ?: "-"
    val memberCount = session?.members?.size ?: 1
    val imageRes = getMountainDrawable(mountainName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 24.dp, top = 52.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AltiDark)
                    .clickable { onBack() },
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
            Text(
                text = "Detail Tiket",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AltiDark
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ticket card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
            ) {
                // Image header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = mountainName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        AltiDark.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                    // Overlay text on image
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = mountainName,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = routeName,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    // Verified badge top-right
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF4CAF50).copy(alpha = 0.9f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Terverifikasi",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info section
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = transaction.orderId,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = AltiDark.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "Rp ${formatNumber(transaction.grossAmount)}",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AltiDark
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DetailInfoItem(label = "Tanggal", value = dateText)
                        DetailInfoItem(label = "Kelompok", value = groupName)
                        DetailInfoItem(label = "Pendaki", value = "$memberCount Orang")
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Dashed divider
                TicketDivider()
                Spacer(modifier = Modifier.height(20.dp))

                // QR Code section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "E-Ticket",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AltiDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tunjukkan QR code ini pada petugas basecamp",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = AltiDark.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code generated from booking code
                    val qrContent = transaction.orderId.ifEmpty { transaction.id }
                    val qrBitmap = remember(qrContent) {
                        runCatching {
                            val writer = QRCodeWriter()
                            val bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 400, 400)
                            val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565)
                            for (x in 0 until 400) {
                                for (y in 0 until 400) {
                                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                                }
                            }
                            bitmap
                        }.getOrNull()
                    }
                    val qrImageBitmap = remember(qrBitmap) { qrBitmap?.asImageBitmap() }

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, AltiDark.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrImageBitmap != null) {
                            Image(
                                bitmap = qrImageBitmap,
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "QR",
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = AltiDark.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gagal memuat QR",
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = AltiDark.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ID: ${transaction.id}",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        color = AltiDark.copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Ticket notch decorations
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-10).dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3E9CD))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 10.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE3E9CD))
            )
        }

        Spacer(modifier = Modifier.height(90.dp))
    }
}

@Composable
private fun DetailInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = AltiDark.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = AltiDark
        )
    }
}

internal fun getAvatarModelForUser(user: UserModel?): Any? {
    if (user == null) return null
    val base64 = user.image
    if (!base64.isNullOrEmpty() && base64.startsWith("data:image")) {
        try {
            val cleanString = if (base64.contains(",")) base64.substring(base64.indexOf(",") + 1) else base64
            val decodedBytes = android.util.Base64.decode(cleanString, android.util.Base64.DEFAULT)
            return android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            // fallback
        }
    }
    val url = if (!user.avatar_url.isNullOrEmpty()) user.avatar_url else user.image
    if (url.isNullOrEmpty()) return null
    return url.replace("localhost", "10.0.2.2").replace("127.0.0.1", "10.0.2.2")
}




