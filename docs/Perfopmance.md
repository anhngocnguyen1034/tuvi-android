---
description: Quy tắc tối ưu hiệu năng tổng quát cho Jetpack Compose trong hệ sinh thái Anhnn.
globs: app/src/main/java/com/anhnn/**/*.kt
---
# 1. Tối ưu hóa Vẽ lại (Recomposition Control)
- **Data Stability**: Mọi Model dữ liệu nhận từ Backend/Static Service phải được đánh dấu `@Immutable` hoặc `@Stable` để trình biên dịch Compose tối ưu hóa việc bỏ qua các lần vẽ lại không cần thiết.
- **Defer State Reads**: Chỉ đọc giá trị của State ở cấp thấp nhất có thể. Sử dụng Lambda `() -> T` khi truyền các giá trị thay đổi nhanh (như tọa độ, scroll, progress) vào các Composable con.
- **DerivedStateOf**: Sử dụng khi một trạng thái UI phụ thuộc vào các trạng thái khác thay đổi liên tục, nhằm giảm số lần tính toán lại logic UI.

# 2. Quản lý Danh sách & Dữ liệu lớn (List Management)
- **Smart Keys**: Bắt buộc cung cấp `key` duy nhất và ổn định cho các item trong danh sách (LazyColumn/Row/Grid).
- **Efficient Loading**: Chỉ tải dữ liệu và tài nguyên khi cần thiết (Lazy Loading). Đảm bảo các tác vụ xử lý danh sách không chặn Main Thread.

# 3. Quản lý Tài nguyên mạng & Bộ nhớ (Resource Efficiency)
- **Static Assets**: Mọi tài nguyên tải từ Static Service phải được cấu hình Cache (Disk & Memory).
- **Asset Lifecycle**: Tự động giải phóng hoặc tạm dừng các hiệu ứng nặng (Lottie, Video, Gif) khi Composable không còn hiển thị (onDispose).

# 4. Quản lý Luồng (Concurrency)
- **Dispatcher Scoping**: Các tác vụ nặng (I/O, Parsing JSON, Image Processing) phải chạy trên `Dispatchers.IO` hoặc `Dispatchers.Default`.
- **Lifecycle Awareness**: Sử dụng `collectAsStateWithLifecycle()` để tự động dừng thu thập dữ liệu khi Lifecycle của ứng dụng không ở trạng thái hoạt động.

# 5. UI Layout
- **Layout Depth**: Giữ cấu trúc cây UI nông nhất có thể. Tránh lồng quá nhiều lớp Layout không cần thiết.