package com.example.altiguide_mobile.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun OfflineCompassDial(
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
