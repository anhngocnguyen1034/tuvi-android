---
description: Quy tắc cốt lõi về kiến trúc và phát triển cho hệ sinh thái Anhnn (Jetpack Compose).
globs: app/src/main/java/com/anhnn/**/*.kt
---
# 1. Architecture & Clean Code (Kiến trúc Anhnn)
- **Clean Architecture**: Bắt buộc phân tách rõ ràng 3 lớp: `presentation`, `domain`, và `data`.
- **Hilt DI**: Sử dụng Hilt cho Dependency Injection. Mọi Repository và UseCase phải được inject qua Constructor.
- **Project Structure**: Tuân thủ cấu trúc thư mục hiện tại của Anhnn, không tự ý tạo các pattern mới khi chưa có chỉ định.

# 2. UI & Design System (Hệ thống giao diện)
- **Material Design 3**: Sử dụng hoàn toàn các component của MD3.
- **Anhnn Theme**: Luôn bọc UI trong `AnhnnTheme`. Sử dụng các token màu sắc từ `MaterialTheme.colorScheme`, không dùng mã màu trực tiếp.

# 3. Data Flow & State Management (Luồng dữ liệu)
- **UDF (Unidirectional Data Flow)**: Dữ liệu chảy xuống (UI State), sự kiện chảy lên (Events/Lambdas).
- **UI State**: Sử dụng `StateFlow` trong ViewModel để quản lý trạng thái màn hình dưới dạng một object duy nhất (ví dụ: `HomeUiState`).
- **Asynchronous**: Sử dụng Kotlin Coroutines và Flow. Luôn xử lý lỗi (Exception handling) trong lớp Data hoặc Domain.

# 4. Navigation & Composition
- **Compose Navigation**: Quản lý chuyển màn hình bằng Navigation Component. Sử dụng Type-safety cho các đối số (arguments) nếu có thể.
- **State Hoisting**: Đưa trạng thái lên cấp cao nhất cần thiết để đảm bảo Composable "stateless" nhất có thể, giúp dễ dàng tái sử dụng và kiểm thử.

# 5. API & External Services
- **Anhnn Static Integration**: Các Service phải được thiết kế để tương tác mượt mà với Static Service của Anhnn thông qua các Repository đã được chuẩn hóa.