package com.example.shoestore.domain.repository

import com.example.shoestore.domain.model.User
import com.example.shoestore.util.Resource

interface AuthRepository {
    val currentUser: User? // Lấy user hiện tại (nếu đã login)

    suspend fun login(email: String, password: String): Resource<User>

    suspend fun register(email: String, password: String, fullName: String): Resource<User>

    fun logout()
}
