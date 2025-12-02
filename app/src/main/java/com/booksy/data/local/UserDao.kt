package com.booksy.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUserOnce(): UserEntity?

    @Query("UPDATE user SET profileImagePath = :imagePath WHERE id = :userId")
    suspend fun updateProfileImage(userId: Long, imagePath: String)

    @Query("DELETE FROM user")
    suspend fun deleteUser()
}
