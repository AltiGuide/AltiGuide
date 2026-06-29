package com.example.altiguide_mobile.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.altiguide_mobile.R
import com.example.altiguide_mobile.data.model.MountainModel
import com.example.altiguide_mobile.data.model.RouteModel
import com.example.altiguide_mobile.data.model.UserModel
import com.example.altiguide_mobile.util.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.altiguide_mobile.ui.mountain.MountainCard

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

@Composable
internal fun ExploreTabContent(
    displayName: String,
    profileState: UiState<UserModel>,
    mountainsState: UiState<List<MountainModel>>,
    activeWeatherState: UiState<com.example.altiguide_mobile.data.model.WeatherResponse>,
    pagerState: PagerState,
    scrollState: androidx.compose.foundation.ScrollState,
    scope: CoroutineScope,
    onAvatarClick: () -> Unit,
    onMountainSelected: (MountainModel) -> Unit,
    onNavigateToRoute: (com.example.altiguide_mobile.data.model.RouteModel) -> Unit,
    viewModel: HomeViewModel
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
    ) {
        // ── Header ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 52.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, $displayName",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AltiDark,
                    lineHeight = 24.sp
                )
                Text(
                    text = "Where would you like to go?",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = AltiDark.copy(alpha = 0.7f)
                )
            }


        }

        Spacer(modifier = Modifier.height(18.dp))

        // ── Search ─────────────────────────────────────────────
        val mountains = (mountainsState as? UiState.Success<List<MountainModel>>)?.data ?: emptyList()
        val filteredMountains = remember(searchQuery, mountains) {
            if (searchQuery.isBlank()) emptyList()
            else mountains.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
        val filteredRoutes = remember(searchQuery, mountains) {
            if (searchQuery.isBlank()) emptyList()
            else {
                mountains.flatMap { mountain ->
                    (mountain.routes ?: emptyList()).map { route ->
                        route.copy(mountain = mountain)
                    }
                }.filter { route ->
                    route.name.contains(searchQuery, ignoreCase = true) ||
                    (route.mountain?.name ?: "").contains(searchQuery, ignoreCase = true)
                }
            }
        }
        val showSuggestions = searchQuery.isNotBlank() && (filteredMountains.isNotEmpty() || filteredRoutes.isNotEmpty())

        // ── Search & Suggestions Container ──────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .border(1.dp, AltiMedium.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Cari gunung atau jalur pendakian...",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = AltiDark.copy(alpha = 0.4f)
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = AltiDark
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(AltiDark),
                    singleLine = true
                )
            }

            // Suggestions dropdown
            if (showSuggestions) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 52.dp)
                ) {
                    androidx.compose.foundation.shape.RoundedCornerShape(16.dp).let { shape ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(Color.White)
                                .border(1.dp, AltiMedium.copy(alpha = 0.2f), shape)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 280.dp)
                                    .padding(vertical = 8.dp)
                            ) {
                                // Mountains Group
                                if (filteredMountains.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "GUNUNG",
                                            fontFamily = Montserrat,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = AltiMedium,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                        )
                                    }
                                    items(filteredMountains) { mountain ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight()
                                                .clickable {
                                                    onMountainSelected(mountain)
                                                    searchQuery = ""
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(text = " ", fontSize = 14.sp)
                                            Column {
                                                Text(
                                                    text = mountain.name,
                                                    fontFamily = Montserrat,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = AltiDark,
                                                    softWrap = true
                                                )
                                                Text(
                                                    text = "${mountain.altitude ?: 3000} mdpl • ${mountain.province ?: "Jawa"}",
                                                    fontFamily = Montserrat,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 10.sp,
                                                    color = AltiDark.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                        if (mountain != filteredMountains.last() || filteredRoutes.isNotEmpty()) {
                                            HorizontalDivider(color = AltiMedium.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                }

                                // Routes Group
                                if (filteredRoutes.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "JALUR PENDAKIAN",
                                            fontFamily = Montserrat,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = AltiMedium,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                        )
                                    }
                                    items(filteredRoutes) { route ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .wrapContentHeight()
                                                .clickable {
                                                    onNavigateToRoute(route)
                                                    searchQuery = ""
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(text = "  ", fontSize = 14.sp)
                                            Column {
                                                Text(
                                                    text = "${route.mountain?.name ?: "Gunung"} via ${route.name}",
                                                    fontFamily = Montserrat,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = AltiDark,
                                                    softWrap = true
                                                )
                                                Text(
                                                    text = "Jalur ${route.difficulty ?: "Sedang"} • Estimasi ${route.duration_hours?.toInt() ?: 7} jam",
                                                    fontFamily = Montserrat,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 10.sp,
                                                    color = AltiDark.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                        if (route != filteredRoutes.last()) {
                                            HorizontalDivider(color = AltiMedium.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ── Section Header ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Find Your Summit!",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AltiDark
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ── Mountains State ─────────────────────────────────────
        when (val state = mountainsState) {
            is UiState.Loading, is UiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AltiDark)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Gagal memuat data",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = AltiDark
                        )
                        Text(
                            text = state.message,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = AltiDark.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.fetchMountains() },
                            colors = ButtonDefaults.buttonColors(containerColor = AltiDark)
                        ) {
                            Text("Coba Lagi", color = Color.White, fontFamily = Montserrat)
                        }
                    }
                }
            }
            is UiState.Success<List<MountainModel>> -> {
                val mountainList = state.data
                val cardWidth = LocalConfiguration.current.screenWidthDp.dp - 48.dp

                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    pageSpacing = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val mountain = mountainList[page]
                    MountainCard(
                        mountain = mountain,
                        cardWidth = cardWidth,
                        onClick = {
                            if (pagerState.currentPage == page) {
                                onMountainSelected(mountain)
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pager dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    mountainList.forEachIndexed { index, _ ->
                        val dotSize by animateDpAsState(
                            targetValue = if (pagerState.currentPage == index) 10.dp else 7.dp,
                            label = "dot_size_$index"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) Color.White
                                    else Color.White.copy(alpha = 0.4f)
                                )
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }
            }
        }

        // ── Prakiraan Cuaca ────────────────────────────────────
        Spacer(modifier = Modifier.height(26.dp))

        val currentMountainName = (mountainsState as? UiState.Success<List<MountainModel>>)
            ?.data?.getOrNull(pagerState.currentPage)?.name?.replace("Gunung ", "") ?: ""

        Text(
            text = "Prakiraan Cuaca - $currentMountainName",
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = AltiDark,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Prakiraan cuaca 7 hari ke depan berdasarkan posisi puncak",
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = AltiDark.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.background_frame_rekap_cuaca),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            WeatherForecastCard(
                activeWeatherState = activeWeatherState,
                currentPage = pagerState.currentPage
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
internal fun WeatherForecastCard(
    activeWeatherState: UiState<com.example.altiguide_mobile.data.model.WeatherResponse>,
    currentPage: Int
) {
    val calendar = java.util.Calendar.getInstance()
    val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val todayIndex = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
    val daysOfWeek = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
    val daysList = List(7) { i -> daysOfWeek[(todayIndex + i) % 7] }

    when (val weatherState = activeWeatherState) {
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
            val hourlyMocks = getHourlyMockList(todayIndex)
            WeatherContentColumn(
                hourlySection = {
                    hourlyMocks.forEach { mock ->
                        WeatherHourItem(time = mock.time, emoji = mock.emoji, label = "${mock.temp}°")
                    }
                },
                dailySection = {
                    daysList.forEachIndexed { dayIndex, dayName ->
                        val mock = getWeatherMock(currentPage, dayIndex)
                        WeatherDayItem(dayName = dayName, emoji = mock.emoji, label = "${mock.tempMax}° - ${mock.tempMin}°")
                    }
                }
            )
        }
        is UiState.Success<com.example.altiguide_mobile.data.model.WeatherResponse> -> {
            val weatherData = weatherState.data.data
            val daily = weatherData?.daily
            val hourly = weatherData?.hourly

            WeatherContentColumn(
                hourlySection = {
                    for (i in 0..5) {
                        val targetHourIndex = (currentHour + i) % 24
                        val timeString = hourly?.time?.getOrNull(targetHourIndex) ?: ""
                        val temp = hourly?.temperature_2m?.getOrNull(targetHourIndex)?.toInt() ?: 20
                        val weatherCode = hourly?.weathercode?.getOrNull(targetHourIndex) ?: 0
                        val emoji = weatherCodeToEmoji(weatherCode)
                        val displayTime = if (timeString.isNotEmpty()) formatHourlyTime(timeString)
                        else "${targetHourIndex.toString().padStart(2, '0')}:00"
                        WeatherHourItem(time = displayTime, emoji = emoji, label = "${temp}°")
                    }
                },
                dailySection = {
                    daysList.forEachIndexed { dayIndex, dayName ->
                        val weatherCode = daily?.weathercode?.getOrNull(dayIndex) ?: 0
                        val tempMax = daily?.temperature_2m_max?.getOrNull(dayIndex)?.toInt() ?: 20
                        val tempMin = daily?.temperature_2m_min?.getOrNull(dayIndex)?.toInt() ?: 14
                        val emoji = weatherCodeToEmoji(weatherCode)
                        WeatherDayItem(dayName = dayName, emoji = emoji, label = "${tempMax}° - ${tempMin}°")
                    }
                }
            )
        }
        else -> {}
    }
}

@Composable
private fun WeatherContentColumn(
    hourlySection: @Composable RowScope.() -> Unit,
    dailySection: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            content = hourlySection
        )

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            content = dailySection
        )
    }
}

@Composable
private fun WeatherHourItem(time: String, emoji: String, label: String) {
    Column(
        modifier = Modifier.width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = time, color = Color.White.copy(alpha = 0.9f), fontFamily = Montserrat, fontWeight = FontWeight.Medium, fontSize = 9.sp)
        Text(text = emoji, fontSize = 16.sp)
        Text(text = label, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = Color.White)
    }
}

@Composable
private fun WeatherDayItem(dayName: String, emoji: String, label: String) {
    Column(
        modifier = Modifier.width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = dayName, color = Color.White.copy(alpha = 0.9f), fontFamily = Montserrat, fontWeight = FontWeight.Medium, fontSize = 9.sp)
        Text(text = emoji, fontSize = 16.sp)
        Text(text = label, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = Color.White)
    }
}

private fun weatherCodeToEmoji(code: Int): String = when (code) {
    0 -> "☀️"
    1, 2, 3 -> "⛅"
    45, 48 -> "🌫️"
    51, 53, 55, 61, 63, 65, 80, 81, 82 -> "🌧️"
    71, 73, 75 -> "❄️"
    95, 96, 99 -> "⛈️"
    else -> "⛅"
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

