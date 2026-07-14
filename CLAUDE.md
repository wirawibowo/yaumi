# CLAUDE.md — Yaumi Android

## Project overview

Yaumi is a native Android app (Kotlin + Jetpack Compose) for daily Islamic practice. It provides Al-Qur'an reading with audio, Hadis collections, daily supplications (Doa), prayer-time schedules (Azan) with reliable notifications, Qibla direction, Tasbih counter, Hijri calendar, and a 99-day Tadabur (Quranic reflection) program.

**Package ID:** `com.yaumi.app`  
**Min SDK:** 26 (Android 8.0) | **Target/Compile SDK:** 36  
**Version:** 0.1.1 (versionCode 2)

---

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin 1.9.25 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM — AndroidViewModel, StateFlow, collectAsStateWithLifecycle |
| Navigation | Navigation Compose (single-activity) |
| Async | Kotlin Coroutines (viewModelScope / Dispatchers.IO) |
| Persistence | SharedPreferences (via typed Store classes) |
| Scheduling | AlarmManager.setExactAndAllowWhileIdle (NOT WorkManager, by design) |
| Location | Fused Location Provider (play-services-location) |
| Audio | MediaPlayer for Qur'an audio, AzanForegroundService for azan playback |
| Build | Gradle 8.x (Kotlin DSL), AGP 8.7.3 |
| JDK | 17 |

No Hilt/Dagger, no Room, no Retrofit. Dependencies are injected manually.

---

## Repository layout

```
yaumi/
├── app/
│   ├── build.gradle.kts          # app module config, signing
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/
│       │   │   ├── data/           # JSON data (doa, hadis, tadabur)
│       │   │   ├── audio/          # Bundled azan/sholawat MP3s + ZIP
│       │   │   ├── fonts/          # Amiri font family
│       │   │   └── quran_index.json / quran_index_full.json / quran_word_index.json
│       │   └── java/com/yaumi/app/
│       │       ├── MainActivity.kt
│       │       ├── navigation/     # AppNavigation, Routes
│       │       ├── ui/
│       │       │   ├── theme/      # Color, Type, Shape, Spacing, AppTheme
│       │       │   └── components/ # RouteHeader, OrnamentPattern, ScreenIntroCard
│       │       ├── core/location/  # DeviceLocationResolver
│       │       ├── home/           # HomeScreen, FeatureKey enum, home components
│       │       ├── azan/           # Prayer schedule, alarm, notification, audio, foreground service
│       │       ├── quran/          # Quran reader, audio, bookmarks, tafsir
│       │       ├── hadis/          # Hadis collections (JSON-backed)
│       │       ├── doa/            # Daily supplications (JSON-backed)
│       │       ├── qibla/          # Qibla math + compass UI
│       │       ├── tasbih/         # Digital counter
│       │       ├── hijriah/        # Hijri calendar display
│       │       ├── tadabur/        # 99-day Tadabur content, progress, notifications
│       │       └── settings/       # App-wide settings screen
│       ├── test/                   # JVM unit tests (parity / math tests)
│       └── androidTest/            # Instrumented tests (Espresso + Compose UI test)
├── build.gradle.kts                # Root project, plugin versions
├── settings.gradle.kts
├── gradle.properties               # JVM args, AndroidX flags
├── key.properties.example          # Template for release signing
└── scripts/
    └── generate_tadabur_99.py      # Python script to generate tadabur JSON
```

---

## Feature module structure

Every feature follows the same package layout:

```
<feature>/
├── data/           # Repository, API service, *Store (SharedPreferences), parsers
├── domain/
│   └── model/      # Pure data classes / UI models / UiState
├── ui/             # *Route (Composable entry point) + *ViewModel
├── notifications/  # NotificationHelper (if feature sends notifications)
├── alarm/ or worker/  # AlarmReceiver, AlarmScheduler, Worker (if feature schedules work)
└── service/        # ForegroundService (azan only)
```

**Pattern summary:**
- `*Repository` — fetches and caches data; runs on `Dispatchers.IO`
- `*Store` — typed wrapper around `SharedPreferences`; synchronous load/save
- `*ViewModel` — extends `AndroidViewModel`; exposes a single `uiState: StateFlow<*UiState>`
- `*Route` — `@Composable` function that collects uiState and delegates to sub-composables; receives `contentPadding: PaddingValues`
- `*UiState` — `data class` with sensible defaults; never nullable fields except error messages

---

## Navigation

`AppNavigation` in `navigation/AppNavigation.kt` hosts a single `NavHost`. All routes are string constants in the `Routes` object. Non-home routes are wrapped in `FeatureRoute(title, subtitle, onBack)` which renders `RouteHeader` + content area.

```kotlin
object Routes {
    const val HOME = "home"
    const val QURAN = "quran"
    const val HADIS = "hadis"
    const val DOA = "doa"
    const val AZAN = "azan"
    const val QIBLA = "qibla"
    const val TASBIH = "tasbih"
    const val HIJRIAH = "hijriah"
    const val SETTINGS = "settings"
    const val TADABUR = "tadabur"
}
```

Entry points are driven from `HomeScreen` via the `FeatureKey` enum.

---

## Theme system

**File:** `ui/theme/`

- `Color.kt` — all named colors; semantic names (`RoyalPurple`, `IndigoNight`, `GoldCrescent`, `Lavender50`, `Ink900`, etc.)
- `Type.kt` — `YaumiTypography`; Latin display uses `FontFamily.SansSerif`, Arabic text uses `AmiriFontFamily` (Amiri loaded lazily from `res/font/`)
- `Shape.kt` — `YaumiShapes`
- `Spacing.kt` — `Spacing` data class with xs/sm/md/lg/xl/xxl; accessed via `LocalSpacing`
- `AppTheme.kt` — `YaumiTheme(darkTheme: Boolean)` wraps Material 3 with light/dark color schemes

**Color tokens** (use these, not hardcoded Color(0xFF…)):
- Hero gradient background: `IndigoNight → RoyalPurple → SoftViolet`
- Page/card background: `Lavender50`, `SurfaceWhite`
- Primary accent: `RoyalPurple`
- Gold accent (countdown, star): `GoldCrescent`
- Text on dark surfaces: `OnNightHigh` (80%), `OnNightMid` (60%), `OnNightLow` (40%)

**Typography roles:**
- Arabic Qur'an/Hadis text → `AmiriFontFamily`, right-to-left layout
- All other text → Material 3 typography scale from `YaumiTypography`

---

## Shared UI components

`ui/components/`:
- `RouteHeader` — gradient header (IndigoNight→RoyalPurple→SoftViolet) with back button, title, optional subtitle and actions. Used by every non-home screen via `FeatureRoute`.
- `YaumiRouteScaffold` — convenience wrapper: `RouteHeader` + content area with `MaterialTheme.colorScheme.background`.
- `OrnamentPattern` — repeating geometric tile drawn on Canvas; used as a subtle overlay on hero surfaces.
- `ScreenIntroCard` — intro card shown at the top of content screens.

---

## Azan / notification system

The azan system is the most complex feature. Key design decisions:

- **AlarmManager over WorkManager** — `setExactAndAllowWhileIdle()` bypasses Doze mode. WorkManager can be delayed or skipped by aggressive OEM battery savers. This decision is documented in `AzanAlarmScheduler.kt`.
- **AzanAlarmReceiver** fires for each prayer event. For Fajr and Maghrib it triggers Sholawat Tahrim audio before the actual azan.
- **AzanForegroundService** — `foregroundServiceType="mediaPlayback"` — plays azan audio so it survives background restrictions.
- **AzanBootReceiver** reschedules alarms after reboot/package replace.
- **AzanRescheduleAlarmReceiver** handles midnight rollover (reschedules for the next day).
- **AzanWorker** (WorkManager) is used only as a fallback for background reschedule operations, not for the actual alarm firing.
- `AzanSettingsStore` persists all user preferences including per-prayer enable/offset toggles and manual location override.

Prayer time API: `https://equran.id/api/v2/shalat` (Indonesian location-based schedule)

---

## Data layer conventions

- **JSON assets** — Qur'an, Hadis, Doa, Tadabur content is bundled in `assets/`. Parsed once and cached in-memory.
- **SharedPreferences** — each feature has a dedicated `*Store` class with typed `load()`/`save()` methods. No global preferences singleton.
- **External APIs** — raw `HttpURLConnection` (no Retrofit). Calls are always wrapped in `runCatching { }.getOrElse { }` or `runCatching { }.onSuccess { }.onFailure { }`.
- **No Room database** — data fits in JSON assets + SharedPreferences for this app's scale.

---

## State management pattern

```kotlin
// ViewModel
private val _uiState = MutableStateFlow(MyUiState())
val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

// Update
_uiState.update { it.copy(isLoading = true) }

// Route (Composable)
val state by vm.uiState.collectAsStateWithLifecycle()
```

- All ViewModel logic is in `viewModelScope.launch { }`.
- IO work always uses `withContext(Dispatchers.IO)`.
- Never expose `MutableStateFlow` publicly.

---

## Build commands

```bash
# Debug APK
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Release APK (requires key.properties)
./gradlew :app:assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk

# Release AAB (Play Store)
./gradlew :app:bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab

# Unit tests
./gradlew :app:testDebugUnitTest

# Lint
./gradlew :app:lint
```

---

## Release signing

Signing is optional for debug but required for release. Create `key.properties` at the repo root (never commit it — it's in `.gitignore`):

```
storeFile=keystore/release-keystore.jks
storePassword=<password>
keyAlias=release
keyPassword=<password>
```

See `key.properties.example` for the template. The `app/build.gradle.kts` reads this file conditionally; if absent, release builds are unsigned.

---

## Tests

Unit tests live in `app/src/test/java/com/eling/nativeapp/` (**note:** the test package `com.eling.nativeapp` is the old project identity; production package is `com.yaumi.app` — this is a known mismatch from the project rename, test imports reference the production types):

- `SmokeUnitTest` — trivial smoke test
- `parity/QiblaParityMathTest` — validates Qibla bearing/distance math
- `parity/AzanParityRulesTest` — validates `AzanTimeUtils.applyOffsets()` and `AzanScheduleRules.selectSchedulableTimings()`
- `parity/AzanTahrimScheduleTest` — validates tahrim lead-time scheduling and event ordering
- `parity/PermissionFallbackLogicTest` — validates permission fallback logic

Framework: JUnit 4 only. No Mockito, no MockK, no Robolectric.

Run unit tests with: `./gradlew :app:testDebugUnitTest`

---

## Notable implementation details

- **Audio asset caching** — `AzanAudioPlayer.ensureCachedFile()` copies audio from APK assets to `context.cacheDir` on first use so `MediaPlayer` can read from a file path rather than an `AssetFileDescriptor`.
- **Qibla sensor stream** — `QiblaRepository.headingFlow()` is a `callbackFlow<CompassReading>` wrapping `SensorEventListener` on the rotation-vector sensor, with accelerometer+magnetic fallback. The ViewModel applies exponential smoothing.
- **Daily Ayat rotation** — `DailyAyatProvider` provides a curated 30-verse rotation keyed to the day-of-year.
- **Prayer name translation** — `Fajr→Subuh`, `Dhuhr→Dzuhur`, etc. is repeated across multiple helper functions (potential refactor into a shared mapping).
- **Fallback data** — every network-dependent feature defaults to Jakarta coordinates (lat -6.2088, lon 106.8456). `AzanUiData.isFallback: Boolean` signals when cached/fallback data is shown.
- **Git LFS** — `app/src/main/assets/audio/**/*.json` (the hadis annotation files) are tracked via Git LFS per `.gitattributes`.
- **Python data script** — `scripts/generate_tadabur_99.py` is a one-time offline utility to generate `tadabur_99.json`. It is not part of the Gradle build pipeline.

---

## Key conventions

1. **Feature isolation** — each feature is a self-contained package. Don't reach across feature boundaries except through `ui/components/` or `core/`. The home screen uses `AzanViewModel` directly because it shows prayer countdown.
2. **No dependency injection framework** — repositories and stores are instantiated directly in ViewModels using the `Application` context.
3. **Indonesian language** — user-facing strings are in Indonesian (`id`). Error messages, labels, and UI text throughout are in Indonesian.
4. **Arabic text** — always use `AmiriFontFamily` and `textDirection = TextDirection.Rtl` for Arabic Qur'an / Hadis / Doa text.
5. **Error handling** — use `runCatching { }.onSuccess { }.onFailure { }` at repository boundaries. Propagate errors as `errorMessage: String?` in UiState, never throw up to the Composable.
6. **No hardcoded colors in Composables** — always use `MaterialTheme.colorScheme.*` or the named color constants from `Color.kt`.
7. **Spacing** — use `LocalSpacing.current` (xs/sm/md/lg/xl/xxl) rather than arbitrary `dp` values where possible.
8. **Sensitive files** — `key.properties`, `*.jks`, `*.keystore`, `local.properties`, and `dist/` are gitignored. Never commit them.

---

## Permissions declared

`INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM`, `WAKE_LOCK`, `VIBRATE`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`

The app requests battery optimization exemption and exact alarm permission at startup (only if azan notifications are enabled).

---

## External API endpoints

| API | Base URL | Used by |
|---|---|---|
| Prayer schedule | `https://equran.id/api/v2/shalat` | AzanApiService |
| Tafsir | `https://equran.id/api/v2/tafsir/{surahId}` | QuranTafsirApi |
| Qur'an audio | `https://everyayah.com/data/{reciter}/{surah}{ayah}.mp3` | QuranViewModel |

All HTTP calls use raw `HttpURLConnection` with 8-second connect/read timeouts.

---

## Scripts

`scripts/generate_tadabur_99.py` — Python script for generating the `tadabur_99.json` asset content. Run manually when updating Tadabur content; output goes to `app/src/main/assets/data/tadabur/`.
