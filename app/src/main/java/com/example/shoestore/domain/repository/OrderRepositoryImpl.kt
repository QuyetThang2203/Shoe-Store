package com.example.shoestore.data.repository

import com.example.shoestore.domain.model.CartItem
import com.example.shoestore.domain.model.Order
import com.example.shoestore.domain.repository.OrderRepository
import com.example.shoestore.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : OrderRepository {

    override suspend fun placeOrder(items: List<CartItem>, address: String, totalPrice: Double): Resource<String> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")

        return try {
            val orderId = db.collection("orders").document().id

            // Tạo object Order
            val order = Order(
                id = orderId,
                userId = uid,
                items = items,
                totalPrice = totalPrice,
                address = address,
                createdAt = Date(),
                status = "PENDING" // Mặc định là Chờ xác nhận
            )

            // Dùng Batch để đảm bảo Atomic: Tạo Order xong thì Xóa Cart
            db.runBatch { batch ->
                // 1. Ghi vào collection orders
                val orderRef = db.collection("orders").document(orderId)
                batch.set(orderRef, order)

                // 2. Xóa từng item trong collection cart của user
                items.forEach { item ->
                    val cartRef = db.collection("users").document(uid).collection("cart").document(item.id)
                    batch.delete(cartRef)
                }
            }.await()

            Resource.Success(orderId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Đặt hàng thất bại")
        }
    }

    override fun getOrderHistory(): Flow<Resource<List<Order>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("Chưa đăng nhập"))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading())

        // Lắng nghe realtime các đơn hàng của user này
        val listener = db.collection("orders")
            .whereEqualTo("userId", uid)
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Uncomment khi đã tạo Index trên Firebase
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Lỗi tải đơn hàng"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // Map dữ liệu và sort thủ công trong memory (nếu chưa có index)
                    val orders = snapshot.toObjects(Order::class.java)
                        .sortedByDescending { it.createdAt }
                    trySend(Resource.Success(orders))
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getAllOrders(): Flow<Resource<List<Order>>> = callbackFlow {
        trySend(Resource.Loading())
        // Admin xem hết, không lọc theo userId
        val listener = db.collection("orders")
            // .orderBy("createdAt", Query.Direction.DESCENDING) // Nhớ index
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Lỗi"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)
                        .sortedByDescending { it.createdAt }
                    trySend(Resource.Success(orders))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String): Resource<Boolean> {
        return try {
            db.collection("orders").document(orderId)
                .update("status", newStatus).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi cập nhật")
        }
    }
}