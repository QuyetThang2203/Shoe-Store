# 👟 ShoeStore - AI-Powered E-commerce Android App

<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/AI-Google%20Gemini-8E75B2?style=for-the-badge&logo=google&logoColor=white" alt="Gemini">
</div>

<br>

<p align="center">
  <b>ShoeStore là ứng dụng thương mại điện tử chuyên bán giày dép, được xây dựng bằng Kotlin và Jetpack Compose. Điểm đặc biệt của dự án là việc tích hợp Trí tuệ nhân tạo (Google Gemini) để cá nhân hóa trải nghiệm mua sắm và hỗ trợ khách hàng tự động.</b>
</p>

---

## 🌟 Tính năng nổi bật

### 1. Phân hệ Khách hàng (User App)

#### 🛍️ Mua sắm thông minh:

- Lướt xem danh sách sản phẩm với giao diện hiện đại.
- **AI Personalized Feed**: Trang chủ tự động sắp xếp lại thứ tự hiển thị dựa trên "Gu" của người dùng (Thương hiệu yêu thích, Tầm giá) được AI phân tích từ lịch sử mua hàng.
- Tìm kiếm Realtime theo tên và thương hiệu.

#### 🤖 Trợ lý ảo AI (Chatbot):

- Chat trực tiếp với nhân viên ảo (Gemini).
- Hỗ trợ tra cứu trạng thái đơn hàng ("Đơn hôm qua của tôi đâu rồi?").
- Tư vấn sản phẩm trong kho ("Có giày Nike nào dưới 100$ không?").

#### 🛒 Quản lý đơn hàng:

- Giỏ hàng thông minh (cộng dồn số lượng).
- Thanh toán và theo dõi trạng thái đơn hàng (Chờ xác nhận, Đang giao...).
- Lịch sử mua hàng chi tiết.

### 2. Phân hệ Quản trị (Admin App)

- **📊 Dashboard**: Thống kê doanh thu theo ngày, tháng, năm.
- **📦 Quản lý Sản phẩm**: Thêm, Sửa, Xóa giày (CRUD).
- **📝 Quản lý Đơn hàng**: Duyệt đơn, cập nhật trạng thái (Pending -> Shipping -> Delivered).
- **👥 Quản lý Người dùng**: Xem danh sách khách hàng đã đăng ký.

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

- **Ngôn ngữ**: Kotlin 2.0
- **UI Toolkit**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Backend (Serverless)**:
    - Firebase Authentication (Đăng nhập/Đăng ký)
    - Firebase Firestore (Cơ sở dữ liệu NoSQL Realtime)
- **Artificial Intelligence**:
    - Google Gemini Pro Model: Dùng cho Chatbot và Hệ thống gợi ý (Recommendation System).
- **Libraries khác**:
    - Coil: Tải ảnh bất đồng bộ.
    - Coroutines & Flow: Xử lý bất đồng bộ.
    - KSP: Kotlin Symbol Processing.

---

## 🤝 Đóng góp

Mọi đóng góp đều được hoan nghênh. Vui lòng mở Pull Request hoặc tạo Issue nếu bạn tìm thấy lỗi hoặc có ý tưởng cải tiến.

---

<div align="center">
  <p>Made with ❤️ using Kotlin & Jetpack Compose</p>
</div>