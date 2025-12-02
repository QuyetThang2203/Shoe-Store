package com.example.shoestore.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val role: String = "user", // "admin" hoặc "user"
    val avatarUrl: String? = null
)
