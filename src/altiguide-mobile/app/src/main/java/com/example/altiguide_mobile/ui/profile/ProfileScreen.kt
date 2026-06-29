package com.example.altiguide_mobile.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.altiguide_mobile.R
import com.example.altiguide_mobile.data.model.UserModel
import com.example.altiguide_mobile.util.UiState
import coil.compose.SubcomposeAsyncImage

private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

private val AltiDark = Color(0xFF20341B)
private val AltiMedium = Color(0xFF859763)
private val AltiLight = Color(0xFFC3CB92)

private val IconEdit: ImageVector get() = ImageVector.Builder(
    name = "Edit", defaultWidth = 20.dp, defaultHeight = 20.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiDark)) {
        moveTo(3f, 17.25f); lineTo(3f, 21f); lineTo(6.75f, 21f)
        lineTo(17.81f, 9.94f); lineTo(14.06f, 6.19f); close()
        moveTo(20.71f, 7.04f)
        curveTo(21.1f, 6.65f, 21.1f, 6.02f, 20.71f, 5.63f)
        lineTo(18.37f, 3.29f)
        curveTo(17.98f, 2.9f, 17.35f, 2.9f, 16.96f, 3.29f)
        lineTo(15.13f, 5.12f); lineTo(18.88f, 8.87f); close()
    }
}.build()

private val IconChevron: ImageVector get() = ImageVector.Builder(
    name = "Chevron", defaultWidth = 20.dp, defaultHeight = 20.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiMedium)) {
        moveTo(9.29f, 6.71f)
        curveTo(8.9f, 7.1f, 8.9f, 7.73f, 9.29f, 8.12f)
        lineTo(13.17f, 12f); lineTo(9.29f, 15.88f)
        curveTo(8.9f, 16.27f, 8.9f, 16.9f, 9.29f, 17.29f)
        curveTo(9.68f, 17.68f, 10.31f, 17.68f, 10.7f, 17.29f)
        lineTo(15.29f, 12.7f)
        curveTo(15.68f, 12.31f, 15.68f, 11.68f, 15.29f, 11.29f)
        lineTo(10.7f, 6.7f)
        curveTo(10.32f, 6.32f, 9.68f, 6.32f, 9.29f, 6.71f); close()
    }
}.build()

@Composable
fun ProfileScreen(
    profileState: UiState<UserModel>,
    onEditProfile: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onLogout: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_main),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        when (profileState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AltiDark)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = profileState.message,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = AltiDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = AltiDark)
                        ) {
                            Text("Coba Lagi", color = Color.White, fontFamily = Montserrat)
                        }
                    }
                }
            }
            is UiState.Success -> {
                val user = profileState.data
                ProfileContent(
                    user = user,
                    onEditProfile = onEditProfile,
                    onNavigateToHelpCenter = onNavigateToHelpCenter,
                    onNavigateToAbout = onNavigateToAbout,
                    onLogout = onLogout
                )
            }
            is UiState.Idle -> { }
        }
    }
}

@Composable
private fun ProfileContent(
    user: UserModel,
    onEditProfile: () -> Unit,
    onNavigateToHelpCenter: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_main),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 52.dp, bottom = 90.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────
            Text(
                text = "Account",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AltiDark
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Avatar + Info ──────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AltiMedium),
                    contentAlignment = Alignment.Center
                ) {
                    val imageModel = user.getAvatarModel()
                    if (imageModel != null) {
                        SubcomposeAsyncImage(
                            model = imageModel,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.take(1).uppercase(),
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        )
                    } else {
                        Text(
                            text = user.name.take(1).uppercase(),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = AltiDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.email,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = AltiDark.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f))
                        .clickable { onEditProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconEdit,
                        contentDescription = "Edit Profile",
                        tint = AltiDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Menu Items ─────────────────────────────────────────────
            ProfileMenuItem(
                icon = IconHelp,
                label = "Help Center",
                onClick = onNavigateToHelpCenter
            )
            ProfileMenuItem(
                icon = IconAbout,
                label = "About",
                onClick = onNavigateToAbout
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Logout ─────────────────────────────────────────────────
            ProfileMenuItem(
                icon = IconLogout,
                label = "Logout",
                textColor = Color(0xFFE53935),
                onClick = onLogout
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    textColor: Color = AltiDark,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = IconChevron,
            contentDescription = null,
            tint = AltiMedium,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ── Menu Icons ────────────────────────────────────────────────────────────
private val IconBookings: ImageVector get() = ImageVector.Builder(
    name = "Bookings", defaultWidth = 22.dp, defaultHeight = 22.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiDark)) {
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

private val IconHelp: ImageVector get() = ImageVector.Builder(
    name = "Help", defaultWidth = 22.dp, defaultHeight = 22.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiDark)) {
        moveTo(11.07f, 12.61f)
        curveTo(11.57f, 12.11f, 12.57f, 11.44f, 12.57f, 10.34f)
        curveTo(12.57f, 9.23f, 11.72f, 8.54f, 10.86f, 8.54f)
        curveTo(9.55f, 8.54f, 9.1f, 9.54f, 9.1f, 9.54f)
        lineTo(7.59f, 8.77f)
        curveTo(7.59f, 8.77f, 8.31f, 7.16f, 10.97f, 7.16f)
        curveTo(12.88f, 7.16f, 14.23f, 8.34f, 14.23f, 10.17f)
        curveTo(14.23f, 12.14f, 12.77f, 12.68f, 12.22f, 13.23f)
        curveTo(11.62f, 13.83f, 11.54f, 14.17f, 11.54f, 14.84f)
        lineTo(9.86f, 14.84f)
        curveTo(9.86f, 13.87f, 9.85f, 13.82f, 11.07f, 12.61f); close()
        moveTo(11.5f, 17.75f)
        curveTo(10.82f, 17.75f, 10.25f, 17.18f, 10.25f, 16.5f)
        curveTo(10.25f, 15.82f, 10.82f, 15.25f, 11.5f, 15.25f)
        curveTo(12.18f, 15.25f, 12.75f, 15.82f, 12.75f, 16.5f)
        curveTo(12.75f, 17.18f, 12.18f, 17.75f, 11.5f, 17.75f); close()
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f); close()
        moveTo(12f, 20f)
        curveTo(7.59f, 20f, 4f, 16.41f, 4f, 12f)
        curveTo(4f, 7.59f, 7.59f, 4f, 12f, 4f)
        curveTo(16.41f, 4f, 20f, 7.59f, 20f, 12f)
        curveTo(20f, 16.41f, 16.41f, 20f, 12f, 20f); close()
    }
}.build()

private val IconAbout: ImageVector get() = ImageVector.Builder(
    name = "About", defaultWidth = 22.dp, defaultHeight = 22.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiDark)) {
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f); close()
        moveTo(13f, 17f); lineTo(11f, 17f); lineTo(11f, 11f); lineTo(13f, 11f); close()
        moveTo(13f, 9f); lineTo(11f, 9f); lineTo(11f, 7f); lineTo(13f, 7f); close()
    }
}.build()

private val IconLogout: ImageVector get() = ImageVector.Builder(
    name = "Logout", defaultWidth = 22.dp, defaultHeight = 22.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color(0xFFE53935))) {
        moveTo(17f, 7f); lineTo(15.59f, 8.41f); lineTo(18.17f, 11f)
        lineTo(8f, 11f); lineTo(8f, 13f); lineTo(18.17f, 13f)
        lineTo(15.59f, 15.59f); lineTo(17f, 17f); lineTo(22f, 12f); close()
        moveTo(4f, 5f); lineTo(12f, 5f); lineTo(12f, 3f); lineTo(4f, 3f)
        curveTo(2.9f, 3f, 2f, 3.9f, 2f, 5f)
        lineTo(2f, 19f)
        curveTo(2f, 20.1f, 2.9f, 21f, 4f, 21f)
        lineTo(12f, 21f); lineTo(12f, 19f); lineTo(4f, 19f); close()
    }
}.build()
