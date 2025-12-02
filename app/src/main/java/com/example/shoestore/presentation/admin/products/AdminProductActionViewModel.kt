package com.example.shoestore.presentation.admin.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.shoestore.domain.model.Product
import com.example.shoestore.domain.repository.ProductRepository
import com.example.shoestore.presentation.home.HomeViewModel
import com.example.shoestore.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminProductActionViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    fun deleteProduct(id: String) {
        viewModelScope.launch { repository.deleteProduct(id) }
    }

    fun addProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.addProduct(product)
            onSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    actionViewModel: AdminProductActionViewModel = hiltViewModel(),
    onAddProduct: () -> Unit,
    onEditProduct: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by homeViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // FIX: AutoMirrored
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProduct) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val result = state) {
                is Resource.Success -> {
                    val products = result.data ?: emptyList()

                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(products) { product ->
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        AsyncImage(
                                            model = product.imageUrl,
                                            contentDescription = null,
                                            modifier = Modifier.size(50.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                                            Text("$${product.price}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }

                                    Row {
                                        IconButton(onClick = { onEditProduct(product.id) }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue)
                                        }
                                        IconButton(onClick = { actionViewModel.deleteProduct(product.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                                // FIX: Divider -> HorizontalDivider
                                HorizontalDivider()
                            }
                        }
                    }
                }
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is Resource.Error -> {
                    Text(
                        text = result.message ?: "Lỗi tải dữ liệu",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}