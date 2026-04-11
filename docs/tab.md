Để xây dựng tính năng này trong Kotlin Jetpack Compose—nơi mỗi "Tab" (hoặc cửa sổ chức năng từ Assistive Touch) không chỉ giữ lịch sử riêng mà còn hiển thị một ảnh thu nhỏ (thumbnail) thực tế của trang cuối cùng người dùng xem trước khi chuyển tab—bạn cần kết hợp giữa quản lý trạng thái điều hướng và kỹ thuật chụp ảnh màn hình (screenshot) trong Compose.

Dưới đây là mô tả chi tiết cơ chế hoạt động và cách triển khai kỹ thuật:

1. Cơ chế hoạt động: Quy trình Chụp và Hiển thị Thumbnail
   [Diagram showing the visual snapshot process: Current Tab (View) -> Capture to Bitmap -> Store in State -> Display in Tab Switcher as Thumbnail]

Người dùng đang ở Tab A (ví dụ: "Chi tiết tin tức"): Giao diện đang hiển thị nội dung động.

Người dùng nhấn chuyển sang Tab B: Ngay khi hành động click xảy ra, trước khi NavHost thực hiện lệnh Maps để ẩn Tab A:

Hệ thống thực hiện một lệnh "Chụp nhanh (Snapshot)" toàn bộ nội dung đang render của Tab A.

Ảnh chụp này được lưu dưới dạng một đối tượng đồ họa nhẹ (thường là Bitmap hoặc ImageBitmap).

Lưu trữ vào trạng thái (State): Ảnh chụp được lưu vào một bản đồ (Map) hoặc danh sách trạng thái, gắn liền với ID hoặc Route của Tab A: Map<TabRoute, ImageBitmap?>.

Hiển thị trong Bộ chuyển đổi (Tab Switcher): Khi người dùng mở giao diện quản lý Tab (giống giao diện xem các tab đang mở của Chrome):

Thay vì chỉ hiển thị Icon và Tiêu đề, hệ thống sử dụng component Image để hiển thị ImageBitmap đã lưu của Tab A. Nó trông giống hệt trang web/màn hình cuối cùng người dùng nhìn thấy.