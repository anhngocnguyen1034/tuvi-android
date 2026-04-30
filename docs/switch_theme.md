# Theme Switch Animation — Mặt Trời / Mặt Trăng

Tài liệu này mô tả toàn bộ cách xây dựng button toggle theme sáng/tối có hiệu ứng mặt trời ↔ mặt trăng bằng Jetpack Compose thuần Canvas (không dùng ảnh/icon).

---

## Kết quả

| Light Mode | Dark Mode |
|-----------|-----------|
| Track màu Neon Blue, thumb màu Vàng + tia sáng | Track màu Xám đậm, thumb màu Trắng + trăng lưỡi liềm + sao |

---

## Phụ thuộc cần có

```kotlin
// build.gradle.kts (app)
implementation("androidx.compose.animation:animation")
implementation("androidx.compose.foundation:foundation")
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

Không cần thư viện animation bên ngoài — dùng hoàn toàn `animateDpAsState`, `animateColorAsState`, `animateFloatAsState` từ Compose.

---

## 1. Định nghĩa ThemeMode

```kotlin
// presentation/ui/theme/ThemeMode.kt
enum class ThemeMode(val value: String, val displayName: String) {
    LIGHT("light", "Sáng"),
    DARK("dark", "Tối")
}
```

---

## 2. Custom Colors cho Switch

Switch cần 2 màu đặc biệt: `neonBlue` (track sáng) và `neonYellow` (thumb sáng).  
Khai báo chúng trong `GenZColors` và cung cấp qua `CompositionLocal`:

```kotlin
// presentation/ui/theme/Theme.kt

@Immutable
data class GenZColors(
    val border: Color,
    val text: Color,
    val neonBlue: Color  = Color(0xFF00E5FF),  // Màu track khi Light
    val neonYellow: Color = Color(0xFFFFE500),  // Màu thumb khi Light
    val neonPink: Color  = Color(0xFFFF0099)
)

private val LightGenZColors = GenZColors(
    border = Color(0xFFE0E0E0),
    text   = Color(0xFF1A1A1A)
)

private val DarkGenZColors = GenZColors(
    border = Color(0xFF444444),
    text   = Color(0xFFEEEEEE)
)

val LocalGenZColors = staticCompositionLocalOf { LightGenZColors }

object GenZTheme {
    val colors: GenZColors
        @Composable get() = LocalGenZColors.current
}
```

---

## 3. Composable `GenZThemeSwitch`

File: `presentation/ui/screen/SettingsScreen.kt`

### 3.1 Khai báo và tham số

```kotlin
@Composable
fun GenZThemeSwitch(
    isDarkTheme: Boolean,       // true = đang ở Dark mode
    onToggle: () -> Unit,       // callback khi nhấn
    modifier: Modifier = Modifier,
    width: Dp = 64.dp,          // chiều rộng track
    height: Dp = 36.dp          // chiều cao track (= đường kính thumb)
)
```

### 3.2 Các animation

```kotlin
// Thumb trượt sang phải khi Dark, sang trái khi Light
val thumbOffsetAnim by animateDpAsState(
    targetValue = if (isDarkTheme) width - height else 0.dp,
    animationSpec = spring(
        dampingRatio = 0.6f,          // hơi nảy nhẹ
        stiffness = Spring.StiffnessLow
    ),
    label = "thumb"
)

// Track: Dark → Xám đậm #2D2D2D | Light → Neon Blue
val trackColorAnim by animateColorAsState(
    targetValue = if (isDarkTheme) Color(0xFF2D2D2D) else GenZTheme.colors.neonBlue,
    label = "track"
)

// Thumb: Dark → Trắng/Xám #DDDDDD | Light → Vàng Neon
val thumbColorAnim by animateColorAsState(
    targetValue = if (isDarkTheme) Color(0xFFDDDDDD) else GenZTheme.colors.neonYellow,
    label = "thumbColor"
)

// Sao xuất hiện khi Dark (alpha 0→1), biến mất khi Light (alpha 1→0)
val starAlphaAnim by animateFloatAsState(
    targetValue = if (isDarkTheme) 1f else 0f,
    label = "star"
)
```

### 3.3 Canvas render

```kotlin
// Lấy borderColor trước Canvas để tránh gọi @Composable bên trong DrawScope
val borderColor = GenZTheme.colors.border

Canvas(
    modifier = modifier
        .size(width, height)
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onToggle
        )
) {
    val cornerRadius = size.height / 2

    // --- Track nền ---
    drawRoundRect(
        color = trackColorAnim,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Fill
    )
    // --- Track viền ---
    drawRoundRect(
        color = borderColor,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = 1.dp.toPx())
    )

    // --- Sao (chỉ hiện Dark mode) ---
    if (starAlphaAnim > 0f) {
        val starColor = Color.White.copy(alpha = starAlphaAnim)
        drawSparkle(center = Offset(size.width * 0.25f, size.height * 0.35f), size = 3.dp.toPx(), color = starColor)
        drawSparkle(center = Offset(size.width * 0.5f,  size.height * 0.7f),  size = 2.dp.toPx(), color = starColor)
    }

    // --- Thumb (Mặt trăng hoặc Mặt trời) ---
    val thumbX      = thumbOffsetAnim.toPx() + (size.height / 2)
    val thumbCenter = Offset(thumbX, size.height / 2)
    val thumbRadius = (size.height / 2) - 4.dp.toPx()

    if (isDarkTheme) {
        drawCrescentMoon(center = thumbCenter, radius = thumbRadius, color = thumbColorAnim)
    } else {
        drawSun(
            center        = thumbCenter,
            coreRadius    = thumbRadius * 0.6f,
            rayLength     = thumbRadius * 0.35f,
            rayWidth      = 2.dp.toPx(),
            rayGap        = 2.dp.toPx(),
            color         = thumbColorAnim,
            rotationDegrees = 0f
        )
    }
}
```

---

## 4. Hàm vẽ Mặt Trăng Lưỡi Liềm

```kotlin
fun DrawScope.drawCrescentMoon(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        // Hình tròn chính (full moon)
        addOval(Rect(center, radius))

        // Hình tròn "cắt" — dịch sang phải + lên trên một chút
        val cutPath = Path().apply {
            addOval(
                Rect(
                    center = Offset(center.x + radius * 0.35f, center.y - radius * 0.1f),
                    radius = radius
                )
            )
        }
        // Phép trừ: main circle - cut circle = lưỡi liềm
        op(this, cutPath, PathOperation.Difference)
    }
    drawPath(path, color)
}
```

**Nguyên lý:** Vẽ 2 hình tròn cùng bán kính, hình thứ hai lệch sang phải (`+radius * 0.35f`) và lệch lên nhẹ (`-radius * 0.1f`). Dùng `PathOperation.Difference` để "đục" phần giao nhau → tạo hình lưỡi liềm.

---

## 5. Hàm vẽ Mặt Trời

```kotlin
fun DrawScope.drawSun(
    center: Offset,
    coreRadius: Float,      // bán kính vòng tròn trung tâm
    rayLength: Float,       // độ dài mỗi tia
    rayWidth: Float,        // độ dày tia (px)
    rayGap: Float,          // khoảng trống giữa core và tia
    color: Color,
    numRays: Int = 8,       // số tia (mặc định 8)
    rotationDegrees: Float  // xoay toàn bộ mặt trời (0f = không xoay)
) {
    rotate(rotationDegrees, center) {
        // Vẽ nhân mặt trời
        drawCircle(color, coreRadius, center)

        // Vẽ 8 tia, mỗi tia cách nhau 360/8 = 45 độ
        val step = 360f / numRays
        repeat(numRays) { i ->
            rotate(i * step, center) {
                drawLine(
                    color = color,
                    start = Offset(center.x, center.y - (coreRadius + rayGap)),
                    end   = Offset(center.x, center.y - (coreRadius + rayGap + rayLength)),
                    strokeWidth = rayWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
```

**Nguyên lý:** Vẽ 1 tia thẳng đứng (từ đỉnh core lên trên), sau đó `rotate()` lặp lại 8 lần mỗi bước 45° xung quanh tâm → tạo 8 tia đều nhau.

---

## 6. Hàm vẽ Sao (Sparkle)

```kotlin
fun DrawScope.drawSparkle(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticBezierTo(center.x, center.y, center.x + size, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y + size)
        quadraticBezierTo(center.x, center.y, center.x - size, center.y)
        quadraticBezierTo(center.x, center.y, center.x, center.y - size)
    }
    drawPath(path, color)
}
```

**Nguyên lý:** 4 đường cong Bezier bậc 2 nối 4 điểm (trên, phải, dưới, trái) qua tâm → tạo hình ngôi sao 4 cánh cong nhẹ.

---

## 7. Dùng trong Settings Screen

```kotlin
// Đọc theme hiện tại
val themeMode by settingsDataSource.themeMode.collectAsState(initial = "system")
val isDarkTheme = themeMode == "dark" ||
    (themeMode == "system" && isSystemInDarkTheme())

// Trong UI
GenZThemeSwitch(
    isDarkTheme = isDarkTheme,
    onToggle = {
        scope.launch {
            val newMode = if (isDarkTheme) ThemeMode.LIGHT.value else ThemeMode.DARK.value
            settingsDataSource.setThemeMode(newMode)
        }
    }
)
```

---

## 8. Áp dụng Theme vào toàn App

```kotlin
// presentation/ui/theme/Theme.kt
@Composable
fun AppTheme(
    themeMode: String = "system",  // "light" | "dark" | "system"
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark"  -> true
        else    -> isSystemDark
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val genZColors  = if (darkTheme) DarkGenZColors  else LightGenZColors

    // Cập nhật màu Status Bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalGenZColors provides genZColors) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
```

```kotlin
// MainActivity.kt
val themeMode by settingsDataSource.themeMode.collectAsState(initial = "system")

AppTheme(themeMode = themeMode) {
    // ... NavHost / content
}
```

---

## 9. Lưu Theme vào DataStore

```kotlin
// data/datasource/SettingsDataSource.kt
private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

val themeMode: Flow<String> = dataStore.data
    .map { it[THEME_MODE_KEY] ?: "system" }

suspend fun setThemeMode(mode: String) {
    dataStore.edit { it[THEME_MODE_KEY] = mode }
}
```

---

## 10. Tóm tắt luồng hoạt động

```
Người dùng nhấn Switch
        │
        ▼
onToggle() → setThemeMode("dark"/"light") → DataStore
        │
        ▼
themeMode Flow emit giá trị mới
        │
        ▼
MainActivity recompose → AppTheme(themeMode) đổi ColorScheme
        │
        ▼
SettingsScreen recompose → isDarkTheme thay đổi
        │
        ▼
GenZThemeSwitch animate:
  • thumbOffsetAnim  → thumb trượt trái/phải (spring)
  • trackColorAnim   → track đổi màu (tween)
  • thumbColorAnim   → thumb đổi màu (tween)
  • starAlphaAnim    → sao fade in/out (tween)
  • Canvas vẽ Moon hoặc Sun tùy isDarkTheme
```

---

## 11. Imports cần thiết cho file chứa Switch

```kotlin
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
```
