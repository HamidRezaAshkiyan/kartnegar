<div align="center">

# کارت‌نگار — KartNegar

**100% On-Device Iranian Bank Card, Sheba (IBAN), and National ID Screenshot Scanner for Android**  
**اسکنر ۱۰۰٪ آفلاین شماره کارت بانکی، شبا و کد ملی از روی اسکرین‌شات و عکس برای اندروید**

[![Website](https://img.shields.io/badge/Website-Live%20Demo%20%26%20Download-10b981?style=for-the-badge)](https://hamidrezaashkiyan.github.io/kartnegar/)
[![Release](https://img.shields.io/github/v/release/HamidRezaAshkiyan/kartnegar?style=for-the-badge&color=0284c7)](https://github.com/HamidRezaAshkiyan/kartnegar/releases/latest)
[![CI Audit](https://img.shields.io/github/actions/workflow/status/HamidRezaAshkiyan/kartnegar/ci.yml?style=for-the-badge&label=CI%20%26%20Privacy%20Audit)](https://github.com/HamidRezaAshkiyan/kartnegar/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-334155?style=for-the-badge)](LICENSE)

[**Live Website & Browser Validator**](https://hamidrezaashkiyan.github.io/kartnegar/) · [**Download APK (v1.0.0)**](https://github.com/HamidRezaAshkiyan/kartnegar/releases/latest) · [**Privacy Audit**](#privacy-guarantee) · [**Build from Source**](#build-from-source)

</div>

---

## Why KartNegar?

Every day, people receive bank card numbers, Sheba (IR-IBAN) codes, and National IDs as screenshots in Telegram, WhatsApp, or Persian banking apps. Typing 16 or 26 digits by hand is error-prone, and uploading financial screenshots to cloud OCR services is a serious privacy risk.

**KartNegar** solves this completely offline on your phone — with **zero permissions**, **zero cloud APIs**, and **strict mathematical checksum validation**.

---

## Privacy Guarantee

KartNegar is engineered so that **your financial data can never leave your device**:

| Guarantee | Implementation |
| --- | --- |
| **Zero `INTERNET` Permission** | Neither `android.permission.INTERNET` nor `ACCESS_NETWORK_STATE` is declared in `AndroidManifest.xml`. |
| **Zero Runtime Permissions** | Uses Android's native Photo Picker (`PickVisualMedia`), Share Sheet (`ACTION_SEND`), and system Camera delegation (`FileProvider`). No Storage or Camera permission prompts. |
| **100% FOSS On-Device OCR** | Bundled **Tesseract 4 LSTM** (`tesseract4android`) with `eng.traineddata` + `fas.traineddata` packaged inside the APK. Zero Google Play Services, ML Kit, Firebase, or telemetry. |
| **Zero Cloud Backup** | `android:allowBackup="false"` and explicit `data_extraction_rules.xml` exclusions keep your local SQLite vault strictly on-device. |
| **Automated CI Audit** | Every GitHub Actions build runs `aapt2 dump permissions` on the compiled APK and fails if any network permission is ever introduced. |

Verify any release APK yourself:

```bash
aapt2 dump permissions kartnegar-v1.0.0.apk
# Expected output: ZERO uses-permission entries
```

Verify the download integrity:

```bash
sha256sum -c kartnegar-v1.0.0.apk.sha256
```

---

## Features

- **Dual-Pass On-Device OCR (Persian + English)**
  - Runs bundled Tesseract 4 LSTM (`eng` + `fas+eng`) to read both Latin digits (`0-9`, `IR`) and Persian digits (`۰-۹`) from banking screenshots, Telegram/WhatsApp images, physical cards, and National ID cards.
  - **Automatic Dark-Mode Inversion**: Detects dark-mode screenshots (`luminance < 105`) and inverts them to dark-text-on-white before OCR.
  - **EXIF Orientation & High-Contrast Fallback**: Auto-rotates camera photos and runs a high-contrast pass when needed for embossed physical cards.
  - **RTL-Safe Card Recovery**: Automatically disambiguates 4-digit card groups reversed by RTL OCR layouts using Luhn Mod-10 + Iranian bank BIN matching.
- **Strict Mathematical Checksum Validation**
  - **Bank Cards (16 digits)**: Luhn (Mod-10) validation + **30+ Iranian bank BINs** (Melli, Mellat, Saderat, Tejarat, Sepah, Keshavarzi, Maskan, Saman, Blu Bank, Pasargad, Parsian, EN, Shahr, Ayandeh, Dey, Sina, Karafarin, Sarmayeh, Refah, Post Bank, Mehr Iran, Resalat, Tourism, Iran Zamin, Middle East, Melal, Noor) + optional expiry date extraction.
  - **Sheba / IR-IBAN (26 chars)**: Full ISO 13616 Mod-97 (`BigInteger`) checksum + 3-digit bank code identification + OCR prefix confusion recovery (`1R` / `O` → `0`).
  - **Iranian National ID / Code Melli (10 digits)**: Official Mod-11 checksum with smart filtering against Sheba/card substrings and mobile numbers.
- **Android Share Sheet & Text Extraction**
  - Share any screenshot directly from Gallery, Telegram, WhatsApp, or your screenshot notification straight to **KartNegar** to scan immediately.
  - Share or paste raw text messages to extract Card / Sheba / National ID instantaneously without OCR.
- **Local Vault & Manual Validator**
  - Save frequently used items in an offline Room SQLite vault with custom labels, search, type filters (`All`, `Cards`, `Sheba`, `National ID`), and one-tap copy/share.
  - Built-in offline checksum validator in the **Tools** tab (also available live in the browser on the [project website](https://hamidrezaashkiyan.github.io/kartnegar/#demo)).

---

## Architecture

```
Screenshot / Photo / Share Sheet
        │
        ▼
ImagePreprocessor (EXIF rotation · optimal scaling · dark-mode luminance inversion)
        │
        ▼
OnDeviceOcrEngine (Bundled Tesseract 4 LSTM: eng → fas+eng → high-contrast fallback)
        │
        ▼
FinancialExtractor & FinancialValidators
  ├── Sheba (ISO 13616 Mod-97 + 3-digit bank code)
  ├── Card  (Luhn Mod-10 + 30+ Iranian BINs + RTL 4-group recovery)
  └── National ID (Code Melli Mod-11 + mobile/substring filtering)
        │
        ▼
Jetpack Compose Material 3 UI ──► Local Room SQLite Vault (allowBackup=false)
```

---

## Download

1. Visit the [**GitHub Pages site**](https://hamidrezaashkiyan.github.io/kartnegar/) or [**GitHub Releases**](https://github.com/HamidRezaAshkiyan/kartnegar/releases/latest).
2. Download `kartnegar-v1.0.0.apk` (and optionally `kartnegar-v1.0.0.apk.sha256` to verify integrity).
3. Install on Android 8.0+ (API 26+).

---

## Build from Source

```bash
git clone https://github.com/HamidRezaAshkiyan/kartnegar.git
cd kartnegar
gradle testDebugUnitTest
gradle assembleRelease
```

Requirements: **JDK 17**, **Android SDK 35**, **Gradle 8.11.1**.

---

## License

MIT License — see [LICENSE](LICENSE).
