# Store release checklist

- Run `./gradlew testDebugUnitTest` and `./gradlew assembleRelease` in an online Android Studio environment.
- Test Android 9, 12, 13, 14, 15, and 16 permission flows.
- Test fresh and throttled scans on Pixel, Samsung, Xiaomi/HyperOS, and at least one Wi‑Fi 6E device.
- Confirm 6 GHz visibility only appears on supported hardware.
- Verify Turkish and English layouts with large font scale and dark theme.
- Produce a signed AAB and retain the upload key securely.
- Host `PRIVACY.md` at a public URL for the Play listing.
- Prepare phone screenshots, feature graphic, short description, and Data Safety answers.
- Do not claim guaranteed speed improvement or universal “best channel.”
