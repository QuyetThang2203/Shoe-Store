package com.example.shoestore.data.repository

import android.util.Log
import com.example.shoestore.domain.model.Product
import com.example.shoestore.domain.repository.ProductRepository
import com.example.shoestore.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ProductRepository {

    override fun getProducts(): Flow<Resource<List<Product>>> = callbackFlow {
        // Log bắt đầu
        Log.d("ProductRepo", "Bắt đầu lấy danh sách sản phẩm...")
        trySend(Resource.Loading())

        val listener = db.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProductRepo", "Lỗi Firestore: ${error.message}")
                    trySend(Resource.Error(error.message ?: "Lỗi tải dữ liệu"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val products = snapshot.toObjects(Product::class.java)
                    Log.d("ProductRepo", "Đã lấy được ${products.size} sản phẩm")
                    trySend(Resource.Success(products))
                } else {
                    Log.d("ProductRepo", "Snapshot null -> List rỗng")
                    trySend(Resource.Success(emptyList()))
                }
            }

        awaitClose {
            Log.d("ProductRepo", "Đóng listener")
            listener.remove()
        }
    }

    override suspend fun getProductById(id: String): Resource<Product> {
        return try {
            val document = db.collection("products").document(id).get().await()
            val product = document.toObject(Product::class.java)
            if (product != null) {
                Resource.Success(product)
            } else {
                Resource.Error("Sản phẩm không tồn tại")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }

    override suspend fun addProduct(product: Product): Resource<Boolean> {
        return try {
            val id = if(product.id.isEmpty()) db.collection("products").document().id else product.id
            val finalProduct = product.copy(id = id)
            db.collection("products").document(id).set(finalProduct).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi thêm sản phẩm")
        }
    }

    override suspend fun deleteProduct(productId: String): Resource<Boolean> {
        return try {
            db.collection("products").document(productId).delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi xóa sản phẩm")
        }
    }

    override suspend fun updateProduct(product: Product): Resource<Boolean> {
        return try {
            db.collection("products").document(product.id).set(product).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi cập nhật")
        }
    }
}