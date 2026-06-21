package com.example.altiguide_mobile.data.repository

import android.content.Context
import com.example.altiguide_mobile.data.model.AuthResponse
import com.example.altiguide_mobile.data.model.LoginRequest
import com.example.altiguide_mobile.data.model.RegisterRequest
import com.example.altiguide_mobile.data.model.UserModel
import com.example.altiguide_mobile.data.model.GoogleAuthRequest
import com.example.altiguide_mobile.data.network.AltiGuideApiService
import com.example.altiguide_mobile.util.AuthDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: AltiGuideApiService,
    private val authDataStore: AuthDataStore,
    @ApplicationContext private val context: Context
) {
    private fun getSafeEmailKey(email: String): String {
        return email.lowercase().trim().replace("@", "_").replace(".", "_")
    }

    private fun loadUsersFromAssets(): List<UserModel> {
        return try {
            val jsonString = context.assets.open("users.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<UserModel>>() {}.type
            Gson().fromJson<List<UserModel>>(jsonString, listType) ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error loading users from assets", e)
            emptyList()
        }
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val email = request.email.trim()
        
        // 1. Try Firebase Realtime Database first (for updated synced data)
        val safeKey = getSafeEmailKey(email)
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val gson = Gson()
        val url = "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/users/$safeKey.json"
        val firebaseRequest = okhttp3.Request.Builder().url(url).build()

        val firebaseUser = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                client.newCall(firebaseRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty() && body != "null") {
                            gson.fromJson(body, UserModel::class.java)
                        } else null
                    } else null
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Error checking firebase user", e)
                null
            }
        }

        if (firebaseUser != null) {
            authDataStore.saveAuthData("mock_token", firebaseUser.email)
            return AuthResponse("Login successful (Firebase)", firebaseUser, "mock_token")
        }

        // 2. Fallback to local users
        val localUser = loadUsersFromAssets().find { it.email.equals(email, ignoreCase = true) }
        if (localUser != null) {
            authDataStore.saveAuthData("mock_token", localUser.email)
            return AuthResponse("Login successful (Offline)", localUser, "mock_token")
        }

        throw Exception("Email atau password salah.")
    }

    private fun getEmailFromIdToken(idToken: String): String? {
        return try {
            val parts = idToken.split(".")
            if (parts.size >= 2) {
                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
                val json = org.json.JSONObject(payload)
                json.optString("email")
            } else null
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error parsing ID token email", e)
            null
        }
    }

    private fun getNameFromIdToken(idToken: String): String? {
        return try {
            val parts = idToken.split(".")
            if (parts.size >= 2) {
                val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
                val json = org.json.JSONObject(payload)
                json.optString("name")
            } else null
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error parsing ID token name", e)
            null
        }
    }

    suspend fun loginWithGoogle(idToken: String): AuthResponse {
        val email = getEmailFromIdToken(idToken)
        val name = getNameFromIdToken(idToken)

        if (email.isNullOrBlank()) {
            throw Exception("Gagal mendapatkan email dari Google ID Token.")
        }

        // 1. Try Firebase Realtime Database first
        val safeKey = getSafeEmailKey(email)
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val gson = Gson()
        val url = "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/users/$safeKey.json"
        
        val firebaseRequest = okhttp3.Request.Builder().url(url).build()
        val firebaseUser = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                client.newCall(firebaseRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty() && body != "null") {
                            gson.fromJson(body, UserModel::class.java)
                        } else null
                    } else null
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Error checking firebase user during Google login", e)
                null
            }
        }

        if (firebaseUser != null) {
            authDataStore.saveAuthData("mock_token", firebaseUser.email)
            return AuthResponse("Login Google Berhasil (Firebase)", firebaseUser, "mock_token")
        }

        // 2. Try local users
        val localUsers = loadUsersFromAssets()
        val matchedUser = localUsers.find { it.email.equals(email, ignoreCase = true) }
        if (matchedUser != null) {
            authDataStore.saveAuthData("mock_token", matchedUser.email)
            return AuthResponse("Login Google Berhasil", matchedUser, "mock_token")
        }

        // 3. Create new user in Firebase if not found (first time Google login)
        val newUser = UserModel(
            id = java.util.UUID.randomUUID().toString(),
            name = name ?: "Google User",
            email = email,
            phone_number = null,
            age = null,
            address = null,
            emergency_contact = null,
            nik = null
        )

        val userJson = gson.toJson(newUser)
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = userJson.toRequestBody(mediaType)
        val writeRequest = okhttp3.Request.Builder().url(url).put(requestBody).build()

        val success = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                client.newCall(writeRequest).execute().use { response ->
                    response.isSuccessful
                }
            } catch (e: Exception) {
                android.util.Log.e("AuthRepository", "Error creating firebase user via Google", e)
                false
            }
        }

        if (success) {
            authDataStore.saveAuthData("mock_token", newUser.email)
            return AuthResponse("Registrasi Google Berhasil", newUser, "mock_token")
        }

        // Fallback to local profile if Firebase write fails
        authDataStore.saveAuthData("mock_token", newUser.email)
        return AuthResponse("Login Google Berhasil (Offline)", newUser, "mock_token")
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        return apiService.register(request)
    }

    suspend fun verifyRegisterOtp(email: String, code: String): AuthResponse {
        val response = apiService.verifyRegisterOtp(mapOf("email" to email, "code" to code))
        response.token?.let { token ->
            authDataStore.saveAuthData(token, email)
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
        val email = authDataStore.authEmailFlow.first()
        if (email.isEmpty()) {
            throw IOException("No user logged in")
        }

        // 1. Try to fetch from Firebase first (for fresh, synced data)
        val safeKey = getSafeEmailKey(email)
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val gson = Gson()
        val url = "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/users/$safeKey.json"
        val firebaseRequest = okhttp3.Request.Builder().url(url).build()

        val firebaseUser = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                client.newCall(firebaseRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty() && body != "null") {
                            gson.fromJson(body, UserModel::class.java)
                        } else null
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }

        if (firebaseUser != null) {
            val localCacheJson = authDataStore.userProfileFlow.first()
            val freshJson = gson.toJson(firebaseUser)
            if (localCacheJson != freshJson) {
                android.util.Log.d("AuthRepository", "Local user profile cache is different or empty, updating cache.")
                authDataStore.saveUserProfile(freshJson)
            }
            return firebaseUser
        }

        // 2. Fallback to local cache in DataStore first
        val cachedProfileJson = authDataStore.userProfileFlow.first()
        if (cachedProfileJson.isNotEmpty()) {
            try {
                val cachedUser = gson.fromJson(cachedProfileJson, UserModel::class.java)
                if (cachedUser != null) {
                    android.util.Log.d("AuthRepository", "Loaded user profile from local cache (offline).")
                    return cachedUser
                }
            } catch (e: Exception) {
                // ignore and fallback
            }
        }

        // 3. Fallback to local asset if offline or not in Firebase and no cache exists
        val localUser = loadUsersFromAssets().find { it.email.equals(email, ignoreCase = true) }
        if (localUser != null) {
            return localUser
        }

        throw IOException("User profile not found")
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
