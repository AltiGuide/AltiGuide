package com.example.altiguide_mobile.data.model

data class AuthResponse(
    val message: String,
    val user: UserModel?,
    val token: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val password_confirmation: String,
    val phone_number: String
)

data class UserModel(
    val id: String,
    val name: String,
    val email: String,
    val phone_number: String?,
    val age: Int?,
    val address: String?,
    val emergency_contact: String?,
    val nik: String?,
    val avatar_url: String? = null,
    val image: String? = null
) {
    fun getAvatarModel(): Any? {
        val base64 = image
        if (!base64.isNullOrEmpty() && base64.startsWith("data:image")) {
            val bitmap = decodeBase64ToBitmapOrNull(base64)
            if (bitmap != null) return bitmap
        }
        val url = if (!avatar_url.isNullOrEmpty()) avatar_url else image
        if (url.isNullOrEmpty()) return null
        return url.replace("localhost", "10.0.2.2")
                  .replace("127.0.0.1", "10.0.2.2")
    }

    private fun decodeBase64ToBitmapOrNull(base64Str: String): android.graphics.Bitmap? {
        return try {
            val cleanString = if (base64Str.contains(",")) {
                base64Str.substring(base64Str.indexOf(",") + 1)
            } else {
                base64Str
            }
            val decodedBytes = android.util.Base64.decode(cleanString, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}

data class GoogleAuthRequest(
    val id_token: String
)

