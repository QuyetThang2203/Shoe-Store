package com.example.shoestore.presentation.cart

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// FIX: Thêm import quan trọng này
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.shoestore.domain.model.CartItem
import com.example.shoestore.presentation.components.ShoeButton
import com.example.shoestore.presentation.components.ShoeInputField
import com.example.shoestore.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: CartViewModel = hiltViewModel(),
    onOrderSuccess: () -> Unit
) {
    // FIX: collectAsState cần import androidx.compose.runtime.collectAsState
    val state by viewModel.cartState.collectAsState()
    val context = LocalContext.current
    var address by remember { mutableStateOf("") }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.orderState.collect { result ->
            when(result) {
                is Resource.Success -> {
                    Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                    showCheckoutDialog = false
                    onOrderSuccess()
                }
                is Resource.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> { }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Giỏ hàng của bạn") }) },
        bottomBar = {
            if (state is Resource.Success) {
                val items = (state as Resource.Success<List<CartItem>>).data ?: emptyList()
                if (items.isNotEmpty()) {
                    val total = items.sumOf { it.price * it.quantity }
                    Surface(shadowElevation = 8.dp) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tổng cộng:", fontWeight = FontWeight.Bold)
                                Text("$${String.format("%.2f", total)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            ShoeButton(text = "Thanh toán ngay", onClick = { showCheckoutDialog = true })
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val result = state) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is Resource.Error -> Text(result.message ?: "Lỗi", modifier = Modifier.align(Alignment.Center))
                is Resource.Success -> {
                    val items = result.data ?: emptyList()
                    if (items.isEmpty()) {
                        Text("Giỏ hàng trống", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp)) {
                            items(items) { item ->
                                CartItemRow(item = item, onDelete = { viewModel.removeItem(item.id) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("Thông tin giao hàng") },
            text = {
                Column {
                    Text("Nhập địa chỉ nhận hàng:")
                    Spacer(modifier = Modifier.height(8.dp))
                    ShoeInputField(value = address, onValueChange = { address = it }, label = "Địa chỉ")
                }
            },
            confirmButton = {
                Button(onClick = {
                    val items = (state as? Resource.Success)?.data ?: emptyList()
                    viewModel.checkout(address, items)
                }) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun CartItemRow(item: CartItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp).background(Color.LightGray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("Size: ${item.selectedSize} | Color: ${item.selectedColor} | x${item.quantity}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("$${item.price * item.quantity}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}