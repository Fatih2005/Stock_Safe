package com.flokin.stocksafe.util

/**
 * Sealed class untuk merepresentasikan hasil operasi asynchronous.
 * Digunakan di ViewModel untuk mengirim state ke UI.
 *
 * Keuntungan sealed class:
 * - Type-safe: compiler tahu semua kemungkinan subclass
 * - when expression bisa memeriksa semua case
 * - Tidak perlu boolean flag (loading/success/error) terpisah
 */
sealed class Result<out T> {

    /** State loading: operasi sedang berjalan, tidak membawa data */
    object Loading : Result<Nothing>()

    /** State sukses: operasi berhasil, membawa data hasil */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * State error: operasi gagal.
     * @param message Pesan error untuk ditampilkan ke user
     * @param code HTTP status code (-1 jika bukan error HTTP)
     */
    data class Error(
        val message: String,
        val code: Int = -1
    ) : Result<Nothing>()
}