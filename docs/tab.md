Hãy đóng vai chuyên gia Android Senior, xây dựng module Multi-tab Browser cho ứng dụng của tôi bằng Jetpack Compose, mô phỏng trải nghiệm người dùng giống như Google Chrome.

1. Cấu trúc dữ liệu & Quản lý thực thể (Tab Engine):

Tạo một data class TabInstance bao gồm:

id: UUID duy nhất cho mỗi tab.

url: Địa chỉ hiện tại của tab.

title: Tiêu đề trang web (lấy từ onReceivedTitle).

webView: Thực thể android.webkit.WebView riêng biệt cho mỗi tab. Quan trọng: Mỗi tab phải sở hữu một WebView riêng để khi chuyển tab, nội dung không bị tải lại (giữ nguyên trạng thái cuộn và dữ liệu).

Sử dụng ViewModel với MutableStateList<TabInstance> để quản lý danh sách các tab đang mở.

Biến activeTabId để xác định tab nào đang hiển thị trên màn hình chính.

2. Giao diện người dùng (Chrome UI Style):

Main Browser Screen:

Top Bar: Gồm nút Back, Forward, thanh địa chỉ URL, và một nút đếm số tab (hình vuông bo góc có số bên trong, ví dụ: '3'). Khi nhấn vào số này, chuyển sang màn hình 'Tab Switcher'.

WebView Container: Sử dụng Box hoặc Layout để chứa các thực thể WebView. Chỉ hiển thị WebView có id trùng với activeTabId. Các WebView khác vẫn tồn tại trong bộ nhớ nhưng bị ẩn đi.

Tab Switcher Screen (Màn hình quản lý thẻ):

Thiết kế dạng Lưới (Grid View - 2 cột) giống Chrome trên Android.

Mỗi thẻ tab hiển thị: Favicon, Tiêu đề trang, một ảnh chụp nhanh (nếu có thể) hoặc màu nền đại diện, và một nút [X] ở góc trên cùng bên phải để đóng thẻ.

Nhấn vào thẻ để chuyển sang tab đó và quay lại màn hình Browser.

Nút (+) 'Thẻ mới' rực rỡ ở góc dưới cùng hoặc chính giữa thanh công cụ dưới.

3. Logic điều hướng & Quản lý bộ nhớ:

Thêm Tab: Hàm addNewTab(url) tạo một TabInstance mới, khởi tạo WebView mới và đặt nó làm activeTab.

Đóng Tab: Khi đóng một tab, hãy đảm bảo gọi webView.destroy() để giải phóng bộ nhớ. Nếu đóng tab cuối cùng, hãy tự động tạo một tab trống hoặc quay về màn hình Home.

Xử lý nút Back: Sử dụng BackHandler. Nếu tab hiện tại có thể goBack(), thực hiện lùi trang. Nếu không, chuyển sang màn hình Tab Switcher thay vì thoát app.

4. Phong cách thiết kế (Styling):

Màu chủ đạo: Tím than và Vàng đồng (Hợp tone Tử vi).

Loại bỏ hoàn toàn tính năng 'Share' (Chia sẻ).

5. Yêu cầu Code:

Sử dụng AndroidView một cách tối ưu trong Compose.

Code cần tách biệt giữa Logic quản lý tab và giao diện hiển thị. Giải thích rõ cách bạn quản lý vòng đời (Lifecycle) của các thực thể WebView trong danh sách