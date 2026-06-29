package com.example.altiguide_mobile.ui.transaction

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.altiguide_mobile.R
import com.example.altiguide_mobile.data.model.TransactionModel
import com.example.altiguide_mobile.data.model.UserModel
import com.example.altiguide_mobile.util.formatNumber
import com.example.altiguide_mobile.util.getMountainDrawable
import com.example.altiguide_mobile.ui.theme.AltiDark
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

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

private val IconClock: ImageVector get() = ImageVector.Builder(
    name = "Clock", defaultWidth = 13.dp, defaultHeight = 13.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(AltiDark)) {
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

@Composable
fun TicketCard(
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
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = mountainName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                Text(
                    text = mountainName,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = AltiDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

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
private fun TicketDivider(color: Color = AltiDark.copy(alpha = 0.15f)) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(30) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(2.dp)
                    .background(color)
            )
        }
    }
}

@Composable
fun EmptyBookingsState() {
    Box(
        modifier = Modifier.fillMaxSize(),
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
fun TicketDetailScreen(
    transaction: TransactionModel,
    onBack: () -> Unit
) {
    val session = transaction.hikingSession
    val route = session?.route
    val mountainName = route?.mountain?.name ?: "Unknown Mountain"
    val routeName = route?.name ?: "Unknown Route"
    val dateText = session?.start_date ?: "-"
    val groupName = session?.group_name ?: "-"
    val imageRes = getMountainDrawable(mountainName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
            .verticalScroll(rememberScrollState())
    ) {
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
            Column {
                Text(
                    text = "E-Ticket",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AltiDark
                )
                Text(
                    text = "Surat Izin Masuk Kawasan Konservasi (SIMAKSI)",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    color = AltiDark.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.frame_ticket),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
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

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDateString(dateText),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFFC3CB92)
                        )
                        Text(
                            text = formatDateString(session?.end_date ?: dateText),
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFFC3CB92)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC3CB92))
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(Color(0xFFC3CB92))
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .border(2.dp, Color(0xFFC3CB92), CircleShape)
                                .background(Color.Transparent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val leaderName = remember(session) {
                    session?.members?.find { it.user_id == session.leader_id }?.full_name
                        ?: session?.members?.firstOrNull()?.full_name
                        ?: "-"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ketua Rombongan",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color(0xFFC3CB92).copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = leaderName,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Nama Rombongan",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color(0xFFC3CB92).copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = groupName,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Order ID",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color(0xFFC3CB92).copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "#${transaction.orderId.removePrefix("#")}",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier.weight(0.8f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Total Bayar",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = Color(0xFFC3CB92).copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rp ${formatNumber(transaction.grossAmount)}",
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TicketDivider(color = Color(0xFFC3CB92).copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            .size(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrImageBitmap != null) {
                            Image(
                                bitmap = qrImageBitmap,
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "ID: ${transaction.id}",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp,
                        color = Color(0xFFC3CB92).copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
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

private fun formatDateString(dateStr: String?): String {
    if (dateStr.isNullOrEmpty() || dateStr == "-") return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputFormat.parse(dateStr) ?: return dateStr
        val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        outputFormat.format(date)
    } catch (e: Exception) {
        dateStr
    }
}
