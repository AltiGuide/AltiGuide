package com.example.altiguide_mobile.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.altiguide_mobile.data.model.AuthResponse
import com.example.altiguide_mobile.data.model.LoginRequest
import com.example.altiguide_mobile.data.repository.AuthRepository
import com.example.altiguide_mobile.data.repository.MountainRepository
import com.example.altiguide_mobile.data.repository.RouteRepository
import com.example.altiguide_mobile.data.repository.TransactionRepository
import com.example.altiguide_mobile.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val mountainRepository: MountainRepository,
    private val transactionRepository: TransactionRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<AuthResponse>> = _loginState.asStateFlow()

    private val _googleLoginState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val googleLoginState: StateFlow<UiState<AuthResponse>> = _googleLoginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<AuthResponse>> = _registerState.asStateFlow()

    private val _verifyOtpState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)
    val verifyOtpState: StateFlow<UiState<AuthResponse>> = _verifyOtpState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val forgotPasswordState: StateFlow<UiState<String>> = _forgotPasswordState.asStateFlow()

    private val _testState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val testState: StateFlow<UiState<String>> = _testState.asStateFlow()

    fun testTransactionsAndEticket() {
        viewModelScope.launch {
            _testState.value = UiState.Loading
            try {
                val transactions = transactionRepository.getTransactions()
                Log.d("API_TEST", "Transactions count: ${transactions.data.size}")

                if (transactions.data.isNotEmpty()) {
                    val firstTx = transactions.data.first()
                    Log.d("API_TEST", "Testing Detail for Tx ID: ${firstTx.id}")

                    val detail = transactionRepository.getTransactionDetail(firstTx.id)
                    Log.d("API_TEST", "Tx Detail: Status=${detail.data.status}, Method=${detail.data.paymentType}")

                    Log.d("API_TEST", "Testing E-Ticket PDF download...")
                    val pdfBody = transactionRepository.downloadETicketPdf(firstTx.id)
                    val contentLength = pdfBody.contentLength()
                    Log.d("API_TEST", "PDF Download Success: Length=$contentLength bytes")
                    
                    _testState.value = UiState.Success("Transaction & E-Ticket tested! Check Logcat.")
                } else {
                    _testState.value = UiState.Success("No transactions found to test.")
                }
            } catch (e: Exception) {
                Log.e("API_TEST", "Error testing Transactions: ${e.message}", e)
                _testState.value = UiState.Error("Transaction Test failed: ${e.message}")
            }
        }
    }

    fun testMountainsAndRoutesDetail() {
        viewModelScope.launch {
            _testState.value = UiState.Loading
            try {
                // Merbabu ID = 2
                Log.d("API_TEST", "Testing Mountain Detail (ID: 2)...")
                val mountain = mountainRepository.getMountainDetail(2)
                Log.d("API_TEST", "Mountain: ${mountain.name}, Routes: ${mountain.routes?.size}")

                Log.d("API_TEST", "Testing Routes List...")
                val routes = routeRepository.getRoutes()
                if (routes.isNotEmpty()) {
                    val firstRoute = routes.first()
                    Log.d("API_TEST", "Testing Route Detail (ID: ${firstRoute.id})...")
                    val detail = routeRepository.getRouteDetail(firstRoute.id)
                    Log.d("API_TEST", "Route Detail: ${detail.name}, Waypoints: ${detail.waypoints?.size}")
                }

                _testState.value = UiState.Success("Mountains & Routes Details tested! Check Logcat.")
            } catch (e: Exception) {
                Log.e("API_TEST", "Error testing Details: ${e.message}", e)
                _testState.value = UiState.Error("Detail Test failed: ${e.message}")
            }
        }
    }

    fun testForgotPasswordFlow() {
        viewModelScope.launch {
            _testState.value = UiState.Loading
            try {
                val email = "test@example.com"
                Log.d("API_TEST", "Testing Forgot Password Code for $email...")
                val response = authRepository.sendForgotPasswordCode(email)
                Log.d("API_TEST", "Send Code Result: ${response.isSuccessful}")

                _testState.value = UiState.Success("Forgot Password flow started! Check Logcat.")
            } catch (e: Exception) {
                Log.e("API_TEST", "Error testing Forgot Pwd: ${e.message}", e)
                _testState.value = UiState.Error("Forgot Pwd Test failed: ${e.message}")
            }
        }
    }

    fun testProfileAndLogout() {
        viewModelScope.launch {
            _testState.value = UiState.Loading
            try {
                val user = authRepository.getUserProfile()
                Log.d("API_TEST", "Profile: ${user.name}, NIK: ${user.nik}")

                Log.d("API_TEST", "Testing Logout...")
                authRepository.logout()
                Log.d("API_TEST", "Logout Success. Local token cleared.")

                _testState.value = UiState.Success("Profile & Logout tested successfully!")
            } catch (e: Exception) {
                Log.e("API_TEST", "Error testing Profile/Logout: ${e.message}", e)
                _testState.value = UiState.Error("Profile/Logout Test failed: ${e.message}")
            }
        }
    }

    fun testAdditionalFeatures() {
        viewModelScope.launch {
            _testState.value = UiState.Loading
            try {
                Log.d("API_TEST", "Testing Validate NIK...")
                val nikResponse = authRepository.validateNik("1234567890123456")
                Log.d("API_TEST", "Validate NIK Result: Success=${nikResponse.isSuccessful}")

                Log.d("API_TEST", "Testing Update Profile...")
                val profileUpdate = mapOf("name" to "Updated Name", "phone_number" to "08123456789")
                val updateResponse = authRepository.updateUserProfile(profileUpdate)
                Log.d("API_TEST", "Update Profile Result: Success=${updateResponse.isSuccessful}")

                _testState.value = UiState.Success("NIK & Profile Update tested! Check Logcat.")
            } catch (e: Exception) {
                Log.e("API_TEST", "Error testing Additional Features: ${e.message}", e)
                _testState.value = UiState.Error("Test failed: ${e.message}")
            }
        }
    }

    fun testHikingSessions() {
        viewModelScope.launch {
            _testState.value = UiState.Loading
            try {
                val sessions = transactionRepository.getHikingSessions()
                Log.d("API_TEST", "Hiking Sessions count: ${sessions.size}")

                if (sessions.isNotEmpty()) {
                    val firstSession = sessions.first()
                    val sessionDetail = transactionRepository.getHikingSessionDetail(firstSession.id)
                    Log.d("API_TEST", "Session Detail Route: ${sessionDetail.route?.name}")
                }
                _testState.value = UiState.Success("Hiking Sessions tested successfully!")
            } catch (e: Exception) {
                Log.e("API_TEST", "Error testing Hiking Sessions: ${e.message}", e)
                _testState.value = UiState.Error("Test failed: ${e.message}")
            }
        }
    }

    fun testWeather() {
        viewModelScope.launch {
            _testState.value = UiState.Loading
            try {
                val mountainWeather = mountainRepository.getMountainWeather(2)
                if (mountainWeather.data != null) {
                    Log.d("API_TEST", "Weather: ${mountainWeather.mountain_name}, Temp: ${mountainWeather.data.current_weather.temperature}")
                }
                val routeWeather = routeRepository.getRouteWeather(4)
                Log.d("API_TEST", "Route Weather: ${routeWeather.route_name}")
                _testState.value = UiState.Success("Weather APIs tested successfully!")
            } catch (e: Exception) {
                Log.e("API_TEST", "Error testing Weather API: ${e.message}", e)
                _testState.value = UiState.Error("Weather Test failed: ${e.message}")
            }
        }
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val response = authRepository.login(request)
                _loginState.value = UiState.Success(response)
            } catch (e: HttpException) {
                _loginState.value = UiState.Error(if (e.code() == 401) "Email atau password salah." else "Server error: ${e.code()}")
            } catch (e: IOException) {
                _loginState.value = UiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                _loginState.value = UiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _googleLoginState.value = UiState.Loading
            Log.d("GOOGLE_AUTH", "Sending Google ID token to backend: $idToken")
            try {
                val response = authRepository.loginWithGoogle(idToken)
                Log.d("GOOGLE_AUTH", "Backend Google login successful: $response")
                _googleLoginState.value = UiState.Success(response)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("GOOGLE_AUTH", "HttpException from backend (code=${e.code()}): $errorBody", e)
                _googleLoginState.value = UiState.Error("Server error: ${e.code()}")
            } catch (e: IOException) {
                Log.e("GOOGLE_AUTH", "IOException connecting to backend", e)
                _googleLoginState.value = UiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                Log.e("GOOGLE_AUTH", "Generic Exception during Google Login", e)
                _googleLoginState.value = UiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun register(request: com.example.altiguide_mobile.data.model.RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            try {
                val response = authRepository.register(request)
                _registerState.value = UiState.Success(response)
            } catch (e: HttpException) {
                val errorMsg = if (e.code() == 422) "Email sudah terdaftar." else "Server error: ${e.code()}"
                _registerState.value = UiState.Error(errorMsg)
            } catch (e: IOException) {
                _registerState.value = UiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                _registerState.value = UiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun verifyRegisterOtp(email: String, code: String) {
        viewModelScope.launch {
            _verifyOtpState.value = UiState.Loading
            try {
                val response = authRepository.verifyRegisterOtp(email, code)
                _verifyOtpState.value = UiState.Success(response)
            } catch (e: HttpException) {
                _verifyOtpState.value = UiState.Error(if (e.code() == 422) "Kode OTP salah atau kedaluwarsa." else "Server error: ${e.code()}")
            } catch (e: IOException) {
                _verifyOtpState.value = UiState.Error("Tidak ada koneksi internet")
            } catch (e: Exception) {
                _verifyOtpState.value = UiState.Error("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun resendRegisterOtp(email: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = authRepository.resendRegisterOtp(email)
                if (response.isSuccessful) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("OTP_RESEND", "Failed to resend OTP: ${e.message}")
            }
        }
    }

    fun sendForgotPasswordCode(email: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _forgotPasswordState.value = UiState.Loading
            try {
                val response = authRepository.sendForgotPasswordCode(email)
                if (response.isSuccessful) {
                    _forgotPasswordState.value = UiState.Success("OTP Terkirim")
                    onSuccess()
                } else {
                    _forgotPasswordState.value = UiState.Error("Email tidak ditemukan.")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = UiState.Error("Gagal mengirim kode: ${e.message}")
            }
        }
    }

    fun verifyForgotPasswordCode(email: String, code: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _forgotPasswordState.value = UiState.Loading
            try {
                val response = authRepository.verifyForgotPasswordCode(email, code)
                if (response.isSuccessful) {
                    _forgotPasswordState.value = UiState.Success("OTP Terverifikasi")
                    onSuccess()
                } else {
                    _forgotPasswordState.value = UiState.Error("Kode OTP salah atau kedaluwarsa.")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = UiState.Error("Gagal memverifikasi kode: ${e.message}")
            }
        }
    }

    fun resetPassword(email: String, code: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _forgotPasswordState.value = UiState.Loading
            try {
                val response = authRepository.resetPassword(
                    mapOf(
                        "email" to email,
                        "code" to code,
                        "password" to password,
                        "password_confirmation" to password
                    )
                )
                if (response.isSuccessful) {
                    _forgotPasswordState.value = UiState.Success("Password Berhasil Direset")
                    onSuccess()
                } else {
                    _forgotPasswordState.value = UiState.Error("Gagal mereset password. Pastikan OTP valid.")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = UiState.Error("Gagal mereset password: ${e.message}")
            }
        }
    }

    fun testEndpoints() {
        viewModelScope.launch {
            _testState.value = UiState.Loading
            try {
                val mountains = mountainRepository.getMountains()
                Log.d("API_TEST", "Mountains count: ${mountains.size}")
                val user = authRepository.getUserProfile()
                Log.d("API_TEST", "User profile: ${user.name}")
                _testState.value = UiState.Success("All endpoints tested successfully!")
            } catch (e: Exception) {
                Log.e("API_TEST", "Error testing API: ${e.message}", e)
                _testState.value = UiState.Error("Test failed: ${e.message}")
            }
        }
    }

    fun resetStates() {
        _loginState.value = UiState.Idle
        _googleLoginState.value = UiState.Idle
        _registerState.value = UiState.Idle
        _verifyOtpState.value = UiState.Idle
        _forgotPasswordState.value = UiState.Idle
    }
}

