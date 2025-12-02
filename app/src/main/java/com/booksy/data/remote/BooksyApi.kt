package com.booksy.data.remote

import com.booksy.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface BooksyApi {

    // auth endpoints
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("auth/me/{userId}")
    suspend fun getCurrentUser(@Path("userId") userId: Long): Response<User>

    // books endpoints
    @GET("books")
    suspend fun getAllBooks(): Response<List<Book>>

    @GET("books/{id}")
    suspend fun getBookById(@Path("id") id: Long): Response<Book>

    @GET("books/category/{category}")
    suspend fun getBooksByCategory(@Path("category") category: String): Response<List<Book>>

    // cart endpoints
    @GET("cart/user/{userId}")
    suspend fun getCartItems(@Path("userId") userId: Long): Response<List<CartItem>>

    @POST("cart")
    suspend fun addToCart(@Body cartItem: CartItem): Response<CartItem>

    @PUT("cart/{id}")
    suspend fun updateCartItem(@Path("id") id: Long, @Body request: Map<String, Int>): Response<CartItem>

    @DELETE("cart/{id}")
    suspend fun deleteCartItem(@Path("id") id: Long): Response<String>

    @GET("cart/total/{userId}")
    suspend fun getCartTotal(@Path("userId") userId: Long): Response<CartTotal>

    @DELETE("cart/clear/{userId}")
    suspend fun clearCart(@Path("userId") userId: Long): Response<String>
}
