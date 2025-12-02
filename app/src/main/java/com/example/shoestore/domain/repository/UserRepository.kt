package com.example.shoestore.domain.repository

import com.example.shoestore.domain.model.User
import com.example.shoestore.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // Admin dùng: Lấy tất cả user
    fun getAllUsers(): Flow<Resource<List<User>>>

    // User dùng: Lấy thông tin bản thân
    suspend fun getUserProfile(uid: String): Resource<User>
}