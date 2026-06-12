# 🐉 Tử Vi Daily

> Ứng dụng Android xem lá số Tử Vi, lịch âm dương và sự kiện cá nhân — xây dựng hoàn toàn bằng **Jetpack Compose** theo kiến trúc **Clean Architecture + MVVM**.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue)
![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)

---

## ✨ Tính năng

- 🔮 **Lá số Tử Vi** — nhập thông tin sinh (ngày/giờ/giới tính) và an lá số đầy đủ 12 Cung, Thiên Bàn, các Sao tô màu theo Ngũ Hành, đánh dấu Tuần/Triệt.
- 📅 **Lịch âm dương** — tra cứu lịch âm/dương, pha mặt trăng, ngày Rằm/Mùng 1 và các ngày lễ.
- ⏰ **Sự kiện & nhắc nhở** — tạo sự kiện cá nhân với thông báo chính xác (AlarmManager), tự khôi phục lịch nhắc sau khi khởi động lại máy.
- 💾 **Lưu lá số** — lưu và xem lại nhiều lá số trên thiết bị (Room Database).
- 🌐 **Trình duyệt tích hợp** — duyệt web đa tab kèm lịch sử và đánh dấu trang.
- 🌍 **Đa ngôn ngữ** — Tiếng Việt, English, 中文.
- 🌗 **Giao diện sáng/tối** — Material 3, chuyển theme mượt mà.

## 📱 Ảnh màn hình

| Trang chủ | Lá số Tử Vi | Lịch âm dương |
|:---:|:---:|:---:|
| _đang cập nhật_ | _đang cập nhật_ | _đang cập nhật_ |

<!-- Thêm ảnh: ![Home](docs/screenshots/home.png) -->

## 🏗️ Kiến trúc

Dự án tuân theo **Clean Architecture** với 3 tầng tách biệt, kết hợp mô hình **MVVM** ở tầng trình bày:

```
app/src/main/java/com/anhnn/tuvi/
├── di/                  # AppContainer — Service Locator (manual DI)
├── domain/              # Pure Kotlin: models, repository interfaces, use cases
├── data/
│   ├── remote/          # Retrofit API service + DTOs
│   ├── local/           # Room database (DAOs, entities, migrations)
│   ├── preferences/     # DataStore (theme, locale, notification settings)
│   ├── mapper/          # DTO → domain mappers
│   └── repository/      # Repository implementations
├── presentation/        # ViewModels + UiState (sealed interfaces)
├── ui/
│   ├── screens/         # Composable screens (tính năng chính)
│   ├── browser/         # Composable screens (trình duyệt tích hợp)
│   └── theme/           # TuViTheme, color tokens
└── notification/        # AlarmManager helpers, BroadcastReceivers
```

### Luồng dữ liệu

```
UI (Compose) → ViewModel → UseCase → Repository → API / Room / DataStore
            ←  StateFlow<UiState>  ←  Result<T>  ←
```

- **Error handling**: use case trả về `Result<T>`; UI nhận `UiState` dạng sealed interface (`Idle | Loading | Success | Error`).
- **State**: thu thập Flow bằng `collectAsStateWithLifecycle()`, state hoisting, Composable thuần stateless.
- **Lưu trữ cục bộ**: Room DB với migration tường minh; DataStore cho cài đặt người dùng.

## 🛠️ Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin 2.0.21 |
| UI | Jetpack Compose (Material 3) + Navigation Compose |
| Kiến trúc | Clean Architecture, MVVM, StateFlow |
| Networking | Retrofit 2.11 + OkHttp 4.12 |
| Serialization | kotlinx.serialization |
| Database | Room |
| Preferences | DataStore |
| Thông báo | AlarmManager + BroadcastReceiver |
| Build | Gradle (Kotlin DSL), AGP 8.13 |
| CI/CD | Jenkins |

## 🚀 Bắt đầu

### Yêu cầu

- Android Studio (bản mới nhất khuyến nghị)
- JDK 17+
- Thiết bị/emulator Android 7.0 (API 24) trở lên

### Build & chạy

```bash
git clone https://github.com/anhngocnguyen1034/tuvi-android.git
cd tuvi-android

./gradlew assembleDebug      # Build APK debug
./gradlew installDebug       # Cài lên thiết bị đang kết nối
```

### Chạy test

```bash
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumentation tests (cần thiết bị/emulator)
```

> **Lưu ý**: tính năng an lá số cần backend API Tử Vi đang chạy. Cấu hình địa chỉ API trong module data trước khi build.

## 🧪 Quy ước phát triển

- Unit test ViewModel dùng **Fake Repository** (không dùng Mockito/Mockk) + `StandardTestDispatcher`.
- Mỗi Composable có tối thiểu 2 `@Preview` (Light + Dark).
- Không hardcode màu trong Composable — luôn dùng token từ `MaterialTheme.colorScheme`.
- Mọi I/O chạy trên `Dispatchers.IO`.

Chi tiết xem thêm trong thư mục [`docs/`](docs/).

## 📚 Thuật ngữ Tử Vi

| Thuật ngữ | Ý nghĩa |
|---|---|
| **Thiên Bàn** | Bảng thông tin tổng quan của lá số |
| **Địa Bàn** | 12 Cung (nhà) của lá số |
| **Cung** | Một nhà trong lá số (12 cung) |
| **Sao** | Tinh đẩu an trong mỗi Cung |
| **Ngũ Hành** | Kim – Mộc – Thủy – Hỏa – Thổ (dùng tô màu Sao) |
| **Tuần / Triệt** | Dấu hiệu án ngữ trên Cung |

## 🔒 Quyền riêng tư

Xem [Chính sách quyền riêng tư](https://anhngocnguyen1034.github.io/tuvi-privacy).

## 📬 Liên hệ

- Email: nguyenanhcry@gmail.com
- Issues: [GitHub Issues](https://github.com/anhngocnguyen1034/tuvi-android/issues)

## 📄 Giấy phép

Copyright © 2026 Nguyễn Ngọc Anh. All rights reserved.

Mã nguồn được công khai với mục đích tham khảo. Vui lòng liên hệ tác giả trước khi sử dụng lại cho mục đích thương mại.
