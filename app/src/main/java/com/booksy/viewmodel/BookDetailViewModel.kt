package com.booksy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksy.data.models.Book
import com.booksy.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BookDetailUiState {
    object Loading : BookDetailUiState()
    data class Success(val book: Book) : BookDetailUiState()
    data class Error(val message: String) : BookDetailUiState()
}

class BookDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            _uiState.value = BookDetailUiState.Loading
            try {
                val response = RetrofitClient.api.getBookById(bookId)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = BookDetailUiState.Success(response.body()!!)
                } else {
                    _uiState.value = BookDetailUiState.Error("Error al cargar libro")
                }
            } catch (e: Exception) {
                _uiState.value = BookDetailUiState.Error("Error de conexion: ${e.message}")
            }
        }
    }
}
