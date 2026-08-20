# Building PushToFinance from source

PushToFinance is a standard Android (Gradle) project. You can build the APK on **Linux**, **Windows** and **macOS**. The GitHub Actions workflow ([`.github/workflows/build.yml`](../.github/workflows/build.yml)) automates exactly this on all three OS.

## 1. Prerequisites

| Requirement | Linux | Windows | macOS |
|---|---|---|---|
| **JDK 17+** | `sudo apt install openjdk-17-jdk` (or use Android Studio's bundled JBR) | Install [Temurin 17+](https://adoptium.net/) or use Android Studio's JBR | `brew install --cask temurin@17` or use Android Studio's JBR |
| **Android SDK** | `sudo apt install android-sdk` or [cmdline-tools](https://developer.android.com/studio#command-line-tools-only) | Android Studio or [cmdline-tools](https://developer.android.com/studio#command-line-tools-only) | Android Studio or [cmdline-tools](https://developer.android.com/studio#command-line-tools-only) |
| **Gradle** | none (wrapper included) | none (wrapper included) | none (wrapper included) |

You do **not** need Gradle installed — the included Gradle wrapper (`gradlew` / `gradlew.bat`) downloads the correct version automatically.

### Android SDK packages

AGP will warn you to accept licenses and will complain if the SDK packages are missing. The required packages (platform 36, build-tools, platform-tools) are installed automatically by the Gradle Android plugin if you run:

```bash
yes | sdkmanager --licenses
```

On Windows: `yes |` does not exist — run `sdkmanager --licenses` and answer `y` to each prompt.

> Tip: using Android Studio for the first run is the easiest path — it installs the SDK, JDK and accepts licenses for you.

## 2. Environment variables

Point the build at your SDK:

**Linux / macOS (bash/zsh):**
```bash
export ANDROID_HOME="$HOME/Android/Sdk"        # macOS: ~/Library/Android/sdk
export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")"
```

**Windows (PowerShell):**
```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
```

Alternatively, create a `local.properties` file in the project root:

```properties
sdk.dir=C:/Users/<you>/AppData/Local/Android/Sdk   # Windows
sdk.dir=/home/<you>/Android/Sdk                    # Linux
sdk.dir=/Users/<you>/Library/Android/sdk           # macOS
```

`local.properties` is gitignored — never commit it.

## 3. Build the debug APK

```bash
# Linux / macOS
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

The debug APK is signed with the standard Android debug keystore, so it installs directly on your device.

## 4. Build a signed release APK

A release build is produced **unsigned** unless you configure a keystore. To produce a signed release APK:

### 4.1 Generate a keystore (once)

```bash
keytool -genkey -v -keystore release.keystore -alias push-to-finance -keyalg RSA -keysize 2048 -validity 10000
```

### 4.2 Provide signing config

Create `keystore.properties` in the project root (gitignored):

```properties
storeFile=../release.keystore
storePassword=YOUR_STORE_PASSWORD
keyAlias=push-to-finance
keyPassword=YOUR_KEY_PASSWORD
```

> `storeFile` is resolved relative to `app/`, so `../release.keystore` points at the repo root.

Alternatively, provide the same values as environment variables:
`KEYSTORE_FILE`, `KEYSTORE_STORE_PASSWORD`, `KEYSTORE_KEY_ALIAS`, `KEYSTORE_KEY_PASSWORD`.

### 4.3 Build

```bash
# Linux / macOS
./gradlew assembleRelease

# Windows
gradlew.bat assembleRelease
```

Output (signed): `app/build/outputs/apk/release/app-release.apk`
Output (unsigned, if no keystore): `app/build/outputs/apk/release/app-release-unsigned.apk`

## 5. Install on a device / emulator

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 5.1 First-run setup

After installing, grant the two things the app needs:

1. **Notification access** — the app shows a button in *Settings → Notification access* that opens the system screen where you enable PushToFinance. This is required to capture pushes.
2. **Notification permission** (Android 13+ / API 33+) — allow notifications so captured payments can be surfaced as heads-up notifications.

By default the app runs a **background listening** foreground service that keeps the notification listener alive, so payments are captured even when the app is closed. If your device reports that notifications are only captured while the app is open, check that *Settings → Notification access → Background listening* is enabled and that the app isn't battery-restricted or force-stopped. On Android 14+ (API 34+) the service uses the `specialUse` foreground service type, declared with `FOREGROUND_SERVICE_SPECIAL_USE` in the manifest.

## 6. Common issues

| Symptom | Fix |
|---|---|
| `Failed to install the following Android SDK packages` | Accept licenses: `sdkmanager --licenses`, then re-run |
| `Could not find or load main class` | Wrong JDK — set `JAVA_HOME` to JDK 17+ (`java -version` must be ≥ 17) |
| `Unsupported class file major version` | JDK too new for the Gradle version — use JDK 17 or 21 |
| Build daemon killed / `mmap failed` | Low RAM — reduce `-Xmx` in `gradle.properties` or close other apps |

## 7. CI

The repository includes a GitHub Actions workflow that builds the debug APK on **Ubuntu**, **Windows** and **macOS** runners on every push/PR, and uploads the APK as an artifact. To also build a signed release APK in CI, add the secrets `KEYSTORE_FILE` (base64), `KEYSTORE_STORE_PASSWORD`, `KEYSTORE_KEY_ALIAS`, `KEYSTORE_KEY_PASSWORD` to the repository and uncomment the release step in `.github/workflows/build.yml`.