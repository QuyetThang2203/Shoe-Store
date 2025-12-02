package com.example.shoestore.domain.repository

import com.example.shoestore.domain.model.Product
import com.example.shoestore.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<Resource<List<Product>>>
    suspend fun getProductById(id: String): Resource<Product>

    // --- Admin Methods ---
    suspend fun addProduct(product: Product): Resource<Boolean>
    suspend fun deleteProduct(productId: String): Resource<Boolean>
    // suspend fun uploadImage(imageUri: Uri): Resource<String> // Tạm thời nhập URL text để đơn giản hoá

    suspend fun updateProduct(product: Product): Resource<Boolean>
}
