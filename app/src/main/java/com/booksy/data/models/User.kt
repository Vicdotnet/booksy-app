package com.booksy.data.models
data class User(
    val id: Long,
    val email: String,
    val name: String,
    val password: String? = null
)
