<div align="center">

<img src="assets/logo.png" width="160" alt="ChannelSense Logo"/>

# ChannelSense

**Privacy-first Wi-Fi Channel Analyzer for Android**

Find the best Wi-Fi channel using **real interference analysis** instead of simply counting nearby networks.

[![Android](https://img.shields.io/badge/Android-9%2B-brightgreen.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-blue.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-orange.svg)]()
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

</div>

---

## Overview

ChannelSense is a modern Android application that helps users choose the most suitable Wi-Fi channel for their router.

Unlike many Wi-Fi analyzer applications that only count nearby access points, ChannelSense evaluates multiple radio characteristics to estimate actual channel interference.

The recommendation engine considers:

- Signal strength (RSSI)
- Channel width
- Frequency overlap
- Band characteristics
- Current connected network
- Confidence score

Everything runs **entirely on-device**.

No cloud.

No analytics.

No tracking.

No advertisements.

---

## Why ChannelSense?

Most Wi-Fi analyzer applications rank channels by simply counting nearby networks.

In real environments this is rarely accurate.

Two networks on the same channel do not always create the same amount of interference.

ChannelSense estimates channel quality using a weighted interference model that includes:

- RSSI weighting
- Channel width
- Frequency overlap
- Adjacent channel influence
- Band-specific behavior

The result is a recommendation that better reflects real wireless conditions.

---

# Features

## Wi-Fi Analysis

- 2.4 GHz analysis
- 5 GHz analysis
- 6 GHz analysis
- Current connected channel detection
- Nearby Access Point discovery
- RSSI visualization
- Channel utilization graph
- Alternative channel recommendations
- Confidence indicator

---

## Analysis Modes

### Quick Analysis

Single scan for instant recommendations.

Ideal for everyday users.

### Detailed Analysis

Performs multiple measurements to reduce temporary fluctuations and provide more stable recommendations.

---

## Recommendation Engine

ChannelSense recommends channels using:

- RSSI weighted scoring
- Frequency overlap calculations
- Channel width impact
- Adjacent channel interference
- Confidence estimation

This produces significantly more useful recommendations than network-count based approaches.

---

## Router Assistance

ChannelSense also helps users apply the recommendation.

Features include:

- Router setup guides
- Common router brands
- Gateway detection
- Open router admin page directly
- Channel configuration instructions

---

## Privacy First

Privacy is a core design principle.

ChannelSense:

- does not require an account
- does not upload Wi-Fi information
- does not collect analytics
- does not use advertising SDKs
- does not communicate with any backend server

All analysis is performed locally on the device.

For more information see:

- **PRIVACY.md**

---

# Screenshots

| Home | Analysis | Recommendation |
|------|-----------|----------------|
| <img src="assets/1.png" width="220"/> | <img src="assets/2.png" width="220"/> | <img src="assets/3.png" width="220"/> |

---

# Architecture

The project follows a clean and modular architecture.

```
app
│
├── domain
│   └── Recommendation Engine
│
├── feature
│   ├── Analyzer
│   ├── Settings
│   └── Router Guide
│
├── wifi
│   ├── Scanner
│   ├── Connection
│   └── Permissions
│
├── ui
│
└── util
```

Core recommendation logic is independent from Android APIs, making it easy to test and maintain.

---

# Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel
- DataStore
- Android Wi-Fi APIs
- Android 9+
- Gradle Kotlin DSL

---

# Project Structure

```
domain/
    ChannelRecommendationEngine

feature/
    analyzer/
    settings/
    guide/

wifi/
    scanner/
    permissions/
    connection/

ui/
theme/
navigation/

util/
```

---

# Requirements

- Android 9 (API 28) or newer
- Wi-Fi enabled
- Location Services enabled (required by Android)
- Nearby Wi-Fi permission
- Location permission (depending on Android version)

---

# Building

Clone the repository.

```bash
git clone https://github.com/mahmutaunal/ChannelSense.git
```

Open with Android Studio.

Build:

```bash
./gradlew assembleDebug
```

Run tests:

```bash
./gradlew testDebugUnitTest
```

Generate Release:

```bash
./gradlew assembleRelease
```

---

# Important Limitations

Android applies several restrictions to Wi-Fi scanning.

Because of platform limitations:

- scan results may be cached
- scan frequency may be throttled
- hidden networks may not appear
- DFS channels behave differently
- mesh systems may affect results

Recommendations are estimates based on the access points visible to the device at scan time.

---

# Contributing

Contributions are welcome.

If you would like to:

- fix bugs
- improve the recommendation engine
- optimize performance
- improve translations
- enhance the UI

please open an Issue or submit a Pull Request.

---

# Roadmap

Future ideas include:

- Material You dynamic color
- Tablet optimization
- Foldable support
- Better visualization
- Export scan results
- Historical comparison
- Automatic periodic scans
- More router setup guides
- Improved DFS awareness

---

# License

Licensed under the Apache License 2.0.

See the **LICENSE** file for details.

---

# Author

**Mahmut Alperen Ünal**

GitHub

https://github.com/mahmutaunal

---

## Support

If you find this project useful:

- ⭐ Star the repository
- 🐞 Report bugs
- 💡 Suggest new features
- 🤝 Contribute to the project

---

Made with ❤️ using Kotlin and Jetpack Compose.