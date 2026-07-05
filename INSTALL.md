# FolderSmith Mobile Install Notes

FolderSmith Mobile is a native Android app built with Kotlin, Jetpack Compose, Material 3, and Room.

## Build Requirements

- Android Studio
- JDK 17
- Android SDK with API 35 installed

## Build Commands

From the project root:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

The debug APK will be generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install On Android

After building the APK:

1. Copy `app-debug.apk` to the phone, or use Android Studio.
2. Allow installation from the selected source if Android prompts for it.
3. Open FolderSmith Mobile.
4. Start with a safe scan and review the cleanup plan before applying any action.

## Current Local Build Limitation

This package was prepared in an environment where `java.exe` and the Android SDK were not available on PATH, so an APK could not be generated here. The project is structured for Android Studio and includes the Gradle wrapper files.

## Storage Safety Notes

FolderSmith Mobile avoids broad manage-all-files permission in this MVP. It uses Android media permissions, MediaStore, and the Storage Access Framework folder picker. Some folders and undo actions may be limited by Android URI permissions.
