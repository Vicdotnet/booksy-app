package com.booksy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksy.data.local.AppDatabase
import com.booksy.data.local.UserEntity
import com.booksy.data.models.LoginRequest
import com.booksy.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val message: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val database: AppDatabase? = null
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        validateEmail(newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        validatePassword(newPassword)
    }

    private fun validateEmail(email: String) {
        _emailError.value = when {
            email.isBlank() -> "El email es requerido"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email invalido"
            else -> null
        }
    }

    private fun validatePassword(password: String) {
        _passwordError.value = when {
            password.isBlank() -> "La contraseña es requerida"
            password.length < 8 -> "Minimo 8 caracteres"
            else -> null
        }
    }

    fun login() {
        validateEmail(_email.value)
        validatePassword(_password.value)

        if (_emailError.value != null || _passwordError.value != null) {
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                val request = LoginRequest(
                    email = _email.value,
                    password = _password.value
                )

                val response = RetrofitClient.api.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    database?.userDao()?.insertUser(
                        UserEntity(
                            id = authResponse.userId,
                            email = _email.value,
                            name = "",
                            token = authResponse.authToken,
                            profileImagePath = null
                        )
                    )

                    _uiState.value = LoginUiState.Success("Login exitoso")
                } else {
                    _uiState.value = LoginUiState.Error("Email o contraseña incorrectos")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Error de conexion: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}