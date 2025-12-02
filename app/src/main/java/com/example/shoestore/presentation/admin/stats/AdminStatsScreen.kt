package com.example.shoestore.presentation.admin.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen(
    viewModel: AdminStatsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.statsState.collectAsState()
    val filter by viewModel.filterType.collectAsState()
    val tabs = listOf("Tất cả", "Tháng này", "Hôm nay", "Năm nay")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê Doanh thu") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Tab Row Filter
            TabRow(selectedTabIndex = filter) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = filter == index,
                        onClick = { viewModel.setFilter(index) },
                        text = { Text(title) }
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    StatCard("Doanh Thu", "$${String.format("%.2f", state.totalRevenue)}")
                    Spacer(modifier = Modifier.height(16.dp))
                    StatCard("Tổng Đơn", "${state.totalOrders}")
                    Spacer(modifier = Modifier.height(16.dp))
                    StatCard("Đã Giao Thành Công", "${state.deliveredOrders}")
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }
    }
}