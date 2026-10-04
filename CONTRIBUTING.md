# Contributing to LifeIsShort

Thanks for considering a contribution. Issues and pull requests should be written in English so they can be reviewed consistently.

## Before you start

- Search existing issues and pull requests to avoid duplicating work.
- For a substantial change, open an issue first to discuss the approach.
- Keep changes focused, explain the user impact, and avoid including unrelated formatting or generated files.
- Do not include personal data, private messages, screenshots containing private content, signing keys, or local configuration files in an issue or pull request.

## Development setup

Use Android Studio or a local JDK 25 installation with Android SDK Platform 37. The repository includes the Gradle wrapper, so use `./gradlew` rather than installing Gradle separately.
On Windows, use `gradlew.bat` in place of `./gradlew`.

Useful checks:

```sh
./gradlew lintDebug
./gradlew assembleDebug assembleDebugAndroidTest
./gradlew connectedDebugAndroidTest
```

Instrumented tests require a connected Android device or emulator. Changes to accessibility behavior should be tested against the relevant Instagram flows when possible; include the Android and Instagram versions used in the pull request description, without sharing private screen contents.

## Pull requests

Include:

- A concise description of the problem and the change.
- Relevant issue links, if any.
- The checks you ran and their results.
- Screenshots for visible UI changes, with private or identifying content removed.

Keep commits understandable and update documentation when behavior, permissions, setup, or known limitations change. By submitting a contribution, you agree that it may be distributed under the project's MIT License.
