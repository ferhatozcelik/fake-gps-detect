# Fake GPS Detect

A small Android library that performs a **best-effort detection of fake / mock GPS
usage**. It combines several heuristics and returns a rich result, so you can decide
how strict your app should be.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

## Features

- Detects the legacy *Allow mock locations* developer setting.
- Detects well-known fake GPS applications installed on the device.
- Detects whether the last known location is flagged as mock by the platform.
- Returns every signal individually so callers stay in control.
- Pure Kotlin, no runtime dependencies beyond AndroidX Core.

## Modules

| Module | Description |
| --- | --- |
| `:fakegpsdetect` | The reusable library. This is what you depend on. |
| `:app` | Sample application that runs the detector and shows the outcome. |

## Installation

```kotlin
dependencies {
    implementation("com.ferhatozcelik:fakegpsdetect:1.0.0")
}
```

Or, inside this repository:

```kotlin
dependencies {
    implementation(project(":fakegpsdetect"))
}
```

## Usage

```kotlin
val detector = FakeGpsDetect()

val result = detector.detect(context)

if (result.isFakeGpsDetected) {
    // mockLocationEnabled = result.mockLocationEnabled
    // fakeGpsApps         = result.fakeGpsApps
    // usingMockLocation   = result.usingMockLocation
}
```

Individual checks are also available:

```kotlin
detector.isMockLocationEnabled(context)   // legacy developer setting
detector.isFakeGpsAppInstalled(context)   // any known fake GPS app installed
detector.getInstalledFakeGpsApps(context) // the list of matching packages
detector.isUsingMockLocation(context)     // last known location flagged as mock
detector.hasLocationPermission(context)   // coarse/fine permission granted
```

`ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION` is required for the
last-known-location check; without it the other heuristics still work.

## Detection heuristics

| Signal | What it means | Reliability |
| --- | --- | --- |
| `mockLocationEnabled` | Legacy *Allow mock locations* setting is on | Low on Android 6+ |
| `fakeGpsApps` | A known fake GPS app package is installed | Medium |
| `usingMockLocation` | The platform flags the latest location as mock | High |

`isFakeGpsDetected` is `true` when **any** of the signals is positive. Detection is
inherently arm-races with rooted devices and custom ROMs, so treat the result as a
strong hint rather than a guarantee.

You can extend the package list by querying `PackageManager` yourself; the built-in
list lives in `FakeGpsDetect.KNOWN_FAKE_GPS_APPS`.

## Requirements

| Tool | Version |
| --- | --- |
| minSdk | 21 |
| compileSdk / targetSdk | 36 |
| Gradle | 8.14.5 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin | 2.2.21 |
| JDK | 17 |

## Building

```bash
./gradlew :fakegpsdetect:assembleRelease   # build the AAR
./gradlew :app:assembleDebug                # build the sample app
```

## License

Apache License 2.0 — see [LICENSE](LICENSE).

## Author

**Ferhat OZCELIK**

- GitHub: [@ferhatozcelik](https://github.com/ferhatozcelik)
- LinkedIn: [ferhatozcelik](https://www.linkedin.com/in/ferhatozcelik/)
