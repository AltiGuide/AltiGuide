package com.example.altiguide_mobile.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

private val AltiDark = Color(0xFF20341B)
private val AltiMedium = Color(0xFF859763)
private val SearchBg = Color(0xFFEAF0D8)

private val IconPhone: ImageVector get() = ImageVector.Builder(
    name = "Phone", defaultWidth = 24.dp, defaultHeight = 24.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(AltiDark),
        strokeLineWidth = 2f,
        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
    ) {
        moveTo(6.62f, 10.79f)
        curveTo(8.06f, 13.62f, 10.38f, 15.94f, 13.21f, 17.38f)
        lineTo(15.41f, 15.18f)
        curveTo(15.68f, 14.91f, 16.08f, 14.82f, 16.43f, 14.94f)
        curveTo(17.55f, 15.31f, 18.76f, 15.51f, 20f, 15.51f)
        curveTo(20.55f, 15.51f, 21f, 15.96f, 21f, 16.51f)
        lineTo(21f, 20f)
        curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
        curveTo(10.61f, 21f, 3f, 13.39f, 3f, 4f)
        curveTo(3f, 3.45f, 3.45f, 3f, 4f, 3f)
        lineTo(7.5f, 3f)
        curveTo(8.05f, 3f, 8.5f, 3.45f, 8.5f, 4f)
        curveTo(8.5f, 5.24f, 8.7f, 6.45f, 9.07f, 7.57f)
        curveTo(9.18f, 7.92f, 9.1f, 8.31f, 8.82f, 8.59f)
        lineTo(6.62f, 10.79f)
        close()
    }
}.build()

private val IconPersonOutline: ImageVector get() = ImageVector.Builder(
    name = "PersonOutline", defaultWidth = 24.dp, defaultHeight = 24.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(AltiDark),
        strokeLineWidth = 2f,
        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round
    ) {
        // Head
        moveTo(12f, 12f)
        curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f)
        curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f)
        curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f)
        curveTo(8f, 10.21f, 9.79f, 12f, 12f, 12f)
        close()
        // shoulders
        moveTo(20f, 20f)
        curveTo(20f, 16.68f, 16.42f, 14f, 12f, 14f)
        curveTo(7.58f, 14f, 4f, 16.68f, 4f, 20f)
    }
}.build()

@Composable
fun HelpCenterScreen(
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
                .padding(start = 24.dp, end = 24.dp, top = 52.dp, bottom = 100.dp)
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
                    text = "Help Center",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AltiDark
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Contact Card
            HelpContactCard(
                icon = IconPhone,
                label = "Contact",
                name = "AltiGuide",
                detail = "+62 82324905011"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Card
            HelpContactCard(
                icon = IconPersonOutline,
                label = "Email",
                name = "altiguide1@gmail.com",
                detail = null
            )
        }
    }
}

@Composable
private fun HelpContactCard(
    icon: ImageVector,
    label: String,
    name: String,
    detail: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SearchBg.copy(alpha = 0.5f))
            .border(1.dp, AltiDark, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = AltiDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AltiDark
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                modifier = Modifier.padding(start = 28.dp)
            ) {
                Text(
                    text = name,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = AltiDark
                )
                if (detail != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = detail,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = AltiDark
                    )
                }
            }
        }
    }
}
