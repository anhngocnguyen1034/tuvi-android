Tôi muốn thêm tính năng Lưu lịch sử duyệt web vào module Browser Jetpack Compose. Hãy thực hiện các yêu cầu sau:

1. Cơ sở dữ liệu (Room Database):

Tạo một Entity tên là HistoryItem gồm: id (Primary Key), url (String), title (String), và timestamp (Long - thời gian truy cập).

Tạo HistoryDao với các hàm: insertHistory, getAllHistory (sắp xếp theo thời gian mới nhất), deleteHistoryItem, và clearAllHistory.

2. Logic lưu trữ (ViewModel):

Mỗi khi một trang web tải xong (onPageFinished trong WebViewClient), hãy tự động lấy url và title hiện tại để lưu vào cơ sở dữ liệu.

Lưu ý: Nếu URL đã tồn tại trong lịch sử, hãy cập nhật lại timestamp mới nhất thay vì tạo dòng mới để tránh trùng lặp.

Quan trọng: Nếu sau này tôi bật chế độ Ẩn danh (Incognito), tính năng lưu lịch sử này phải được tạm dừng (không lưu dữ liệu).

3. Giao diện Lịch sử (UI):

Tạo một màn hình HistoryScreen hiển thị danh sách lịch sử theo dạng danh sách đứng (LazyColumn).

Mỗi dòng hiển thị: Tiêu đề trang web, URL rút gọn và thời gian (ví dụ: 10:30 - 15/05).

Cho phép người dùng nhấn vào một mục để mở lại URL đó trong Browser, hoặc nhấn giữ để xóa một mục cụ thể.

Có nút 'Xóa tất cả lịch sử' ở góc trên cùng.

4. Phong cách thiết kế:

Sử dụng màu Tím than và Vàng đồng đồng bộ với app Tử vi.

Sử dụng biểu tượng History (đồng hồ) để đại diện cho tính năng này.

5. Yêu cầu về Code:

Sử dụng Hilt (hoặc Koin) để inject Database/Dao nếu có thể, hoặc khởi tạo thủ công một cách sạch sẽ.

Code phải xử lý bất đồng bộ (Coroutines) khi tương tác với Database để tránh treo máy (ANR)."