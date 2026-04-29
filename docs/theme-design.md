# Theme Design — Dark / Light

## Tổng quan

App TuVi hỗ trợ hai chế độ màu: **Dark** (mặc định) và **Light**. Chế độ được user chọn trong màn Settings, lưu vào DataStore, **không phụ thuộc vào system theme của điện thoại**.

---

## Bảng màu token

| Token | Dark value | Light value | Dùng cho |
|-------|-----------|-------------|----------|
| `TuViNavy` | `#0A1628` | `#F5F0E8` | Background chính |
| `TuViNavyLight` | `#112240` | `#EDE8DC` | Surface / card nền |
| `TuViNavyCard` | `#1A2E4A` | `#E8E0D0` | Card nền |
| `TuViGold` | `#D4AF37` | `#B8962E` | Accent chính, icon, label |
| `TuViGoldLight` | `#F0D060` | `#D4AF37` | Highlight nhạt |
| `TuViGoldDark` | `#A0871A` | `#8B6914` | Border, shadow |
| `TuViIvory` | `#F5F0E8` | `#1A1A2E` | Text chính |
| `TuViIvoryDim` | `#B8A898` | `#4A4060` | Text phụ / muted |
| `TuViRed` | `#C0392B` | `#C0392B` | Accent đỏ (cố định) |
| `TuViDivider` | `#2A3F5F` | `#C8BFAA` | Đường kẻ |

> Màu được định nghĩa trong `res/values/colors.xml` (dark) và `res/values-night/colors.xml` (light), đọc qua `TuViComposeColors`.

---

## Cách theme hoạt động

```
UserPreferencesRepository (DataStore)
  └── themeDarkFlow: Flow<Boolean>
        └── SettingsViewModel.uiState.themeDark
              └── MainActivity → TuViTheme(darkTheme = state.themeDark)
                    └── MaterialTheme colorScheme (dark/light)
                          └── toàn bộ Composable screen
```

**Luồng khởi động:**
1. `TuViApplication.onCreate()` đọc `themeDark` từ DataStore qua `runBlocking`.
2. Gọi `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES/NO)` để Android resources trả về đúng night/day colors.
3. `TuViComposeColors.initIfNeeded(forceNight = savedDark)` load màu từ `colors.xml`.
4. `MainActivity` collect `SettingsViewModel.uiState` → truyền `darkTheme` vào `TuViTheme`.
5. System theme của điện thoại **bị bỏ qua hoàn toàn**.

---

## MaterialTheme mapping

### Dark mode
```
primary             = TuViGold        (#D4AF37)
onPrimary           = TuViNavy        (#0A1628)
primaryContainer    = TuViNavyCard    (#1A2E4A)
background          = TuViNavy        (#0A1628)
onBackground        = TuViIvory       (#F5F0E8)
surface             = TuViNavyLight   (#112240)
onSurface           = TuViIvory       (#F5F0E8)
```

### Light mode
```
primary             = TuViGoldDark    (#A0871A)
onPrimary           = TuViIvory       (#F5F0E8)
primaryContainer    = TuViGoldLight @ 35% alpha
background          = TuViNavy        (#F5F0E8)  ← đọc từ colors.xml night variant
onBackground        = TuViIvory       (#1A1A2E)  ← đọc từ colors.xml night variant
surface             = TuViNavyLight   (#EDE8DC)
onSurface           = TuViIvory       (#1A1A2E)
```

> **Lưu ý**: `background` và `surface` trong Light mode thực ra là các màu sáng vì `TuViNavy` / `TuViNavyLight` được override trong `res/values-night/colors.xml` (Android tự swap khi `MODE_NIGHT_NO`).

---

## Status bar / Navigation bar

```kotlin
isAppearanceLightStatusBars    = !isDark   // dark mode → icon sáng, light mode → icon tối
isAppearanceLightNavigationBars = !isDark
```

---

## Settings Screen

- Switch toggle: bật = Dark, tắt = Light
- Icon: mặt trời (light) / mặt trăng (dark) — hiện dùng `ic_clock_light` placeholder
- Thay đổi áp dụng **ngay lập tức** qua StateFlow, không cần restart app (sau fix)

---

## Điểm cần review

- [ ] `res/values-night/colors.xml` — kiểm tra Light palette có đủ contrast không (nền sáng, chữ tối)
- [ ] `ChartDeepBg`, `ChartNavy`, `ChartCardBg` trong Light mode — lá số tử vi nên có nền sáng hay vẫn giữ tông navy?
- [ ] Icon sun/moon trong `ThemeSwitchRow` đang dùng `ic_clock_light` — cần thay bằng icon đúng
- [ ] Browser (incognito colors) chưa có Light variant riêng

---

## Files liên quan

```
ui/theme/Theme.kt                        — TuViTheme composable
ui/theme/Color.kt                        — token getters
ui/theme/TuViComposeColors.kt            — load màu từ XML resources
data/preferences/UserPreferencesRepository.kt  — DataStore lưu themeDark
presentation/SettingsViewModel.kt        — setThemeDark(), uiState flow
ui/screens/SettingsScreen.kt             — ThemeSwitchRow UI
MainActivity.kt                          — wiring TuViTheme(darkTheme=...)
TuViApplication.kt                       — bootstrap synchronous load
res/values/colors.xml                    — dark palette
res/values-night/colors.xml              — light palette (nếu có)
```
