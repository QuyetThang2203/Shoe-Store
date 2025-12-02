package com.example.shoestore.presentation.detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.shoestore.domain.model.Product
import com.example.shoestore.presentation.components.ProductCard
import com.example.shoestore.presentation.components.ShoeButton
import com.example.shoestore.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onProductClick: (String) -> Unit // Callback khi bấm vào sản phẩm gợi ý
) {
    val state by viewModel.productState.collectAsState()
    val similarProducts by viewModel.similarProducts.collectAsState() // Lấy list gợi ý từ AI/Fallback

    val context = LocalContext.current

    // State local để lưu lựa chọn của người dùng
    var selectedSize by remember { mutableStateOf<Int?>(null) }
    var selectedColor by remember { mutableStateOf<String?>(null) }

    // Lắng nghe sự kiện thêm vào giỏ hàng thành công
    LaunchedEffect(Unit) {
        viewModel.addToCartState.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        // Icon hỗ trợ RTL (Right-to-Left)
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // Thanh Bottom Bar chứa nút "Thêm vào giỏ"
            if (state is Resource.Success) {
                val product = (state as Resource.Success<Product>).data
                Box(modifier = Modifier.padding(16.dp)) {
                    ShoeButton(
                        text = if (product != null) "Thêm vào giỏ - $${product.price}" else "Loading...",
                        onClick = {
                            if (product != null && selectedSize != null) {
                                // Nếu sản phẩm có màu, ưu tiên màu đã chọn, nếu không lấy màu đầu tiên làm mặc định
                                val color = selectedColor ?: product.colors.firstOrNull() ?: "Default"
                                viewModel.addToCart(product, selectedSize!!, color)
                            } else {
                                Toast.makeText(context, "Vui lòng chọn Size", Toast.LENGTH_SHORT).show()
                            }
                        },
                        // Chỉ cho phép bấm khi đã chọn Size
                        enabled = selectedSize != null
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val result = state) {
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
                is Resource.Success -> {
                    val product = result.data!!

                    // Reset lựa chọn khi ID sản phẩm thay đổi (trường hợp bấm vào gợi ý)
                    LaunchedEffect(product.id) {
                        selectedSize = null
                        if (product.colors.isNotEmpty()) {
                            selectedColor = product.colors[0]
                        } else {
                            selectedColor = null
                        }
                    }

                    ProductContent(
                        product = product,
                        similarProducts = similarProducts,
                        selectedSize = selectedSize,
                        selectedColor = selectedColor,
                        onSizeSelected = { selectedSize = it },
                        onColorSelected = { selectedColor = it },
                        onSimilarClick = onProductClick
                    )
                }
            }
        }
    }
}

@Composable
fun ProductContent(
    product: Product,
    similarProducts: List<Product>,
    selectedSize: Int?,
    selectedColor: String?,
    onSizeSelected: (Int) -> Unit,
    onColorSelected: (String) -> Unit,
    onSimilarClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp) // Chừa khoảng trống cho BottomBar
    ) {
        // 1. Ảnh sản phẩm lớn
        AsyncImage(
            model = product.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray.copy(0.2f)),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.padding(16.dp)) {
            // 2. Thông tin cơ bản: Hãng, Tên, Giá
            Text(
                text = product.brand.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$${product.price}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Chọn Size
            Text(text = "Chọn Size", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                product.sizes.forEach { size ->
                    val isSelected = size == selectedSize
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape)
                            .clickable { onSizeSelected(size) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = size.toString(),
                            color = if (isSelected) Color.White else Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Chọn Màu (nếu có)
            if (product.colors.isNotEmpty()) {
                Text(text = "Chọn Màu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    product.colors.forEach { colorName ->
                        val isSelected = colorName == selectedColor
                        FilterChip(
                            selected = isSelected,
                            onClick = { onColorSelected(colorName) },
                            label = { Text(colorName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 5. Mô tả sản phẩm
            Text(text = "Mô tả", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = product.description, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }

        // 6. PHẦN GỢI Ý SẢN PHẨM (RECOMMENDATION SYSTEM)
        if (similarProducts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(thickness = 8.dp, color = Color.LightGray.copy(alpha = 0.2f)) // Ngăn cách đậm
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Có thể bạn sẽ thích (${product.brand})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Danh sách cuộn ngang
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                similarProducts.forEach { item ->
                    // Tái sử dụng ProductCard nhưng ép kích thước nhỏ hơn cho cân đối
                    ProductCard(
                        name = item.name,
                        price = item.price,
                        imageUrl = item.imageUrl,
                        onClick = { onSimilarClick(item.id) },
                        // Ẩn nút thêm nhanh ở đây để giao diện gọn gàng
                        onAddToCart = { },
                        modifier = Modifier.width(150.dp).height(240.dp)
                    )
                }
            }
        }
    }
}