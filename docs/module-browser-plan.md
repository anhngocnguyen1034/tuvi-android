# Kế hoạch Module Browser — TuVi App

> **Mục tiêu:** Xây dựng một module WebView có thể tái sử dụng, hoạt động như một tính năng độc lập trên màn hình Home, đồng thời có thể được nhúng vào bất kỳ màn hình nào khác trong ứng dụng.

---

## 1. Tổng quan thiết kế

### Module là gì?

`BrowserModule` là một in-app browser dựa trên `WebView` của Android, được bọc trong Jetpack Compose. Nó cho phép:

- Mở URL bất kỳ trong ứng dụng mà không chuyển sang trình duyệt ngoài
- Tích hợp sẵn toolbar với điều hướng (Back / Forward / Reload / Share)
- Hỗ trợ thanh địa chỉ để người dùng nhập URL hoặc từ khoá tìm kiếm
- Truyền cấu hình từ nơi gọi (URL ban đầu, tiêu đề, có hiện toolbar không...)
- Được dùng từ `HomeScreen` như một tính năng "Trình Duyệt Web"
- Được nhúng trực tiếp vào màn hình khác khi cần load trang bên ngoài (ví dụ: đọc bài viết phong thuỷ, xem tài liệu online)

---

## 2. Cấu trúc file

```
ui/
└── browser/
    ├── BrowserScreen.kt          ← Composable full-screen: toolbar + WebView
    ├── BrowserViewModel.kt       ← Quản lý state: url, title, progress, canGoBack...
    ├── BrowserUiState.kt         ← Sealed class cho trạng thái tải trang
    ├── WebViewContainer.kt       ← AndroidView wrapper cho WebView (tái sử dụng)
    └── BrowserConfig.kt          ← Data class cấu hình đầu vào của module
```

> **Không có layer domain/data** — module này thuần UI, không cần repository hay use case. State nằm hoàn toàn trong ViewModel.

---

## 3. API của module (điểm tích hợp)

### 3.1 `BrowserConfig` — Truyền vào khi gọi module

```kotlin
data class BrowserConfig(
    val initialUrl: String,          // URL mở ngay khi vào
    val title: String = "Trình duyệt", // Tiêu đề mặc định trên toolbar
    val showAddressBar: Boolean = true, // Hiện thanh địa chỉ
    val allowUserNavigation: Boolean = true, // Cho phép nhập URL mới
    val userAgent: String? = null,   // Custom user-agent nếu cần
    val javaScriptEnabled: Boolean = true
)
```

### 3.2 `BrowserScreen` — Composable đầu vào

```kotlin
@Composable
fun BrowserScreen(
    config: BrowserConfig,
    onBack: () -> Unit
)
```

Gọi từ bất cứ đâu chỉ với 2 tham số — `config` và `onBack`.

### 3.3 Tích hợp vào NavHost (MainActivity)

```kotlin
// Route dạng: browser?url=...&title=...
composable(
    "browser?url={url}&title={title}",
    arguments = listOf(
        navArgument("url") { defaultValue = "https://www.google.com" },
        navArgument("title") { defaultValue = "Trình duyệt" }
    )
) { backStackEntry ->
    val url   = backStackEntry.arguments?.getString("url") ?: ""
    val title = backStackEntry.arguments?.getString("title") ?: "Trình duyệt"
    BrowserScreen(
        config = BrowserConfig(initialUrl = url, title = title),
        onBack = { navController.popBackStack() }
    )
}
```

**Cách navigate đến browser từ bất kỳ màn hình nào:**

```kotlin
val encodedUrl = Uri.encode("https://vi.wikipedia.org/wiki/Tử_vi")
navController.navigate("browser?url=$encodedUrl&title=Tử Vi Wikipedia")
```

---

## 4. Chi tiết từng file

### 4.1 `BrowserUiState.kt`

```kotlin
data class BrowserUiState(
    val url: String = "",
    val displayTitle: String = "",
    val progress: Int = 0,          // 0..100
    val isLoading: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val error: String? = null
)
```

### 4.2 `BrowserViewModel.kt`

State được cập nhật bởi callback từ `WebViewClient` / `WebChromeClient`:

| Method | Trigger |
|--------|---------|
| `onPageStarted(url)` | WebViewClient.onPageStarted |
| `onPageFinished(url, title)` | WebViewClient.onPageFinished |
| `onProgressChanged(progress)` | WebChromeClient.onProgressChanged |
| `onReceivedError(error)` | WebViewClient.onReceivedError |
| `navigateTo(url)` | Người dùng nhập URL mới |

ViewModel **không giữ reference đến WebView** (tránh leak). Thay vào đó dùng `SharedFlow<BrowserCommand>` để ra lệnh cho `WebViewContainer`:

```kotlin
sealed interface BrowserCommand {
    data class LoadUrl(val url: String) : BrowserCommand
    object GoBack    : BrowserCommand
    object GoForward : BrowserCommand
    object Reload    : BrowserCommand
}
```

### 4.3 `WebViewContainer.kt`

```kotlin
@Composable
fun WebViewContainer(
    modifier: Modifier,
    config: BrowserConfig,
    commandFlow: SharedFlow<BrowserCommand>,
    onStateChanged: (url: String, title: String, progress: Int, canBack: Boolean, canForward: Boolean) -> Unit,
    onError: (String) -> Unit
)
```

- Dùng `AndroidView { WebView(context) }` bên trong
- Khai báo `DisposableEffect` để hủy WebView khi Composable rời khỏi composition
- `remember { webView }` để giữ instance qua recompose
- Collect `commandFlow` bằng `LaunchedEffect`

### 4.4 `BrowserScreen.kt`

Layout:
```
┌─────────────────────────────────┐
│  ← [Tiêu đề]          ↺  ⬆  ⋮  │  ← TopAppBar
│─────────────────────────────────│
│  🔒 https://example.com      [→]│  ← Address bar (nếu showAddressBar=true)
│─────────────────────────────────│
│  ████░░░░░░░░░░░░░░░░  45%      │  ← LinearProgressIndicator (ẩn khi xong)
│─────────────────────────────────│
│                                 │
│         WebViewContainer        │
│                                 │
│─────────────────────────────────│
│   ←   →   ↺   ⬆   📋           │  ← BottomBar: back/forward/reload/share/copy
└─────────────────────────────────┘
```

---

## 5. Tích hợp vào HomeScreen

Thêm card **"Trình Duyệt"** vào grid 2×2 hiện tại (thay placeholder "Tương Hợp"):

```kotlin
// HomeScreen.kt — thay card placeholder đầu tiên
SecondaryFeatureCard(
    emoji = "🌐",
    title = "Trình\nDuyệt",
    description = "Duyệt web & tài liệu",
    onClick = onOpenBrowser
)
```

Và thêm callback vào `HomeScreen`:

```kotlin
fun HomeScreen(
    onOpenTuVi: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenBrowser: () -> Unit   // ← thêm
)
```

Từ `MainActivity`, gọi:
```kotlin
onOpenBrowser = {
    val url = Uri.encode("https://www.google.com")
    navController.navigate("browser?url=$url&title=Trình Duyệt")
}
```

---

## 6. Điểm tái sử dụng nội bộ

Module này có thể được gọi từ bất kỳ màn hình nào:

| Màn hình | Khi nào dùng | URL mặc định |
|----------|--------------|--------------|
| `HomeScreen` | Card "Trình Duyệt" | google.com |
| `TuViChartScreen` | Nút "Tìm hiểu thêm" về sao/cung | Tra cứu sao Tử Vi |
| `SavedChartsScreen` | Xem tài liệu hướng dẫn | Docs app |
| Bất kỳ màn hình nào | Deep link từ notification | URL tùy ý |

---

## 7. Dependency cần thêm

Không cần thêm dependency bên ngoài. `WebView` có sẵn trong Android SDK.

Chỉ cần đảm bảo trong `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<!-- Đã có sẵn -->
```

Nếu muốn tải file (PDF, ảnh) từ browser, cần thêm sau:
```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
<!-- Đã có sẵn -->
```

---

## 8. Các lưu ý kỹ thuật quan trọng

### WebView memory leak
- **Bắt buộc** gọi `webView.destroy()` trong `DisposableEffect { onDispose { ... } }`
- **Không** lưu `WebView` instance trong ViewModel
- Dùng `remember(config.initialUrl) { WebView(context) }` để giữ reference đúng cách

### Back press
- Override `BackHandler` trong Compose để khi ấn Back mà WebView `canGoBack()`, thì WebView go back thay vì pop stack:
```kotlin
BackHandler(enabled = uiState.canGoBack) {
    viewModel.sendCommand(BrowserCommand.GoBack)
}
```

### JavaScript
- Mặc định bật JS (`javaScriptEnabled = true` trong `BrowserConfig`)
- Với các URL nội bộ/tin cậy có thể thêm `addJavascriptInterface` nếu cần

### HTTPS / Mixed content
- `settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE` để tránh lỗi trên các trang HTTP

### Process Text / File chooser
- Nếu cần upload file trong WebView: override `WebChromeClient.onShowFileChooser`

---

## 9. Lộ trình triển khai

| Bước | Nội dung | File |
|------|----------|------|
| 1 | Tạo `BrowserConfig` + `BrowserUiState` | `BrowserConfig.kt`, `BrowserUiState.kt` |
| 2 | Tạo `BrowserViewModel` với command flow | `BrowserViewModel.kt` |
| 3 | Tạo `WebViewContainer` (AndroidView + lifecycle) | `WebViewContainer.kt` |
| 4 | Tạo `BrowserScreen` (toolbar + address bar + bottom nav) | `BrowserScreen.kt` |
| 5 | Thêm route `browser` vào `NavHost` trong `MainActivity` | `MainActivity.kt` |
| 6 | Thêm card "Trình Duyệt" vào `HomeScreen` | `HomeScreen.kt` |

---

## 10. Cấu trúc package sau khi hoàn thành

```
com/example/tuvi/
├── ui/
│   ├── browser/                ← Module mới (toàn bộ tự chứa)
│   │   ├── BrowserConfig.kt
│   │   ├── BrowserUiState.kt
│   │   ├── BrowserViewModel.kt
│   │   ├── WebViewContainer.kt
│   │   └── BrowserScreen.kt
│   └── screens/
│       ├── HomeScreen.kt       ← Thêm onOpenBrowser callback
│       ├── InputScreen.kt
│       ├── SavedChartsScreen.kt
│       ├── SaveChartDialog.kt
│       └── TuViChartScreen.kt
└── MainActivity.kt             ← Thêm route "browser"
```
