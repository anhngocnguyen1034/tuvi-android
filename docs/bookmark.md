Hãy đóng vai chuyên gia Android Senior, bổ sung tính năng Bookmark (Dấu trang) vào module Browser Jetpack Compose hiện tại. Yêu cầu chi tiết như sau:

1. Cơ sở dữ liệu (Room Database):

Tạo một Entity tên là BookmarkItem gồm: id (Primary Key), url (String), title (String), faviconPath (String - đường dẫn ảnh biểu tượng nếu có), và createdTime (Long).

Tạo BookmarkDao với các hàm: insertBookmark, deleteBookmark, getAllBookmarks, và isBookmarked(url: String) (để kiểm tra xem trang hiện tại đã được lưu chưa).

2. Giao diện người dùng (UI - Compose):

Trên Toolbar: Thêm một nút hình Ngôi sao (Star Icon).

Nếu URL hiện tại chưa có trong Bookmark: Hiển thị ngôi sao rỗng (Icons.Outlined.Star).

Nếu URL hiện tại đã có trong Bookmark: Hiển thị ngôi sao đặc (Icons.Filled.Star) màu vàng đồng.

Khi nhấn vào nút này: Thực hiện thêm hoặc xóa Bookmark ngay lập tức (Toggle).

Màn hình Danh sách Dấu trang (Bookmark Manager):

Thiết kế một màn hình riêng biệt (có thể là một Tab bên cạnh màn hình History hoặc một màn hình mới).

Hiển thị danh sách các trang đã lưu với Tiêu đề và URL.

Nhấn vào một mục sẽ mở URL đó trong một Tab mới hoặc Tab hiện tại.

Cho phép vuốt để xóa (Swipe to delete) hoặc nhấn giữ để chỉnh sửa Tiêu đề của Bookmark.

3. Logic tích hợp:

Khi người dùng đang ở màn hình Tab Switcher, hãy thêm một lối tắt (Shortcut) để truy cập nhanh vào danh sách Bookmark.

Đảm bảo tính năng này hoạt động độc lập với 'Lịch sử' (History) nhưng có thể dùng chung một ViewModel để quản lý dữ liệu.

Lưu ý: Vẫn giữ nguyên quy tắc không có tính năng 'Share' (Chia sẻ).

4. Phong cách thiết kế:

Đồng bộ với tone màu Tím than và Vàng đồng của app Tử vi.

Các icon nên thanh mảnh, sang trọng.

5. Yêu cầu về Code:

Viết code sạch, xử lý bất đồng bộ bằng Flow hoặc LiveData để UI tự động cập nhật khi trạng thái ngôi sao thay đổi.

Tối ưu hóa việc kiểm tra isBookmarked mỗi khi người dùng chuyển trang web."

Tại sao tính năng Bookmark lại quan trọng cho App Tử vi?
Trong một ứng dụng tâm linh, người dùng thường có những trang web "ruột" (ví dụ: trang tra cứu lịch khổng minh, trang xem ngày tốt, hoặc một bài viết về bản mệnh).

Truy cập tức thì: Bookmark giúp họ không phải tìm lại trong hàng trăm mục Lịch sử (History).

Cá nhân hóa: Biến trình duyệt trong app thành một thư viện kiến thức cá nhân của riêng họ.

Đồng bộ giao diện: Nút ngôi sao vàng đồng trên nền tím than sẽ tạo điểm nhấn thị giác rất sang trọng và đúng chất "Tử vi".

Luồng tương tác (UX Flow) bạn nên kiểm tra sau khi AI viết code:
Khi nhấn ngôi sao -> Hiện thông báo nhỏ (Toast/Snackbar): "Đã thêm vào Dấu trang".

Khi ở màn hình danh sách Bookmark -> Nhấn vào link -> Tự động đóng màn hình Bookmark và load trang trên WebView.