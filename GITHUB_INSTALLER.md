# Build The FolderSmith Mobile Installer On GitHub

This repository includes a GitHub Actions workflow at:

```text
.github/workflows/android-debug-apk.yml
```

## How To Get The APK

1. Push this project to GitHub.
2. Open the repository on GitHub.
3. Go to **Actions**.
4. Run **Build Android Debug APK**, or push to `main`.
5. Open the completed workflow run.
6. Download the artifact named:

```text
FolderSmith-Mobile-debug-apk
```

The artifact contains:

```text
app-debug.apk
```

That APK is the Android debug installer for FolderSmith Mobile.

## Notes

The debug APK is signed with the standard Android debug key. It is suitable for local testing and sideloading, not Play Store distribution.
