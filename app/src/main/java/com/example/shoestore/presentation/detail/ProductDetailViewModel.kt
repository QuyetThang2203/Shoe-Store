package com.example.shoestore.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestore.domain.model.CartItem
import com.example.shoestore.domain.model.Product
import com.example.shoestore.domain.repository.CartRepository
import com.example.shoestore.domain.repository.ProductRepository
import com.example.shoestore.util.Resource
// Đã xóa import Gemini
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    // Đã xóa generativeModel
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _productState = MutableStateFlow<Resource<Product>>(Resource.Loading())
    val productState: StateFlow<Resource<Product>> = _productState

    private val _similarProducts = MutableStateFlow<List<Product>>(emptyList())
    val similarProducts: StateFlow<List<Product>> = _similarProducts

    private val _addToCartState = MutableSharedFlow<String>()
    val addToCartState = _addToCartState.asSharedFlow()

    init {
        val productId = savedStateHandle.get<String>("productId")
        if (productId != null) {
            getProductDetail(productId)
        }
    }

    private fun getProductDetail(id: String) {
        viewModelScope.launch {
            _productState.value = Resource.Loading()
            val result = productRepository.getProductById(id)
            _productState.value = result

            if (result is Resource.Success) {
                val currentProduct = result.data
                if (currentProduct != null) {
                    // Gọi logic lọc đơn giản
                    loadSimilarProducts(currentProduct)
                }
            }
        }
    }

    // Logic cũ: Chỉ lọc theo Brand, không dùng AI
    private fun loadSimilarProducts(currentProduct: Product) {
        viewModelScope.launch {
            productRepository.getProducts().collect { result ->
                if (result is Resource.Success) {
                    val allProducts = result.data ?: emptyList()

                    // Lọc: Cùng Brand + Khác ID hiện tại
                    val similar = allProducts.filter {
                        it.brand.equals(currentProduct.brand, ignoreCase = true) &&
                                it.id != currentProduct.id
                    }.take(3) // Lấy tối đa 3 sản phẩm

                    _similarProducts.value = similar
                }
            }
        }
    }

    fun addToCart(product: Product, size: Int, color: String) {
        viewModelScope.launch {
            val cartItem = CartItem(
                productId = product.id,
                productName = product.name,
                price = product.price,
                imageUrl = product.imageUrl,
                quantity = 1,
                selectedSize = size,
                selectedColor = color
            )

            when(val result = cartRepository.addToCart(cartItem)) {
                is Resource.Success -> _addToCartState.emit("Đã thêm vào giỏ hàng!")
                is Resource.Error -> _addToCartState.emit(result.message ?: "Lỗi!")
                else -> {}
            }
        }
    }
}