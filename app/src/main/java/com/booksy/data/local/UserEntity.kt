package com.booksy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: Long,
    val email: String,
    val name: String,
    val token: String,
    val profileImagePath: String?
)
