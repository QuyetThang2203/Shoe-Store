package com.example.shoestore.presentation.admin.products

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.shoestore.domain.model.Product
import com.example.shoestore.domain.repository.ProductRepository
import com.example.shoestore.presentation.components.ShoeButton
import com.example.shoestore.presentation.components.ShoeInputField
import com.example.shoestore.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val repository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // State chứa dữ liệu sản phẩm đang sửa
    private val _productState = MutableStateFlow<Product?>(null)
    val productState = _productState

    init {
        val productId = savedStateHandle.get<String>("productId")
        if (productId != null) {
            viewModelScope.launch {
                val result = repository.getProductById(productId)
                if (result is Resource.Success) {
                    _productState.value = result.data
                }
            }
        }
    }

    fun updateProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProduct(product)
            onSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    viewModel: EditProductViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val productState by viewModel.productState.collectAsState()
    val context = LocalContext.current

    // Các biến state cho form
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // Khi load được product từ API, điền vào form
    LaunchedEffect(productState) {
        productState?.let {
            name = it.name
            price = it.price.toString()
            brand = it.brand
            description = it.description
            imageUrl = it.imageUrl
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sửa Sản phẩm") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        if (productState == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ShoeInputField(value = name, onValueChange = { name = it }, label = "Tên giày")
                Spacer(modifier = Modifier.height(12.dp))
                ShoeInputField(value = price, onValueChange = { price = it }, label = "Giá", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(modifier = Modifier.height(12.dp))
                ShoeInputField(value = brand, onValueChange = { brand = it }, label = "Thương hiệu")
                Spacer(modifier = Modifier.height(12.dp))
                ShoeInputField(value = imageUrl, onValueChange = { imageUrl = it }, label = "Link Ảnh")
                Spacer(modifier = Modifier.height(12.dp))
                ShoeInputField(value = description, onValueChange = { description = it }, label = "Mô tả")

                Spacer(modifier = Modifier.height(24.dp))

                ShoeButton(text = "Cập nhật", onClick = {
                    val updatedProduct = productState!!.copy(
                        name = name,
                        price = price.toDoubleOrNull() ?: 0.0,
                        brand = brand,
                        imageUrl = imageUrl,
                        description = description
                    )
                    viewModel.updateProduct(updatedProduct) {
                        Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                })
            }
        }
    }
}
