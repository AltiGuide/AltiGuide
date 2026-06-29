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
        if (!image.isNullOrEmpty() && image.startsWith("data:image")) {
            return image
        }
        if (!image.isNullOrEmpty() && image.startsWith("/")) {
            return java.io.File(image)
        }
        val url = if (!avatar_url.isNullOrEmpty()) avatar_url else image
        if (url.isNullOrEmpty()) return null
        return url.replace("localhost", "10.0.2.2")
                  .replace("127.0.0.1", "10.0.2.2")
    }
}

data class GoogleAuthRequest(
    val id_token: String
)

