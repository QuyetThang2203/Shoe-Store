package com.example.shoestore.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestore.domain.repository.AuthRepository
import com.example.shoestore.domain.repository.OrderRepository
import com.example.shoestore.domain.repository.ProductRepository
import com.example.shoestore.util.Resource
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository, // Inject thêm OrderRepo
    private val authRepository: AuthRepository    // Inject thêm AuthRepo
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Xin chào! Tôi có thể giúp bạn kiểm tra đơn hàng hoặc tư vấn sản phẩm.", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Biến lưu trữ "Bộ não" context
    private var fullContext: String = ""

    init {
        loadFullContext()
    }

    private fun loadFullContext() {
        viewModelScope.launch {
            _isLoading.value = true
            val sb = StringBuilder()

            // 1. SYSTEM ROLE: Định hình tính cách
            sb.append("Bạn là trợ lý AI chuyên nghiệp của ShoeStore. Nhiệm vụ: Tư vấn sản phẩm và Hỗ trợ tra cứu đơn hàng.\n")
            sb.append("Quy tắc: Trả lời ngắn gọn, thân thiện, dùng emoji. Nếu thông tin không có trong dữ liệu cung cấp, hãy nói là không biết, đừng bịa đặt.\n\n")

            // 2. LOAD SẢN PHẨM (KHO HÀNG)
            productRepository.getProducts().collect { result ->
                if (result is Resource.Success) {
                    val products = result.data ?: emptyList()
                    sb.append("--- DANH SÁCH SẢN PHẨM HIỆN CÓ ---\n")
                    products.forEach { p ->
                        sb.append("- ID: ${p.id} | Tên: ${p.name} | Hãng: ${p.brand} | Giá: $${p.price} | Màu: ${p.colors.joinToString()} | Size: ${p.sizes.joinToString()}\n")
                    }
                }

                // 3. LOAD ĐƠN HÀNG (CỦA NGƯỜI DÙNG HIỆN TẠI)
                // Lưu ý: Trong thực tế nên dùng combine, nhưng để đơn giản ta gọi lồng nhau hoặc tuần tự trong coroutine
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    orderRepository.getOrderHistory().collect { orderResult ->
                        if (orderResult is Resource.Success) {
                            val orders = orderResult.data ?: emptyList()
                            sb.append("\n--- LỊCH SỬ ĐƠN HÀNG CỦA KHÁCH ---\n")
                            if (orders.isEmpty()) {
                                sb.append("(Khách hàng chưa có đơn hàng nào)\n")
                            } else {
                                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                // Chỉ lấy 5 đơn gần nhất để tiết kiệm token
                                orders.take(5).forEach { order ->
                                    val dateStr = dateFormat.format(order.createdAt)
                                    val itemsStr = order.items.joinToString { "${it.productName} (Size ${it.selectedSize}, ${it.selectedColor})" }

                                    sb.append("- Mã đơn: ${order.id.takeLast(6).uppercase()}\n")
                                    sb.append("  + Ngày đặt: $dateStr\n")
                                    sb.append("  + Trạng thái: ${convertStatusToVietnamese(order.status)}\n")
                                    sb.append("  + Tổng tiền: $${order.totalPrice}\n")
                                    sb.append("  + Sản phẩm: $itemsStr\n")
                                    sb.append("  + Địa chỉ: ${order.address}\n")
                                }
                            }
                        }

                        // Hoàn tất context
                        fullContext = sb.toString()
                        _isLoading.value = false
                    }
                } else {
                    sb.append("\n(Khách hàng chưa đăng nhập)\n")
                    fullContext = sb.toString()
                    _isLoading.value = false
                }
            }
        }
    }

    // Helper để AI hiểu trạng thái tiếng Anh
    private fun convertStatusToVietnamese(status: String): String {
        return when(status) {
            "PENDING" -> "Đang chờ xác nhận"
            "SHIPPING" -> "Đang giao hàng"
            "DELIVERED" -> "Đã giao thành công"
            "CANCELLED" -> "Đã hủy"
            else -> status
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val currentList = _messages.value.toMutableList()
        currentList.add(ChatMessage(userMessage, true))
        _messages.value = currentList
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Nếu context chưa load xong hoặc rỗng
                val promptToSend = if (fullContext.isEmpty()) {
                    "Hiện tại hệ thống đang tải dữ liệu. Vui lòng đợi trong giây lát."
                } else {
                    fullContext
                }

                // Gửi prompt context trước (System Prompt hack)
                val chat = generativeModel.startChat(
                    history = listOf(
                        content("user") { text(promptToSend) },
                        content("model") { text("Đã nhận dữ liệu kho hàng và lịch sử đơn hàng. Tôi sẵn sàng hỗ trợ!") }
                    )
                )

                val response = chat.sendMessage(userMessage)
                val reply = response.text ?: "Xin lỗi, tôi chưa hiểu ý bạn."

                currentList.add(ChatMessage(reply, false))
                _messages.value = currentList

            } catch (e: Exception) {
                currentList.add(ChatMessage("Lỗi kết nối AI: ${e.message}", false, true))
                _messages.value = currentList
            } finally {
                _isLoading.value = false
            }
        }
    }
}