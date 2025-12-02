package com.example.shoestore.domain.model

import java.util.Date

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val status: String = "PENDING", // PENDING, SHIPPING, DELIVERED, CANCELLED
    val address: String = "",
    val createdAt: Date = Date()
)
