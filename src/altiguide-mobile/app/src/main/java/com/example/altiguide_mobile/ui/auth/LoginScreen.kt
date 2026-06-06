package com.example.altiguide_mobile.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.altiguide_mobile.R
import com.example.altiguide_mobile.data.model.LoginRequest
import com.example.altiguide_mobile.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsState()

    var email by remember { mutableStateOf("test@example.com") }
    var password by remember { mutableStateOf("password123") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3E9CD),
                        Color(0xFFE1FF74)
                    )
                )
            ), // Matches splash and onboarding gradient background
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Logo & Heading Section
            Image(
                painter = painterResource(id = R.drawable.logo_altiguide),
                contentDescription = "AltiGuide Logo",
                modifier = Modifier
                    .size(90.dp)
                    .padding(bottom = 12.dp)
            )

            Text(
                text = "AltiGuide",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF2D4A1E)
            )

            Text(
                text = "Your Best Summit Companion",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF4A6741),
                modifier = Modifier.padding(top = 4.dp, bottom = 48.dp),
                textAlign = TextAlign.Center
            )

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D4A1E),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2D4A1E),
                            unfocusedBorderColor = Color(0xFFB0C2A7),
                            focusedLabelColor = Color(0xFF2D4A1E),
                            unfocusedLabelColor = Color(0xFF4A6741),
                            cursorColor = Color(0xFF2D4A1E)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2D4A1E),
                            unfocusedBorderColor = Color(0xFFB0C2A7),
                            focusedLabelColor = Color(0xFF2D4A1E),
                            unfocusedLabelColor = Color(0xFF4A6741),
                            cursorColor = Color(0xFF2D4A1E)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Login Button or Progress
                    if (loginState is UiState.Loading) {
                        CircularProgressIndicator(
                            color = Color(0xFF2D4A1E),
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Button(
                            onClick = {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    viewModel.login(LoginRequest(email, password))
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2D4A1E),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Handle Error States
                    if (loginState is UiState.Error) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = (loginState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
