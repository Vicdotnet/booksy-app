package com.booksy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksy.data.local.AppDatabase
import com.booksy.data.local.UserEntity
import com.booksy.data.models.RegisterRequest
import com.booksy.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val message: String) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(
    private val database: AppDatabase? = null
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError = _nameError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError = _confirmPasswordError.asStateFlow()

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onNameChange(newName: String) {
        _name.value = newName
        validateName(newName)
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        validateEmail(newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        validatePassword(newPassword)
        if (_confirmPassword.value.isNotBlank()) {
            validateConfirmPassword(_confirmPassword.value)
        }
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _confirmPassword.value = newConfirmPassword
        validateConfirmPassword(newConfirmPassword)
    }

    private fun validateName(name: String) {
        _nameError.value = when {
            name.isBlank() -> "El nombre es requerido"
            name.length < 2 -> "Minimo 2 caracteres"
            else -> null
        }
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

    private fun validateConfirmPassword(confirmPassword: String) {
        _confirmPasswordError.value = when {
            confirmPassword.isBlank() -> "Confirma tu contraseña"
            confirmPassword != _password.value -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    fun register() {
        validateName(_name.value)
        validateEmail(_email.value)
        validatePassword(_password.value)
        validateConfirmPassword(_confirmPassword.value)

        if (_nameError.value != null ||
            _emailError.value != null ||
            _passwordError.value != null ||
            _confirmPasswordError.value != null) {
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            try {
                val request = RegisterRequest(
                    email = _email.value,
                    password = _password.value,
                    name = _name.value
                )

                val response = RetrofitClient.api.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    database?.userDao()?.insertUser(
                        UserEntity(
                            id = authResponse.userId,
                            email = _email.value,
                            name = _name.value,
                            token = authResponse.authToken,
                            profileImagePath = null
                        )
                    )

                    _uiState.value = RegisterUiState.Success("Registro exitoso")
                } else {
                    _uiState.value = RegisterUiState.Error("Error al registrar")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error("Error de conexion: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}