package com.booksy.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.booksy.data.local.SessionManager
import com.booksy.data.models.CartItem
import com.booksy.viewmodel.CheckoutViewModel
import com.booksy.viewmodel.CheckoutUiState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartItems: List<CartItem>,
    total: Double,
    onNavigateBack: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: CheckoutViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getUserId() ?: ""

    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val countryInfo by viewModel.countryInfo.collectAsState()
    val isValidatingRegion by viewModel.isValidatingRegion.collectAsState()

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(uiState) {
        if (uiState is CheckoutUiState.Success) {
            onOrderSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalizar Compra") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is CheckoutUiState.ProcessingPayment -> {
                    ProcessingPaymentAnimation()
                }
                else -> {
                    CheckoutForm(
                        formState = formState,
                        countryInfo = countryInfo,
                        regions = viewModel.chileRegions,
                        isValidatingRegion = isValidatingRegion,
                        total = total,
                        itemCount = cartItems.size,
                        hasLocationPermission = locationPermissionState.status.isGranted,
                        onNameChange = viewModel::updateName,
                        onAddressChange = viewModel::updateAddress,
                        onRegionChange = viewModel::updateRegion,
                        onPhoneChange = viewModel::updatePhone,
                        onRequestLocation = {
                            if (locationPermissionState.status.isGranted) {
                                getCurrentLocation(context) { lat, lng ->
                                    viewModel.updateLocation(lat, lng)
                                }
                            } else {
                                locationPermissionState.launchPermissionRequest()
                            }
                        },
                        onConfirmOrder = {
                            viewModel.processOrder(userId, cartItems, total)
                        },
                        error = if (state is CheckoutUiState.Error) state.message else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutForm(
    formState: com.booksy.viewmodel.CheckoutFormState,
    countryInfo: com.booksy.data.models.CountryResponse?,
    regions: List<com.booksy.data.models.ChileRegion>,
    isValidatingRegion: Boolean,
    total: Double,
    itemCount: Int,
    hasLocationPermission: Boolean,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onRequestLocation: () -> Unit,
    onConfirmOrder: () -> Unit,
    error: String?
) {
    var showRegionMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Información de Chile (API externa)
        countryInfo?.let { info ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = info.flag,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = "Envío a ${info.name.common}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        info.currencies?.entries?.firstOrNull()?.let { currency ->
                            Text(
                                text = "Moneda: ${currency.value.name} (${currency.value.symbol})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Resumen del pedido
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Resumen del Pedido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$itemCount ${if (itemCount == 1) "libro" else "libros"}")
                    Text(
                        text = "$${String.format("%.0f", total)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Formulario de envío
        Text(
            text = "Información de Envío",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Nombre
        OutlinedTextField(
            value = formState.name,
            onValueChange = onNameChange,
            label = { Text("Nombre completo") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            isError = formState.nameError != null,
            supportingText = formState.nameError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Dirección
        OutlinedTextField(
            value = formState.address,
            onValueChange = onAddressChange,
            label = { Text("Dirección") },
            leadingIcon = { Icon(Icons.Default.Home, null) },
            isError = formState.addressError != null,
            supportingText = formState.addressError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Región (Dropdown)
        ExposedDropdownMenuBox(
            expanded = showRegionMenu,
            onExpandedChange = { showRegionMenu = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            OutlinedTextField(
                value = formState.region,
                onValueChange = {},
                readOnly = true,
                label = { Text("Región") },
                leadingIcon = { Icon(Icons.Default.Place, null) },
                trailingIcon = { 
                    Row {
                        if (isValidatingRegion) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRegionMenu)
                    }
                },
                isError = formState.regionError != null,
                supportingText = formState.regionError?.let { { Text(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = showRegionMenu,
                onDismissRequest = { showRegionMenu = false }
            ) {
                regions.forEach { region ->
                    DropdownMenuItem(
                        text = { Text("${region.name} (${region.code})") },
                        onClick = {
                            onRegionChange(region.name)
                            showRegionMenu = false
                        }
                    )
                }
            }
        }

        // Teléfono
        OutlinedTextField(
            value = formState.phone,
            onValueChange = onPhoneChange,
            label = { Text("Teléfono") },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = formState.phoneError != null,
            supportingText = formState.phoneError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Botón GPS
        Button(
            onClick = onRequestLocation,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                if (formState.latitude != null) 
                    "Ubicación: ${String.format("%.4f", formState.latitude)}, ${String.format("%.4f", formState.longitude)}"
                else "Usar mi ubicación"
            )
        }

        // Error message
        error?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Botón de confirmar compra
        Button(
            onClick = onConfirmOrder,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                "Confirmar Compra - $${String.format("%.0f", total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProcessingPaymentAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "payment")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Procesando pago...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Por favor espera",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(context: android.content.Context, onLocationReceived: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val cancellationTokenSource = CancellationTokenSource()
    
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
    ).addOnSuccessListener { location ->
        location?.let {
            onLocationReceived(it.latitude, it.longitude)
        }
    }
}
