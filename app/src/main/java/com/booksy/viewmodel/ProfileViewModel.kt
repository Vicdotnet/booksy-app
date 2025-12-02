package com.booksy.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksy.data.local.AppDatabase
import com.booksy.data.local.UserEntity
import com.booksy.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(
    private val database: AppDatabase? = null
) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri = _profileImageUri.asStateFlow()

    init {
        loadUserData()
        loadUserFromApi()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            database?.userDao()?.getUser()?.collect { user ->
                _currentUser.value = user
                user?.profileImagePath?.let { path ->
                    _profileImageUri.value = Uri.parse(path)
                }
            }
        }
    }

    private fun loadUserFromApi() {
        viewModelScope.launch {
            try {
                val userId = _currentUser.value?.id ?: return@launch
                val response = RetrofitClient.api.getCurrentUser(userId)

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    database?.userDao()?.insertUser(
                        UserEntity(
                            id = user.id,
                            email = user.email,
                            name = user.name ?: "",
                            token = _currentUser.value?.token ?: "",
                            profileImagePath = _currentUser.value?.profileImagePath
                        )
                    )
                }
            } catch (e: Exception) {
                // no hacer nada si falla
            }
        }
    }

    fun createImageUri(context: Context): Uri {
        val imageFile = File(context.filesDir, "profile_temp.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    fun saveProfileImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val fileName = "profile_${System.currentTimeMillis()}.jpg"
                val destinationFile = File(context.filesDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destinationFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val savedUri = Uri.fromFile(destinationFile)
                _profileImageUri.value = savedUri

                _currentUser.value?.let { user ->
                    database?.userDao()?.updateProfileImage(
                        user.id,
                        savedUri.toString()
                    )
                }
            } catch (e: Exception) {
                // no hacer nada si falla
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            database?.userDao()?.deleteUser()
        }
    }
}