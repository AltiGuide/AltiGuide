package com.example.altiguide_mobile.data.repository

import com.example.altiguide_mobile.data.model.AuthResponse
import com.example.altiguide_mobile.data.model.LoginRequest
import com.example.altiguide_mobile.data.model.RegisterRequest
import com.example.altiguide_mobile.data.model.UserModel
import com.example.altiguide_mobile.data.network.AltiGuideApiService
import com.example.altiguide_mobile.util.AuthDataStore
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: AltiGuideApiService,
    private val authDataStore: AuthDataStore
) {
    suspend fun login(request: LoginRequest): AuthResponse {
        val response = apiService.login(request)
        response.token?.let {
            authDataStore.saveToken(it)
        }
        return response
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        return apiService.register(request)
    }

    suspend fun verifyRegisterOtp(email: String, code: String): AuthResponse {
        val response = apiService.verifyRegisterOtp(mapOf("email" to email, "code" to code))
        response.token?.let {
            authDataStore.saveToken(it)
        }
        return response
    }

    suspend fun resendRegisterOtp(email: String): Response<Any> {
        return apiService.resendRegisterOtp(mapOf("email" to email))
    }

    suspend fun logout() {
        try {
            apiService.logout()
        } finally {
            authDataStore.clearToken()
        }
    }

    suspend fun getUserProfile(): UserModel {
        return apiService.getUserProfile()
    }

    suspend fun updateUserProfile(request: Map<String, @JvmSuppressWildcards Any>): Response<AuthResponse> {
        return apiService.updateUserProfile(request)
    }

    suspend fun changePassword(request: Map<String, String>): Response<AuthResponse> {
        return apiService.changePassword(request)
    }

    suspend fun validateNik(
        nik: String,
        startDate: String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
        hikeType: String = "camp"
    ): Response<Any> {
        return apiService.validateNik(
            mapOf(
                "nik" to nik,
                "start_date" to startDate,
                "hike_type" to hikeType
            )
        )
    }

    suspend fun sendForgotPasswordCode(email: String): Response<Any> {
        return apiService.sendForgotPasswordCode(mapOf("email" to email))
    }

    suspend fun verifyForgotPasswordCode(email: String, code: String): Response<Any> {
        return apiService.verifyForgotPasswordCode(mapOf("email" to email, "code" to code))
    }

    suspend fun resetPassword(request: Map<String, String>): Response<Any> {
        return apiService.resetPassword(request)
    }
}
