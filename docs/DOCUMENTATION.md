# CPU-V – Documentation

Package: `com.CPU.V.Vhynlain`
Languages: Java and Kotlin
Build: Gradle 8.11.1, Android Gradle Plugin 8.9.1, Kotlin 1.9.24, JDK 17
SDK: minSdk 21, targetSdk 36, compileSdk 36

## Features

| Tab | Content |
|-----|---------|
| Dashboard | Chipset icon and name, realtime RAM chart, realtime battery current chart, battery summary |
| Device | Model, manufacturer, brand, device, board, hardware, fingerprint |
| System | Android version, API level, security patch, build, kernel, bootloader |
| CPU | Chipset, GPU renderer, vendor and OpenGL ES version, ABI, core count, per-core current and max frequency |
| Battery | Level, status, power source, health, technology, temperature, voltage, current |
| Network | Connection type, Wi-Fi IP, SSID, link speed, frequency, operator, mobile network type |
| Display | Resolution, density, screen size, supported refresh rates |
| Memory | RAM and internal storage totals, used and free |
| Connectivity | Support for Wi-Fi (incl. 5 GHz and 6 GHz), Wi-Fi Direct, Bluetooth, BLE, NFC, UWB, USB Host, GPS, fingerprint, telephony, infrared |
| Bluetooth | Adapter state and name, BLE capabilities (2M PHY, Coded PHY, extended and periodic advertising, offloading, LE Audio), paired devices |
| Codecs | All media decoders and encoders with hardware/software flag and supported MIME types |
| Camera | Per camera: megapixels, resolution, focal length, aperture, sensor size, flash |
| Thermal | Battery temperature, system thermal status, readable thermal zones |
| Sensors | List of all sensors; each opens its own page with details and a realtime chart |
| Audio | Native sample rate, buffer size, low-latency/pro audio, output and input devices |
| Features | All system features reported by the device |
| Tests | Interactive hardware tests (see below) |

## Tests

Each test opens a full-screen page and asks whether it worked. The result (passed or failed) is saved and shown in the list.

Display (color cycle), Multi-touch (live touch counter), Flashlight, Loudspeaker, Earpiece, Microphone (live level meter), Proximity, Light sensor, Accelerometer, Charging, Vibration.

## Permissions

| Permission | Why |
|------------|-----|
| `ACCESS_FINE_LOCATION` | Android requires it to read the Wi-Fi SSID |
| `BLUETOOTH_CONNECT` | Android 12+ requires it to read Bluetooth adapter name, state and paired devices |
| `RECORD_AUDIO` | Microphone test only. Audio is measured live and never recorded or stored |
| `MODIFY_AUDIO_SETTINGS` | Earpiece test routes sound to the earpiece |
| `VIBRATE`, `FLASHLIGHT` | Vibration and flashlight tests |
| `INTERNET` | Download the chipset icon once |
| `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` | Read connection details |

The Network tab shows a "Grant permission" button while a permission is missing. Everything else works without permissions.

Long-press any row to copy it. Menu > Share report shares all info as text. The privacy policy URL (`PRIVACY_POLICY_URL` in `app/build.gradle.kts`) is linked in Settings.

## Chipset icon

The Dashboard picks the chipset vendor from the device's SoC info (Qualcomm Snapdragon, MediaTek, Samsung Exynos, Google Tensor, Unisoc, Huawei HiSilicon Kirin, Rockchip, Allwinner, Amlogic, Nvidia Tegra, Intel Atom, Broadcom, Realtek, Texas Instruments OMAP) and downloads its logo once from Wikimedia Commons in a background thread (`CHIPSET_ICON_URL_TEMPLATE` in `app/build.gradle.kts`, `%s` = file name). The file names are set in `ChipsetIconLoader.vendorKey()`. SVG files are rendered with AndroidSVG; PNG/JPG files are decoded directly. The logo is saved in private app storage and reused on later launches. If the vendor is unknown  or the download fails, a built-in chip drawable is shown. The URL must use HTTPS.

The Dashboard also shows the Android logo (`android.svg`, same source) with the version name, codename and API level. A built-in Android drawable is the fallback.

The System tab starts with a banner: logo, Android version, codename, release date and API level. Versions listed in `ANDROID_LOGOS` (`InfoFragment.kt`) download their own logo from Wikimedia Commons (`ANDROID_VERSION_LOGO_URL_TEMPLATE`, `%s` = file name). Other versions use the generic Android robot (`ANDROID_LOGO_URL_TEMPLATE`). To add a version, add `API to "File_name.svg"` using the exact file name from commons.wikimedia.org.

## Languages

English, Indonesian, Spanish, French, German, Portuguese, Russian, Arabic, Hindi, Chinese.

The app follows the system language by default. A language can also be chosen in Settings, or per app in Android 13+ system settings (`locales_config.xml`). `AppLocalesMetadataHolderService` stores the choice on older Android versions.

## Themes

Settings offers: System default, Light, Dark, Black (pure black for AMOLED). Light and Dark follow the system day/night mode when "System default" is selected.

## Project layout

```
app/src/main/java/com/CPU/V/Vhynlain/
  MainActivity.kt          tabs
  DashboardFragment.kt     dashboard and charts
  InfoFragment.kt          generic info tab with permission handling
  SensorsFragment.kt       sensor list
  SensorDetailActivity.kt  per-sensor page
  SettingsActivity.kt      language and theme
  SystemInfo.java          reads all device data
  LineChartView.java       custom realtime chart
  ChipsetIconLoader.kt     background download and private storage
```

## Build with GitHub Actions

1. Push the project to a GitHub repository (branch `main`).
2. Open the Actions tab and run "Build APK", or push a commit.
3. Download the `CPU-V-debug` artifact. It contains the debug APK.

Local build: `gradle assembleDebug`.

## Note on targetSdk

targetSdk is 36. Android 14 and newer refuse to install apps that target below API 23, so targetSdk 17 would not install on modern devices.
