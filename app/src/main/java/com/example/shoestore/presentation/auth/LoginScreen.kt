package com.example.shoestore.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shoestore.presentation.components.ShoeButton
import com.example.shoestore.presentation.components.ShoeInputField
import com.example.shoestore.util.Resource

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: (String) -> Unit, // String ở đây là ROLE (admin/user)
    onNavigateToRegister: () -> Unit
) {
    val state by viewModel.authState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Lắng nghe thay đổi trạng thái đăng nhập
    LaunchedEffect(state) {
        when (val result = state) {
            is Resource.Success -> {
                val user = result.data
                if (user != null) {
                    Toast.makeText(context, "Xin chào ${user.fullName}!", Toast.LENGTH_SHORT).show()
                    // Truyền role ra ngoài để NavGraph quyết định hướng đi
                    onLoginSuccess(user.role)
                    viewModel.resetState()
                }
            }
            is Resource.Error -> {
                Toast.makeText(context, result.message ?: "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo hoặc Header
        Text(
            text = "SHOE STORE",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Form nhập liệu
        ShoeInputField(
            value = email,
            onValueChange = { email = it },
            label = "Email"
        )

        Spacer(modifier = Modifier.height(16.dp))

        ShoeInputField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Nút Login
        ShoeButton(
            text = "LOGIN",
            onClick = {
                if(email.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.login(email, password)
                } else {
                    Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                }
            },
            isLoading = state is Resource.Loading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Chuyển sang Register
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Don't have an account? ", color = Color.Gray)
            Text(
                text = "Sign Up",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}