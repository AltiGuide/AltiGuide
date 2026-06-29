package com.example.altiguide_mobile.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.drop
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
private val SearchBg = Color(0xFFEAF0D8)

private val IconCamera: ImageVector get() = ImageVector.Builder(
    name = "Camera", defaultWidth = 20.dp, defaultHeight = 20.dp,
    viewportWidth = 24f, viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
        moveTo(12f, 12f)
        curveTo(13.66f, 12f, 15f, 10.66f, 15f, 9f)
        curveTo(15f, 7.34f, 13.66f, 6f, 12f, 6f)
        curveTo(10.34f, 6f, 9f, 7.34f, 9f, 9f)
        curveTo(9f, 10.66f, 10.34f, 12f, 12f, 12f)
        close()
        
        moveTo(9f, 2f); lineTo(7.17f, 4f); lineTo(4f, 4f)
        curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
        lineTo(2f, 18f)
        curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
        lineTo(20f, 20f)
        curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
        lineTo(22f, 6f)
        curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
        lineTo(16.83f, 4f); lineTo(15f, 2f)
        close()
        
        moveTo(12f, 17f)
        curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
        curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
        curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
        curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
        close()
    }
}.build()

@Composable
fun EditProfileScreen(
    user: UserModel,
    updateState: UiState<String>,
    onBack: () -> Unit = {},
    onSave: (
        name: String, 
        email: String, 
        phone: String?, 
        age: Int?, 
        address: String?, 
        emergencyContact: String?, 
        nik: String?, 
        password: String,
        avatarPath: String?
    ) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
    onResetUpdate: () -> Unit = {}
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone_number ?: "") }
    var age by remember { mutableStateOf(user.age?.toString() ?: "") }
    var address by remember { mutableStateOf(user.address ?: "") }
    var emergencyContact by remember { mutableStateOf(user.emergency_contact ?: "") }
    var nik by remember { mutableStateOf(user.nik ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedImagePath = saveImageToCache(context, uri)
        }
    }

    // Skip initial updateState (apapun itu), reaksi hanya terhadap perubahan setelahnya
    LaunchedEffect(Unit) {
        snapshotFlow { updateState }
            .drop(1)
            .collect { state ->
                if (state is UiState.Success) onBack()
            }
    }

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
                .padding(start = 24.dp, end = 24.dp, top = 52.dp, bottom = 120.dp)
        ) {
            // ── Back + Title ─────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AltiDark
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Profile",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AltiDark
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Avatar with Edit Camera Icon Overlay ───────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(88.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.TopStart)
                        .clip(CircleShape)
                        .background(AltiMedium)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val imageModel = selectedImageUri ?: user.getAvatarModel()
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
                                        modifier = Modifier.size(24.dp)
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
                                        fontSize = 34.sp,
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
                            fontSize = 34.sp,
                            color = Color.White
                        )
                    }
                }

                // Camera overlay badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(AltiDark)
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = IconCamera,
                        contentDescription = "Edit Profile Picture",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Name Field ─────────────────────────────────────────
            FieldLabel("Full Name")
            Spacer(modifier = Modifier.height(6.dp))
            EditTextField(value = name, onValueChange = { name = it })

            Spacer(modifier = Modifier.height(14.dp))

            // ── Email Field ────────────────────────────────────────
            FieldLabel("Email")
            Spacer(modifier = Modifier.height(6.dp))
            EditTextField(value = email, onValueChange = { email = it })

            Spacer(modifier = Modifier.height(14.dp))

            // ── Phone Field ────────────────────────────────────────
            FieldLabel("Phone Number")
            Spacer(modifier = Modifier.height(6.dp))
            EditTextField(
                value = phone,
                onValueChange = { phone = it },
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Age Field ──────────────────────────────────────────
            FieldLabel("Age")
            Spacer(modifier = Modifier.height(6.dp))
            EditTextField(
                value = age,
                onValueChange = { age = it },
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Address Field ──────────────────────────────────────
            FieldLabel("Address")
            Spacer(modifier = Modifier.height(6.dp))
            EditTextField(value = address, onValueChange = { address = it })

            Spacer(modifier = Modifier.height(14.dp))

            // ── Emergency Contact Field ─────────────────────────────
            FieldLabel("Emergency Contact")
            Spacer(modifier = Modifier.height(6.dp))
            EditTextField(
                value = emergencyContact,
                onValueChange = { emergencyContact = it },
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── NIK Field ──────────────────────────────────────────
            FieldLabel("NIK")
            Spacer(modifier = Modifier.height(6.dp))
            EditTextField(
                value = nik,
                onValueChange = { nik = it },
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Password Field ──────────────────────────────────────
            FieldLabel("Password (biarkan kosong jika tidak ingin ganti)")
            Spacer(modifier = Modifier.height(6.dp))
            PasswordField(
                value = password,
                onValueChange = { password = it },
                visible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Update State ───────────────────────────────────────
            when (updateState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AltiDark, modifier = Modifier.size(28.dp))
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = updateState.message,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color(0xFFE53935),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SaveButton(
                        enabled = true,
                        onClick = {
                            onSave(
                                name, email,
                                phone.ifBlank { null },
                                age.toIntOrNull(),
                                address.ifBlank { null },
                                emergencyContact.ifBlank { null },
                                nik.ifBlank { null },
                                password,
                                selectedImagePath
                            )
                        }
                    )
                }
                else -> {
                    SaveButton(
                        enabled = true,
                        onClick = {
                            onResetUpdate()
                            onSave(
                                name, email,
                                phone.ifBlank { null },
                                age.toIntOrNull(),
                                address.ifBlank { null },
                                emergencyContact.ifBlank { null },
                                nik.ifBlank { null },
                                password,
                                selectedImagePath
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = AltiDark
    )
}

@Composable
private fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SearchBg.copy(alpha = 0.7f))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = AltiDark
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            cursorBrush = SolidColor(AltiDark)
        )
    }
}


@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(35.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SearchBg.copy(alpha = 0.7f))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AltiDark
                ),
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.weight(1f),
                cursorBrush = SolidColor(AltiDark)
            )
            Text(
                text = if (visible) "Hide" else "Show",
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = AltiMedium,
                modifier = Modifier.clickable { onToggleVisibility() }
            )
        }
    }
}

@Composable
private fun SaveButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = AltiDark)
    ) {
        Text(
            text = "Save Changes",
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Color.White
        )
    }
}

private fun saveImageToCache(context: android.content.Context, uri: android.net.Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val buffered = java.io.BufferedInputStream(inputStream)

        val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        buffered.mark(Integer.MAX_VALUE)
        android.graphics.BitmapFactory.decodeStream(buffered, null, opts)
        buffered.reset()

        val maxDim = 256
        val scaleFactor = if (opts.outWidth > 0 && opts.outHeight > 0) {
            maxOf(
                (opts.outWidth + maxDim - 1) / maxDim,
                (opts.outHeight + maxDim - 1) / maxDim,
                1
            )
        } else 1

        val decodeOpts = android.graphics.BitmapFactory.Options().apply { inSampleSize = scaleFactor }
        val bitmap = android.graphics.BitmapFactory.decodeStream(buffered, null, decodeOpts)
        buffered.close()
        if (bitmap == null) return null

        val dir = java.io.File(context.filesDir, "avatars")
        dir.mkdirs()
        val file = java.io.File(dir, "avatar_${java.util.UUID.randomUUID()}.jpg")
        val outputStream = java.io.FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.close()
        bitmap.recycle()

        file.absolutePath
    } catch (e: Exception) {
        null
    }
}
