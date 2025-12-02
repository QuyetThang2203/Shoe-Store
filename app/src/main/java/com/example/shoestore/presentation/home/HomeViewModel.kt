package com.example.shoestore.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestore.data.local.UserPreferences
import com.example.shoestore.domain.model.CartItem
import com.example.shoestore.domain.model.Product
import com.example.shoestore.domain.repository.*
import com.example.shoestore.util.Resource
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class UserPreference(
    val favoriteBrands: List<String> = emptyList(),
    val isPriceSensitive: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val generativeModel: GenerativeModel,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())

    val uiState: StateFlow<Resource<List<Product>>> = _uiState
    val searchQuery: StateFlow<String> = _searchQuery

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _userName = MutableStateFlow("Khách")
    val userName: StateFlow<String> = _userName

    init {
        getCurrentUserName()
        loadProducts()
        observeOrderUpdates()
        observeUiUpdates()
    }

    private fun observeUiUpdates() {
        viewModelScope.launch {
            combine(_allProducts, _searchQuery) { products, query ->
                if (products.isEmpty()) return@combine null

                if (query.isBlank()) {
                    Resource.Success(products)
                } else {
                    val filtered = products.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.brand.contains(query, ignoreCase = true)
                    }
                    Resource.Success(filtered)
                }
            }.collect { newState ->
                if (newState != null) _uiState.value = newState
            }
        }
    }

    private fun getCurrentUserName() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                when(val result = userRepository.getUserProfile(currentUser.id)) {
                    is Resource.Success -> _userName.value = result.data?.fullName ?: "Bạn"
                    else -> _userName.value = currentUser.email.substringBefore("@")
                }
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProducts().collect { result ->
                if (result is Resource.Success) {
                    val products = result.data ?: emptyList()
                    val cachedPref = userPreferences.getUserPreference()

                    if (cachedPref != null) {
                        _allProducts.value = sortProductsByPreference(products, cachedPref)
                    } else {
                        _allProducts.value = products
                    }
                } else if (result is Resource.Error) {
                    _uiState.value = Resource.Error(result.message ?: "Lỗi tải")
                }
            }
        }
    }

    private fun observeOrderUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                orderRepository.getOrderHistory().collect { result ->
                    if (result is Resource.Success) {
                        val allOrders = result.data ?: emptyList()
                        val validOrders = allOrders.filter { it.status != "CANCELLED" }

                        if (validOrders.isNotEmpty()) {
                            Log.d("HOME_AI", "--- BẮT ĐẦU PHÂN TÍCH ---")
                            val newPref = analyzeUserTasteWithAI(validOrders)

                            // Nếu Gu mới khác Gu cũ thì mới cập nhật UI
                            val oldPref = userPreferences.getUserPreference()
                            if (oldPref != newPref) {
                                userPreferences.saveUserPreference(newPref)
                                Log.d("HOME_AI", "Gu thay đổi! Cũ: $oldPref -> Mới: $newPref")

                                withContext(Dispatchers.Main) {
                                    val currentList = _allProducts.value
                                    if (currentList.isNotEmpty()) {
                                        val sorted = sortProductsByPreference(currentList, newPref)

                                        // MẸO: Reset list về rỗng trong 100ms để ép UI vẽ lại
                                        _allProducts.value = emptyList()
                                        delay(100)
                                        _allProducts.value = sorted

                                        Log.d("HOME_AI", "--- ĐÃ CẬP NHẬT UI ---")
                                    }
                                }
                            } else {
                                Log.d("HOME_AI", "Gu không đổi, giữ nguyên danh sách.")
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun analyzeUserTasteWithAI(orders: List<com.example.shoestore.domain.model.Order>): UserPreference {
        val purchaseHistory = StringBuilder()
        orders.take(10).forEach { order ->
            order.items.forEach { item ->
                purchaseHistory.append("- ${item.productName} (Giá: ${item.price})\n")
            }
        }

        val prompt = """
            Dưới đây là lịch sử mua giày:
            $purchaseHistory
            Phân tích và trả về TEXT thuần (ngăn cách |):
            BRANDS:[list hãng]|PRICE_SENSITIVE:[TRUE/FALSE]
            Ví dụ: BRANDS:nike,adidas|PRICE_SENSITIVE:FALSE
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            parseAiResponse(response.text ?: "")
        } catch (e: Exception) {
            UserPreference()
        }
    }

    private fun parseAiResponse(text: String): UserPreference {
        var brands = listOf<String>()
        var priceSensitive = false
        try {
            val cleanText = text.replace("\n", "|").trim()
            val parts = cleanText.split("|")
            parts.forEach { part ->
                val trimmed = part.trim()
                when {
                    trimmed.contains("BRANDS:", ignoreCase = true) -> {
                        val raw = trimmed.substringAfter(":")
                        brands = raw.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
                    }
                    trimmed.contains("PRICE_SENSITIVE:", ignoreCase = true) -> {
                        priceSensitive = trimmed.substringAfter(":").trim().toBoolean()
                    }
                }
            }
        } catch (e: Exception) { }
        return UserPreference(brands, priceSensitive)
    }

    // --- FIX LOGIC CHẤM ĐIỂM (DEBUG) ---
    private fun sortProductsByPreference(products: List<Product>, pref: UserPreference): List<Product> {
        Log.d("HOME_AI", "--- BẢNG ĐIỂM SẮP XẾP ---")

        val sortedList = products.sortedByDescending { product ->
            var score = 0

            // 1. BRAND (+100 điểm): So sánh tương đối (contains)
            // Ví dụ: Pref="nike" sẽ khớp với Product="Nike Air", "NIKE", "Nike Jordan"
            pref.favoriteBrands.forEach { favBrand ->
                if (product.brand.lowercase().contains(favBrand)) {
                    score += 100
                }
                // Check thêm cả trong tên sản phẩm cho chắc
                if (product.name.lowercase().contains(favBrand)) {
                    score += 50
                }
            }

            // 2. GIÁ (+50 điểm)
            if (pref.isPriceSensitive && product.price < 100) {
                score += 50
            } else if (!pref.isPriceSensitive && product.price >= 100) {
                score += 50
            }

            Log.d("HOME_AI", "Giày: ${product.name} | Brand: ${product.brand} | Giá: ${product.price} -> ĐIỂM: $score")
            score
        }

        return sortedList
    }

    // ... (Giữ nguyên onSearchQueryChanged, logout, addToCart) ...
    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
    fun logout() { authRepository.logout() }
    fun addToCart(product: Product) {
        viewModelScope.launch {
            val cartItem = CartItem(
                productId = product.id, productName = product.name, price = product.price,
                imageUrl = product.imageUrl, quantity = 1, selectedSize = product.sizes.firstOrNull()?:0, selectedColor = product.colors.firstOrNull()?:"Default"
            )
            when(val result = cartRepository.addToCart(cartItem)) {
                is Resource.Success -> _toastMessage.emit("Đã thêm ${product.name} vào giỏ")
                is Resource.Error -> _toastMessage.emit(result.message ?: "Lỗi")
                else -> {}
            }
        }
    }
}