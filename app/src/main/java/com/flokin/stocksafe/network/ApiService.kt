package com.flokin.stocksafe.network

import com.flokin.stocksafe.model.ApiResponse
import com.flokin.stocksafe.model.LoginData
import com.flokin.stocksafe.model.LoginRequest
import com.flokin.stocksafe.model.Product
import com.flokin.stocksafe.model.RegisterData
import com.flokin.stocksafe.model.RegisterRequest
import com.flokin.stocksafe.model.StockLog
import com.flokin.stocksafe.model.StockRequest
import com.flokin.stocksafe.model.StockUpdateData
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {

    @POST("api/login.php")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<LoginData>

    @POST("api/logout.php")
    suspend fun logout(): ApiResponse<Unit>

    @GET("api/products.php")
    suspend fun getProducts(
        @Query("search")   search: String   = "",
        @Query("category") category: String = ""
    ): ApiResponse<List<Product>>

    @GET("api/products.php")
    suspend fun getLowStockProducts(
        @Query("low_stock") lowStock: Int = 1
    ): ApiResponse<List<Product>>

    @POST("api/products.php")
    suspend fun createProduct(
        @Body product: Product
    ): ApiResponse<Map<String, Int>>

    @GET("api/product.php")
    suspend fun getProductById(
        @Query("id") id: Int
    ): ApiResponse<Product>

    @PUT("api/product.php")
    suspend fun updateProduct(
        @Query("id") id: Int,
        @Body product: Product
    ): ApiResponse<Unit>

    @DELETE("api/product.php")
    suspend fun deleteProduct(
        @Query("id") id: Int
    ): ApiResponse<Unit>

    @GET("api/stock.php")
    suspend fun getStockLog(
        @Query("product_id") productId: Int = 0
    ): ApiResponse<List<StockLog>>

    @POST("api/stock.php")
    suspend fun updateStock(
        @Body request: StockRequest
    ): ApiResponse<StockUpdateData>

    /**
 * Registrasi user baru.
 * Tidak memerlukan Bearer Token (public endpoint).
 * @param request Body berisi name, email, password, password_confirm
 * @return ApiResponse berisi id dan nama user baru
 */
    @POST("api/register.php")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<RegisterData>
}