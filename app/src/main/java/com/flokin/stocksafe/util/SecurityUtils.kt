package com.flokin.stocksafe.util

import java.io.File

/**
 * Utility untuk deteksi keamanan device.
 * Digunakan di MainActivity untuk memeriksa apakah device di-root.
 */
object SecurityUtils {

    // Daftar path yang mengindikasikan device telah di-root
    private val ROOT_INDICATORS = listOf(
        "/system/app/Superuser.apk",
        "/system/app/SuperSU.apk",
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/data/local/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su"
    )

    /**
     * Mendeteksi apakah device sudah di-root.
     * Menggunakan dua metode deteksi untuk akurasi lebih baik.
     * @return true jika device terdeteksi rooted, false jika aman
     */
    fun isDeviceRooted(): Boolean {
        // Metode 1: Cek keberadaan file binary su
        for (path in ROOT_INDICATORS) {
            if (File(path).exists()) {
                return true
            }
        }

        // Metode 2: Coba eksekusi perintah 'which su'
        return try {
            val process  = Runtime.getRuntime()
                .exec(arrayOf("/system/xbin/which", "su"))
            val exitCode = process.waitFor()
            // Exit code 0 berarti perintah berhasil (su ditemukan)
            exitCode == 0
        } catch (e: Exception) {
            // Jika error, anggap tidak rooted
            false
        }
    }
}