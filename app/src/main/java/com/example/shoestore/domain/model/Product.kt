package com.example.shoestore.domain.model

// Sử dụng Data Class với giá trị mặc định là cách tốt nhất để tránh NullPointerException
data class Product(
    val id: String = "",
    val name: String = "No Name", // Tên mặc định nếu thiếu
    val price: Double = 0.0,
    val description: String = "",
    val brand: String = "",
    val imageUrl: String = "", // Nếu rỗng, UI sẽ hiện placeholder
    val sizes: List<Int> = emptyList(),
    val colors: List<String> = emptyList(),
    val stock: Int = 0
)