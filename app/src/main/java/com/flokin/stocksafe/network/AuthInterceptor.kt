package com.flokin.stocksafe.network

import android.content.Context
import com.flokin.stocksafe.util.SecureStorage
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor untuk menambahkan header autentikasi
 * secara otomatis ke setiap request yang dikirim ke server.
 *
 * Header yang ditambahkan:
 * 1. X-API-KEY   : Identifikasi aplikasi client
 * 2. Authorization: Bearer Token untuk autentikasi user
 * 3. Content-Type: application/json untuk semua request
 *
 * Juga menangani response 401 (token expired/invalid):
 * - Hapus token lokal dari SecureStorage
 * - User akan diarahkan ke login saat cek isLoggedIn()
 */
class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // Ambil request asli
        val originalRequest = chain.request()

        // Buat builder untuk memodifikasi request
        val requestBuilder = originalRequest.newBuilder()

        // ================================================
        // TAMBAHKAN HEADER X-API-KEY
        // Wajib untuk semua request ke API StockSafe
        // ================================================
        val apiKey = SecureStorage.getApiKey(context)
        requestBuilder.addHeader("X-API-KEY", apiKey)

        // ================================================
        // TAMBAHKAN HEADER AUTHORIZATION BEARER TOKEN
        // Hanya jika token tersedia (sudah login)
        // ================================================
        val token = SecureStorage.getToken(context)
        if (token.isNotEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // Tambahkan Content-Type untuk semua request
        requestBuilder.addHeader("Content-Type", "application/json")

        // Bangun request yang sudah dimodifikasi
        val modifiedRequest = requestBuilder.build()

        // Kirim request dan terima response
        val response = chain.proceed(modifiedRequest)

        // ================================================
        // HANDLE RESPONSE 401 (TOKEN EXPIRED/INVALID)
        // Hapus token lokal agar user diarahkan ke login
        // ================================================
        if (response.code == 401) {
            SecureStorage.clearAll(context)
        }

        return response
    }
}