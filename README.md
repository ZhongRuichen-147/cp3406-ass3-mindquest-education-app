# MindQuest

## Project Overview
MindQuest is an educational Android app for primary-school children (roughly ages 6–11) that
builds pattern recognition, memory, and problem-solving through two short activities: a trivia
Quiz and a Memory Match puzzle. It follows on from the ethics research in Assessment 2
("Children's Data Privacy & Data Security in Educational Apps") — that research is not just cited
here, it directly shaped what the app does and does not do. See [Ethics-to-Design Mapping](#ethics-to-design-mapping)
below.

## Screens
- **Landing** — greeting, current streak, games played, and shortcuts into Quiz or Memory Match
- **Activity** — tabbed between:
  - **Quiz**: multiple-choice trivia flashcards, fetched from Open Trivia DB and cached locally
  - **Memory Match**: an offline card-matching puzzle, grid size scales with difficulty
- **Settings** — difficulty (Easy/Medium/Hard), sound toggle, dark theme, a "what we store"
  transparency panel, and a parental-gate-protected "Clear my data" action
- **Statistics** — games played, day streak, quiz accuracy, best score, and recent activity history

## Architecture
- **UI**: Jetpack Compose + Material Design 3, single Activity, [Navigation Compose](https://developer.android.com/develop/ui/compose/navigation)
  (`NavHost` + bottom `NavigationBar`) between Landing / Activity / Statistics, with Settings
  reachable from the top app bar
- **MVVM**: one `ViewModel` per screen, exposing `StateFlow<UiState>`
- **Repository pattern**: `QuizRepository`, `StatsRepository`, `SettingsRepository` sit between
  the ViewModels and the data sources, wrapping results in a `NetworkResult` sealed class
- **DI**: [Koin](https://insert-koin.io/) (`di/Modules.kt`), split into network / database /
  repository / view-model modules
- **Persistence**:
  - [Room](https://developer.android.com/training/data-storage/room) for cached quiz questions and
    the activity/score history (`data/local`)
  - [Jetpack DataStore (Preferences)](https://developer.android.com/topic/libraries/architecture/datastore)
    for user settings (`data/settings`)
- **Networking**: Retrofit + kotlinx.serialization, calling the free
  [Open Trivia DB](https://opentdb.com/) API — no API key required

## Tech Stack
- Kotlin, Jetpack Compose, Material Design 3
- Navigation Compose
- ViewModel + Repository pattern, Koin dependency injection
- Room (persistence) + Jetpack DataStore (preferences)
- Retrofit, OkHttp, Kotlinx Serialization, Kotlin Coroutines
- JUnit4, MockK, kotlinx-coroutines-test (unit tests)
- Compose UI Test + Espresso (instrumented tests)

## Ethics-to-Design Mapping
Every row below is a specific finding from the Assessment 2 research turned into a specific,
checkable engineering decision in this codebase — not a general privacy statement.

| Assessment 2 finding | MindQuest implementation |
|---|---|
| Child-only apps must not request location or transmit device identifiers | The app never requests `ACCESS_FINE_LOCATION` or collects any advertising/device ID — the only permission declared is the normal `INTERNET` permission (no runtime prompt needed) |
| Audit every third-party SDK; no ad-sharing | Zero ad or analytics SDKs anywhere in the dependency graph — the only component with network access is `TriviaApi`, and it only ever *receives* trivia content, never sends anything about the user |
| Collect only what learning requires | No account, login, or personal data of any kind — Room only stores anonymous play results (score, timestamp, duration) and a cached copy of trivia questions |
| No indefinite retention | `Settings → Clear my data` wipes Room *and* DataStore in one action (`SettingsViewModel.submitGateAnswer`) |
| Respect privacy & confidentiality / transparency | The Settings screen has a plain-English "What we store" card explaining exactly what is kept and that it never leaves the device |
| Google Play Families Policy — neutral screening before a sensitive action | A lightweight arithmetic "parental gate" (`ParentGateDialog`) guards only the destructive data-reset action — deliberately *not* every settings toggle, since gating benign preferences would add friction without any privacy benefit |
| Sandboxed storage for student records | Room's default internal app storage is used; nothing is written to shared/external storage |
| Kid-safe content sourcing | Trivia questions are restricted to a whitelist of safe Open Trivia DB categories (Animals, Science & Nature, General Knowledge) at `easy`/`medium`/`hard` difficulty only — see `TriviaCategories.SAFE_CATEGORIES` |

## Testing
**Unit tests** (`app/src/test`, JUnit4 + MockK + kotlinx-coroutines-test) — 24 tests:
- `QuizRepositoryImplTest` — network-success caching, offline fallback to Room cache, error when both fail
- `StatsRepositoryImplTest` — entity↔domain mapping, insert/clear delegation
- `QuizViewModelTest` — scoring, double-answer guard, quiz completion recording
- `MemoryMatchViewModelTest` — deck size per difficulty, match/mismatch state transitions, completion recording
- `SettingsViewModelTest` — parental gate accepts/rejects, and only clears data on a correct answer
- `StreakCalculatorTest` — pure day-streak logic across timezone-safe day buckets
- `StatisticsUiStateMapperTest` — accuracy/aggregate calculation, including the zero-quiz-games edge case

Run with:
```
./gradlew testDebugUnitTest
```

**Instrumented UI tests** (`app/src/androidTest`, Compose UI Test + Espresso) — require a connected
device or emulator:
- `QuizScreenInstrumentedTest` — selecting an answer reveals the "Next question" control
- `NavigationInstrumentedTest` — bottom navigation switches between Landing / Statistics / Activity

Run with:
```
./gradlew connectedAndroidTest
```

## Running the Project
1. Open this folder in Android Studio, or run from the command line:
   ```
   ./gradlew assembleDebug
   ```
2. `local.properties` must point `sdk.dir` at your local Android SDK (Android Studio generates
   this automatically; it is git-ignored).
3. minSdk 24, targetSdk 36.

## Declaration of AI-Generated Material
Development of this project was assisted by Claude Code (Anthropic). As required by the
Assessment 3 academic integrity guidelines, a completed Declaration of AI-Generated Material
should accompany the final submission alongside this repository and the self-reflection PDF.
