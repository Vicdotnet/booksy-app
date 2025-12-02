package com.booksy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.booksy.data.local.SessionManager
import com.booksy.data.models.CartItem
import com.booksy.data.remote.RetrofitClient
import com.booksy.viewmodel.BookDetailViewModel
import com.booksy.viewmodel.BookDetailUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit = {},
    viewModel: BookDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del libro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "volver")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is BookDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is BookDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is BookDetailUiState.Success -> {
                    val book = state.book
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Título
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Autor
                        Text(
                            text = "Por: ${book.author}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Categoría
                        Surface(
                            color = Color(0xFFE8EAF6),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Text(
                                text = book.category,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF6200ea)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Precio
                        Text(
                            text = "$ ${String.format("%,.0f", book.price).replace(",", ".")}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFF6200ea),
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Sinopsis
                        Text(
                            text = "Sinopsis",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = book.description ?: "Sin sinopsis disponible",
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.5
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Botón Agregar al carrito
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val userId = SessionManager.getInstance(context).getUserId()
                                        if (userId != null) {
                                            val cartItem = CartItem(
                                                userId = userId,
                                                bookId = book.id,
                                                quantity = 1,
                                                pricePerUnit = book.price
                                            )
                                            val response = RetrofitClient.api.addToCart(cartItem)
                                            if (response.isSuccessful) {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Libro agregado al carrito",
                                                    actionLabel = "Ver carrito",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    onNavigateToCart()
                                                }
                                            } else {
                                                snackbarHostState.showSnackbar(
                                                    message = "Error al agregar al carrito",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(
                                            message = "Error: ${e.message}",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4caf50)
                            )
                        ) {
                            Icon(Icons.Default.ShoppingCart, "agregar")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar al carrito", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
