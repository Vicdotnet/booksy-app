package com.booksy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.booksy.data.local.SessionManager
import com.booksy.viewmodel.CartViewModel
import com.booksy.viewmodel.CartUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    viewModel: CartViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val userId = SessionManager.getInstance(context).getUserId()

    LaunchedEffect(Unit) {
        userId?.let {
            viewModel.loadCart(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is CartUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is CartUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is CartUiState.Success -> {
                    if (state.items.isEmpty()) {
                        Text(
                            text = "Carrito vacio",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.items) { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Libro ID: ${item.bookId}",
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = "Cantidad: ${item.quantity}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "$${String.format("%,.0f", item.pricePerUnit).replace(",", ".")}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    item.id?.let { id ->
                                                        userId?.let { uid ->
                                                            viewModel.removeItem(id, uid)
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, "eliminar")
                                            }
                                        }
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Total:",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = "$${String.format("%,.0f", state.total.subtotal).replace(",", ".")}",
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = onNavigateToCheckout,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(
                                            "Ir a Pagar",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedButton(
                                        onClick = {
                                            userId?.let {
                                                viewModel.clearCart(it)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Limpiar carrito")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
