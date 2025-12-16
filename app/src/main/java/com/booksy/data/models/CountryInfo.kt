package com.booksy.data.models

import com.google.gson.annotations.SerializedName

// Respuesta de REST Countries API
data class CountryResponse(
    val name: CountryName,
    val capital: List<String>?,
    val region: String,
    val subregion: String?,
    val currencies: Map<String, Currency>?,
    val flag: String
)

data class CountryName(
    val common: String,
    val official: String
)

data class Currency(
    val name: String,
    val symbol: String
)

// Modelo simplificado para la app
data class ChileRegion(
    val name: String,
    val code: String
)
