Tôi đã cấu hình xong Typography mới cho ứng dụng Tử Vi của mình. TuViTheme hiện đã tích hợp TuViTypography với 2 font chính:

Lora (Serif): Áp dụng cho các style display, headline, title (mang nét cổ điển, dùng cho Tiêu đề, Tên Cung, Thiên Bàn).

Be Vietnam Pro (Sans-serif): Áp dụng cho các style body, label (nhỏ gọn, dễ đọc, đã được ép lineHeight để dùng cho danh sách Sao, Ngũ Hành trong ô Cung hẹp).

Task:
Hãy refactor toàn bộ UI code trong project (đặc biệt là thư mục ui/screens/, ui/components/ và màn hình vẽ lá số TuViChartScreen) để áp dụng triệt để hệ thống Typography mới này.

Yêu cầu chi tiết (Quy tắc Refactor):

Loại bỏ Hardcode: Xóa bỏ toàn bộ các khai báo TextStyle đang bị hardcode fontFamily, fontSize, fontWeight lẻ tẻ bên trong các Composable Text.

Quy chuẩn Mapping Style: Thay thế bằng tham số style lấy từ MaterialTheme.typography theo đúng logic sau:

Tên Cung (Tý, Sửu...) & Tên Bản Mệnh / Cục: Dùng MaterialTheme.typography.titleMedium hoặc titleLarge.

Danh sách Chính Tinh, Phụ Tinh, Vòng Tràng Sinh (trong ô Cung): BẮT BUỘC dùng MaterialTheme.typography.labelSmall hoặc bodySmall. Tuyệt đối không dùng các style lớn hơn để tránh tràn layout (overflow) vì lưới Địa Bàn rất chật.

Text thông thường (mô tả, nội dung lịch, pop-up): Dùng MaterialTheme.typography.bodyMedium hoặc bodyLarge.

Text trên Button, Tab, Navigation: Dùng MaterialTheme.typography.labelLarge.

Giữ nguyên Logic & Modifier: Chỉ can thiệp vào tham số style và fontWeight (nếu cần nhấn mạnh màu sắc/ngũ hành của sao). Tuyệt đối không làm thay đổi luồng dữ liệu (StateFlow), cấu trúc Grid/Layout (Modifier.weight, padding) hoặc logic tính toán sao của app.

Xử lý Text Overflow: Đảm bảo các Text chứa tên Sao trong ô Cung đều có maxLines = 1 và overflow = TextOverflow.Ellipsis (hoặc Modifier.basicMarquee() nếu tên sao quá dài).