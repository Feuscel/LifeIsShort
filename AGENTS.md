# Repository instructions for coding agents

## Project map and checks

- This is a single-module Android app (`:app`), package `dev.feuscel.lifeisshort`. `MainActivity` owns the settings UI; `ScrollBlockerService` detects Instagram UI and applies blocking; `ScrollBlockerSettings` and `ScrollBlockingPolicy` hold preferences/migration and pure blocking rules.
- Use the Gradle wrapper (`./gradlew`); builds require JDK 25 and Android SDK Platform 37 (minimum Android API 24). The wrapper pins Gradle 9.8.0.
- CI-equivalent local verification: `./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest`. For one JVM test, use `./gradlew :app:testDebugUnitTest --tests 'dev.feuscel.lifeisshort.ScrollBlockingPolicyTest'`.
- `connectedDebugAndroidTest` requires a connected device or emulator. For AccessibilityService changes, test relevant Android/Instagram flows on-device when available and include Android and Instagram versions in the PR.

## Issue and pull request workflow

- Before editing project files, search GitHub issues for the work; use an existing issue or create one first. Keep the change focused and include the issue number in the branch name where practical.
- PR titles must start with exactly one of `[FEATURE]`, `[BUGFIX]`, `[DOCS]`, or `[CHORE]`. The PR body must reference the earlier issue as `Closes #123` or `Refs #123`; run relevant checks and review the diff before opening it.
- For GitHub issue/PR bodies and comments, use actual line breaks, never visible literal `\\n` escapes. Prefer a temporary body file or safely quoted multiline shell string; verify the posted body with `gh issue view` or `gh pr view` and correct formatting if needed.
- Follow `CONTRIBUTING.md`, `.github/PULL_REQUEST_TEMPLATE.md`, and the workflows in `.github/workflows/` for details.

## Accessibility and release constraints

- The service identifies Instagram screens from visible UI structure/resource IDs; do not inspect or log message text. Do not claim its use prevents Android or Play Protect warnings about restricted settings.
- Publish releases only through `.github/workflows/release.yml` using a new `vMAJOR.MINOR.PATCH` tag. Do not manually create a release or bypass signing, signature, or checksum verification. Confirm the workflow succeeded and both release assets are present before reporting publication; report missing secrets or permissions as blockers.
