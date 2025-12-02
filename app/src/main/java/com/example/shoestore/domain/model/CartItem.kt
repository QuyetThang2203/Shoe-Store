package com.example.shoestore.domain.model

data class CartItem(
    val id: String = "", // Document ID
    val productId: String = "",
    val productName: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 1,
    val selectedSize: Int = 0,
    val selectedColor: String = ""
)
