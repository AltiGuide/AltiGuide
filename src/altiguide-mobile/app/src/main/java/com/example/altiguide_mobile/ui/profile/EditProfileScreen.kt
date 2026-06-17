package com.example.altiguide_mobile.ui.profile

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
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

@Composable
fun EditProfileScreen(
    user: UserModel,
    updateState: UiState<String>,
    onBack: () -> Unit = {},
    onSave: (name: String, email: String, phone: String?, age: Int?, address: String?, emergencyContact: String?, nik: String?, password: String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onResetUpdate: () -> Unit = {}
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone_number ?: "") }
    var age by remember { mutableStateOf(user.age?.toString() ?: "") }
    var address by remember { mutableStateOf(user.address ?: "") }
    var emergencyContact by remember { mutableStateOf(user.emergency_contact ?: "") }
    var nik by remember { mutableStateOf(user.nik ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) {
            onBack()
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

            // ── Avatar ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AltiMedium),
                contentAlignment = Alignment.Center
            ) {
                val imageUrl = if (!user.avatar_url.isNullOrEmpty()) user.avatar_url else user.image
                if (!imageUrl.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = imageUrl,
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
                                password
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
                                password
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
