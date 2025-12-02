package com.example.shoestore.presentation.admin.orders

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
class AdminOrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {
    private val _orders = MutableStateFlow<Resource<List<Order>>>(Resource.Loading())
    val orders: StateFlow<Resource<List<Order>>> = _orders

    init {
        getAllOrders()
    }

    private fun getAllOrders() {
        viewModelScope.launch {
            repository.getAllOrders().collect { _orders.value = it }
        }
    }

    fun updateStatus(orderId: String, currentStatus: String) {
        val nextStatus = when(currentStatus) {
            "PENDING" -> "SHIPPING"
            "SHIPPING" -> "DELIVERED"
            else -> return // Đã giao hoặc huỷ thì không đổi tiếp
        }
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, nextStatus)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderScreen(
    viewModel: AdminOrderViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.orders.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Đơn hàng") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val result = state) {
                is Resource.Success -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(result.data ?: emptyList()) { order ->
                            AdminOrderItem(order, onNextStatus = {
                                viewModel.updateStatus(order.id, order.status)
                                Toast.makeText(context, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show()
                            })
                        }
                    }
                }
                else -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun AdminOrderItem(order: Order, onNextStatus: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("User: ${order.userId.take(5)}...", fontWeight = FontWeight.Bold)
                Text("Total: $${order.totalPrice}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Address: ${order.address}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onNextStatus,
                enabled = order.status != "DELIVERED" && order.status != "CANCELLED",
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (order.status == "PENDING") Color(0xFFFFA000) else Color(0xFF1976D2)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                val btnText = when(order.status) {
                    "PENDING" -> "Xác nhận & Giao hàng (PENDING -> SHIPPING)"
                    "SHIPPING" -> "Xác nhận đã giao (SHIPPING -> DELIVERED)"
                    "DELIVERED" -> "Đã hoàn thành"
                    else -> "Đã hủy"
                }
                Text(btnText)
            }
        }
    }
}
