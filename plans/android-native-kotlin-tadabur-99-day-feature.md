# Blueprint: Tadabur 99-Day Feature (Android Native Kotlin)

## Objective
Implement Tadabur 99-day feature end-to-end: JSON content, repository/storage, UI/navigation, daily notification, and verification.

## Repository Notes
- Git present but no commits yet; treat as direct-edit mode (no branching/PR automation).
- GitHub CLI not available; skip `gh` steps.

## Scope Summary
- Content source: `app/src/main/assets/data/tadabur/tadabur_99.json`.
- Data layer: `TadaburRepository`, `TadaburProgressStore`, `TadaburSettingsStore`.
- UI: `TadaburRoute`, `TadaburViewModel`, navigation in `AppNavigation` and Home tiles.
- Notifications: `TadaburAlarmScheduler`, `TadaburAlarmReceiver`, `TadaburNotificationHelper`.

## Plan Steps

### Step 1 — Content Schema + Repository Validation
**Context Brief**
- JSON is already present at `app/src/main/assets/data/tadabur/tadabur_99.json` and parsed by `TadaburRepository`.
- Ensure schema matches UI expectations (day index, surah, ayah range, arab, terjemah, hikmah, praktik, amal_tracker, hadis fields).

**Tasks**
- Inspect JSON structure and confirm every item has required fields and valid day indices (1..99).
- Update `TadaburRepository` parsing if any field names are mismatched or optional behavior is needed.
- Add guardrails for missing fields (empty strings/lists) to avoid UI crashes.

**Verification Commands**
- None (manual inspection only).

**Exit Criteria**
- JSON schema and repository parsing align with UI data expectations.

---

### Step 2 — Progress + Settings Storage Behavior
**Context Brief**
- `TadaburProgressStore` controls day progression and checklist persistence.
- `TadaburSettingsStore` controls notification toggle and offset minutes.

**Tasks**
- Validate day roll-over logic (daily increment, wrap to 1 after 99).
- Ensure checklist serialization works for variable list sizes.
- Confirm settings defaults align with product requirements (notif enabled, offset minutes after Fajr).
- Add missing helpers if needed (e.g., reset progress, mark last opened day explicitly).

**Verification Commands**
- None (logic review and optional unit tests later if needed).

**Exit Criteria**
- Progress and settings logic supports 99-day flow without drift or crash.

---

### Step 3 — UI Route and ViewModel Wiring
**Context Brief**
- `TadaburRoute` renders day content and checklists; `TadaburViewModel` loads current day.
- UI exists but must ensure it uses JSON-based content and progress state consistently.

**Tasks**
- Confirm `TadaburViewModel` loads current day from repository and progress store.
- Ensure checklist size matches `amalTracker` list and persists correctly.
- Add loading/empty/error states if required by UX (e.g., missing JSON item).

**Verification Commands**
- Optional: run app and open Tadabur screen.

**Exit Criteria**
- Tadabur screen shows correct day content, checklists, and completion state.

---

### Step 4 — Navigation + Home Entry Point
**Context Brief**
- App navigation is defined in `AppNavigation` with feature routes.
- Home cards live in `HomeScreen` and its components.

**Tasks**
- Add Tadabur route to `Routes` and `NavHost` in `AppNavigation`.
- Add Tadabur feature tile/hero in `HomeScreen` or relevant component (e.g., `ExploreGrid`, `TadaburHeroCard`).
- Ensure route title/subtitle matches product language.

**Verification Commands**
- Optional: run app and navigate from Home to Tadabur.

**Exit Criteria**
- Tadabur is accessible from Home and navigates to its route.

---

### Step 5 — Notification Scheduling + Receiver Flow
**Context Brief**
- `TadaburAlarmScheduler` schedules daily alarm relative to Fajr time.
- `TadaburAlarmReceiver` shows notification if not completed.
- `SettingsRoute` already includes Tadabur settings controls.

**Tasks**
- Ensure notification channel is created and used (`TadaburNotificationHelper`).
- Validate schedule time calculation and fallback if Azan data is missing.
- Confirm alarm reschedules on app launch and settings changes.
- Verify receiver respects completion state to avoid noisy reminders.

**Verification Commands**
- Optional: set system time forward to test notification fire.

**Exit Criteria**
- Daily notification appears only when enabled and day is not completed.

---

### Step 6 — Verification Pass + QA Checklist
**Context Brief**
- Final pass to ensure all feature surfaces work together.

**Tasks**
- Manual QA checklist:
  - Tadabur opens, shows day content, Arabic/translation present.
  - Checklist toggles persist between app restarts.
  - "Selesai Hari Ini" sets completion and prevents notification.
  - Day increments on date change, wraps to 1 after day 99.
- If needed, add lightweight unit test(s) around progress store.

**Verification Commands**
- Optional: `./gradlew :app:testDebugUnitTest` (only if tests added).

**Exit Criteria**
- All feature requirements verified, no regression in existing routes.

## Parallelism
- Steps 1–2 can run in parallel (content schema and storage logic).
- Steps 3–4 can run in parallel after Step 1 (UI + navigation).
- Step 5 depends on Steps 2–4 (settings and UI hooks).
- Step 6 depends on all steps.

## Rollback Strategy
- Each step is isolated to feature files under `tadabur/` plus navigation/home routes.
- Revert by removing the specific step’s file changes if a regression appears.
