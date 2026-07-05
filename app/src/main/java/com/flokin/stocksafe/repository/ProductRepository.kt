package com.flokin.stocksafe.repository

import android.content.Context
import com.flokin.stocksafe.model.Product
import com.flokin.stocksafe.model.StockLog
import com.flokin.stocksafe.model.StockRequest
import com.flokin.stocksafe.network.RetrofitClient
import com.flokin.stocksafe.util.NetworkHelper
import com.flokin.stocksafe.util.Result
import com.flokin.stocksafe.util.SecureStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository untuk operasi CRUD produk dan manajemen stok.
 * Menggunakan higher-order function safeApiCall untuk menghindari
 * duplikasi kode try-catch di setiap fungsi.
 */
class ProductRepository(private val context: Context) {

    /**
     * Generic function untuk menangani error pada semua operasi API.
     * Menggunakan higher-order function (block) sebagai parameter.
     *
     * @param block Fungsi API yang akan dijalankan (suspend)
     * @return Result.Success atau Result.Error
     */
    private suspend fun <T> safeApiCall(
        block: suspend () -> com.flokin.stocksafe.model.ApiResponse<T>
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                // Cek koneksi internet sebelum request
                if (!NetworkHelper.isInternetAvailable(context)) {
                    return@withContext Result.Error(
                        "Tidak ada koneksi internet. Periksa koneksi Anda."
                    )
                }

                // Cek token sebelum melakukan request
                if (SecureStorage.isTokenExpired(context)) {
                    return@withContext Result.Error(
                        "Sesi telah berakhir, silakan login ulang", 401
                    )
                }

                // Eksekusi block API
                val response = block()

                if (response.success && response.data != null) {
                    Result.Success(response.data)
                } else {
                    Result.Error(
                        response.message.ifEmpty { "Terjadi kesalahan" }
                    )
                }

            } catch (e: IOException) {
                Result.Error("Tidak ada koneksi internet. Periksa koneksi Anda.")

            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401  -> "Sesi habis, silakan login ulang"
                    404  -> "Data tidak ditemukan"
                    409  -> "Data sudah terdaftar"
                    500  -> "Terjadi kesalahan pada server"
                    else -> "HTTP Error: ${e.code()}"
                }
                Result.Error(errorMessage, e.code())

            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    // ================================================
    // PRODUCT CRUD
    // ================================================

    /**
     * Mendapatkan semua produk dengan filter pencarian dan kategori.
     */
    suspend fun getAllProducts(
        search: String   = "",
        category: String = ""
    ): Result<List<Product>> {
        return safeApiCall {
            RetrofitClient.getInstance(context).getProducts(search, category)
        }
    }

    /**
     * Mendapatkan produk yang stoknya menipis (stock <= stock_min).
     */
    suspend fun getLowStockProducts(): Result<List<Product>> {
        return safeApiCall {
            RetrofitClient.getInstance(context).getLowStockProducts()
        }
    }

    /**
     * Mendapatkan detail satu produk berdasarkan ID.
     */
    suspend fun getProductById(id: Int): Result<Product> {
        return safeApiCall {
            RetrofitClient.getInstance(context).getProductById(id)
        }
    }

    /**
     * Menambahkan produk baru ke database.
     */
    suspend fun createProduct(product: Product): Result<Map<String, Int>> {
        return safeApiCall {
            RetrofitClient.getInstance(context).createProduct(product)
        }
    }

    /**
     * Mengupdate data produk berdasarkan ID.
     */
    suspend fun updateProduct(id: Int, product: Product): Result<Unit> {
        return safeApiCall {
            RetrofitClient.getInstance(context).updateProduct(id, product)
        }
    }

    /**
     * Menghapus produk berdasarkan ID.
     */
    suspend fun deleteProduct(id: Int): Result<Unit> {
        return safeApiCall {
            RetrofitClient.getInstance(context).deleteProduct(id)
        }
    }

    // ================================================
    // STOCK MANAGEMENT
    // ================================================

    /**
     * Mendapatkan riwayat transaksi stok.
     * @param productId Jika > 0, filter berdasarkan produk tertentu
     */
    suspend fun getStockLog(productId: Int = 0): Result<List<StockLog>> {
        return safeApiCall {
            RetrofitClient.getInstance(context).getStockLog(productId)
        }
    }

    /**
     * Input stok masuk.
     * @param productId ID produk
     * @param quantity Jumlah stok masuk
     * @param note Keterangan transaksi (opsional)
     */
    suspend fun stockIn(
        productId: Int,
        quantity: Int,
        note: String = ""
    ): Result<com.flokin.stocksafe.model.StockUpdateData> {
        return safeApiCall {
            RetrofitClient.getInstance(context).updateStock(
                StockRequest(
                    productId = productId,
                    type      = "in",
                    quantity  = quantity,
                    note      = note
                )
            )
        }
    }

    /**
     * Input stok keluar.
     * @param productId ID produk
     * @param quantity Jumlah stok keluar
     * @param note Keterangan transaksi (opsional)
     */
    suspend fun stockOut(
        productId: Int,
        quantity: Int,
        note: String = ""
    ): Result<com.flokin.stocksafe.model.StockUpdateData> {
        return safeApiCall {
            RetrofitClient.getInstance(context).updateStock(
                StockRequest(
                    productId = productId,
                    type      = "out",
                    quantity  = quantity,
                    note      = note
                )
            )
        }
    }
}