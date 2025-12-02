package com.example.shoestore.domain.repository

import com.example.shoestore.domain.model.CartItem
import com.example.shoestore.util.Resource
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<Resource<List<CartItem>>>
    suspend fun addToCart(item: CartItem): Resource<Boolean>
    suspend fun removeFromCart(cartItemId: String): Resource<Boolean>
}
