# AGENTS.md — KartNegar (کارت‌نگار)

## Overview
**KartNegar** (`v1.0.0`, `com.kartnegar.app`) is a 100% offline, zero-permission Android app that scans Iranian **bank card numbers (16 digits)**, **Sheba / IR-IBAN codes (26 chars)**, and **National IDs / Code Melli (10 digits)** from screenshots, camera photos, Android Share Sheet images, or pasted text. It runs bundled **Tesseract 4 LSTM** (`eng` + `fas`) on-device, validates all extracted values with strict mathematical checksums (Luhn Mod-10, ISO 13616 Mod-97, Code Melli Mod-11), and stores saved entries in an offline Room SQLite vault.

## Tech Stack & Requirements
- **Language / Toolchain:** Kotlin `2.0.21` (JVM 17), Android Gradle Plugin `8.7.3`, KSP `2.0.21-1.0.27`, **Gradle `8.11.1`**, **JDK 17**, **Android SDK 35** (`compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`).
- **UI:** Jetpack Compose (`compose-bom:2024.12.01`), Material 3, `material-icons-extended`, `lifecycle-viewmodel-compose`, `activity-compose`.
- **On-Device OCR:** `cz.adaptech.tesseract4android:tesseract4android:4.9.0` with bundled `eng.traineddata` and `fas.traineddata` in `assets/tessdata/`, plus `androidx.exifinterface` for EXIF auto-rotation.
- **Storage & Async:** AndroidX **Room** `2.6.1` (KSP) for local SQLite vault, `kotlinx-coroutines-android` `1.9.0`.
- **Testing:** JUnit 4 (`4.13.2`), `kotlinx-coroutines-test` `1.9.0`.

## Repository Structure
```text
kartnegar/
├── build.gradle.kts / settings.gradle.kts / gradle.properties
├── app/
│   ├── build.gradle.kts                        # Dependencies, signingConfigs, ProGuard/R8 release config
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml             # Zero INTERNET/runtime permissions, ACTION_SEND & FileProvider
│       │   ├── assets/tessdata/                # Bundled eng.traineddata & fas.traineddata
│       │   ├── res/xml/                        # data_extraction_rules.xml (no backup), file_paths.xml
│       │   └── java/com/kartnegar/app/
│       │       ├── KartNegarApp.kt             # Application class (DI/database/OCR wiring)
│       │       ├── MainActivity.kt             # Entry Activity, Share Sheet intent handling, Compose host
│       │       ├── ocr/
│       │       │   ├── ImagePreprocessor.kt    # EXIF rotation, scaling, dark-mode inversion (luminance < 105)
│       │       │   └── OnDeviceOcrEngine.kt    # Dual-pass Tesseract 4 LSTM (eng -> fas+eng -> high-contrast)
│       │       ├── domain/
│       │       │   ├── FinancialExtractor.kt   # Regex/candidate extraction + RTL 4-digit card group recovery
│       │       │   ├── FinancialValidators.kt  # Luhn Mod-10, Sheba Mod-97, Code Melli Mod-11
│       │       │   └── IranianBankRegistry.kt  # 30+ Iranian bank BINs & 3-digit Sheba bank codes
│       │       ├── data/                       # Room: KartNegarDatabase, ScannedItemEntity/Dao/Repository
│       │       └── ui/
│       │           ├── KartNegarRoot.kt        # Bottom navigation & root scaffold
│       │           ├── MainViewModel.kt        # UI state & OCR/vault orchestration
│       │           ├── screens/                # ScanScreen, VaultScreen, ToolsPrivacyScreen
│       │           ├── components/DetectedItemCard.kt
│       │           └── theme/Theme.kt
│       └── test/java/com/kartnegar/app/domain/ # Unit tests: FinancialExtractorTest, FinancialValidatorsTest, IranianBankRegistryTest
├── docs/index.html                             # GitHub Pages landing site & live browser validator
└── .github/workflows/                          # ci.yml (unit tests + APK + zero-INTERNET aapt2 audit) & release.yml
```

## Architecture & Key Flows
- **Entry Point:** `KartNegarApp.kt` initializes app dependencies; `MainActivity.kt` handles launcher starts as well as incoming `ACTION_SEND` image/text shares from Gallery, Telegram, or WhatsApp, and mounts `KartNegarRoot`.
- **OCR & Validation Pipeline:**
  1. `ImagePreprocessor`: fixes EXIF orientation, scales optimally, and inverts dark-mode screenshots (`luminance < 105`) to dark-on-light.
  2. `OnDeviceOcrEngine`: copies bundled `tessdata/` on first run and executes Tesseract 4 LSTM (`eng` → `fas+eng` → high-contrast fallback for embossed cards).
  3. `FinancialExtractor` + `FinancialValidators` + `IranianBankRegistry`: normalizes Persian/Arabic numerals (`۰-۹`), recovers RTL-reversed 4-digit card blocks via Luhn + BIN lookup, fixes Sheba prefix OCR noise (`1R`/`O` → `0`), validates Mod-97/Mod-10/Mod-11, and filters National ID false positives.
- **Persistence:** `VaultScreen` saves labeled results into Room (`KartNegarDatabase`) with `android:allowBackup="false"`.

## Development & Commands
```bash
# Run JVM unit tests (validators, extractor, bank registry)
gradle testDebugUnitTest --stacktrace

# Build debug APK
gradle assembleDebug --stacktrace

# Build minified release APK (uses debug signing unless KEYSTORE_* env vars are set)
gradle assembleRelease

# Verify zero network permissions on compiled APK (same check as CI)
aapt2 dump permissions app/build/outputs/apk/debug/app-debug.apk
```

## Configuration & Environment
- **Release Signing (optional env vars read by `app/build.gradle.kts`):**
  - `KEYSTORE_FILE` (path to `.jks`/`.keystore`)
  - `KEYSTORE_PASSWORD`
  - `KEY_ALIAS`
  - `KEY_PASSWORD`
  - If `KEYSTORE_FILE` is unset or missing, `release` falls back to `debug` signing.
- **No network/API keys:** App is strictly offline.

## Conventions & Gotchas
- **Strict Zero-Permission Privacy Invariant:** NEVER add dependencies or manifest entries that merge `android.permission.INTERNET` or `ACCESS_NETWORK_STATE` (no Play Services, ML Kit, Firebase, or analytics). `.github/workflows/ci.yml` runs `aapt2 dump permissions` and fails CI if either permission appears.
- **No Gradle Wrapper committed:** There is no `./gradlew` script in the repo root; invoke system `gradle` (v8.11.1) directly.
- **Zero-Backup Storage:** Keep `android:allowBackup="false"` and `res/xml/data_extraction_rules.xml` intact so financial vault data never syncs to cloud backups.
- **Pure-Kotlin Domain Tests:** Keep `domain/` classes free of Android framework dependencies so `gradle testDebugUnitTest` runs fast on the JVM.
