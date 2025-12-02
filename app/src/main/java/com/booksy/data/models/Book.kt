package com.booksy.data.models

data class Book(
    val id: Long,
    val title: String,
    val author: String,
    val price: Double,
    val category: String,
    val imageUrl: String
)