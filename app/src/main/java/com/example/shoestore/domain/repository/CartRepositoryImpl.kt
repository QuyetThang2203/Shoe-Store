package com.example.shoestore.data.repository

import com.example.shoestore.domain.model.CartItem
import com.example.shoestore.domain.repository.CartRepository
import com.example.shoestore.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : CartRepository {

    override fun getCartItems(): Flow<Resource<List<CartItem>>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(Resource.Error("Vui lòng đăng nhập"))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading())
        val listener = db.collection("users").document(uid).collection("cart")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Lỗi tải giỏ hàng"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(CartItem::class.java)
                    trySend(Resource.Success(items))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addToCart(item: CartItem): Resource<Boolean> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
        return try {
            val cartCollection = db.collection("users").document(uid).collection("cart")

            // 1. Kiểm tra xem sản phẩm này (cùng ID, Size, Màu) đã có chưa
            val querySnapshot = cartCollection
                .whereEqualTo("productId", item.productId)
                .whereEqualTo("selectedSize", item.selectedSize)
                .whereEqualTo("selectedColor", item.selectedColor)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                // 2. Nếu có rồi -> Cộng dồn số lượng
                val existingDoc = querySnapshot.documents[0]
                val currentQuantity = existingDoc.getLong("quantity")?.toInt() ?: 1
                cartCollection.document(existingDoc.id)
                    .update("quantity", currentQuantity + 1)
                    .await()
            } else {
                // 3. Nếu chưa có -> Tạo mới
                val docRef = cartCollection.document()
                val newItem = item.copy(id = docRef.id)
                docRef.set(newItem).await()
            }
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi thêm giỏ hàng")
        }
    }

    override suspend fun removeFromCart(cartItemId: String): Resource<Boolean> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Chưa đăng nhập")
        return try {
            db.collection("users").document(uid).collection("cart").document(cartItemId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi xóa sản phẩm")
        }
    }
}
