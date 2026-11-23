# Changelog

## v1.0.0

### Highlights
- **100% On-Device OCR Engine**: Bundled FOSS Tesseract 4 LSTM (`eng` + `fas`) with zero `INTERNET`, `ACCESS_NETWORK_STATE`, Storage, or Camera permissions.
- **Smart Image Preprocessing**: EXIF orientation correction, optimal scaling, automatic dark-mode screenshot inversion, and high-contrast fallback pass.
- **Iranian Financial Validators & Extractor**:
  - 16-digit Bank Card detection with Luhn (Mod-10) validation, 30+ Iranian bank BIN registry (including Blu Bank), RTL 4-group recovery, and expiry date detection.
  - 26-character Sheba (`IR` + 24 digits) detection with ISO 13616 Mod-97 validation and 3-digit bank code lookup.
  - 10-digit Iranian National ID (`کد ملی`) detection with Mod-11 validation and mobile-number/substring filtering.
- **Share Sheet & Text Extraction**: Share screenshots or text directly from Gallery, Telegram, or WhatsApp into KartNegar.
- **Offline Local Vault & Tools**: Room SQLite vault with search, filters, custom labels, one-tap copy/share, and an instant manual checksum validator.
- **Automated CI Privacy Audit**: `aapt2 dump permissions` verifies zero network permissions on every build.
