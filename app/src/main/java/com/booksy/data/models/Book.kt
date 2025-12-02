package com.booksy.data.models

import com.google.gson.annotations.SerializedName

data class Book(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val author: String,
    val price: Double,
    val category: String,
    val imageUrl: String,
    val description: String? = null
)