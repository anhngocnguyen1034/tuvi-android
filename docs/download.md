Hãy đóng vai chuyên gia Android Senior, xây dựng tính năng Quản lý Tải xuống tích hợp vào module Browser Jetpack Compose. Yêu cầu hệ thống phải hoạt động ổn định và tuân thủ các tiêu chuẩn bảo mật của Android mới nhất.

1. Xử lý sự kiện Tải xuống từ WebView:

Thiết lập setDownloadListener cho WebView.

Khi phát hiện link tải, không được tải tự động mà phải hiển thị một Bottom Sheet/Dialog xác nhận: Hiển thị tên tệp, định dạng (MimeType) và dung lượng (nếu có).

Cho phép người dùng tùy chỉnh lại tên tệp trước khi nhấn 'Tải về'.

2. Logic xử lý bằng Android DownloadManager:

Sử dụng android.app.DownloadManager để thực hiện tải tệp dưới nền (Background).

Cấu hình Request:

setDestinationInExternalPublicDir: Lưu vào thư mục Environment.DIRECTORY_DOWNLOADS.

setNotificationVisibility: Hiển thị tiến trình tải trên thanh thông báo hệ thống và cho phép nhấn vào để mở tệp khi hoàn tất.

setAllowedOverMetered(true): Cho phép tải bằng cả Wifi và 4G/5G.

Xử lý việc lấy tên tệp chính xác từ Content-Disposition hoặc từ URL (sử dụng URLUtil.guessFileName).

3. Màn hình Quản lý Danh sách Tải xuống (UI - Compose):

Xây dựng màn hình DownloadListScreen sử dụng LazyColumn.

Mỗi mục tệp (Download Item) bao gồm:

Icon định dạng tệp (PDF, Image, Document, Zip) dựa trên MimeType.

Tên tệp, dung lượng đã tải/tổng dung lượng, ngày tháng.

Trạng thái: Đang tải (hiện % và ProgressBar), Đã xong, hoặc Bị lỗi.

Tính năng tương tác:

Nhấn vào tệp đã tải xong: Sử dụng FileProvider và Intent.ACTION_VIEW để mở tệp bằng ứng dụng tương ứng trong máy.

Nút 'Xóa': Xóa tệp khỏi danh sách quản lý và hỏi người dùng có muốn xóa tệp vật lý khỏi bộ nhớ máy hay không.

4. Bảo mật và Quyền hạn (Permissions):

Xử lý yêu cầu quyền WRITE_EXTERNAL_STORAGE cho Android < 10.

Xử lý quyền POST_NOTIFICATIONS cho Android 13+.

Cấu hình FileProvider trong AndroidManifest.xml để chia sẻ tệp an toàn (Safe File Sharing).

5. Yêu cầu về Code:

Tách biệt logic vào DownloadViewModel và DownloadRepository.

Sử dụng BroadcastReceiver để lắng nghe sự kiện ACTION_DOWNLOAD_COMPLETE nhằm cập nhật trạng thái UI ngay lập tức khi tệp tải xong.

Code phải sạch, xử lý các trường hợp ngoại lệ (URL lỗi, bộ nhớ đầy, mất mạng)