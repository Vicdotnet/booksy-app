package com.booksy.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksy.data.models.*
import com.booksy.data.remote.CountriesClient
import com.booksy.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class CheckoutUiState {
    object Idle : CheckoutUiState()
    object ProcessingPayment : CheckoutUiState()
    data class Success(val message: String) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

data class CheckoutFormState(
    val name: String = "",
    val address: String = "",
    val region: String = "",
    val phone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val nameError: String? = null,
    val addressError: String? = null,
    val regionError: String? = null,
    val phoneError: String? = null
)

class CheckoutViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(CheckoutFormState())
    val formState = _formState.asStateFlow()

    private val _countryInfo = MutableStateFlow<CountryResponse?>(null)
    val countryInfo = _countryInfo.asStateFlow()

    private val _isValidatingRegion = MutableStateFlow(false)
    val isValidatingRegion = _isValidatingRegion.asStateFlow()

    // Regiones de Chile
    val chileRegions = listOf(
        ChileRegion("Región Metropolitana", "RM"),
        ChileRegion("Valparaíso", "V"),
        ChileRegion("Biobío", "VIII"),
        ChileRegion("La Araucanía", "IX"),
        ChileRegion("Los Lagos", "X"),
        ChileRegion("Antofagasta", "II"),
        ChileRegion("Atacama", "III"),
        ChileRegion("Coquimbo", "IV"),
        ChileRegion("O'Higgins", "VI"),
        ChileRegion("Maule", "VII"),
        ChileRegion("Aysén", "XI"),
        ChileRegion("Magallanes", "XII"),
        ChileRegion("Arica y Parinacota", "XV"),
        ChileRegion("Tarapacá", "I"),
        ChileRegion("Ñuble", "XVI"),
        ChileRegion("Los Ríos", "XIV")
    )

    init {
        loadChileInfo()
    }

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(
            name = name,
            nameError = if (name.isBlank()) "el nombre es requerido" else null
        )
    }

    fun updateAddress(address: String) {
        _formState.value = _formState.value.copy(
            address = address,
            addressError = if (address.isBlank()) "la dirección es requerida" else null
        )
    }

    fun updateRegion(region: String) {
        _formState.value = _formState.value.copy(
            region = region,
            regionError = null
        )
        if (region.isNotBlank()) {
            validateRegion(region)
        }
    }

    fun updatePhone(phone: String) {
        _formState.value = _formState.value.copy(
            phone = phone,
            phoneError = if (phone.isBlank()) "el teléfono es requerido" 
                        else if (phone.length < 9) "teléfono inválido (mín 9 dígitos)" 
                        else null
        )
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _formState.value = _formState.value.copy(
            latitude = latitude,
            longitude = longitude
        )
    }

    private fun loadChileInfo() {
        viewModelScope.launch {
            try {
                val response = CountriesClient.api.getCountryByName("chile")
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    _countryInfo.value = response.body()!![0]
                }
            } catch (e: Exception) {
                // silently fail - country info is optional
            }
        }
    }

    private fun validateRegion(region: String) {
        viewModelScope.launch {
            _isValidatingRegion.value = true
            try {
                // Verificar que la región esté en la lista de regiones de Chile
                val isValid = chileRegions.any { 
                    it.name.equals(region, ignoreCase = true) || 
                    it.code.equals(region, ignoreCase = true)
                }
                
                if (!isValid) {
                    _formState.value = _formState.value.copy(
                        regionError = "región no válida para Chile"
                    )
                } else {
                    _formState.value = _formState.value.copy(
                        regionError = null
                    )
                }
            } catch (e: Exception) {
                // si falla la validación, no mostramos error
            } finally {
                _isValidatingRegion.value = false
            }
        }
    }

    fun processOrder(userId: String, cartItems: List<CartItem>, total: Double) {
        val form = _formState.value
        
        // Validaciones
        if (form.name.isBlank()) {
            _formState.value = form.copy(nameError = "el nombre es requerido")
            return
        }
        if (form.address.isBlank()) {
            _formState.value = form.copy(addressError = "la dirección es requerida")
            return
        }
        if (form.region.isBlank()) {
            _formState.value = form.copy(regionError = "la región es requerida")
            return
        }
        if (form.phone.isBlank() || form.phone.length < 9) {
            _formState.value = form.copy(phoneError = "teléfono inválido")
            return
        }

        viewModelScope.launch {
            _uiState.value = CheckoutUiState.ProcessingPayment
            
            try {
                // Simular procesamiento de pago (2 segundos)
                delay(2000)

                // Crear orden
                val orderItems = cartItems.map { 
                    OrderItem(
                        bookId = it.bookId,
                        title = "Libro", // título genérico por ahora
                        quantity = it.quantity,
                        price = it.pricePerUnit
                    )
                }

                val shippingInfo = ShippingInfo(
                    name = form.name,
                    address = form.address,
                    region = form.region,
                    phone = form.phone,
                    latitude = form.latitude,
                    longitude = form.longitude
                )

                val orderRequest = OrderRequest(
                    userId = userId,
                    items = orderItems,
                    total = total,
                    shippingInfo = shippingInfo
                )

                val response = RetrofitClient.api.createOrder(orderRequest)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Limpiar carrito
                    RetrofitClient.api.clearCart(userId)
                    
                    _uiState.value = CheckoutUiState.Success("¡Pago exitoso! Tu pedido está en camino")
                } else {
                    _uiState.value = CheckoutUiState.Error("error al procesar el pago")
                }
            } catch (e: Exception) {
                _uiState.value = CheckoutUiState.Error("error de conexión: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = CheckoutUiState.Idle
    }
}
