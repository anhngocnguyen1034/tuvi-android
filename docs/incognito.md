Hãy đóng vai chuyên gia Android Senior, tích hợp tính năng Chế độ ẩn danh (Incognito Mode) vào module Browser Jetpack Compose. Yêu cầu chi tiết như sau:

1. Cơ chế quản lý trạng thái (State Management):

Thêm thuộc tính isIncognito: Boolean vào TabInstance.

Khi một Tab được đánh dấu là isIncognito = true:

Không lưu Lịch sử: Tuyệt đối không gọi hàm lưu vào Room Database (HistoryItem).

Tách biệt dữ liệu: Sử dụng một cấu hình WebView riêng không lưu Cache, không lưu Form Data.

Xóa Cookies: Khi đóng Tab ẩn danh hoặc thoát module, phải gọi CookieManager.getInstance().removeAllCookies() (chỉ áp dụng cho các phiên ẩn danh).

2. Giao diện người dùng (UI - Compose):

Chuyển đổi chế độ: Trong màn hình Tab Switcher, thiết kế 2 tab ở trên cùng: 'Thường' (Normal) và 'Ẩn danh' (Incognito) để người dùng chuyển đổi qua lại giữa 2 danh sách thẻ.

Nhận diện thị giác: - Khi ở chế độ ẩn danh, toàn bộ giao diện (Toolbar, Background) chuyển sang tông màu Tối hơn (Darker Grey/Black) thay vì Tím than.

Icon thanh địa chỉ thay đổi thành biểu tượng 'Thám tử/Kính râm'.

Nút 'Đóng tất cả thẻ ẩn danh': Hiển thị nổi bật trong màn hình Tab Switcher ẩn danh để xóa sạch dấu vết nhanh chóng.

3. Bảo mật và Quyền riêng tư:

Chặn chụp màn hình: Khi đang ở Tab ẩn danh, hãy kích hoạt WindowManager.LayoutParams.FLAG_SECURE để ngăn người dùng hoặc các app khác chụp ảnh/quay phim màn hình.

Ẩn nội dung trong Recent Apps: Khi người dùng nhấn nút đa nhiệm của điện thoại, nội dung của Tab ẩn danh phải được che đi (thường dùng một lớp phủ màu đen).

4. Logic điều hướng:

Khi nhấn nút (+) ở chế độ Ẩn danh -> Tạo tab mới với isIncognito = true.

Cho phép người dùng mở một liên kết từ Tab thường sang Tab ẩn danh thông qua menu nhấn giữ (Context Menu).

5. Yêu cầu về Code:

Viết hàm createWebView(context, isIncognito) để khởi tạo WebView với các thông số cấu hình (WebSettings) khác nhau tùy theo chế độ.

Đảm bảo việc chuyển đổi giữa Tab thường và Tab ẩn danh mượt mà, không làm treo ứng dụng.

Giải thích cách bạn xử lý WebStorage và Geolocation để đảm bảo không bị rò rỉ dữ liệu trong chế độ ẩn danh.