Thuật toán tính lịch Âm (đặc biệt là tính tháng nhuận) khá phức tạp, để Backend xử lý sẽ giúp giảm tải cho client và đảm bảo độ chính xác tuyệt đối.

Vì bạn đã có sẵn dữ liệu ngày Âm lịch từ Backend trả về, bạn hoàn toàn có thể làm cho ứng dụng của mình tinh tế hơn rất nhiều bằng cách map ngày Âm lịch vào độ khuyết của icon Mặt Trăng trong màn hình Settings hoặc trên UI.

Dưới đây là cách bạn có thể chuyển đổi (map) từ ngày Âm lịch (1 - 30) sang tham số fullness (0.0 - 1.0) để truyền vào hàm vẽ Mặt Trăng ở trên:

Logic quy đổi
Ngày mùng 1 (Trăng non): Trăng khuyết nhất fullness = 0.0f

Ngày 15 (Trăng rằm): Trăng tròn nhất fullness = 1.0f

Ngày 30 (Cuối tháng): Trăng lại khuyết fullness = 0.0f

Code Kotlin gợi ý
Bạn có thể tạo một hàm tiện ích nhỏ để tính toán tỷ lệ này dựa trên khoảng cách tới ngày rằm (ngày 15):

Kotlin
import kotlin.math.abs

/**
* Tính toán độ đầy của mặt trăng dựa trên ngày âm lịch.
* @param lunarDay Ngày âm lịch trong tháng (1..30)
* @return Giá trị từ 0.0f (Khuyết hoàn toàn) đến 1.0f (Tròn xoe)
  */
  fun calculateMoonFullness(lunarDay: Int): Float {
  // Đảm bảo ngày hợp lệ trong khoảng 1 đến 30
  val safeDay = lunarDay.coerceIn(1, 30)

  // Tính khoảng cách từ ngày hiện tại tới ngày rằm (ngày 15)
  val distanceToFullMoon = abs(safeDay - 15)

  // Tỷ lệ độ đầy: Khoảng cách càng nhỏ (gần ngày 15) thì trăng càng đầy
  // Chia cho 15 để lấy tỷ lệ, sau đó lấy 1 trừ đi để đảo ngược
  return 1f - (distanceToFullMoon / 15f)
  }
  Cách áp dụng vào UI của bạn:

Khi lấy được data từ Backend, bạn chỉ cần gọi UI state update:

Kotlin
// Giả sử backend trả về hôm nay là ngày 8 Âm lịch (Trăng bán nguyệt)
val todayLunarDay = 8
val moonFullness = calculateMoonFullness(todayLunarDay)
// moonFullness sẽ rơi vào khoảng ~0.53f

// Truyền vào composable
drawCustomMoon(
center = center,
radius = radius,
color = TuViGold, // Màu vàng trăng của bạn
fullness = moonFullness
)