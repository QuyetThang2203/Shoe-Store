package com.example.shoestore.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

// Import đầy đủ các màn hình
import com.example.shoestore.presentation.auth.LoginScreen
import com.example.shoestore.presentation.auth.RegisterScreen
import com.example.shoestore.presentation.cart.CartScreen
import com.example.shoestore.presentation.detail.ProductDetailScreen
import com.example.shoestore.presentation.home.HomeScreen
import com.example.shoestore.presentation.order.OrderHistoryScreen
import com.example.shoestore.presentation.chat.ChatScreen // Import mới

// Admin Screens
import com.example.shoestore.presentation.admin.AdminDashboardScreen
import com.example.shoestore.presentation.admin.products.AdminProductScreen
import com.example.shoestore.presentation.admin.products.AddProductScreen
import com.example.shoestore.presentation.admin.products.EditProductScreen
import com.example.shoestore.presentation.admin.orders.AdminOrderScreen
import com.example.shoestore.presentation.admin.users.AdminUserScreen
import com.example.shoestore.presentation.admin.stats.AdminStatsScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Cart : Screen("cart")
    object OrderHistory : Screen("order_history")
    object Chat : Screen("chat") // Route mới cho AI
    object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: String) = "detail/$productId"
    }

    // Admin Routes
    object AdminDashboard : Screen("admin_dashboard")
    object AdminProducts : Screen("admin_products")
    object AdminAddProduct : Screen("admin_add_product")
    object AdminEditProduct : Screen("admin_edit_product/{productId}") {
        fun createRoute(productId: String) = "admin_edit_product/$productId"
    }
    object AdminOrders : Screen("admin_orders")
    object AdminUsers : Screen("admin_users")
    object AdminStats : Screen("admin_stats")
}

@Composable
fun ShoeNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // --- AUTH ---
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    if (role == "admin") {
                        navController.navigate(Screen.AdminDashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    } else {
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // --- USER MAIN ---
        composable(Screen.Home.route) {
            HomeScreen(
                onProductClick = { productId -> navController.navigate(Screen.Detail.createRoute(productId)) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onHistoryClick = { navController.navigate(Screen.OrderHistory.route) },
                onLogoutClick = { navController.navigate(Screen.Login.route) { popUpTo(0) } },
                // Callback mở Chatbot
                onChatClick = { navController.navigate(Screen.Chat.route) }
            )
        }

        // --- Màn hình Chatbot ---
        composable(Screen.Chat.route) {
            ChatScreen(onBack = { navController.popBackStack() })
        }

        // --- Chi tiết sản phẩm (Có Recommendation) ---
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            ProductDetailScreen(
                onBack = { navController.popBackStack() },
                // Khi bấm vào sản phẩm gợi ý -> Điều hướng tiếp
                onProductClick = { newId ->
                    navController.navigate(Screen.Detail.createRoute(newId))
                }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onOrderSuccess = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
            )
        }

        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(onBack = { navController.popBackStack() })
        }

        // --- ADMIN ---
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigate = { route -> navController.navigate(route) },
                onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }
            )
        }
        composable(Screen.AdminProducts.route) {
            AdminProductScreen(
                onAddProduct = { navController.navigate(Screen.AdminAddProduct.route) },
                onEditProduct = { id -> navController.navigate(Screen.AdminEditProduct.createRoute(id)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminAddProduct.route) {
            AddProductScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.AdminEditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) {
            EditProductScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AdminOrders.route) {
            AdminOrderScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AdminUsers.route) {
            AdminUserScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AdminStats.route) {
            AdminStatsScreen(onBack = { navController.popBackStack() })
        }
    }
}