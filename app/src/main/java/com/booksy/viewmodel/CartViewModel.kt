package com.booksy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksy.data.models.CartItem
import com.booksy.data.models.CartTotal
import com.booksy.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CartUiState {
    object Loading : CartUiState()
    data class Success(val items: List<CartItem>, val total: CartTotal) : CartUiState()
    data class Error(val message: String) : CartUiState()
}

class CartViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadCart(userId: Long) {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading

            try {
                val itemsResponse = RetrofitClient.api.getCartItems(userId)
                val totalResponse = RetrofitClient.api.getCartTotal(userId)

                if (itemsResponse.isSuccessful && totalResponse.isSuccessful) {
                    val items = itemsResponse.body() ?: emptyList()
                    val total = totalResponse.body() ?: CartTotal(0.0, 0.0, 0.0)

                    _uiState.value = CartUiState.Success(items, total)
                } else {
                    _uiState.value = CartUiState.Error("Error al cargar carrito")
                }
            } catch (e: Exception) {
                _uiState.value = CartUiState.Error("Error de conexion: ${e.message}")
            }
        }
    }

    fun removeItem(itemId: Long, userId: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.deleteCartItem(itemId)
                loadCart(userId)
            } catch (e: Exception) {
                _uiState.value = CartUiState.Error("Error al eliminar: ${e.message}")
            }
        }
    }

    fun clearCart(userId: Long) {
        viewModelScope.launch {
            try {
                RetrofitClient.api.clearCart(userId)
                loadCart(userId)
            } catch (e: Exception) {
                _uiState.value = CartUiState.Error("Error al limpiar: ${e.message}")
            }
        }
    }
}
