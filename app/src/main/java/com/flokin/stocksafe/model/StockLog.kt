package com.flokin.stocksafe.model

import com.google.gson.annotations.SerializedName

data class StockLog(
    val id: Int = 0,
    @SerializedName("product_id")
    val productId: Int = 0,
    @SerializedName("product_name")
    val productName: String = "",
    val type: String = "",
    val quantity: Int = 0,
    val note: String = "",
    @SerializedName("user_name")
    val userName: String = "",
    @SerializedName("created_at")
    val createdAt: String = ""
) {
    fun isStockIn(): Boolean = type == "in"
}