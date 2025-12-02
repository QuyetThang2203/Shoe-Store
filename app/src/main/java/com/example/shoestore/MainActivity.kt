package com.example.shoestore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.shoestore.presentation.navigation.ShoeNavGraph
import com.example.shoestore.ui.theme.ShoeStoreTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Bắt buộc: Kích hoạt Hilt injection cho Activity này
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Áp dụng Theme cho toàn bộ ứng dụng (Màu sắc, Font chữ)
            ShoeStoreTheme {
                // Surface là container nền tảng, sử dụng màu background mặc định của theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Khởi tạo NavController tại root để quản lý điều hướng xuyên suốt
                    val navController = rememberNavController()

                    // Gọi NavGraph - Nơi chứa bản đồ điều hướng của cả User và Admin
                    ShoeNavGraph(navController = navController)
                }
            }
        }
    }
}