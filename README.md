# کارت‌نگار — KartNegar

[![CI Build & Offline Privacy Audit](https://github.com/HamidRezaAshkiyan/kartnegar/actions/workflows/ci.yml/badge.svg)](https://github.com/HamidRezaAshkiyan/kartnegar/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/HamidRezaAshkiyan/kartnegar)](https://github.com/HamidRezaAshkiyan/kartnegar/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-emerald.svg)](LICENSE)
[![100% On-Device](https://img.shields.io/badge/Privacy-100%25%20On--Device%20%7C%20Zero%20Permissions-059669.svg)](#privacy-guarantee)

**100% On-Device Iranian Bank Card, Sheba (IBAN), and National ID Screenshot Scanner for Android**  
**اسکنر ۱۰۰٪ آفلاین شماره کارت بانکی، شبا و کد ملی از روی اسکرین‌شات و عکس برای اندروید**

---

## Privacy Guarantee

KartNegar is engineered so that **your cards, Sheba numbers, and National IDs can never leave your device**:

- **Zero `INTERNET` Permission** — `android.permission.INTERNET` and `android.permission.ACCESS_NETWORK_STATE` are intentionally **not declared** in `AndroidManifest.xml`.
- **Zero Runtime Permissions** — Uses Android's native Photo Picker (`PickVisualMedia`), Share Sheet (`ACTION_SEND`), and system Camera delegation (`FileProvider`), so the app does not even ask for Storage or Camera permissions.
- **100% FOSS On-Device OCR** — Powered by bundled **Tesseract 4 LSTM** (`tesseract4android`) with both `eng.traineddata` and `fas.traineddata` packaged inside the APK. Zero Google Play Services, zero ML Kit telemetry, zero cloud APIs.
- **Zero Cloud Backup** — `android:allowBackup="false"` and explicit `data_extraction_rules.xml` exclusions keep your local SQLite vault strictly on your phone.
- **Automated CI Audit** — Every GitHub Actions build runs `aapt2 dump permissions` on the compiled APK and fails if any network permission is ever introduced.

Verify any release APK yourself:

```bash
aapt2 dump permissions kartnegar-v1.0.0.apk
```

---

## Features

- **Screenshot & Photo OCR (Persian + English)**:
  - Dual-pass on-device Tesseract 4 LSTM (`eng` + `fas+eng`) reads both Latin digits (`0-9`, `IR`) and Persian digits (`۰-۹`) from banking screenshots, Telegram/WhatsApp images, physical cards, and National ID cards.
  - **Automatic Dark-Mode Inversion**: Detects dark-mode screenshots (`luminance < 105`) and inverts them before OCR for dramatically higher accuracy.
  - **RTL-Safe Card Recovery**: Automatically recovers 4-group card numbers reversed by RTL OCR layouts when Luhn + Iranian bank BIN match.
- **Strict Mathematical Validation**:
  - **Iranian Bank Cards (16 digits)**: Luhn (Mod-10) checksum + automatic recognition of **30+ Iranian banks** (Melli, Mellat, Saderat, Tejarat, Sepah, Keshavarzi, Maskan, Saman, Blu Bank, Pasargad, Parsian, EN, Shahr, Ayandeh, Dey, Sina, Karafarin, Sarmayeh, Refah, Post Bank, Mehr Iran, Resalat, etc.) + optional expiry date extraction.
  - **Sheba / IR-IBAN (26 chars)**: Full ISO 13616 Mod-97 (`BigInteger`) checksum + 3-digit bank code identification + OCR confusion recovery (`1R`/`O`→`0`).
  - **Iranian National ID / Code Melli (10 digits)**: Official Mod-11 checksum with smart filtering against Sheba/card substrings and mobile numbers.
- **Android Share Sheet Integration**:
  - Share any screenshot directly from Gallery, Telegram, WhatsApp, or your screenshot notification straight to **KartNegar** to scan immediately.
  - Share or paste raw text messages to extract Card / Sheba / National ID instantly without OCR.
- **Local Vault & Instant Validator**:
  - Save frequently used cards, Shebas, and National IDs in a local Room SQLite vault with custom labels, search, type filters, and one-tap copy/share.
  - Built-in offline checksum validator in the **Tools** tab.

---

## Download

Download the latest signed APK and SHA-256 checksum from [GitHub Releases](https://github.com/HamidRezaAshkiyan/kartnegar/releases).

---

## Build from Source

```bash
git clone https://github.com/HamidRezaAshkiyan/kartnegar.git
cd kartnegar
gradle testDebugUnitTest
gradle assembleRelease
```

Requirements: JDK 17, Android SDK 35, Gradle 8.11.1.

---

## License

MIT License — see [LICENSE](LICENSE).
