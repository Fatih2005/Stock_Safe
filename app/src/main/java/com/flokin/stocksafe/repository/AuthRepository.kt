package com.flokin.stocksafe.repository

import android.content.Context
import com.flokin.stocksafe.model.LoginRequest
import com.flokin.stocksafe.model.RegisterData
import com.flokin.stocksafe.model.RegisterRequest
import com.flokin.stocksafe.network.RetrofitClient
import com.flokin.stocksafe.util.NetworkHelper
import com.flokin.stocksafe.util.Result
import com.flokin.stocksafe.util.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(private val context: Context) {

    suspend fun login(email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkHelper.isInternetAvailable(context)) {
                    return@withContext Result.Error("Tidak ada koneksi internet. Periksa koneksi Anda.")
                }

                val apiService = RetrofitClient.getInstance(context)
                val response   = apiService.login(LoginRequest(email, password))

                if (response.success && response.data != null) {
                    val data = response.data

                    SecureStorage.saveToken(context, data.token)
                    SecureStorage.saveUserInfo(context, data.name, data.role)
                    SecureStorage.saveTokenExp(context, data.expiresAt)
                    RetrofitClient.resetInstance()

                    Result.Success(data.name)

                } else {
                    Result.Error(response.message.ifEmpty { "Login gagal" })
                }

            } catch (e: IOException) {
                Result.Error("Tidak ada koneksi internet. Periksa koneksi Anda.")

            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    400  -> "Email atau password tidak valid"
                    401  -> "Email atau password salah"
                    404  -> "Server tidak ditemukan"
                    500  -> "Terjadi kesalahan pada server"
                    else -> "Server error: ${e.code()}"
                }
                Result.Error(errorMessage, e.code())

            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan tak terduga")
            }
        }
    }

    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                try {
                    val apiService = RetrofitClient.getInstance(context)
                    apiService.logout()
                } catch (e: Exception) {
                    // Abaikan error network saat logout
                }

                SecureStorage.clearAll(context)
                RetrofitClient.resetInstance()

                Result.Success(Unit)

            } catch (e: Exception) {
                Result.Error(e.message ?: "Logout gagal")
            }
        }
    }

    suspend fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirm: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkHelper.isInternetAvailable(context)) {
                    return@withContext Result.Error("Tidak ada koneksi internet. Periksa koneksi Anda.")
                }

                val apiService = RetrofitClient.getInstance(context)
                val response   = apiService.register(
                    RegisterRequest(
                        name            = name,
                        email           = email,
                        password        = password,
                        passwordConfirm = passwordConfirm
                    )
                )

                if (response.success && response.data != null) {
                    Result.Success(response.data.name)
                } else {
                    Result.Error(response.message.ifEmpty { "Registrasi gagal" })
                }

            } catch (e: IOException) {
                Result.Error("Tidak ada koneksi internet. Periksa koneksi Anda.")

            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    400  -> "Data registrasi tidak valid"
                    409  -> "Email sudah terdaftar"
                    500  -> "Terjadi kesalahan pada server"
                    else -> "Server error: ${e.code()}"
                }
                Result.Error(errorMessage, e.code())

            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan tak terduga")
            }
        }
    }
}