# Mahjong Score Tracker

Android app for tracking a simplified four-player Mahjong score ledger.

## Current Scoring Model

This app intentionally uses a simple zero-sum point transfer model:

- Self-draw: each of the three losing players pays the entered amount, and the winner receives three times that amount.
- Discard win: one selected payer loses the entered amount, and the winner receives that amount.
- Dealer bonuses, repeat counters, riichi/honba sticks, limit hands, and regional rule variants are not included yet.

Round amounts must be between 1 and 999,999 points.

## Features

- Table-style score view for East, South, West, and North.
- Record, undo, and reset game actions.
- Full round history, newest first and grouped by date.
- Light, dark, and system theme settings.
- App language selection for the supported locales declared in `locales_config.xml`.
- Saved game history and preferences are included in Android backup/device transfer.

## Build And Verify

Install a JDK before running Gradle. The project uses the Gradle wrapper.

```sh
./gradlew test
./gradlew lint
./gradlew bundleRelease   # signed AAB for Play Store (requires keystore.properties)
```

Instrumented UI tests require an Android device or emulator:

```sh
./gradlew connectedAndroidTest
```

### Release signing

Create `keystore.properties` at the project root (already gitignored):

```
storeFile=/absolute/path/to/release-key.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Generate a keystore once with:

```sh
keytool -genkeypair -v -keystore ~/release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias mahjongscorecounter
```
