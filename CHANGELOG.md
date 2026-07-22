# Changelog

## 1.1.1

- Fixed missing 5 GHz DFS channels (52–64 and 100–144) in channel charts and statistics.
- Added the complete 20 MHz channel plan for 2.4 GHz, 5 GHz, and 6 GHz analysis.
- Ensured every valid channel reported by the Android Wi-Fi scanner is retained in analysis.
- Kept recommendation candidates separate from display channels to preserve sensible 2.4 GHz guidance.
- Added regression tests for DFS, 6 GHz, observed-channel retention, and empty-band channel plans.

## 1.1.0

- Replaced network-count recommendation logic with RSSI, channel-width, and frequency-overlap analysis.
- Added confidence levels, alternative channels, and current-channel suitability.
- Added quick and three-measurement detailed analysis modes.
- Added explicit permission, Wi-Fi disabled, Location Services disabled, throttling, and cached-result states.
- Added connected-network context.
- Redesigned the analyzer UI for a result-first, accessible Material 3 experience.
- Added deterministic recommendation-engine unit tests.
- Corrected user terminology from “device count” to “detected access points.”
- Added store-ready privacy and project documentation.
