---
name: android-gradle
description: Android, Kotlin, Gradle, JDK 25, Android SDK Platform 37, ./gradlew, emulator, adb, and AccessibilityService development for this repository; use when changing, building, or testing the Android app.
---

# Android and Gradle development

- This repository is one Android app module, `:app`, package `dev.feuscel.lifeisshort`. `MainActivity` owns the settings UI; `ScrollBlockerService` detects Instagram UI; `ScrollBlockerSettings` handles persisted preferences and migration; `ScrollBlockingPolicy` contains pure blocking rules.
- Use `./gradlew`, JDK 25, and Android SDK Platform 37 (minimum Android API 24). The wrapper pins Gradle 9.8.0.
- Run one JVM test with `./gradlew :app:testDebugUnitTest --tests 'dev.feuscel.lifeisshort.ScrollBlockingPolicyTest'`.
- CI-equivalent verification is `./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest`.
- Run `./gradlew connectedDebugAndroidTest` only with a connected device or emulator; `adb devices -l` checks whether one is available.
- For `ScrollBlockerService` changes, test the relevant Instagram navigation flows on-device when possible and note Android and Instagram versions. Detection relies on visible UI structure and resource IDs; do not inspect or log message text.
