# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests (JVM, no device needed)
./gradlew test

# Run a single unit test class
./gradlew :app:test --tests "com.ricdev.mahjongscorecounter.logic.ScoreEngineTest"

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint
```

## Project State

This is a **stub project**. `MainActivity.kt` currently contains only a "Hello Android" placeholder. The full implementation described in the implementation plan has not been built yet.

## Target Architecture

The app is a **single-activity Jetpack Compose app**. Do not add additional Activities.

### Planned package structure

```
com.ricdev.mahjongscorecounter/
  model/       — domain data classes and enums (Seat, WinType, ScoreRules, RoundInput, RoundResult, GameState)
  logic/       — pure Kotlin scoring engine, no Android/Compose imports
  ui/          — Compose screens and components
  ui/theme/    — already exists (Color.kt, Type.kt, Theme.kt)
  viewmodel/   — ViewModel holding GameState and form state
```

### Key domain models to implement

- `Seat` — enum: EAST, SOUTH, WEST, NORTH
- `WinType` — enum: SELF_DRAW, DISCARD_WIN
- `ScoreRules` — configurable 1-fan bases (`discardWinBase`, `selfDrawBase`); defaults: both `8`
- `RoundInput` — winner, winType, fanCount, optional discarder
- `RoundResult` — `Map<Seat, Int>` deltas (must sum to zero) + summary string
- `GameState` — player totals, current rules, form state, round history list (for undo)

### Scoring engine rules

- `amountForFan(base, fan)` = `base * 2^(fan-1)`
- **Self Draw**: each of the 3 non-winners pays `perLoser = amountForFan(selfDrawBase, fan)`; winner gains `perLoser * 3`
- **Discard Win**: only discarder pays `amountForFan(discardWinBase, fan)`; winner gains same; others pay 0
- All four deltas must sum to zero — enforce in engine and test

### Validation rules

- Fan count must be a positive integer
- Winner is always required
- Discarder required for DISCARD_WIN, forbidden for SELF_DRAW
- Winner ≠ discarder
- Base amounts must be non-negative integers

## Platform Constraints

- `minSdk = 26` (Android 8.0) — do not use APIs that raise this
- `compileSdk = 36`, `targetSdk = 36`
- Kotlin `2.2.10`, Java 11 source/target compatibility
- Compose BOM `2026.02.01`, Material 3

## Localization

All user-facing strings must go in string resources — never hardcode text in Kotlin or Compose.

- English: `app/src/main/res/values/strings.xml`
- Traditional Chinese (HK): `app/src/main/res/values-zh-rHK/strings.xml`

Use `自摸` for Self Draw and `食出` for Discard Win in zh-HK.

## Persistence

Use **Jetpack DataStore** (Preferences) for scoring rules and current game state. Add `androidx.datastore:datastore-preferences` as a dependency when implementing persistence.

## Dependencies to Add

The current `app/build.gradle.kts` has only core Compose deps. Add when needed:

- `androidx.lifecycle:lifecycle-viewmodel-compose` — ViewModel in Compose
- `androidx.datastore:datastore-preferences` — rule/state persistence
- `org.jetbrains.kotlin:kotlin-test` or JUnit4 already present via `libs.junit`

Add new library entries to `gradle/libs.versions.toml` rather than inline version strings.
