# ChannelSense

ChannelSense is a privacy-first Android Wi-Fi channel advisor. It analyzes nearby access points entirely on-device and recommends a lower-interference channel using signal strength, channel width, and real frequency overlap rather than simply counting networks on the same channel.

## Highlights

- 2.4 GHz, 5 GHz, and 6 GHz analysis
- Quick scan and multi-measurement detailed analysis
- Connected network and current-channel context
- Confidence-aware recommendations and alternatives
- Channel-width and RSSI-weighted interference model
- Router setup guides for common brands
- Direct opening of the detected router gateway
- No account, analytics, advertising, or cloud backend
- English and Turkish UI

## Important limitations

Android can throttle Wi-Fi scans and may return cached results. Recommendations are estimates based on access points visible to the phone. Router capabilities, DFS behavior, walls, client hardware, mesh backhaul, and the internet connection itself can also affect performance.

ChannelSense never changes router settings automatically and never stores router credentials.

## Requirements

- Android 9 or newer
- Wi-Fi enabled
- Location Services enabled where Android requires them for Wi-Fi scans
- Nearby Wi-Fi / precise location permission depending on Android version

## Architecture

The recommendation engine is Android-independent and unit-testable. Android-specific scanning and connected-network discovery are isolated under `wifi/`; feature UI and state live under `feature/`.

## Build

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

## Privacy

See [PRIVACY.md](PRIVACY.md).

## License

Apache License 2.0.
