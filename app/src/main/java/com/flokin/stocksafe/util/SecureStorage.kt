package com.flokin.stocksafe.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Utility untuk menyimpan data sensitif secara terenkripsi.
 * Menggunakan EncryptedSharedPreferences + Android Keystore.
 * Data yang disimpan: API Key, Token, Username, Role, Token Expiry.
 */
object SecureStorage {

    // Nama file SharedPreferences (tersimpan terenkripsi)
    private const val PREFS_NAME = "secure_stocksafe_prefs"

    // Key untuk setiap data yang disimpan
    private const val KEY_API_KEY   = "api_key"
    private const val KEY_TOKEN     = "auth_token"
    private const val KEY_USERNAME  = "username"
    private const val KEY_ROLE      = "user_role"
    private const val KEY_TOKEN_EXP = "token_exp"

    // API Key default — disimpan terenkripsi, bukan hardcode di kode lain
    private const val DEFAULT_API_KEY = "StockSafe_SecureKey_2026_RKS2"

    /**
     * Membuat instance EncryptedSharedPreferences.
     * MasterKey disimpan di Android Keystore (tidak bisa diekstrak).
     * Enkripsi: AES256-GCM untuk value, AES256-SIV untuk key.
     */
    private fun getEncryptedPrefs(ctx: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(ctx)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            ctx,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Mendapatkan API Key tersimpan.
     * Jika belum ada, simpan dan kembalikan default.
     */
    fun getApiKey(ctx: Context): String {
        val prefs     = getEncryptedPrefs(ctx)
        val storedKey = prefs.getString(KEY_API_KEY, null)

        if (storedKey.isNullOrEmpty()) {
            prefs.edit().putString(KEY_API_KEY, DEFAULT_API_KEY).apply()
            return DEFAULT_API_KEY
        }

        return storedKey
    }

    /**
     * Menyimpan Bearer Token setelah login sukses.
     */
    fun saveToken(ctx: Context, token: String) {
        getEncryptedPrefs(ctx).edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    /**
     * Mendapatkan Bearer Token tersimpan.
     */
    fun getToken(ctx: Context): String {
        return getEncryptedPrefs(ctx).getString(KEY_TOKEN, "") ?: ""
    }

    /**
     * Menyimpan informasi user (nama dan role) setelah login.
     */
    fun saveUserInfo(ctx: Context, name: String, role: String) {
        getEncryptedPrefs(ctx).edit()
            .putString(KEY_USERNAME, name)
            .putString(KEY_ROLE, role)
            .apply()
    }

    /**
     * Mendapatkan nama user yang sedang login.
     */
    fun getUsername(ctx: Context): String {
        return getEncryptedPrefs(ctx).getString(KEY_USERNAME, "User") ?: "User"
    }

    /**
     * Mendapatkan role user yang sedang login.
     */
    fun getRole(ctx: Context): String {
        return getEncryptedPrefs(ctx).getString(KEY_ROLE, "staff") ?: "staff"
    }

    /**
     * Menyimpan waktu expired token.
     */
    fun saveTokenExp(ctx: Context, exp: String) {
        getEncryptedPrefs(ctx).edit()
            .putString(KEY_TOKEN_EXP, exp)
            .apply()
    }

    /**
     * Mengecek apakah user sudah login (token ada dan belum expired).
     */
    fun isLoggedIn(ctx: Context): Boolean {
        val token = getToken(ctx)
        return token.isNotEmpty() && !isTokenExpired(ctx)
    }

    /**
     * Mengecek apakah token sudah expired.
     */
    fun isTokenExpired(ctx: Context): Boolean {
        val expStr = getEncryptedPrefs(ctx)
            .getString(KEY_TOKEN_EXP, null) ?: return true

        return try {
            val formatter = java.time.format.DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
            val expTime = java.time.LocalDateTime.parse(expStr, formatter)
            java.time.LocalDateTime.now().isAfter(expTime)
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Menghapus semua data tersimpan (digunakan saat logout).
     */
    fun clearAll(ctx: Context) {
        getEncryptedPrefs(ctx).edit().clear().apply()
    }
}