# San Siri Residency - Android App (`com.anil.sansiri`)

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Auth%20%26%20Firestore-orange.svg)](https://firebase.google.com/)

A modern, comprehensive society & residency management mobile application built for **San Siri Residency**. Designed with Kotlin, Jetpack Compose (Material Design 3), MVVM Architecture, and Firebase Cloud Backend.

---

## 🌟 Key Feature Matrix

| Feature | Description |
| :--- | :--- |
| **Authentication & Roles** | Role-based access control with Phone Number + OTP/Passcode authentication for **Super Admin**, **Admin**, and **House Owner / User**. |
| **Dynamic Balance Sheet** | Complete residency financial overview tracking income, maintenance collections, operational expenses, balance totals, and transaction history. |
| **4-Month Payment Tabs** | Visual multi-month maintenance tracking screen displaying bi-monthly and 4-month maintenance payment history, pending dues, and payment options. |
| **Multi-Property Support** | Allows residents and owners to register, link, and switch between multiple houses/units within the San Siri complex under a single profile. |
| **Peoples Directory** | Full resident contact directory categorized by block, floor, and house number with search and filter capabilities. |
| **Emergency Contacts** | Quick access to society staff, security desk, plumber, electrician, committee members, and nearby emergency services. |
| **Request Comments & Queries** | Interactive complaint, maintenance request, and query system with status updates (Pending, In Progress, Resolved) and threaded comments. |
| **Notification Center** | In-app notification hub providing real-time alerts for maintenance updates, announcements, payment confirmations, and approval requests. |

---

## 🔑 Standard Test Credentials Matrix

For quick demo access and testing without waiting for SMS OTP, use the pre-configured credentials below:

| Role | Phone Number | Passcode / OTP Code | Default Name | Access Level |
| :--- | :--- | :--- | :--- | :--- |
| **Super Admin** | `9964532102` | `Anil@102` (or `123456`) | San Siri Super Admin | Full system control, admin management, pricing rules, system approvals |
| **Admin** | `9876543210` | `123456` | San Siri Admin | Society maintenance verifications, announcements, user management |
| **House Owner / Resident** | `9123456789` | `123456` | House Owner | House details, maintenance payments, raising requests, directory access |

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 100%
- **UI Framework:** Jetpack Compose with Material Design 3 (`androidx.compose.material3`)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **State Management:** Kotlin Coroutines `StateFlow` and `SharedFlow` with `lifecycle-viewmodel-compose`
- **Backend & Database:**
  - **Firebase Auth:** Phone authentication and user session management
  - **Cloud Firestore:** Cloud NoSQL database for real-time document synchronization
  - **Firebase Realtime Database:** Low-latency live data feeds
- **Local Cache & Storage:** Room Database (`androidx.room`) and DataStore Preferences
- **Asynchronous Execution:** Kotlin Coroutines (`kotlinx.coroutines`)
- **Dependency Injection & KSP:** Kotlin Symbol Processing for Room & Moshi codegen

---

## ⚙️ Firebase Setup (`google-services.json`) Guide

To connect the application to your custom Firebase project:

1. **Create Firebase Project:**
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Click **Add Project** and name it `San Siri Residency`.

2. **Register Android Application:**
   - Register an Android app with Package Name: `com.anil.sansiri`.
   - Download the generated `google-services.json` configuration file.

3. **Place Configuration File:**
   - Place the `google-services.json` file inside the `app/` module directory:
     ```text
     SanSiri/
     ├── app/
     │   ├── google-services.json   <-- Place file here
     │   ├── build.gradle.kts
     │   └── proguard-rules.pro
     ```

4. **Enable Firebase Services:**
   - **Firebase Authentication:** Enable *Phone* authentication provider.
   - **Cloud Firestore:** Create a database in production or test mode.

---

## 🚀 Build & Unit Test Commands

Execute the following Gradle commands from the project root directory:

### Assemble Debug Build
```bash
./gradlew :app:assembleDebug
```

### Run All Unit Tests
```bash
./gradlew :app:testDebugUnitTest
```

### Run Unit Tests with Stack Trace
```bash
./gradlew :app:testDebugUnitTest --stacktrace
```

---

## 📄 License & Maintainer

- **Package:** `com.anil.sansiri`
- **Application:** San Siri Residency Mobile Application
