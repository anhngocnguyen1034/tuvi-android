# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumentation tests (requires connected device/emulator)
./gradlew clean                  # Clean build artifacts
./gradlew installDebug           # Build and install debug APK to connected device
```

## Architecture

**Clean Architecture + MVVM** Android app (Jetpack Compose) for Tử Vi — a Vietnamese astrological chart system.

### Layers

```
di/                → AppContainer (Service Locator singleton, no Hilt — manual DI)
domain/            → Pure Kotlin: models, repository interfaces, use cases
data/repository/   → Repository implementations
data/remote/       → Retrofit API service + DTOs
data/local/        → Room database (TuViDatabase v7), DAOs, entities
data/preferences/  → DataStore (UserPreferencesRepository) for theme/locale/notification settings
data/mapper/       → DTO → domain mappers
presentation/      → ViewModels + UiState sealed interfaces
ui/screens/        → Composable screens (main app features)
ui/browser/        → Composable screens (built-in browser feature)
ui/theme/          → TuViTheme, color tokens, TuViComposeColors
notification/      → AlarmManager helpers, BroadcastReceivers
```

### Data Flow

```
InputScreen → TuViViewModel.getTuVi() → GetTuViChartUseCase → TuViRepositoryImpl
→ TuViApiService (Retrofit POST /api/tuvi) → TuViMapper.toDomain() → Result<TuViChart>
→ StateFlow<TuViUiState> → TuViChartScreen recompose
```

### Key Patterns

- **DI**: `AppContainer` (object) holds all lazily-initialized dependencies. `AppContainer.init(context)` is called in `TuViApplication`. ViewModels use `ViewModelProvider.Factory` to pull from `AppContainer`.
- **Error handling**: `Result<T>` returned from use cases; UiState is a sealed interface (`Idle | Loading | Success | Error`).
- **API**: `POST http://192.168.0.100:8000/api/tuvi` (local network). Cleartext traffic enabled. JSON via `kotlinx.serialization` with `ignoreUnknownKeys = true`.
- **Local storage**: Room DB (`tuvi_database`, v7) with 5 tables: `saved_charts`, `history`, `bookmarks`, `tab_sessions`, `su_kien`. Always write explicit migrations — never rely on `fallbackToDestructiveMigration` for schema correctness.
- **Preferences**: `UserPreferencesRepository` wraps DataStore for dark theme, locale, and notification toggles (holiday alerts, Rằm/Mùng 1).

### Navigation Routes (NavHost in `MainActivity`)

| Route | Screen |
|---|---|
| `home` | HomeScreen — hub with 4 feature tiles |
| `input` | InputScreen — birth data entry |
| `chart` | TuViChartScreen — 12-Cung astrological chart |
| `lich` | LichScreen — lunar/solar calendar with moon phase |
| `calendar_chooser` | CalendarChooserScreen |
| `settings` | SettingsScreen (→ saved_charts) |
| `saved_charts` | SavedChartsScreen |
| `browser?url={url}&title={title}` | BrowserScreen — multi-tab WebView browser |
| `browser_history` | HistoryScreen |
| `browser_bookmarks` | BookmarkScreen |

URL navigation between browser sub-screens uses `savedStateHandle["pendingUrl"]` so the BrowserViewModel's tab state is preserved.

### Notification System

`AlarmHelper` schedules `AlarmManager.setExactAndAllowWhileIdle` for `SuKienEntity` events (falls back to `am.set` if exact alarm permission is missing on API 31+). `SuKienReceiver` fires the notification; `BootReceiver` reschedules all active alarms after device reboot.

## Tech Stack

- **Min SDK**: 24 | **Target SDK**: 36
- **Kotlin**: 2.0.21 | **AGP**: 8.13.2
- **Compose BOM**: 2024.09.00 + Material 3 + Navigation Compose
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0 (BODY-level logging interceptor)
- **Serialization**: `kotlinx-serialization-json` 1.7.3
- **Local DB**: Room (via `TuViDatabase`) | **Preferences**: DataStore

## Coding Standards (Anhnn Ecosystem)

- **Theme**: Always wrap UI in `TuViTheme`. Use `MaterialTheme.colorScheme` tokens — never hardcode hex colors in Composables. Gradient signature: `Brush.verticalGradient(listOf(Color(0xFFA1A2FF), Color(0xFF4B4EEE)))`.
- **Modifier ordering**: Size → Clip/Background → Clickable → Padding.
- **State**: `collectAsStateWithLifecycle()` for all Flow collection in Composables. Hoist state to the caller; keep Composables stateless.
- **Performance**: Mark stable domain models with `@Immutable` or `@Stable`. Use `remember` for heavy computations; `derivedStateOf` for derived state that changes frequently. Always supply `key` in `LazyColumn`/`LazyRow`.
- **Concurrency**: All I/O on `Dispatchers.IO`. Never block the main thread.
- **Testing**: Use Fake Repositories (not Mockito/Mockk) for ViewModel unit tests. Use `StandardTestDispatcher` for coroutines. Test name format: `should_show_error_when_x_fails()`.
- **Previews**: Each Composable must have ≥ 2 `@Preview` annotations (Light + Dark).

## Domain Vocabulary

- **Thiên Bạn** — Heavenly Board (master chart info)
- **Địa Bạn** — Earthly Board (12 Cung/houses)
- **Cung** — Astrological house (12 total)
- **Sao** — Star placed in a Cung
- **Ngũ Hành** — Five elements (Water/Fire/Metal/Wood/Earth) used for star coloring
- **Tuần/Triệt** — Damaged/Broken indicators on a Cung
- **Sự Kiện** — Calendar event with optional alarm notification
- **Lịch** — Lunar/solar calendar screen
