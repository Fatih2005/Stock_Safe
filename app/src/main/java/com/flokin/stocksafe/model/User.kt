package com.flokin.stocksafe.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val role: String = ""
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginData(
    val token: String,
    val name: String,
    val role: String,
    @SerializedName("expires_at")
    val expiresAt: String
)

/**
 * Data class untuk request body register.
 * Dikirim ke endpoint api/register.php.
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirm")
    val passwordConfirm: String
)

/**
 * Data class untuk response setelah register sukses.
 */
data class RegisterData(
    val id: Int,
    val name: String,
    val role: String
)
