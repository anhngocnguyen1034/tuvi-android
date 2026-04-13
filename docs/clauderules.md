// Anhnn Ecosystem - Android Jetpack Compose .cursorrules

// Flexibility Notice
// Note: This is the official Anhnn Ecosystem structure. Adapt to existing project organization while maintaining Anhnn standards.
// Focus on consistency with Anhnn's architecture: Clean Architecture + Jetpack Compose.

// Project Architecture and Best Practices
const androidJetpackComposeBestPractices = [
"Strictly follow Anhnn Ecosystem's Clean Architecture (Data, Domain, Presentation)",
"Use Material Design 3 guidelines with Anhnn's signature Purple Gradient (#A1A2FF to #4B4EEE)",
"Use Retrofit with Coroutines (suspend functions) for network calls",
"Leverage Kotlin Flow for reactive data streams from Data to Presentation layer",
"Implement dependency injection using Hilt",
"Maintain Unidirectional Data Flow (UDF) with ViewModel and single UI State objects",
"Use Compose Navigation with Type-safety",
"State Hoisting: Keep composables stateless by moving state to the caller or ViewModel",
];

// Folder Structure (Anhnn Standard)
const projectStructure = `
app/
  src/
    main/
      java/com/anhnn/
        core/             # Base classes, common constants, and Anhnn's branding tokens
        di/               # Hilt Modules
        data/
          repository/     # Concrete implementations
          datasource/     # Remote (Retrofit/Static Service) & Local (Room)
          models/         # DTOs
        domain/
          usecases/       # Pure business logic
          models/         # Domain entities (Stable/Immutable)
          repository/     # Interfaces
        presentation/
          screens/        # Feature screens
          components/     # Anhnn reusable components (AnhnnButton, AnhnnSwitch)
          theme/          # AnhnnDesignSystem (Color.kt, Theme.kt)
          viewmodels/
        utils/            # Extensions and Helpers
      res/                # Android resources (XML tokens only)
    test/                 # Unit tests for ViewModels & UseCases
    androidTest/          # UI tests for Compose
`;

// Compose UI Guidelines
const composeGuidelines = `
1. Use remember and derivedStateOf to minimize unnecessary recompositions
2. Apply Anhnn's Purple Gradient using Brush.verticalGradient where applicable
3. Modifiers ordering: Size -> Clip -> Background -> Clickable -> Padding
4. Naming: Composable functions must be Nouns (e.g., AnhnnPremiumCard)
5. Previews: Provide at least two @Preview (Light/Dark) for every UI component
6. Error Handling: Use sealed classes for UI State (Loading, Success, Error)
7. Accessibility: Ensure minimum touch target of 48dp and provide contentDescriptions
   `;

// Testing Guidelines
const testingGuidelines = `
1. Use StandardTestDispatcher for testing Coroutines/Flows
2. Implement Fake Repositories for robust ViewModel unit testing
3. Use createComposeRule() for testing Anhnn custom components
4. Mock network responses from Static Service (hihoay.com) for isolated tests
   `;

// Performance Guidelines
const performanceGuidelines = `
1. Key usage: Always provide unique keys in LazyColumn/LazyRow
2. Stability: Mark domain models with @Immutable or @Stable
3. Image Loading: Use Coil with disk caching for assets from Anhnn Static Service
4. State Reads: Defer state reads using lambda modifiers where possible
5. Lifecycle: Use collectAsStateWithLifecycle() to observe Flows safely
6. Background: Offload all I/O and heavy parsing to Dispatchers.IO
   `;