package com.example.shoestore.presentation.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shoestore.presentation.components.ProductCard
import com.example.shoestore.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onChatClick: () -> Unit
) {
    // Collect State: Đây là chìa khóa để UI tự động cập nhật
    val state by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Xin chào,", style = MaterialTheme.typography.bodySmall)
                            Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    },
                    actions = {
                        IconButton(onClick = onHistoryClick) { Icon(Icons.AutoMirrored.Filled.ListAlt, "History") }
                        IconButton(onClick = onCartClick) { Icon(Icons.Default.ShoppingCart, "Cart") }
                        IconButton(onClick = { viewModel.logout(); onLogoutClick() }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Tìm giày Nike, Adidas...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onChatClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Face, "AI") },
                text = { Text("Trợ lý AI") }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val result = state) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is Resource.Success -> {
                    val products = result.data ?: emptyList()
                    if (products.isEmpty()) {
                        Text("Không tìm thấy sản phẩm", modifier = Modifier.align(Alignment.Center))
                    } else {
                        // DANH SÁCH SẢN PHẨM (Đã được sắp xếp bởi AI)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(products, key = { it.id }) { product -> // Thêm key để tối ưu render
                                ProductCard(
                                    name = product.name,
                                    price = product.price,
                                    imageUrl = product.imageUrl,
                                    onClick = { onProductClick(product.id) },
                                    onAddToCart = { viewModel.addToCart(product) }
                                )
                            }
                        }
                    }
                }

                is Resource.Error -> Text(result.message ?: "Lỗi", modifier = Modifier.align(Alignment.Center), color = Color.Red)
            }
        }
    }
}