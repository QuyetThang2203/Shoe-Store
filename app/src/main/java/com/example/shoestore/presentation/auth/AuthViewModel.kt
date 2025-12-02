package com.example.shoestore.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestore.domain.model.User
import com.example.shoestore.domain.repository.AuthRepository
import com.example.shoestore.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // Trạng thái chung cho cả Login và Register
    private val _authState = MutableStateFlow<Resource<User>?>(null)
    val authState: StateFlow<Resource<User>?> = _authState

    // Hàm login: Gọi Repository và cập nhật state
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            val result = repository.login(email, password)
            _authState.value = result
        }
    }

    // Hàm register: Gọi Repository và cập nhật state
    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            val result = repository.register(email, password, fullName)
            _authState.value = result
        }
    }

    // Hàm reset state: Dùng khi đã điều hướng thành công để tránh trigger lại logic khi xoay màn hình
    fun resetState() {
        _authState.value = null
    }
}