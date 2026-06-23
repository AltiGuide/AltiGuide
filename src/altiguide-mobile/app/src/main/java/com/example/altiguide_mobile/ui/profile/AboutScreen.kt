package com.example.altiguide_mobile.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import android.text.Layout
import androidx.core.content.res.ResourcesCompat

private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

private val AltiDark = Color(0xFF20341B)

private val IconNavOffline: ImageVector get() = ImageVector.Builder(
    name = "NavOffline", defaultWidth = 24.dp, defaultHeight = 24.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(AltiDark),
        strokeLineWidth = 2f,
        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
    ) {
        moveTo(12f, 2f)
        lineTo(4.5f, 20.29f)
        lineTo(12f, 17f)
        lineTo(19.5f, 20.29f)
        close()
    }
}.build()

private val IconTicketOutline: ImageVector get() = ImageVector.Builder(
    name = "TicketOutline", defaultWidth = 24.dp, defaultHeight = 24.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(AltiDark),
        strokeLineWidth = 2f,
        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
    ) {
        // Main rectangle outline
        moveTo(3f, 6f)
        lineTo(21f, 6f)
        lineTo(21f, 18f)
        lineTo(3f, 18f)
        close()
        
        // Left stub line
        moveTo(7f, 6f)
        lineTo(7f, 18f)
        
        // Right stub line
        moveTo(17f, 6f)
        lineTo(17f, 18f)
    }
}.build()

private val IconWeatherOutline: ImageVector get() = ImageVector.Builder(
    name = "WeatherOutline", defaultWidth = 24.dp, defaultHeight = 24.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(AltiDark),
        strokeLineWidth = 2f,
        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
    ) {
        moveTo(17f, 14f)
        curveTo(18.5f, 14f, 20f, 12.5f, 20f, 11f)
        curveTo(20f, 9.5f, 18.5f, 8f, 17f, 8f)
        curveTo(16.8f, 8f, 16.5f, 8.1f, 16.3f, 8.2f)
        curveTo(15.7f, 5.7f, 13.5f, 4f, 11f, 4f)
        curveTo(8.2f, 4f, 5.8f, 6.2f, 5.5f, 9f)
        curveTo(4.1f, 9.2f, 3f, 10.5f, 3f, 12f)
        curveTo(3f, 13.7f, 4.3f, 15f, 6f, 15f)
        lineTo(16f, 15f)
    }
    path(
        stroke = SolidColor(AltiDark),
        strokeLineWidth = 2.5f,
        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round
    ) {
        moveTo(7f, 18f); lineTo(6.5f, 19.5f)
        moveTo(11f, 18f); lineTo(10.5f, 19.5f)
        moveTo(15f, 18f); lineTo(14.5f, 19.5f)
    }
}.build()

@Composable
fun AboutScreen(
    onBack: () -> Unit
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
                .padding(start = 24.dp, end = 24.dp, top = 52.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Back button + Centered Title
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AltiDark
                    )
                }

                Text(
                    text = "About",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AltiDark
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logo Image (Clean uncropped brand symbol)
            Image(
                painter = painterResource(id = R.drawable.logo_altiguide),
                contentDescription = "AltiGuide Logo Symbol",
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth(0.5f),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Version
            Text(
                text = "version 1.0.0",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = AltiDark.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Description
            Text(
                text = "AltiGuide adalah teman pendakian terbaik yang dirancang khusus untuk para penjelajah pegunungan Jawa Tengah. Misi kami adalah menjembatani presisi pendakian profesional dengan kenyamanan digital yang mulus, menyediakan semua yang Anda butuhkan mulai dari basecamp hingga puncak.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp), // Tetap gunakan padding agar ada jarak manis dari tepi layar
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = AltiDark,
                textAlign = TextAlign.Center, // <--- Cukup ganti bagian ini ke Center
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Three feature cards horizontally
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AboutFeatureCard(
                    icon = IconNavOffline,
                    title = "Navigasi\nOffline",
                    modifier = Modifier.weight(1f)
                )
                AboutFeatureCard(
                    icon = IconTicketOutline,
                    title = "E - Ticket",
                    modifier = Modifier.weight(1f)
                )
                AboutFeatureCard(
                    icon = IconWeatherOutline,
                    title = "Perkiraan\nCuaca",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AboutFeatureCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFD6DEC2))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AltiDark,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = AltiDark,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
