package com.example.altiguide_mobile.ui.mountain

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.altiguide_mobile.R
import com.example.altiguide_mobile.data.model.MountainModel
import com.example.altiguide_mobile.util.formatNumber
import com.example.altiguide_mobile.util.getMountainDrawable
import com.example.altiguide_mobile.ui.theme.AltiDark

private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

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

@Composable
fun MountainCard(
    mountain: MountainModel,
    cardWidth: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val altitudeText = mountain.altitude?.let { "${formatNumber(it)} mdpl" } ?: ""
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
