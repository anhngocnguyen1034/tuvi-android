---
description: Tiêu chuẩn kiểm thử (Testing) cho các thành phần trong hệ sinh thái Anhnn.
globs: 
  - app/src/test/java/com/anhnn/**/*.kt
  - app/src/androidTest/java/com/anhnn/**/*.kt
---
# 1. Unit Testing (ViewModels & UseCases)
- Bắt buộc sử dụng **Fake Repositories** thay vì dùng Mockito/Mockk cho các data layer (để phản ánh đúng logic của Anhnn).
- Sử dụng `StandardTestDispatcher` để kiểm soát Coroutines trong Unit Test.
- Kiểm tra tính đúng đắn của `AnhnnUIState` (phải đi từ Loading -> Success/Error).

# 2. UI Testing (Jetpack Compose)
- Sử dụng `createComposeRule()` để test các Composable độc lập.
- **Anhnn Theme Check**: Đảm bảo các component hiển thị đúng dải màu Gradient và Font chữ của hệ sinh thái trong môi trường test.
- Kiểm tra các tương tác quan trọng: Click vào Premium Button, Toggle Switch của Anhnn.

# 3. Static Service Mocking
- Khi test các thành phần gọi đến `static.api.hihoay.com`, phải sử dụng dữ liệu mẫu (Mock JSON) để đảm bảo test chạy offline được.

# 4. Coverage & Naming
- Tên hàm test phải mô tả rõ kịch bản: `should_show_error_when_static_service_fails()`.
- Đảm bảo độ bao phủ (Coverage) cho các logic tính toán giá Premium hoặc xử lý dữ liệu từ Anhnn Static.