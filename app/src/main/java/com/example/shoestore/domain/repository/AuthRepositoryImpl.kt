package com.example.shoestore.data.repository

import com.example.shoestore.domain.model.User
import com.example.shoestore.domain.repository.AuthRepository
import com.example.shoestore.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {

    override val currentUser: User?
        get() {
            // Chỉ lấy info cơ bản từ Auth, muốn full info (role) cần query Firestore
            // Tạm thời trả về object User cơ bản để check null
            return auth.currentUser?.let {
                User(id = it.uid, email = it.email ?: "")
            }
        }

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("User ID null")

            // Lấy thông tin role từ Firestore
            val document = db.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java)

            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("Dữ liệu người dùng không tồn tại")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Đăng nhập thất bại")
        }
    }

    override suspend fun register(email: String, password: String, fullName: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Tạo tài khoản thất bại")

            // Tạo user model mặc định role là "user"
            val newUser = User(
                id = uid,
                email = email,
                fullName = fullName,
                role = "user",
                avatarUrl = null
            )

            // Lưu vào Firestore
            db.collection("users").document(uid).set(newUser).await()

            Resource.Success(newUser)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Đăng ký thất bại")
        }
    }

    override fun logout() {
        auth.signOut()
    }
}
