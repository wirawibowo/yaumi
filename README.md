# Yaumi Android (Kotlin) 🌙

[![Platform](https://img.shields.io/badge/Platform-Android-3ddc84?logo=android&logoColor=white)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Kotlin-7f52ff?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/Jetpack%20Compose-4285f4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

Android native app untuk Yaumi dengan Jetpack Compose. Fokus pada parity fitur inti dan peningkatan native-first.

## ✨ Fitur utama

- Quran (read-only)
- Hadis (read-only)
- Doa (read-only)
- Jadwal azan + notifikasi terjadwal
- Qibla (bearing, jarak Ka'bah, heading kompas)

## 🧰 Teknologi

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose

## 🗂️ Struktur folder

- `app/` kode aplikasi
- `scripts/` utilitas data
- `dist/` output build (AAB/APK)

## ✅ Persyaratan

- Android Studio
- JDK 17
- Android SDK

## 🚀 Menjalankan (dev)

1. Clone repo dari GitHub lalu buka di Android Studio.
2. Tunggu Gradle sync selesai.
3. Jalankan app pada emulator/perangkat Android.

## 🔧 Setup GitHub (CLI)

```bash
git init
git add .
git commit -m "init yaumi android"
git branch -M main
git remote add origin https://github.com/wirawibowo/yaumi.git
git push -u origin main
```

Disarankan tambahkan ini ke `.gitignore`:

```bash
key.properties
*.jks
dist/
```

## 🏗️ Build

### Debug APK

```bash
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK

```bash
./gradlew :app:assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Release AAB (Play Store)

```bash
./gradlew :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## 🔐 Signing release

Release build membutuhkan `key.properties` dan keystore `.jks` di root project.
Jangan commit file tersebut ke GitHub.

## 🧾 Versi

Atur di `app/build.gradle.kts`:

- `versionCode`
- `versionName`

