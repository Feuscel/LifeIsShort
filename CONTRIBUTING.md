# Contributing to LifeIsShort

Thanks for considering a contribution. Issues and pull requests should be written in English so they can be reviewed consistently.

## Before you start

- Search existing issues and pull requests to avoid duplicating work.
- Every pull request must be based on a GitHub issue. Open one before starting work, or use an existing issue that covers the change. This also applies to small documentation changes.
- Link the issue from the pull request; use `Closes #123` when the pull request resolves it, or `Refs #123` when it does not.
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

Start every pull request title with one of these category tags:

- `[FEATURE]` for new user-facing functionality.
- `[BUGFIX]` for a bug fix.
- `[DOCS]` for documentation-only changes.
- `[CHORE]` for maintenance and other non-feature changes.

For example: `[DOCS] Require an issue and category tag for every PR`.

Include:

- A concise description of the problem and the change.
- A reference to the issue created before the pull request (`Closes #123` or `Refs #123`).
- The checks you ran and their results.
- Screenshots for visible UI changes, with private or identifying content removed.

Keep commits understandable and update documentation when behavior, permissions, setup, or known limitations change. By submitting a contribution, you agree that it may be distributed under the project's MIT License.
