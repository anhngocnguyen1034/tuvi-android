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

This is a **Clean Architecture + MVVM** Android app (Jetpack Compose) for Tử Vi — a Vietnamese astrological chart system.

### Layers

```
di/           → Manual DI via AppContainer (Service Locator pattern, singleton object)
domain/       → Pure Kotlin: models, repository interface, use cases
data/         → Repository impl, Retrofit API service, DTOs, mappers (DTO → domain)
presentation/ → ViewModel + UiState (sealed interface)
ui/screens/   → Composable screens
```

### Data Flow

```
InputScreen → TuViViewModel.getTuVi() → GetTuViChartUseCase → TuViRepositoryImpl
→ TuViApiService (Retrofit POST /api/tuvi) → TuViMapper.toDomain() → Result<TuViChart>
→ StateFlow<TuViUiState> → TuViChartScreen recompose
```

### Key Patterns

- **DI**: `AppContainer` (object) holds all lazily-initialized dependencies. ViewModel uses `ViewModelProvider.Factory` to pull from `AppContainer`.
- **Error handling**: `Result<T>` returned from use cases; `TuViUiState` is a sealed interface (`Idle | Loading | Success | Error`).
- **API**: Single endpoint `POST http://192.168.1.19:8000/api/tuvi` (local network). Cleartext traffic enabled. JSON via `kotlinx.serialization` with `IntOrStringSerializer` for polymorphic fields.
- **No local database**: App is stateless; all data comes from the remote API.

### Navigation

Two screens via `NavHost`:
1. `input` → `InputScreen`: collects birth date/time, name, gender, viewing year
2. `chart` → `TuViChartScreen`: renders 12-house (Cung) chart with stars (Sao) colored by Ngũ Hành (5 elements)

## Tech Stack

- **Min SDK**: 24 | **Target SDK**: 36
- **Kotlin**: 2.0.21 | **AGP**: 8.13.2
- **Compose BOM**: 2024.09.00 + Material 3 + Navigation Compose
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0 (with `BODY`-level logging interceptor)
- **Serialization**: `kotlinx-serialization-json` 1.7.3

## Domain Vocabulary

- **Thiên Bạn** — Heavenly Board (master chart info)
- **Địa Bạn** — Earthly Board (12 Cung/houses)
- **Cung** — Astrological house (12 total)
- **Sao** — Star placed in a Cung
- **Ngũ Hành** — Five elements (Water/Fire/Metal/Wood/Earth) used for star coloring
- **Tuần/Triệt** — Damaged/Broken indicators on a Cung
