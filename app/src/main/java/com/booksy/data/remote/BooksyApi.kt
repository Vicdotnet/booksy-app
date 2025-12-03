package com.booksy.data.remote

import com.booksy.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface BooksyApi {

    // auth endpoints
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/signup")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/auth/me/{userId}")
    suspend fun getCurrentUser(@Path("userId") userId: String): Response<User>

    // books endpoints
    @GET("api/books")
    suspend fun getAllBooks(): Response<List<Book>>

    @GET("api/books/{id}")
    suspend fun getBookById(@Path("id") id: String): Response<Book>

    @GET("api/books/category/{category}")
    suspend fun getBooksByCategory(@Path("category") category: String): Response<List<Book>>

    // cart endpoints
    @GET("api/cart/user/{userId}")
    suspend fun getCartItems(@Path("userId") userId: String): Response<List<CartItem>>

    @POST("api/cart")
    suspend fun addToCart(@Body cartItem: CartItem): Response<CartItem>

    @PUT("api/cart/{id}")
    suspend fun updateCartItem(@Path("id") id: String, @Body request: Map<String, Int>): Response<CartItem>

    @DELETE("api/cart/{id}")
    suspend fun deleteCartItem(@Path("id") id: String): Response<String>

    @GET("api/cart/total/{userId}")
    suspend fun getCartTotal(@Path("userId") userId: String): Response<CartTotal>

    @DELETE("api/cart/clear/{userId}")
    suspend fun clearCart(@Path("userId") userId: String): Response<String>
}
