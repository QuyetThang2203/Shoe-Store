package com.example.shoestore.presentation.admin.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shoestore.domain.model.Product
import com.example.shoestore.presentation.components.ShoeButton
import com.example.shoestore.presentation.components.ShoeInputField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AdminProductActionViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // Hardcode size/color cho nhanh (Phase 3 basic)
    val sizes = listOf(38, 39, 40, 41, 42)
    val colors = listOf("Black", "White", "Red")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm Sản phẩm") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ShoeInputField(value = name, onValueChange = { name = it }, label = "Tên giày")
            Spacer(modifier = Modifier.height(12.dp))
            ShoeInputField(value = price, onValueChange = { price = it }, label = "Giá ($)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(modifier = Modifier.height(12.dp))
            ShoeInputField(value = brand, onValueChange = { brand = it }, label = "Thương hiệu")
            Spacer(modifier = Modifier.height(12.dp))
            ShoeInputField(value = imageUrl, onValueChange = { imageUrl = it }, label = "Link Ảnh (URL)")
            Spacer(modifier = Modifier.height(12.dp))
            ShoeInputField(value = description, onValueChange = { description = it }, label = "Mô tả chi tiết")

            Spacer(modifier = Modifier.height(24.dp))

            ShoeButton(text = "Lưu Sản phẩm", onClick = {
                val newProduct = Product(
                    name = name,
                    price = price.toDoubleOrNull() ?: 0.0,
                    brand = brand,
                    imageUrl = imageUrl,
                    description = description,
                    sizes = sizes,
                    colors = colors,
                    stock = 100
                )
                viewModel.addProduct(newProduct, onSuccess = onBack)
            })
        }
    }
}
