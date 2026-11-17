# Ultra HD 4K & 8K Wallpapers

Kotlin + Jetpack Compose (Material 3) wallpaper app. minSdk 29, target/compile SDK 36.

**Features:** 10 categories, curated feed with infinite scroll, search by text + category, full-screen preview,
download original file (Pictures/UltraHD Wallpapers), set as Home / Lock / Both, favourites, recently viewed,
share, light/dark (follows system, Material You on Android 12+), splash screen, offline-friendly error states.

**Wallpaper source:** [Pexels API](https://www.pexels.com/api/) (free key, Pexels License, photographer credit
shown in-app). Only originals ≥ 3840 px on the long edge are listed; the 4K/8K badge is computed from the real
pixel size of each file, so 8K only appears when an 8K original exists.

## Setup
1. Get a free key at https://www.pexels.com/api/.
2. Copy `local.properties.example` → `local.properties` and set `PEXELS_API_KEY=...`
   (Android Studio adds `sdk.dir` automatically). Without a key the app shows a clear "key not configured" message.
3. Open the folder in Android Studio (JDK 17+), let Gradle sync.
   Wrapper JAR is not included: run `gradle wrapper --gradle-version 8.11.1` once (or let Android Studio sync).

> **Key exposure:** a key in an APK can be extracted. For a public release, put the key behind your own small proxy
> server, or accept Pexels' rate limits (200 req/hour, 20,000/month) as the worst case.

## Build
```bash
./gradlew :app:assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleRelease   # signed APK if signing is configured, else *-unsigned.apk
./gradlew :app:bundleRelease     # app/build/outputs/bundle/release/app-release.aab
```
Install debug build: `adb install app/build/outputs/apk/debug/app-debug.apk`

## Release signing (no secrets in code)
Create an upload key: `keytool -genkeypair -v -keystore upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000`
Then either copy `keystore.properties.example` → `keystore.properties` (git-ignored), or export env vars
`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Upload the AAB to Play Console and enrol in
Play App Signing (Google holds the app signing key; you keep the upload key).

## GitHub Actions (`.github/workflows/android.yml`)
Builds on every push to `main`; uploads `debug-apk`, `release-apk`, `release-aab` artifacts.
Repository secrets: `PEXELS_API_KEY`; for signed releases also `KEYSTORE_BASE64` (`base64 -w0 upload.jks`),
`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Without keystore secrets the release outputs are **unsigned**
and cannot be installed or uploaded to Play.

## Play Store checklist
- Verify **target API** against the current requirement at https://developer.android.com/google/play/requirements/target-sdk and bump `targetSdk`/`compileSdk` if needed.
- Host `PRIVACY_POLICY.md` (fill in the placeholders) and link it in Play Console.
- Data safety form: the app collects no data itself; disclose that images load from Pexels (IP address is visible to that third party).
- Content rating questionnaire (Pexels content is general-audience, but fill it in honestly); no ads included.
- Permissions: only `INTERNET` and `SET_WALLPAPER`.
- Provide your own store icon (512×512), feature graphic and screenshots.
- Pexels terms: keep photographer credit and don't present the images as your own.

## Known versions (verify/update)
AGP 8.9.2, Kotlin 2.1.20, Gradle 8.11.1, Compose BOM 2025.04.01, Coil 2.7.0. These were chosen without being able to
check for newer releases; update them in Android Studio if prompted.
