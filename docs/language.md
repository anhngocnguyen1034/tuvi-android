# anhnn-language

Module chọn ngôn ngữ dùng chung cho Android (Jetpack Compose + DataStore).

Hỗ trợ 24 ngôn ngữ, kèm cờ quốc gia và khoá xoay màn hình tự động.

---

## Cài đặt

### 1. Thêm JitPack vào `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Thêm dependency vào `app/build.gradle.kts`

```kotlin
dependencies {
    implementation("com.github.anhngocnguyen1034:anhnn-language:1.0.0")
}
```

---

## Tích hợp

### Bước 1 — Áp dụng ngôn ngữ đã lưu khi app khởi động

Override `attachBaseContext` trong mọi `Activity` (hoặc chỉ `MainActivity` nếu dùng single-activity):

```kotlin
import com.anhnn.language.LanguageDataSource
import com.anhnn.language.LanguageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val code = runBlocking { LanguageDataSource(newBase).languageCode.first() }
        super.attachBaseContext(LanguageManager.setLanguage(newBase, code))
    }
}
```

### Bước 2 — Hiển thị màn hình chọn ngôn ngữ

```kotlin
import com.anhnn.language.LanguageScreen

// Trong NavGraph hoặc bất kỳ Composable nào:
val context = LocalContext.current

LanguageScreen(
    onBack = { navController.popBackStack() },
    onLanguageSaved = { langCode ->
        // Gọi recreate() để app load lại đúng ngôn ngữ
        (context as? Activity)?.recreate()
    }
)
```

### Bước 3 — Thêm bản dịch cho app của bạn

Module chỉ chứa string của chính nó (`anhnn_select_language`).
Các string khác của app bạn đặt trong file `strings.xml` theo từng ngôn ngữ như bình thường:

```
app/src/main/res/
  values/strings.xml          ← tiếng Anh (mặc định)
  values-vi/strings.xml       ← tiếng Việt
  values-fr/strings.xml       ← tiếng Pháp
  ...
```

Khi người dùng chọn ngôn ngữ, Android tự động load đúng file `strings.xml` tương ứng.

---

## Ngôn ngữ hỗ trợ

| Code | Tên |
|------|-----|
| `en` | English |
| `vi` | Tiếng Việt |
| `es` | Español |
| `fr` | Français |
| `de` | Deutsch |
| `it` | Italiano |
| `pt` | Português |
| `ru` | Русский |
| `ja` | 日本語 |
| `ko` | 한국어 |
| `zh-CN` | 简体中文 |
| `zh-TW` | 繁體中文 |
| `ar` | العربية |
| `hi` | हिन्दी |
| `tr` | Türkçe |
| `pl` | Polski |
| `nl` | Nederlands |
| `id` | Bahasa Indonesia |
| `th` | ไทย |
| `el` | Ελληνικά |
| `cs` | Čeština |
| `sv` | Svenska |
| `no` | Norsk |
| `fi` | Suomi |

---

## API

### `LanguageScreen`

```kotlin
@Composable
fun LanguageScreen(
    onBack: () -> Unit,
    onLanguageSaved: (langCode: String) -> Unit = {}
)
```

Màn hình chọn ngôn ngữ. Tự động khoá xoay màn hình (portrait) khi đang hiển thị và khôi phục lại khi thoát.

---

### `LanguageManager`

```kotlin
// Áp dụng locale, trả về Context mới (dùng trong attachBaseContext)
LanguageManager.setLanguage(context: Context, languageCode: String): Context

// Lấy danh sách ngôn ngữ hỗ trợ
LanguageManager.getSupportedLanguages(): List<Language>

// Tìm ngôn ngữ theo code
LanguageManager.Language.fromCode("vi") // → Language.VIETNAMESE
```

---

### `LanguageDataSource`

```kotlin
val source = LanguageDataSource(context)

// Flow lấy code ngôn ngữ đang dùng (mặc định "en")
source.languageCode: Flow<String>

// Lưu code ngôn ngữ mới
source.setLanguageCode("vi")
```

---

## Cập nhật version

Khi có version mới, chỉ cần đổi số version trong `build.gradle.kts`:

```kotlin
implementation("com.github.anhngocnguyen1034:anhnn-language:1.1.0")
```