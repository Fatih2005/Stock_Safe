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
 */
class ProductRepository(private val context: Context) {

    private suspend fun <T> safeApiCall(
        block: suspend () -> com.flokin.stocksafe.model.ApiResponse<T>
    ): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                if (!NetworkHelper.isInternetAvailable(context)) {
                    return@withContext Result.Error(
                        "Tidak ada koneksi internet (WiFi/Data mati)."
                    )
                }

                if (SecureStorage.isTokenExpired(context)) {
                    return@withContext Result.Error(
                        "Sesi telah berakhir, silakan login ulang", 401
                    )
                }

                val response = block()

                if (response.success && response.data != null) {
                    Result.Success(response.data)
                } else {
                    Result.Error(
                        response.message.ifEmpty { "Terjadi kesalahan pada server" }
                    )
                }

            } catch (e: java.net.SocketTimeoutException) {
                Result.Error("Koneksi Timeout. Server terlalu lama merespons.")
            } catch (e: IOException) {
                // PERBAIKAN: Jangan hardcode pesannya!
                // Keluarkan pesan asli agar kita tahu jika ini masalah JSON atau Backend
                Result.Error("Error Sistem/Parsing: ${e.message}")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401  -> "Sesi habis, silakan login ulang"
                    404  -> "Data tidak ditemukan"
                    409  -> "Data sudah terdaftar"
                    500  -> "Terjadi kesalahan pada server (HTTP 500)"
                    else -> "HTTP Error: ${e.code()}"
                }
                Result.Error(errorMessage, e.code())
            } catch (e: Exception) {
                Result.Error(e.message ?: "Terjadi kesalahan yang tidak diketahui")
            }
        }
    }

    // ================================================
    // PRODUCT CRUD
    // ================================================

    suspend fun getAllProducts(
        search: String   = "",
        category: String = ""
    ): Result<List<Product>> {
        return safeApiCall {
            RetrofitClient.getInstance(context).getProducts(search, category)
        }
    }

    suspend fun getLowStockProducts(): Result<List<Product>> {
        return safeApiCall {
            RetrofitClient.getInstance(context).getLowStockProducts()
        }
    }

    suspend fun getProductById(id: Int): Result<Product> {
        return safeApiCall {
            RetrofitClient.getInstance(context).getProductById(id)
        }
    }

    suspend fun createProduct(product: Product): Result<Map<String, Int>> {
        return safeApiCall {
            RetrofitClient.getInstance(context).createProduct(product)
        }
    }

    suspend fun updateProduct(id: Int, product: Product): Result<Unit> {
        return safeApiCall {
            RetrofitClient.getInstance(context).updateProduct(id, product)
        }
    }

    suspend fun deleteProduct(id: Int): Result<Unit> {
        return safeApiCall {
            RetrofitClient.getInstance(context).deleteProduct(id)
        }
    }

    // ================================================
    // STOCK MANAGEMENT
    // ================================================

    suspend fun getStockLog(productId: Int = 0): Result<List<StockLog>> {
        return safeApiCall {
            RetrofitClient.getInstance(context).getStockLog(productId)
        }
    }

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