package com.example.altiguide_mobile.ui.auth

import android.widget.Toast
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.altiguide_mobile.R
import com.example.altiguide_mobile.data.model.LoginRequest
import com.example.altiguide_mobile.data.model.RegisterRequest
import com.example.altiguide_mobile.util.UiState
import kotlinx.coroutines.delay
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// Define Montserrat FontFamily locally for complete styling consistency
private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

// Screen states for the unified Auth flow
private enum class AuthScreen {
    LOGIN_SIGNUP,
    REGISTER_OTP,
    FORGOT_PASSWORD_EMAIL,
    FORGOT_PASSWORD_OTP,
    FORGOT_PASSWORD_RESET
}

// Custom Vector Path for Google Logo
private val GoogleIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Google",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color(0xFFEA4335))) {
            moveTo(12.24f, 10.285f)
            lineTo(12.24f, 13.715f)
            lineTo(19.24f, 13.715f)
            curveTo(18.96f, 15.605f, 17.5f, 17.895f, 14.28f, 19.345f)
            lineTo(17.39f, 21.825f)
            curveTo(19.24f, 20.085f, 22f, 16.985f, 22f, 12.245f)
            curveTo(22f, 11.495f, 21.92f, 10.885f, 21.78f, 10.285f)
            close()
        }
        path(fill = SolidColor(Color(0xFFFBBC05))) {
            moveTo(12.24f, 4.285f)
            curveTo(13.62f, 4.285f, 14.8f, 4.795f, 15.65f, 5.565f)
            lineTo(18.27f, 2.945f)
            curveTo(16.59f, 1.385f, 14.47f, 0.445f, 12.24f, 0.445f)
            curveTo(8.65f, 0.445f, 5.48f, 2.525f, 3.86f, 5.545f)
            lineTo(7.1f, 8.085f)
            curveTo(7.94f, 5.895f, 9.94f, 4.285f, 12.24f, 4.285f)
            close()
        }
        path(fill = SolidColor(Color(0xFF34A853))) {
            moveTo(12.24f, 20.245f)
            curveTo(9.94f, 20.245f, 7.94f, 18.635f, 7.1f, 16.445f)
            lineTo(3.86f, 18.985f)
            curveTo(5.48f, 22.005f, 8.65f, 24.085f, 12.24f, 24.085f)
            curveTo(15.26f, 24.085f, 17.82f, 23.085f, 19.68f, 21.385f)
            lineTo(16.57f, 18.905f)
            curveTo(15.54f, 19.745f, 14.1f, 20.245f, 12.24f, 20.245f)
            close()
        }
        path(fill = SolidColor(Color(0xFF4285F4))) {
            moveTo(3.86f, 5.545f)
            curveTo(3.4f, 6.945f, 3.12f, 8.445f, 3.12f, 10.005f)
            curveTo(3.12f, 11.565f, 3.4f, 13.065f, 3.86f, 14.465f)
            lineTo(7.1f, 11.925f)
            curveTo(6.98f, 11.305f, 6.92f, 10.665f, 6.92f, 10.005f)
            curveTo(6.92f, 9.345f, 6.98f, 8.705f, 7.1f, 8.085f)
            close()
        }
    }.build()

// Custom Vector Path for Visibility (Eye) Icon
private val VisibilityIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Visibility",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 4.5f)
            curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
            curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
            curveTo(17f, 19.5f, 21.27f, 16.39f, 23f, 12f)
            curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
            close()
            moveTo(12f, 17f)
            curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
            curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
            curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
            curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
            close()
            moveTo(12f, 9f)
            curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
            curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
            curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
            curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
            close()
        }
    }.build()

// Custom Vector Path for VisibilityOff (Eye Slashed) Icon
private val VisibilityOffIcon: ImageVector
    get() = ImageVector.Builder(
        name = "VisibilityOff",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(12f, 7f)
            curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
            curveTo(17f, 13.11f, 16.63f, 14.14f, 16f, 14.97f)
            lineTo(18.89f, 17.86f)
            curveTo(20.62f, 16.35f, 21.95f, 14.35f, 23f, 12f)
            curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
            curveTo(10.5f, 4.5f, 9.09f, 4.81f, 7.82f, 5.37f)
            lineTo(9.78f, 7.33f)
            curveTo(10.49f, 7.12f, 11.23f, 7f, 12f, 7f)
            close()
            moveTo(2f, 4.27f)
            lineTo(4.28f, 6.55f)
            curveTo(2.83f, 8.01f, 1.7f, 9.87f, 1f, 12f)
            curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
            curveTo(13.88f, 19.5f, 15.63f, 19.04f, 17.18f, 18.25f)
            lineTo(19.73f, 20.8f)
            lineTo(21f, 19.53f)
            lineTo(3.27f, 3f)
            close()
            moveTo(12f, 17f)
            curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
            curveTo(7f, 11.09f, 7.25f, 10.23f, 7.69f, 9.5f)
            lineTo(14.5f, 16.31f)
            curveTo(13.77f, 16.75f, 12.91f, 17f, 12f, 17f)
            close()
            moveTo(12.5f, 9.05f)
            lineTo(14.95f, 11.5f)
            curveTo(14.98f, 11.33f, 15f, 11.17f, 15f, 11f)
            curveTo(15f, 9.34f, 13.66f, 8f, 12f, 8f)
            curveTo(11.83f, 8f, 11.67f, 8.02f, 11.5f, 8.05f)
            close()
        }
    }.build()

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()
    val googleLoginState by viewModel.googleLoginState.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    val verifyOtpState by viewModel.verifyOtpState.collectAsState()
    val forgotPasswordState by viewModel.forgotPasswordState.collectAsState()

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d("GOOGLE_AUTH", "Result received: resultCode = ${result.resultCode}")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            android.util.Log.d("GOOGLE_AUTH", "Google Sign-In succeeded, token length = ${idToken?.length}")
            if (idToken != null) {
                viewModel.loginWithGoogle(idToken)
            } else {
                android.util.Log.e("GOOGLE_AUTH", "ID Token is null")
                Toast.makeText(context, "Google Sign-In failed: ID Token is null", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            android.util.Log.e("GOOGLE_AUTH", "Google Sign-In failed: statusCode = ${e.statusCode}", e)
            Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle login errors — navigation is driven by the token flow in MainActivity
    LaunchedEffect(loginState) {
        when (loginState) {
            is UiState.Error -> {
                Toast.makeText(context, (loginState as UiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetStates()
            }
            else -> {}
        }
    }

    LaunchedEffect(googleLoginState) {
        android.util.Log.d("GOOGLE_AUTH", "googleLoginState changed: $googleLoginState")
        when (googleLoginState) {
            is UiState.Error -> {
                Toast.makeText(context, (googleLoginState as UiState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetStates()
            }
            else -> {}
        }
    }

    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN_SIGNUP) }
    var isSignUpMode by remember { mutableStateOf(false) }

    // Forms State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    var signUpFullName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPhone by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }

    // OTP / Password Reset states
    var otpEmailTarget by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmNewPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resetStates()
    }

    // Resend OTP timer logic (59 seconds countdown)
    var resendTimerSeconds by remember { mutableStateOf(59) }
    LaunchedEffect(key1 = currentScreen, key2 = resendTimerSeconds) {
        if ((currentScreen == AuthScreen.REGISTER_OTP || currentScreen == AuthScreen.FORGOT_PASSWORD_OTP) && resendTimerSeconds > 0) {
            delay(1000L)
            resendTimerSeconds -= 1
        }
    }

    // React to successful sign up API call to redirect to OTP screen
    LaunchedEffect(key1 = registerState) {
        if (registerState is UiState.Success) {
            otpEmailTarget = signUpEmail
            resendTimerSeconds = 59
            otpCode = ""
            currentScreen = AuthScreen.REGISTER_OTP
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Full screen background image
        Image(
            painter = painterResource(id = R.drawable.img_trackposition),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Green-tinted dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF374426).copy(alpha = 0.55f))
        )

        // Scrollable content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 48.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            
            // Top Navigation & Branding Header (Shows back button on sub-screens)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (currentScreen != AuthScreen.LOGIN_SIGNUP) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(36.dp)
                            .border(1.dp, Color.White, CircleShape)
                            .clickable {
                                // Navigate back
                                currentScreen = if (currentScreen == AuthScreen.REGISTER_OTP) {
                                    AuthScreen.LOGIN_SIGNUP
                                } else if (currentScreen == AuthScreen.FORGOT_PASSWORD_OTP) {
                                    AuthScreen.FORGOT_PASSWORD_EMAIL
                                } else if (currentScreen == AuthScreen.FORGOT_PASSWORD_RESET) {
                                    AuthScreen.FORGOT_PASSWORD_OTP
                                } else {
                                    AuthScreen.LOGIN_SIGNUP
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_altiguide),
                        contentDescription = "AltiGuide Logo",
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "AltiGuide",
                        color = Color(0xFF2D4A1E), // Dark green title
                        fontSize = 32.sp,
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (currentScreen) {
                AuthScreen.LOGIN_SIGNUP -> {
                    // TAB SELECTOR (Login / Sign Up)
                    TabSelector(
                        isSignUpMode = isSignUpMode,
                        onTabSelected = { isSignUpMode = it },
                        modifier = Modifier.padding(bottom = 28.dp)
                    )

                    if (!isSignUpMode) {
                        // LOGIN SCREEN
                        FormGroup(label = "Email") {
                            CustomTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it },
                                placeholder = "you@gmail.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FormGroup(label = "Password") {
                            CustomTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                placeholder = "Password",
                                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (loginPasswordVisible) VisibilityIcon else VisibilityOffIcon,
                                            contentDescription = "Toggle password visibility",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            )
                        }

                        // Forgot Password Link (Modified to White)
                        Text(
                            text = "Forgot Password?",
                            color = Color.White, // Adjusted to white only
                            fontSize = 10.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .width(342.dp)
                                .padding(top = 6.dp, bottom = 24.dp)
                                .clickable {
                                    forgotPasswordState
                                    resendTimerSeconds = 59
                                    currentScreen = AuthScreen.FORGOT_PASSWORD_EMAIL
                                }
                        )

                        // Submit Login Button
                        if (loginState is UiState.Loading) {
                            CircularProgressIndicator(color = Color(0xFFE3E9CD), modifier = Modifier.size(24.dp))
                        } else {
                            Button(
                                onClick = {
                                    if (loginEmail.isNotBlank() && loginPassword.isNotBlank()) {
                                        viewModel.login(LoginRequest(loginEmail, loginPassword))
                                    } else {
                                        Toast.makeText(context, "Harap isi Email dan Password", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF374426),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .width(342.dp)
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Continue",
                                    fontSize = 13.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Login Error Display
                        if (loginState is UiState.Error) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = (loginState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(342.dp)
                            )
                        }

                    } else {
                        // SIGN UP SCREEN (Full Name, Email, Phone, Password)
                        FormGroup(label = "Nama Lengkap") {
                            CustomTextField(
                                value = signUpFullName,
                                onValueChange = { signUpFullName = it },
                                placeholder = "Nama lengkap"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FormGroup(label = "Email") {
                            CustomTextField(
                                value = signUpEmail,
                                onValueChange = { signUpEmail = it },
                                placeholder = "you@gmail.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FormGroup(label = "No HP") {
                            CustomTextField(
                                value = signUpPhone,
                                onValueChange = { signUpPhone = it },
                                placeholder = "081234567890",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FormGroup(label = "Password") {
                            CustomTextField(
                                value = signUpPassword,
                                onValueChange = { signUpPassword = it },
                                placeholder = "Password",
                                visualTransformation = if (signUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { signUpPasswordVisible = !signUpPasswordVisible }) {
                                        Icon(
                                            imageVector = if (signUpPasswordVisible) VisibilityIcon else VisibilityOffIcon,
                                            contentDescription = "Toggle password visibility",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Submit Sign Up Button
                        if (registerState is UiState.Loading) {
                            CircularProgressIndicator(color = Color(0xFFE3E9CD), modifier = Modifier.size(24.dp))
                        } else {
                            Button(
                                onClick = {
                                    if (signUpFullName.isNotBlank() && signUpEmail.isNotBlank() && signUpPhone.isNotBlank() && signUpPassword.isNotBlank()) {
                                        // Send registration trigger
                                        viewModel.register(
                                            RegisterRequest(
                                                name = signUpFullName,
                                                email = signUpEmail,
                                                password = signUpPassword,
                                                password_confirmation = signUpPassword,
                                                phone_number = signUpPhone
                                            )
                                        )
                                    } else {
                                        Toast.makeText(context, "Harap lengkapi semua field", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF374426),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .width(342.dp)
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Continue",
                                    fontSize = 13.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Register Error Display
                        if (registerState is UiState.Error) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = (registerState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(342.dp)
                            )
                        }
                    }

                    // OR Separator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .width(342.dp)
                            .padding(vertical = 20.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.6f)))
                        Text(
                            text = "OR",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.White.copy(alpha = 0.6f)))
                    }

                    // Continue with Google Button
                    if (googleLoginState is UiState.Loading) {
                        CircularProgressIndicator(color = Color(0xFFE3E9CD), modifier = Modifier.size(24.dp))
                    } else {
                        Button(
                            onClick = {
                                val signInIntent = googleSignInClient.signInIntent
                                googleSignInLauncher.launch(signInIntent)
                            },
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE3E9CD),
                                contentColor = Color(0xFF374426)
                            ),
                            modifier = Modifier
                                .width(342.dp)
                                .height(56.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = GoogleIcon,
                                    contentDescription = "Google Logo",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontSize = 13.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                AuthScreen.REGISTER_OTP -> {
                    // REGISTRATION OTP VERIFICATION SCREEN (Mock-up Layout matching)
                    Column(
                        modifier = Modifier.width(342.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer(modifier = Modifier.height(60.dp))
                        Text(
                            text = "Verifikasi OTP",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "Kami telah mengirimkan 6 digit kode verifikasi ke email $otpEmailTarget.",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // 6 Code digit inputs
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            OtpCodeInput(code = otpCode, onCodeChange = { otpCode = it })
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Resend Countdown Timer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (resendTimerSeconds > 0) {
                                Text(
                                    text = "Tidak menerima kode? ",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Kirim ulang dalam 00:${resendTimerSeconds.toString().padStart(2, '0')}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Tidak menerima kode? ",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Kirim ulang kode",
                                    color = Color(0xFFE3E9CD),
                                    fontSize = 12.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        viewModel.resendRegisterOtp(otpEmailTarget) {
                                            resendTimerSeconds = 59
                                            Toast.makeText(context, "OTP baru terkirim!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Verify Button
                        if (verifyOtpState is UiState.Loading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFFE3E9CD))
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (otpCode.length == 6) {
                                        viewModel.verifyRegisterOtp(otpEmailTarget, otpCode)
                                    } else {
                                        Toast.makeText(context, "Masukkan 6 digit kode OTP", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF374426),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Verifikasi & Lanjutkan",
                                        fontSize = 14.sp,
                                        fontFamily = Montserrat,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Display OTP verify errors
                        if (verifyOtpState is UiState.Error) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = (verifyOtpState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                AuthScreen.FORGOT_PASSWORD_EMAIL -> {
                    // FORGOT PASSWORD STEP 1: INPUT EMAIL
                    Column(
                        modifier = Modifier.width(342.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer(modifier = Modifier.height(60.dp))
                        Text(
                            text = "Lupa Password",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "Masukkan alamat email Anda untuk menerima kode verifikasi OTP reset password.",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        FormGroup(label = "Email") {
                            CustomTextField(
                                value = otpEmailTarget,
                                onValueChange = { otpEmailTarget = it },
                                placeholder = "you@gmail.com",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        if (forgotPasswordState is UiState.Loading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFFE3E9CD))
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (otpEmailTarget.isNotBlank()) {
                                        viewModel.sendForgotPasswordCode(otpEmailTarget) {
                                            resendTimerSeconds = 59
                                            otpCode = ""
                                            currentScreen = AuthScreen.FORGOT_PASSWORD_OTP
                                        }
                                    } else {
                                        Toast.makeText(context, "Harap masukkan email Anda", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF374426),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Kirim Kode OTP",
                                    fontSize = 14.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (forgotPasswordState is UiState.Error) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = (forgotPasswordState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                AuthScreen.FORGOT_PASSWORD_OTP -> {
                    // FORGOT PASSWORD STEP 2: VERIFY OTP
                    Column(
                        modifier = Modifier.width(342.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer(modifier = Modifier.height(60.dp))
                        Text(
                            text = "Verifikasi OTP",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "Kami telah mengirimkan 6 digit kode verifikasi ke email $otpEmailTarget.",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            OtpCodeInput(code = otpCode, onCodeChange = { otpCode = it })
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Resend Countdown Timer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (resendTimerSeconds > 0) {
                                Text(
                                    text = "Tidak menerima kode? ",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Kirim ulang dalam 00:${resendTimerSeconds.toString().padStart(2, '0')}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Tidak menerima kode? ",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Kirim ulang kode",
                                    color = Color(0xFFE3E9CD),
                                    fontSize = 12.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        viewModel.sendForgotPasswordCode(otpEmailTarget) {
                                            resendTimerSeconds = 59
                                            Toast.makeText(context, "OTP baru terkirim!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        if (forgotPasswordState is UiState.Loading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFFE3E9CD))
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (otpCode.length == 6) {
                                        viewModel.verifyForgotPasswordCode(otpEmailTarget, otpCode) {
                                            newPassword = ""
                                            confirmNewPassword = ""
                                            currentScreen = AuthScreen.FORGOT_PASSWORD_RESET
                                        }
                                    } else {
                                        Toast.makeText(context, "Masukkan 6 digit kode OTP", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF374426),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Verifikasi Kode",
                                    fontSize = 14.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (forgotPasswordState is UiState.Error) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = (forgotPasswordState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                AuthScreen.FORGOT_PASSWORD_RESET -> {
                    // FORGOT PASSWORD STEP 3: INPUT NEW PASSWORD
                    Column(
                        modifier = Modifier.width(342.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Spacer(modifier = Modifier.height(60.dp))
                        Text(
                            text = "Password Baru",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "Buat password baru untuk akun Anda. Minimal 8 karakter.",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        FormGroup(label = "Password Baru") {
                            CustomTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                placeholder = "Masukkan password baru",
                                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                        Icon(
                                            imageVector = if (newPasswordVisible) VisibilityIcon else VisibilityOffIcon,
                                            contentDescription = "Toggle password visibility",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        FormGroup(label = "Konfirmasi Password Baru") {
                            CustomTextField(
                                value = confirmNewPassword,
                                onValueChange = { confirmNewPassword = it },
                                placeholder = "Ulangi password baru",
                                visualTransformation = if (confirmNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(onClick = { confirmNewPasswordVisible = !confirmNewPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmNewPasswordVisible) VisibilityIcon else VisibilityOffIcon,
                                            contentDescription = "Toggle password visibility",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        if (forgotPasswordState is UiState.Loading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFFE3E9CD))
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (newPassword.length >= 8) {
                                        if (newPassword == confirmNewPassword) {
                                            viewModel.resetPassword(otpEmailTarget, otpCode, newPassword) {
                                                Toast.makeText(context, "Password berhasil diubah! Silakan login.", Toast.LENGTH_LONG).show()
                                                currentScreen = AuthScreen.LOGIN_SIGNUP
                                                isSignUpMode = false
                                            }
                                        } else {
                                            Toast.makeText(context, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF374426),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = "Simpan & Lanjutkan",
                                    fontSize = 14.sp,
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (forgotPasswordState is UiState.Error) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = (forgotPasswordState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Helper Composable to wrap labels with their corresponding inputs
@Composable
private fun FormGroup(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.width(342.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            color = Color(0xFF374426),
            fontSize = 13.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 6.dp, start = 8.dp)
        )
        content()
    }
}

// Customized basic text field to fit the premium 342dp x 56dp rounded pill styling
@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color.White // Typed text is white
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        singleLine = true,
        cursorBrush = SolidColor(Color.White),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.White.copy(alpha = 0.6f), // Placeholder text is semi-transparent white
                            fontSize = 13.sp,
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    innerTextField()
                }
                if (trailingIcon != null) {
                    trailingIcon()
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFC0C0C0).copy(alpha = 0.5f), RoundedCornerShape(28.dp))
    )
}

// Segmented Control Composable to Switch Tabs (Pill 342dp wide)
@Composable
private fun TabSelector(
    isSignUpMode: Boolean,
    onTabSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(342.dp)
            .height(44.dp)
            .background(Color(0xFFC0C0C0).copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Login Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (!isSignUpMode) Color.White else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onTabSelected(false) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Login",
                    color = if (!isSignUpMode) Color(0xFF374426) else Color(0xFF374426).copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Sign Up Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSignUpMode) Color.White else Color.Transparent,
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onTabSelected(true) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign Up",
                    color = if (isSignUpMode) Color(0xFF374426) else Color(0xFF374426).copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Styled 6-digit OTP code layout matching the mockup
@Composable
private fun OtpCodeInput(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = code,
        onValueChange = {
            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                onCodeChange(it)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(Color.Transparent), // hide cursor
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 6) {
                        val char = code.getOrNull(i)?.toString() ?: ""
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 56.dp)
                                .background(Color(0xFFE3E9CD), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                color = Color(0xFF374426),
                                fontSize = 20.sp,
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}
