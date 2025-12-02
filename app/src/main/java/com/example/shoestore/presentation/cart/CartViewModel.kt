package com.example.shoestore.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestore.domain.model.CartItem
import com.example.shoestore.domain.repository.CartRepository
import com.example.shoestore.domain.repository.OrderRepository
import com.example.shoestore.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _cartState = MutableStateFlow<Resource<List<CartItem>>>(Resource.Loading())
    val cartState: StateFlow<Resource<List<CartItem>>> = _cartState

    private val _orderState = MutableSharedFlow<Resource<String>>() // String là OrderID
    val orderState = _orderState.asSharedFlow()

    init {
        getCartItems()
    }

    private fun getCartItems() {
        viewModelScope.launch {
            cartRepository.getCartItems().collect {
                _cartState.value = it
            }
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(itemId)
            // Flow getCartItems tự động cập nhật lại UI nhờ addSnapshotListener
        }
    }

    fun checkout(address: String, items: List<CartItem>) {
        if (address.isBlank()) {
            viewModelScope.launch { _orderState.emit(Resource.Error("Vui lòng nhập địa chỉ")) }
            return
        }

        viewModelScope.launch {
            _orderState.emit(Resource.Loading())
            val totalPrice = items.sumOf { it.price * it.quantity }
            val result = orderRepository.placeOrder(items, address, totalPrice)
            _orderState.emit(result)
        }
    }
}
