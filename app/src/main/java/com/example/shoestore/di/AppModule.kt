package com.example.shoestore.di

import com.example.shoestore.data.repository.*
import com.example.shoestore.domain.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Firebase Instances ---
    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore() = FirebaseFirestore.getInstance()

    // --- Repositories ---

    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth, db: FirebaseFirestore): AuthRepository {
        return AuthRepositoryImpl(auth, db)
    }

    @Provides
    @Singleton
    fun provideProductRepository(db: FirebaseFirestore): ProductRepository {
        return ProductRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideCartRepository(auth: FirebaseAuth, db: FirebaseFirestore): CartRepository {
        return CartRepositoryImpl(auth, db)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(auth: FirebaseAuth, db: FirebaseFirestore): OrderRepository {
        return OrderRepositoryImpl(auth, db)
    }

    // Mới: Repository quản lý User cho Admin
    @Provides
    @Singleton
    fun provideUserRepository(db: FirebaseFirestore): UserRepository {
        return UserRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        // Cấu hình an toàn để AI không trả lời linh tinh
        val safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
        )

        return GenerativeModel(
            modelName = "gemini-2.5-flash", // Sử dụng bản Pro (Text-only) hoặc gemini-pro-vision
            apiKey = "AIzaSyA5L8b3KBgU_tPHzIObJFhRZUU_WCZ-Clo", // 🔥 THAY API KEY CỦA BẠN VÀO ĐÂY
            safetySettings = safetySettings
        )
    }
}