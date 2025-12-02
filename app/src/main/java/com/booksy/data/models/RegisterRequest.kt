package com.booksy.data.models

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)