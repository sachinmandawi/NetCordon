<div align="center">

  <img src="public/logo.png" alt="NetCordon Logo" width="100" height="100" style="border-radius: 24px;" />

  # 🛡️ NetCordon
  ### Intelligent Rootless Android App Firewall & Privacy Shield

  <p align="center">
    <a href="https://github.com/sachinmandawi/NetCordon/releases/latest"><img src="https://img.shields.io/badge/Release-v1.0.0-4CAF50?style=for-the-badge&logo=android&logoColor=white" alt="Release" /></a>
    <a href="https://github.com/sachinmandawi/NetCordon/stargazers"><img src="https://img.shields.io/badge/Stars-Rate%20Repo-FFA000?style=for-the-badge&logo=github&logoColor=white" alt="Stars" /></a>
    <a href="https://github.com/sachinmandawi/NetCordon/blob/main/LICENSE"><img src="https://img.shields.io/badge/License-MIT-2196F3?style=for-the-badge" alt="License" /></a>
    <a href="https://shizuku.rikka.app"><img src="https://img.shields.io/badge/Engine-Shizuku%20ADB-9C27B0?style=for-the-badge&logo=android" alt="Shizuku" /></a>
  </p>

  <p align="center">
    <b>NetCordon</b> is a modern, lightweight, rootless Android firewall built with <b>Jetpack Compose</b>.<br/>
    Take complete control of per-app internet access across <b>WiFi</b> and <b>Mobile Data</b> without creating VPN tunnels or root permissions.
  </p>

  <p align="center">
    <a href="https://github.com/sachinmandawi/NetCordon/releases/download/v1.0.0/NetCordon_debug.apk">
      <img src="https://img.shields.io/badge/⬇️_DOWNLOAD_APK-v1.0.0_(Latest)-2E7D32?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" />
    </a>
  </p>

</div>

---

## 🌟 Key Highlights

* 📴 **1. Background Traffic Freeze (Single-Tick ✓ Privacy):**
  When a protected app (e.g., WhatsApp, Instagram) is minimized or your screen is locked, NetCordon cuts 100% of its background data. Senders only see a **Single Tick (✓)**, eliminating background telemetry, data drainage, and battery drain.

* ⚡ **2. Foreground Auto-Resume (Instant On-Demand Access):**
  The moment you open the app to use it, NetCordon immediately detects foreground usage and restores full internet connectivity seamlessly. As soon as you minimize or exit the app, network access is instantly frozen again.

* 🔕 **3. Distraction-Free Notification Muting:**
  Completely silences all background push alerts, sound notifications, pop-ups, and vibrations from restricted apps. Enjoy gaming, studying, or watching videos without unwanted message disruptions.

---

## 📸 Overview & Flow

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│  Shizuku Binder │ ───> │ NetCordon Core  │ ───> │ Android Kernel  │
│  (ADB Bridge)   │       │ Policy Engine   │       │   (netpolicy)   │
└─────────────────┘       └─────────────────┘       └─────────────────┘
                                   │
              ┌────────────────────┴────────────────────┐
              ▼                                         ▼
   📶 Per-App WiFi Firewall                  📊 Mobile Data Firewall
   (REJECT_METERED / UID block)              (REJECT_ALL / Background Cut)
```

---

## 🚀 Getting Started

### 1. Prerequisites
* Android device running **Android 8.0 (Oreo) or higher** (Android 8.0 - 15+ supported).
* [**Shizuku App**](https://shizuku.rikka.app/) installed and activated via **Wireless Debugging** or **ADB PC**.

### 2. Installation
1. Download the latest APK from the [**Releases Page**](https://github.com/sachinmandawi/NetCordon/releases).
2. Install the APK on your device.
3. Launch NetCordon, grant **Shizuku permission**, and tap **"Continue to NetCordon"**.
4. Tap the **WiFi** or **Data** icon next to any app to instantly restrict or allow its network access!

---

## 🛠️ Architecture & Tech Stack

* **Language:** 100% Kotlin
* **UI Framework:** Android Jetpack Compose (Material 3 Dark Palette)
* **Privilege Layer:** Shizuku API (Rikka Binder Service)
* **Background Engine:** Android Foreground Service with BroadcastReceivers (`SCREEN_OFF`, `USER_PRESENT`, `BOOT_COMPLETED`)
* **Persistence:** Android SharedPreferences (`PrefsManager`)

---

## 📬 Developer & Support

Developed with ❤️ by **Sachin Mandawi**.

* 📧 **Email:** [sachinmandawi@gmail.com](mailto:sachinmandawi@gmail.com)
* 🐙 **GitHub Profile:** [@sachinmandawi](https://github.com/sachinmandawi)
* 🌟 **Repository:** [NetCordon on GitHub](https://github.com/sachinmandawi/NetCordon)

If you find this project helpful, please consider **starring ⭐ the repository**!

---

<div align="center">
  <sub>Licensed under the <a href="LICENSE">MIT License</a>. Copyright © 2026 Sachin Mandawi.</sub>
</div>
