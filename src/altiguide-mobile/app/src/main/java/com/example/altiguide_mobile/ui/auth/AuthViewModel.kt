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
}
