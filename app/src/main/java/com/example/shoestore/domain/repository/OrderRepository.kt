package com.example.shoestore.domain.repository

import com.example.shoestore.domain.model.CartItem
import com.example.shoestore.domain.model.Order
import com.example.shoestore.util.Resource
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    // Tạo đơn hàng mới từ danh sách trong giỏ
    suspend fun placeOrder(items: List<CartItem>, address: String, totalPrice: Double): Resource<String>

    // Lấy lịch sử đơn hàng của User hiện tại
    fun getOrderHistory(): Flow<Resource<List<Order>>>

    // --- Admin Methods ---
    fun getAllOrders(): Flow<Resource<List<Order>>> // Admin xem tất cả
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Resource<Boolean>
}