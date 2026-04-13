---
description: Tiêu chuẩn thiết kế giao diện Jetpack Compose cho hệ sinh thái Anhnn
globs: app/src/main/java/**/*.kt
---
# 1. Colors & Theming (Màu sắc chủ đạo)
- Luôn định nghĩa Gradient chính: `Brush.verticalGradient(colors = listOf(Color(0xFFA1A2FF), Color(0xFF4B4EEE)))`.
- Sử dụng MaterialTheme.colorScheme để quản lý màu sắc, không hardcode mã hex trong Composable.

# 2. Reusable Components (Thành phần dùng chung)
- **PremiumSwitch**: Tạo một Composable riêng cho Switch sử dụng dải màu gradient ở trên khi `checked`.
- **StaticLottie**: Tạo một wrapper cho `LottieAnimationView` nhận URL từ `https://static.api.hihoay.com/static/`.

# 3. Best Practices (Quy tắc code)
- **Modifier Ordering**: Luôn tuân thủ thứ tự: Size -> Padding -> Background/Clip -> Clickable -> Padding cuối.
- **State Management**: Ưu tiên "State Hoisting". Truyền State xuống và sự kiện (Events) lên.
- **Optimization**:
    - Sử dụng `remember` cho các tính toán nặng.
    - Dùng `derivedStateOf` khi quan sát các trạng thái thay đổi liên tục (như scroll position).
    - Sử dụng `Immutable` hoặc `Stable` annotations cho các Data Class chứa dữ liệu UI.

# 4. Assets Handling (Xử lý tài nguyên)
- Hình ảnh nặng hoặc Lottie JSON: Luôn load từ Static Service URL.
- Sử dụng thư viện Coil cho việc load và cache ảnh từ server.

# 5. Previews
- Mỗi Composable UI phải có ít nhất 2 `@Preview`: 1 cho Light Mode và 1 cho Dark Mode.
- Sử dụng `CompositionLocalProvider` để cung cấp dữ liệu mẫu cho Preview.