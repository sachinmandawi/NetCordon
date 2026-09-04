<div align="center">

  <img src="public/logo.png" alt="NetCordon Logo" width="108" height="108" style="border-radius: 26px;" />

  # 🛡️ NetCordon
  ### 100% Free & Open-Source Android Firewall & Privacy Shield
  **Zero Root · Zero VPN · Powered by Shizuku API**

  <p align="center">
    <a href="https://github.com/sachinmandawi/NetCordon"><img src="https://img.shields.io/badge/Release-v1.0.0-4CAF50?style=for-the-badge&logo=android&logoColor=white" alt="Release" /></a>
    <a href="https://github.com/sachinmandawi/NetCordon/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-2196F3?style=for-the-badge" alt="License" /></a>
    <a href="https://shizuku.rikka.app"><img src="https://img.shields.io/badge/Engine-Shizuku%20ADB-9C27B0?style=for-the-badge&logo=android" alt="Shizuku" /></a>
    <a href="https://github.com/sachinmandawi/NetCordon/stargazers"><img src="https://img.shields.io/github/stars/sachinmandawi/NetCordon?style=for-the-badge&color=FFA000" alt="Stars" /></a>
    <a href="https://github.com/sachinmandawi/NetCordon/issues"><img src="https://img.shields.io/github/issues/sachinmandawi/NetCordon?style=for-the-badge&color=E91E63" alt="Issues" /></a>
  </p>

  <p align="center">
    <b>NetCordon</b> is a modern, privacy-first Android app firewall built completely with <b>Jetpack Compose</b>.<br/>
    Unlike traditional firewalls that force a battery-draining local VPN tunnel, NetCordon controls network traffic directly at the system level via <b>Shizuku (ADB binder)</b>.
  </p>

  <p align="center">
    <a href="https://github.com/sachinmandawi/NetCordon">
      <img src="https://img.shields.io/badge/⬇️_DOWNLOAD_LATEST_APK-v1.0.0-00E676?style=for-the-badge&logo=android&logoColor=black" alt="Download APK" />
    </a>
    &nbsp;&nbsp;
    <a href="https://sachinmandawi.github.io">
      <img src="https://img.shields.io/badge/🌐_OFFICIAL_WEBSITE-Visit_Portal-1E88E5?style=for-the-badge&logo=googlechrome&logoColor=white" alt="Website" />
    </a>
  </p>

</div>

---

## ⚡ Why NetCordon Over Other Firewalls?

| Feature | Standard VPN Firewalls (NetGuard, etc.) | NetCordon (Open Source) |
|---|---|---|
| **Root Required?** | ❌ No | ✅ **No Root Needed** (Works via Shizuku) |
| **Battery Drain?** | ⚠️ High (Constantly runs local VPN loopback) | 🟢 **Ultra-Low** (Native OS policy triggers) |
| **Can use Real VPN alongside?** | ❌ No (Android allows only 1 active VPN) | ✅ **YES! Use any VPN (Proton, Mullvad) alongside** |
| **Single-Tick (✓) WhatsApp Privacy** | ⚠️ Complicated setup | ✅ **Automatic (Smart Shield mode)** |
| **In-App Direct Auto-Update** | ❌ Requires manual reinstall | ✅ **Direct 1-tap in-app update (No uninstall)** |
| **Play Store Independent** | ⚠️ Often restricted | ✅ **100% Free & Open-Source on GitHub** |

---

## 🌟 The 3-Tier Firewall Architecture

NetCordon provides granular, per-app network isolation:

### 1. 🟢 Always Allowed (Normal Mode)
* Apps have unrestricted access to Wi-Fi and Mobile Data.

### 2. 🟡 Smart Shield (Background Freeze & Single-Tick Privacy)
* **What it does:** The instant you minimize an app (like WhatsApp, Instagram, Telegram) or lock your phone screen, NetCordon immediately cuts all background traffic.
* **Single-Tick Experience:** Message senders see only a **Single Tick (✓)** because background connections are completely blocked.
* **On-Demand Access:** The exact millisecond you re-open the app, full internet is restored automatically.

### 3. 🔴 Total Blackout Mode
* Completely eliminates all incoming and outgoing network traffic for chosen apps across Wi-Fi and Mobile Data, both foreground and background.

---

## 🚀 Key Features

* 🚫 **Distraction-Free Notification Muting:** Suppresses heads-up popups, alerts, and vibrations from restricted apps during gaming, movies, or work.
* 📈 **Floating Speedometer & Traffic Radar:** Live per-app upload/download speed overlay and leak detection.
* 🔒 **Biometric & Device Credential Security:** Hardware-backed fingerprint, face unlock, and device PIN/Pattern app lock.
* 🔄 **Built-in GitHub Auto-Updater:** Checks GitHub Releases for new updates and installs them in-place with zero data loss.
* 💾 **JSON Profile Studio:** Export, import, and backup your firewall rules and custom schedules with syntax validation.

---

## 📥 Installation & Setup Guide

### Step 1: Install Shizuku
1. Install [**Shizuku**](https://shizuku.rikka.app/) on your Android device (Android 8.0 to Android 15+).
2. Open Shizuku and start it via **Wireless Debugging** (no PC required on Android 11+) or via **ADB on PC**.

### Step 2: Install NetCordon
1. Download the latest **[NetCordon_v1.0.0.apk](https://github.com/sachinmandawi/NetCordon)**.
2. Install the APK and grant **Shizuku & Usage Access** permissions.
3. Tap the **WiFi** or **Mobile Data** icon next to any app to protect your privacy!

---

## 🛠️ Building From Source

You can easily build NetCordon yourself using Gradle:

`ash
# Clone the open-source repository
git clone https://github.com/sachinmandawi/NetCordon.git
cd NetCordon/android_project

# Run unit test suite (48 tests)
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
`
The compiled APK will be at: ndroid_project/app/build/outputs/apk/debug/app-debug.apk.

---

## 🤝 Contributing

Contributions, feature requests, and bug reports are warmly welcome!
- Check existing [Issues](https://github.com/sachinmandawi/NetCordon/issues) or open a new one.
- Fork the repository and submit a Pull Request.

---

## 📄 License & Privacy Promise

* **License:** This project is licensed under the **[MIT License](LICENSE)**.
* **Privacy:** NetCordon operates **100% on-device**. No user data, app lists, network packets, or telemetry are ever collected, logged, or transmitted to any external server.

---

<div align="center">
  <sub>Developed with ❤️ by <b><a href="https://github.com/sachinmandawi">Sachin Mandavi</a></b>.</sub>
</div>