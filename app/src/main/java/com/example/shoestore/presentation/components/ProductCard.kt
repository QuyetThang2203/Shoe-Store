package com.example.shoestore.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.shoestore.ui.theme.Primary

@Composable
fun ProductCard(
    name: String,
    price: Double,
    imageUrl: String,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(260.dp) // FIX: Cố định chiều cao Card để Grid không bị lệch
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Vùng hiển thị ảnh (Chiếm phần lớn diện tích)
            Box(
                modifier = Modifier
                    .weight(1f) // Chiếm hết không gian còn lại phía trên
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                // Sử dụng SubcomposeAsyncImage của Coil để handle Loading/Error tốt hơn
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl.ifBlank { null }) // Nếu url rỗng thì coi như null để nhảy vào error
                        .crossfade(true)
                        .build(),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    // Hiển thị khi đang load
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    },
                    // Hiển thị khi lỗi hoặc url rỗng
                    error = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.BrokenImage, contentDescription = null, tint = Color.Gray)
                            Text("No Image", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                )
            }

            // 2. Thông tin sản phẩm
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                // Tên sản phẩm: Giới hạn 1 dòng, dư thì ...
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1, // FIX: Giới hạn 1 dòng
                    overflow = TextOverflow.Ellipsis // FIX: Hiện ... nếu dài quá
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Hàng chứa Giá và Nút Add
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", price)}", // Format giá đẹp hơn (vd: 12.50)
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Nút Add nhỏ gọn
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = "Add to cart",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}