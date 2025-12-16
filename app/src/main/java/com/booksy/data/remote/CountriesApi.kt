package com.booksy.data.remote

import com.booksy.data.models.CountryResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface CountriesApi {
    
    @GET("v3.1/name/{country}")
    suspend fun getCountryByName(@Path("country") country: String): Response<List<CountryResponse>>
    
    @GET("v3.1/alpha/{code}")
    suspend fun getCountryByCode(@Path("code") code: String): Response<List<CountryResponse>>
}

object CountriesClient {
    private const val BASE_URL = "https://restcountries.com/"
    
    val api: CountriesApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CountriesApi::class.java)
    }
}
