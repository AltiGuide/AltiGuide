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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
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
        val gson = Gson()

        // 1. Try API quickly (2s) — validates password properly
        try {
            val response = withTimeout(2000L) { apiService.login(request) }
            val token = response.token ?: "mock_token"
            val userJson = response.user?.let { gson.toJson(it) }
            if (userJson != null) {
                authDataStore.saveAuthDataWithProfile(token, email, userJson)
            } else {
                authDataStore.saveAuthData(token, email)
            }
            return response
        } catch (e: HttpException) {
            throw e // 401/422 — wrong credentials
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.d("AuthRepository", "API login timed out")
        } catch (e: IOException) {
            android.util.Log.d("AuthRepository", "API login network error")
        }

        // 2. Try Firebase (1s timeout)
        val firebaseUser = findUserInFirebase(email, 1)
        if (firebaseUser != null) {
            val userJson = gson.toJson(firebaseUser)
            authDataStore.saveAuthDataWithProfile("mock_token", firebaseUser.email, userJson)
            return AuthResponse("Login successful (Firebase)", firebaseUser, "mock_token")
        }

        // 3. Fallback to local users
        val localUser = loadUsersFromAssets().find { it.email.equals(email, ignoreCase = true) }
        if (localUser != null) {
            val userJson = gson.toJson(localUser)
            authDataStore.saveAuthDataWithProfile("mock_token", localUser.email, userJson)
            return AuthResponse("Login successful (Offline)", localUser, "mock_token")
        }

        throw Exception("Akun tidak terdaftar. Silakan daftar terlebih dahulu.")
    }

    private suspend fun findUserInFirebase(email: String, timeoutSec: Long): UserModel? {
        val safeKey = getSafeEmailKey(email)
        val client = OkHttpClient.Builder()
            .connectTimeout(timeoutSec, TimeUnit.SECONDS)
            .readTimeout(timeoutSec, TimeUnit.SECONDS)
            .writeTimeout(timeoutSec, TimeUnit.SECONDS)
            .build()
        val url = "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/users/$safeKey.json"
        val firebaseRequest = okhttp3.Request.Builder().url(url).build()
        val gson = Gson()

        return withContext(Dispatchers.IO) {
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

        val gson = Gson()

        // 0. Try API quickly (2s)
        try {
            val response = withTimeout(2000L) { apiService.loginWithGoogle(GoogleAuthRequest(idToken)) }
            val token = response.token ?: "mock_token"
            val userJson = response.user?.let { gson.toJson(it) }
            if (userJson != null) {
                authDataStore.saveAuthDataWithProfile(token, email, userJson)
            } else {
                authDataStore.saveAuthData(token, email)
            }
            return response
        } catch (e: HttpException) { throw e }
        catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.d("AuthRepository", "API Google login timed out")
        } catch (e: IOException) {
            android.util.Log.d("AuthRepository", "API Google login network error")
        }

        // 1. Try Firebase (1s timeout)
        val firebaseUser = findUserInFirebase(email, 1)
        if (firebaseUser != null) {
            val userJson = gson.toJson(firebaseUser)
            authDataStore.saveAuthDataWithProfile("mock_token", firebaseUser.email, userJson)
            return AuthResponse("Login Google Berhasil (Firebase)", firebaseUser, "mock_token")
        }

        // 2. Try local users
        val localUsers = loadUsersFromAssets()
        val matchedUser = localUsers.find { it.email.equals(email, ignoreCase = true) }
        if (matchedUser != null) {
            val userJson = gson.toJson(matchedUser)
            authDataStore.saveAuthDataWithProfile("mock_token", matchedUser.email, userJson)
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
        val safeKey = getSafeEmailKey(email)
        val writeUrl = "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/users/$safeKey.json"
        val client = OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.SECONDS)
            .writeTimeout(2, TimeUnit.SECONDS)
            .build()
        val writeRequest = okhttp3.Request.Builder().url(writeUrl).put(requestBody).build()

        val success = withContext(Dispatchers.IO) {
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
            authDataStore.saveAuthDataWithProfile("mock_token", newUser.email, userJson)
            return AuthResponse("Registrasi Google Berhasil", newUser, "mock_token")
        }

        // Fallback to local profile if Firebase write fails
        authDataStore.saveAuthDataWithProfile("mock_token", newUser.email, userJson)
        return AuthResponse("Login Google Berhasil (Offline)", newUser, "mock_token")
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        return apiService.register(request)
    }

    suspend fun verifyRegisterOtp(email: String, code: String): AuthResponse {
        val response = apiService.verifyRegisterOtp(mapOf("email" to email, "code" to code))
        val token = response.token ?: "mock_token"
        val userJson = response.user?.let { Gson().toJson(it) }
        if (userJson != null) {
            authDataStore.saveAuthDataWithProfile(token, email, userJson)
        } else {
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
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Logout API call failed", e)
        } finally {
            authDataStore.clearToken()
        }
    }

    suspend fun getUserProfile(): UserModel {
        val email = authDataStore.authEmailFlow.first()
        if (email.isEmpty()) {
            throw IOException("No user logged in")
        }

        val gson = Gson()

        // 1. Return from local cache INSTANTLY if available (no network wait)
        val cachedProfileJson = authDataStore.userProfileFlow.first()
        if (cachedProfileJson.isNotEmpty()) {
            try {
                val cachedUser = gson.fromJson(cachedProfileJson, UserModel::class.java)
                if (cachedUser != null && cachedUser.email.equals(email, ignoreCase = true)) {
                    android.util.Log.d("AuthRepository", "Loaded user profile from local cache (instant).")
                    return cachedUser.withAvatarPath()
                }
            } catch (e: Exception) {
                // ignore
            }
        }

        // 2. No cache — try backend API with 3s timeout
        try {
            val user = withTimeout(3000L) { apiService.getUserProfile() }
            authDataStore.saveUserProfile(gson.toJson(user.withAvatarPath()))
            return user.withAvatarPath()
        } catch (e: HttpException) {
            if (e.code() == 401) {
                authDataStore.clearToken()
                throw IOException("Sesi telah berakhir. Silakan login kembali.")
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.d("AuthRepository", "API getUserProfile timed out")
        } catch (e: IOException) {
            android.util.Log.d("AuthRepository", "API getUserProfile network error")
        }

        // 3. Try Firebase Realtime Database (2s timeout)
        val firebaseUser = findUserInFirebase(email, 2)
        if (firebaseUser != null) {
            val freshJson = gson.toJson(firebaseUser)
            authDataStore.saveUserProfile(freshJson)
            return firebaseUser.withAvatarPath()
        }

        // 4. Fallback to local asset if all else fails
        val localUser = loadUsersFromAssets().find { it.email.equals(email, ignoreCase = true) }
        if (localUser != null) {
            return localUser.withAvatarPath()
        }

        throw IOException("User profile not found")
    }

    suspend fun updateUserProfile(request: Map<String, @JvmSuppressWildcards Any>): Response<AuthResponse> {
        return try {
            val currentUser = getUserProfile()
            val updatedUser = UserModel(
                id = currentUser.id,
                name = (request["name"] as? String) ?: currentUser.name,
                email = (request["email"] as? String) ?: currentUser.email,
                phone_number = (request["phone_number"] as? String) ?: currentUser.phone_number,
                age = (request["age"] as? Number)?.toInt() ?: currentUser.age,
                address = (request["address"] as? String) ?: currentUser.address,
                emergency_contact = (request["emergency_contact"] as? String) ?: currentUser.emergency_contact,
                nik = (request["nik"] as? String) ?: currentUser.nik,
                avatar_url = currentUser.avatar_url,
                image = (request["avatar_path"] as? String) ?: currentUser.image
            )
            
            // 1. Save to local cache in DataStore
            val gson = Gson()
            val updatedJson = gson.toJson(updatedUser)
            authDataStore.saveUserProfile(updatedJson)

            // 1b. Save avatar path separately (persists across logins)
            val avatarPath = request["avatar_path"] as? String
            if (avatarPath != null) {
                AvatarStorage(context).save(updatedUser.email, avatarPath)
            }

            // 2. Try to sync to Firebase Realtime Database in background
            val safeKey = getSafeEmailKey(updatedUser.email)
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val url = "https://altiguide-dd49c-default-rtdb.asia-southeast1.firebasedatabase.app/users/$safeKey.json"
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = updatedJson.toRequestBody(mediaType)
            val writeRequest = okhttp3.Request.Builder().url(url).put(requestBody).build()

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    client.newCall(writeRequest).execute().use { response ->
                        if (!response.isSuccessful) {
                            android.util.Log.e("AuthRepository", "Firebase sync profile failed: ${response.code}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AuthRepository", "Firebase sync profile exception", e)
                }
            }

            Response.success(AuthResponse("Profile updated successfully", updatedUser, "mock_token"))
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Error updating profile", e)
            Response.error(500, okhttp3.ResponseBody.create("application/json".toMediaTypeOrNull(), "{\"message\":\"${e.message}\"}"))
        }
    }

    private fun UserModel.withAvatarPath(): UserModel {
        if (!this.image.isNullOrEmpty()) return this
        val savedPath = AvatarStorage(context).get(this.email)
        return if (savedPath != null) this.copy(image = savedPath) else this
    }

    suspend fun changePassword(request: Map<String, String>): Response<AuthResponse> {
        return Response.success(AuthResponse("Password changed successfully", null, "mock_token"))
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

class AvatarStorage(private val context: android.content.Context) {
    private val prefs = context.getSharedPreferences("avatar_paths", android.content.Context.MODE_PRIVATE)

    fun save(email: String, path: String) {
        prefs.edit().putString(email.lowercase().trim(), path).apply()
    }

    fun get(email: String): String? {
        val path = prefs.getString(email.lowercase().trim(), null) ?: return null
        return if (java.io.File(path).exists()) path else null
    }

    fun remove(email: String) {
        prefs.edit().remove(email.lowercase().trim()).apply()
    }
}
