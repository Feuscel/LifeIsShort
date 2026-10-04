# LifeIsShort

LifeIsShort is an Android app that helps you avoid Instagram Reels by blocking the Reels tab and Reels opened from selected parts of Instagram. Choose whether to block Reels from the Home feed, the Reels tab, and Direct Messages.

LifeIsShort is an independent, unofficial project and is not affiliated with, endorsed by, or sponsored by Instagram or Meta.

## How it works

LifeIsShort uses an Android Accessibility Service to inspect visible Instagram UI structure and identify the selected tab, the Reels viewer, and the screen from which a Reel was opened. When a selected source is blocked, the app returns you to the prior screen or to Instagram Home.

The service is opt-in: Android requires you to enable it in system Accessibility settings before blocking can work. You can pause blocking at any time in LifeIsShort or disable the service in Android settings.

## Requirements

- Android 7.0 (API 24) or newer.
- Instagram installed, for the blocking features to apply.
- To build from source: JDK 25 and Android SDK Platform 37. Gradle is provided by the included wrapper.

## Build and install

1. Clone or download this source code.
2. Open the project in Android Studio, or build from a terminal:

   ```sh
   ./gradlew assembleDebug
   ```

   On Windows, use `gradlew.bat` in place of `./gradlew`.

3. Install the debug APK on a connected device:

   ```sh
   ./gradlew installDebug
   ```

4. Open LifeIsShort and tap **Open Accessibility settings**.
5. Find LifeIsShort in Android's installed accessibility services and enable it. Android will show its standard notice explaining that accessibility services can observe screen content; review and confirm this system prompt to use the blocker.
6. Return to LifeIsShort, enable Instagram blocking, and select the sources you want to block.

Run the build and static checks with:

```sh
./gradlew lintDebug assembleDebug assembleDebugAndroidTest
```

Run instrumented tests on a connected Android device or running emulator with `./gradlew connectedDebugAndroidTest`. On Windows, use `gradlew.bat` in place of `./gradlew`.

## Privacy

- Blocking preferences are stored locally in the app's private preferences.
- The service examines visible UI structure and resource identifiers to detect Instagram screens. It does not read or log message text.
- Debug builds can write structural UI identifiers to Android's local log for troubleshooting. These diagnostics are not enabled in non-debuggable release builds.
- The app does not declare the Android Internet permission and has no network or analytics code.
- Android's Accessibility Service permission is required for the core blocking feature. The service is configured to retrieve the active window's UI hierarchy so it can detect navigation and Reel screens.

## Known limitations

Instagram does not provide a public API for identifying these screens. Detection relies on Instagram's internal view identifiers and screen structure, which may change without notice. A future Instagram update may make some blocking paths stop working until LifeIsShort is updated. Behavior can also vary between Instagram versions and device configurations.

LifeIsShort does not block Reels in other apps, remove Reel content, or control Instagram's servers. The app's source-selection controls describe where a Reel was opened, not the Reel's content.

## Contributing

Bug reports, feature requests, and pull requests are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a change, follow the [Code of Conduct](CODE_OF_CONDUCT.md), and review [SECURITY.md](SECURITY.md) before reporting a security issue.

## License

LifeIsShort is licensed under the [MIT License](LICENSE).
