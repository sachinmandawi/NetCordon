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
    <b>NetCordon</b> is a modern, rootless Android firewall built with <b>Jetpack Compose</b>.<br/>
    Take complete control of background network traffic, foreground app states, and push alerts without VPN tunnels or root access.
  </p>

  <p align="center">
    <a href="https://github.com/sachinmandawi/NetCordon/releases/download/v1.0.0/NetCordon_debug.apk">
      <img src="https://img.shields.io/badge/⬇️_DOWNLOAD_APK-v1.0.0_(Latest)-2E7D32?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" />
    </a>
  </p>

</div>

---

## 🌟 The 3 Core Pillars of NetCordon

NetCordon is designed around three powerful, automated privacy and network management pillars:

### 📴 1. Background Traffic Freeze (Single-Tick ✓ Privacy)
* **What it does:** The instant a restricted app (such as **WhatsApp**, **Instagram**, or **Telegram**) is minimized, closed, or your screen is locked, NetCordon cuts 100% of its background internet access.
* **Real-World Experience:** Senders see only a **Single Tick (✓)** on sent messages because the app cannot establish background connections.
* **Benefits:** Complete peace of mind, zero background telemetry, and massive mobile data & battery savings.

---

### ⚡ 2. Foreground Auto-Resume (Seamless On-Demand Access)
* **What it does:** The exact moment you actively open a protected app, NetCordon automatically detects foreground activity and instantly restores full internet access.
* **Real-World Experience:** You can chat, browse, or call normally without touching any firewall settings. As soon as you exit or minimize the app, network access is immediately frozen again.
* **Benefits:** Zero manual toggling — internet works on-demand only when you are actively using the app.

---

### 🔕 3. Distraction-Free Notification Muting (Zero Popups & Vibrations)
* **What it does:** Completely suppresses and silences all background push notifications, heads-up popups, vibration alerts, and ringtones from restricted apps.
* **Real-World Experience:** No unexpected message popups or vibrating alerts while you are playing competitive games, watching movies, studying, or attending meetings.
* **Benefits:** 100% focused, distraction-free smartphone experience.

---

## 🚀 Getting Started

### 1. Prerequisites
* Android device running **Android 8.0 (Oreo) or higher** (Android 8.0 - 15+ supported).
* [**Shizuku App**](https://shizuku.rikka.app/) installed and activated via **Wireless Debugging** or **ADB PC**.

### 2. Installation & Usage
1. Download the latest APK from the [**Releases Page**](https://github.com/sachinmandawi/NetCordon/releases).
2. Install the APK on your device.
3. Launch NetCordon, grant **Shizuku & Usage Access permissions**, and tap **"Continue to NetCordon"**.
4. Tap the **WiFi** or **Data** icon next to any app to activate background freezing and notification protection!

---

## 🛠️ Architecture & Tech Stack

* **Language:** 100% Kotlin
* **UI Framework:** Android Jetpack Compose (Material 3 Dark Palette)
* **Privilege Layer:** Shizuku API (Rikka Binder Service — Zero Root / Zero VPN)
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
