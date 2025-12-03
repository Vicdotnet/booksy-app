package com.booksy.data.models

import com.google.gson.annotations.SerializedName

data class CartItem(
    @SerializedName("_id")
    val id: String? = null,
    val userId: String,
    val bookId: String,
    val quantity: Int,
    val pricePerUnit: Double
)