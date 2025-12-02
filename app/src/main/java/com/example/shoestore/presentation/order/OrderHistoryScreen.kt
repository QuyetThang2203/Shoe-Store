package com.example.shoestore.presentation.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shoestore.domain.model.Order
import com.example.shoestore.util.Resource
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    viewModel: OrderHistoryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.ordersState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // FIX: AutoMirrored
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val result = state) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(result.message ?: "Lỗi", modifier = Modifier.align(Alignment.Center))
                is Resource.Success -> {
                    val orders = result.data ?: emptyList()
                    if (orders.isEmpty()) {
                        Text("Bạn chưa có đơn hàng nào", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(orders) { order ->
                                OrderItem(order)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: Order) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    val statusColor = when(order.status) {
        "PENDING" -> Color(0xFFFFA000)
        "SHIPPING" -> Color(0xFF1976D2)
        "DELIVERED" -> Color(0xFF388E3C)
        "CANCELLED" -> Color(0xFFD32F2F)
        else -> Color.Gray
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Đơn #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold)
                Text(text = dateFormat.format(order.createdAt), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))
            // FIX: Divider -> HorizontalDivider
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text("Sản phẩm: ${order.items.size} món")
            Text(
                text = order.items.joinToString { it.productName },
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${order.totalPrice}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = order.status,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}