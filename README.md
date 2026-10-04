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

## Download a release APK

Download the latest signed APK and its SHA-256 checksum from the project's [GitHub Releases](https://github.com/Feuscel/LifeIsShort/releases) page. Verify the APK before installing it:

```sh
sha256sum -c LifeIsShort-1.0.0.apk.sha256
```

Install the APK on Android. If prompted, allow installation from the app used to open the downloaded APK, then follow the setup steps above to enable the Accessibility Service.

Release APKs are signed with the project's release key. Before publishing the first release, maintainers must create and securely back up a long-lived release keystore, then add these repository Actions secrets under **Settings → Secrets and variables → Actions**:

- `RELEASE_KEYSTORE_BASE64`: the keystore file encoded as base64.
- `RELEASE_STORE_PASSWORD`: the keystore password.
- `RELEASE_KEY_ALIAS`: the key alias.
- `RELEASE_KEY_PASSWORD`: the key password.

For example, create a JKS keystore with `keytool -genkeypair -keystore lifeisshort-release.jks -storetype JKS -keyalg RSA -keysize 2048 -validity 10000 -alias lifeisshort`. Encode the file with `base64 lifeisshort-release.jks > lifeisshort-release.jks.base64`, then securely transfer that file's contents and the passwords into the Actions secrets. Keep secure offline backups of the keystore and credentials; never commit them or put them in an issue, pull request, or build log. Losing the key prevents future APK updates from being installable over existing installs.

Tags must use the `vMAJOR.MINOR.PATCH` format (for example, `v1.0.0`). The release workflow uses its GitHub Actions run number for Android's monotonically increasing version code.

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
