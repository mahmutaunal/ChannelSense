# ChannelSense 1.1.0 — Smarter Wi‑Fi Channel Recommendations

This release rebuilds ChannelSense around a more realistic, privacy-first recommendation engine and a simpler result-first interface.

## What’s new

- Signal-strength, channel-width, and frequency-overlap based interference scoring
- Separate candidate sets for 2.4 GHz, 5 GHz, and 6 GHz
- Quick scan and three-measurement detailed analysis
- Confidence level and alternative-channel suggestions
- Connected network and current-channel suitability
- Explicit handling for permissions, disabled Wi‑Fi, disabled Location Services, scan throttling, and cached Android results
- Redesigned Material 3 analyzer experience
- Updated terminology: access points rather than client/device count
- Unit tests for the recommendation engine
- Offline-only privacy model and store-ready documentation

## Important note

ChannelSense provides estimates from Wi‑Fi signals visible to the phone. It does not automatically modify the router. DFS behavior, regional channel availability, router/client support, walls, mesh topology, and ISP performance can affect the real result.
