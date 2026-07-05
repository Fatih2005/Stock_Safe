package com.flokin.stocksafe.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int = 0,
    val name: String = "",
    val sku: String = "",
    val category: String = "",
    val supplier: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    @SerializedName("stock_min")
    val stockMin: Int = 5,
    val unit: String = "pcs",
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("is_low_stock")
    val isLowStock: Boolean = false
) {
    fun getStockStatus(): StockStatus = when {
        stock <= 0        -> StockStatus.OUT
        stock <= stockMin -> StockStatus.LOW
        else              -> StockStatus.OK
    }
}

enum class StockStatus { OK, LOW, OUT }