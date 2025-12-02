package com.example.shoestore.data.repository

import com.example.shoestore.domain.model.User
import com.example.shoestore.domain.repository.UserRepository
import com.example.shoestore.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : UserRepository {

    override fun getAllUsers(): Flow<Resource<List<User>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = db.collection("users")
            .whereEqualTo("role", "user")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Lỗi"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.toObjects(User::class.java)
                    trySend(Resource.Success(users))
                }
            }
        awaitClose { listener.remove() }
    }

    // --- HÀM MỚI ---
    override suspend fun getUserProfile(uid: String): Resource<User> {
        return try {
            val document = db.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Không tìm thấy thông tin người dùng")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Lỗi kết nối")
        }
    }
}