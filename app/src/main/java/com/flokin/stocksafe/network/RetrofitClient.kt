package com.flokin.stocksafe.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton object untuk membuat dan mengelola instance Retrofit.
 * Menggunakan pola Singleton agar hanya ada satu instance
 * Retrofit sepanjang aplikasi berjalan.
 */
object RetrofitClient {

    // ================================================
    // BASE URL — SESUAIKAN DENGAN ENVIRONMENT KAMU
    // ================================================
    // Untuk emulator Android: 10.0.2.2 = localhost komputer
    // Untuk HP fisik: ganti dengan IP komputer di jaringan yang sama
    // Cek IP: buka terminal Laragon → ketik ipconfig → lihat IPv4
    // ================================================
    private const val BASE_URL = "http://10.0.2.2/stocksafe_api/"
    // Jika pakai HP fisik, ganti dengan:
    // private const val BASE_URL = "http://192.168.1.xxx/stocksafe_api/"

    // Instance ApiService yang bisa digunakan (volatile = thread-safe)
    @Volatile
    private var instance: ApiService? = null

    /**
     * Mendapatkan instance ApiService.
     * Membuat instance baru jika belum ada (lazy initialization).
     * @param context Context aplikasi untuk SecureStorage
     */
    fun getInstance(context: Context): ApiService {
        return instance ?: synchronized(this) {
            instance ?: createApiService(context).also {
                instance = it
            }
        }
    }

    /**
     * Reset instance — dipanggil saat logout atau token berubah.
     * Agar interceptor menggunakan token terbaru di request berikutnya.
     */
    fun resetInstance() {
        instance = null
    }

    /**
     * Membuat instance ApiService baru dengan konfigurasi lengkap.
     */
    private fun createApiService(context: Context): ApiService {

        // ================================================
        // LOGGING INTERCEPTOR (untuk debugging)
        // Di production, set ke NONE
        // ================================================
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
            // Untuk production: level = HttpLoggingInterceptor.Level.NONE
        }

        // ================================================
        // OKHTTP CLIENT
        // Menggabungkan semua interceptor dan konfigurasi timeout
        // ================================================
        val okHttpClient = OkHttpClient.Builder()
            // Interceptor autentikasi (X-API-KEY + Bearer Token)
            .addInterceptor(AuthInterceptor(context))
            // Interceptor logging untuk debugging
            .addInterceptor(loggingInterceptor)
            // Timeout koneksi: 30 detik
            .connectTimeout(30, TimeUnit.SECONDS)
            // Timeout membaca response: 30 detik
            .readTimeout(30, TimeUnit.SECONDS)
            // Timeout menulis request: 30 detik
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // ================================================
        // RETROFIT BUILDER
        // ================================================
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // Converter: mengubah JSON ↔ Kotlin data class otomatis
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}