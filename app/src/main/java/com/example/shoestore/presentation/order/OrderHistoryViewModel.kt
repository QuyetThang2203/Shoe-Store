package com.example.shoestore.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestore.domain.model.Order
import com.example.shoestore.domain.repository.OrderRepository
import com.example.shoestore.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _ordersState = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val ordersState: StateFlow<Resource<List<Order>>> = _ordersState

    init {
        getOrders()
    }

    private fun getOrders() {
        viewModelScope.launch {
            orderRepository.getOrderHistory().collect {
                _ordersState.value = it
            }
        }
    }
}
