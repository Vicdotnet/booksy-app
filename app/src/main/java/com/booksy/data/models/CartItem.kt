package com.booksy.data.models

data class CartItem(
    val id: Long? = null,
    val userId: Long,
    val bookId: Long,
    val quantity: Int,
    val pricePerUnit: Double
)