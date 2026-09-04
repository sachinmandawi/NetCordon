# Contributing to NetCordon

Thank you for your interest in contributing to NetCordon! NetCordon is an open-source, privacy-first Android app firewall built with Jetpack Compose and Shizuku.

## How to Contribute

### 1. Reporting Bugs
- Search existing [GitHub Issues](https://github.com/sachinmandawi/NetCordon/issues) before opening a new one.
- Provide clear steps to reproduce, device model, Android version, and whether Shizuku is running.

### 2. Suggesting Enhancements
- Open a feature request under [Issues](https://github.com/sachinmandawi/NetCordon/issues) describing the use-case and expected behavior.

### 3. Submitting Pull Requests
1. Fork the repository: https://github.com/sachinmandawi/NetCordon
2. Create your feature branch: git checkout -b feature/my-new-feature
3. Commit your changes: git commit -m 'Add awesome feature'
4. Ensure all unit tests pass: ./gradlew testDebugUnitTest
5. Push to the branch: git push origin feature/my-new-feature
6. Submit a Pull Request on GitHub.

## Code Style & Principles
- 100% Kotlin with Android Jetpack Compose.
- Zero battery drain & zero unnecessary background wakeups.
- Strict privacy: No user telemetry or analytics tracking.