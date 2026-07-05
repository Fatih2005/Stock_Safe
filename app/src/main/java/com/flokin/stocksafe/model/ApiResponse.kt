package com.flokin.stocksafe.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val data: T? = null
)

data class StockRequest(
    @SerializedName("product_id")
    val productId: Int,
    val type: String,
    val quantity: Int,
    val note: String = ""
)

data class StockUpdateData(
    @SerializedName("stock_now")
    val stockNow: Int,
    @SerializedName("is_low_stock")
    val isLowStock: Boolean
)