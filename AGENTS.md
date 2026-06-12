# Repository Guidelines

## Project Structure & Module Organization

This is a single-module Android app. The app module lives in `app/`, with Kotlin source under `app/src/main/java/com/example/tuvi/`. The project follows Clean Architecture with MVVM:

- `domain/`: pure Kotlin models, repository interfaces, and use cases.
- `data/`: Retrofit DTOs/services, Room DAOs/entities, DataStore preferences, and repository implementations.
- `presentation/`: ViewModels, UI state, and screen-facing logic.
- `ui/`: Jetpack Compose screens, browser UI, and theme files.
- `di/`: `AppContainer` manual dependency wiring.
- `notification/`: alarm scheduling and receivers.

Resources are in `app/src/main/res/`, static assets in `app/src/main/assets/`, unit tests in `app/src/test/`, and instrumentation tests in `app/src/androidTest/`. Additional notes are in `docs/`.

## Build, Test, and Development Commands

Use the Gradle wrapper from the repository root:

- `./gradlew assembleDebug`: build a debug APK.
- `./gradlew installDebug`: build and install on a connected device or emulator.
- `./gradlew test`: run local JVM unit tests.
- `./gradlew connectedAndroidTest`: run Android instrumentation and Compose UI tests; requires a device or emulator.
- `./gradlew clean`: remove build outputs.

## Coding Style & Naming Conventions

Write Kotlin with 4-space indentation and idiomatic Compose patterns. Keep domain models and use cases free of Android framework dependencies. Use `Result<T>` and sealed UI state types for error/loading/success flows. Collect flows with lifecycle-aware APIs.

Name ViewModels as `FeatureViewModel`, UI state as `FeatureUiState`, repositories as `FeatureRepository` and `FeatureRepositoryImpl`, and use cases as verb phrases such as `GetTuViChartUseCase`. Keep Composables in `ui/screens/` or feature UI packages.

## Testing Guidelines

Use JUnit for unit tests and AndroidX/Compose test APIs for instrumentation tests. Prefer fake repositories over Mockito/MockK for ViewModel and use case tests. For coroutine tests, use `StandardTestDispatcher`. Test names should describe behavior, for example `should_show_error_when_static_service_fails()`. Add tests for Room migrations, mappers, and user-facing state transitions.

## Commit & Pull Request Guidelines

Recent commits use Conventional Commit style, often with scopes: `feat(ai): ...`, `fix(settings): ...`, `refactor: ...`. Keep subjects short and imperative, and use Vietnamese or English consistently within a change set.

Pull requests should include a concise summary, test results, linked issues if any, and screenshots or screen recordings for UI changes.

## Security & Configuration Tips

Do not add new secrets, API keys, or signing credentials. Treat `google-services.json`, keystores, local API addresses, and release signing settings as sensitive; prefer local configuration for environment-specific values.
