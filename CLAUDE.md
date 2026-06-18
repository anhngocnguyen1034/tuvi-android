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

### Advertising (AdMob)

The ad **engine** lives in the external module `com.anhnn.ads` (`anhnn-components-ads`, JitPack). The app only holds two ad files in `ads/`:

- **`AdNames`** — placement constants + `formatOf(name): AdFormat?` registry (the app declares which placement is interstitial/native/banner).
- **`RemoteConfigManager`** — Firebase Remote Config: `adsEnabled()`, `interMinIntervalMs()`, and per-format ad-unit resolution from the `ad_units` JSON (falls back to Google **test** unit IDs when a placement isn't configured).

Wiring (do not reintroduce a local ad manager — it was deliberately removed):

1. `TuViApplication.onCreate` → `RemoteConfigManager.init()` then `Ads.init(AdsConfig(...))`, injecting `adsEnabled` / `adUnitId` / `adFormat` / `interCooldownMs` from `RemoteConfigManager` + `AdNames` (the module is Firebase-agnostic — all app config flows through `AdsConfig`).
2. `MainActivity.onCreate` → `Ads.start(this) { Ads.preload(...) }` (UMP consent + `MobileAds.initialize`, then preload). Each NavHost destination calls `Ads.preload(context, ...)` in a `LaunchedEffect` to prime the placements it will show.
3. Interstitials: `Ads.showInterstitial(activity, AdNames.X) { next() }` — the callback **always runs** (even when skipped by the global ~30s cooldown or not yet loaded), so navigation/actions are never blocked.
4. Native/banner: `NativeAd(adName)` / `BannerAd(adName)` composables (cache-first → instant; banners load inline). Native is theme-aware via `MaterialTheme.colorScheme`.

**Banner placement convention**: put `BannerAd` *inside* screen content (content area takes `Modifier.weight(1f)`, banner pinned below) rather than `Scaffold.bottomBar` — so it shares the screen background and avoids a color seam.

New production placements must be added to the `ad_units` JSON in Firebase Remote Config; otherwise they serve test ads.

## Tech Stack

- **Min SDK**: 24 | **Target SDK**: 36
- **Kotlin**: 2.0.21 | **AGP**: 8.13.2
- **Compose BOM**: 2024.09.00 + Material 3 + Navigation Compose
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0 (BODY-level logging interceptor)
- **Serialization**: `kotlinx-serialization-json` 1.7.3
- **Local DB**: Room (via `TuViDatabase`) | **Preferences**: DataStore
- **Ads / Remote**: AdMob (`play-services-ads`) + UMP consent + Firebase Remote Config, driven through the `anhnn-components-ads` module
- **Analytics**: Firebase Analytics via the `anhnn-components-analytics` module. `Analytics.init()` in `TuViApplication`, `TrackScreenViews(navController)` in `MainActivity` (auto `screen_view`), app-specific event names in `analytics/Events.kt`. **Never log birth data (name/date/time)** — only enum/boolean params.
- **Shared libraries** (JitPack, `com.github.anhngocnguyen1034.*`): `anhnn-components-{feedback,rate,exit,ads,analytics}` + `anhnn-language`. These are ad-/app-agnostic Compose modules consumed via the version catalog — edits to them happen in the separate `anhnn-components` repo, published by tag (no `includeBuild`).

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
