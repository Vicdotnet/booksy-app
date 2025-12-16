package com.booksy.data.models

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("_id")
    val id: String? = null,
    val userId: String,
    val items: List<OrderItem>,
    val total: Double,
    val shippingInfo: ShippingInfo,
    val status: String = "completed",
    val createdAt: String? = null
)

data class OrderItem(
    val bookId: String,
    val title: String,
    val quantity: Int,
    val price: Double
)

data class ShippingInfo(
    val name: String,
    val address: String,
    val region: String,
    val phone: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class OrderRequest(
    val userId: String,
    val items: List<OrderItem>,
    val total: Double,
    val shippingInfo: ShippingInfo
)

data class OrderResponse(
    val success: Boolean,
    val message: String,
    val order: Order? = null
)
