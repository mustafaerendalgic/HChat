# 📱 ChatApp — Hybrid Android Messaging Platform

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)
![Bluetooth](https://img.shields.io/badge/Bluetooth-0082FC?style=for-the-badge&logo=bluetooth&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)

> **Graduation Thesis Project** — Çukurova University, Department of Computer Engineering

**ChatApp** is a sophisticated Android communication platform engineered to work seamlessly in both connected and disconnected environments. It combines cloud-based Firebase messaging with low-level Bluetooth RFCOMM peer-to-peer communication, making it resilient in scenarios where internet infrastructure is compromised — such as natural disasters, remote industrial areas, or off-grid deployments.

---

## 📑 Table of Contents

- [Project Overview](#-project-overview)
- [Screenshots](#-screenshots)
- [Key Technical Features](#-key-technical-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Installation](#-installation)
- [Firebase Setup](#-firebase-setup)
- [Permissions](#-permissions)
- [How It Works](#-how-it-works)
- [Contributing](#-contributing)
- [License](#-license)

---

## 📋 Project Overview

The core objective of this thesis is to engineer a resilient messaging platform that seamlessly transitions between two communication modes:

| Mode | Technology | Requirement |
|---|---|---|
| **Online** | Firebase Realtime Database | Active internet connection |
| **Offline P2P** | Bluetooth RFCOMM | No internet needed — device proximity only |

By leveraging **Bluetooth RFCOMM** (Radio Frequency Communication), ChatApp establishes stable, high-throughput serial port emulations for real-time text exchange without requiring any internet connectivity. This hybrid model ensures continuous communication regardless of infrastructure availability.

---

## 📸 Screenshots

### Online Mode
| Login Screen | Chat List | Chat Interface |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/215fee3a-0a90-48b6-ac1e-12c96458c5ed" width="200" /> | <img src="https://github.com/user-attachments/assets/3cc7e437-cdd6-4d76-bf71-c2b242315333" width="200" /> | <img src="https://github.com/user-attachments/assets/17fa258b-aa87-44a9-8c4e-227ece0d30ff" width="200" /> |

### Bluetooth (Offline P2P) Mode
| Bluetooth Scan | Connected Devices | Bluetooth Chat |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/7a2bc9b2-2883-413b-99c9-7d5661e9b0e2" width="200" /> | <img src="https://github.com/user-attachments/assets/10ead6c9-bbdb-459e-8a83-b118f7132ab7" width="200" /> | <img src="https://github.com/user-attachments/assets/8f28c0ba-00ec-49f3-b4ed-262eccfe32d5" width="200" /> |

---

## ✨ Key Technical Features

- **🔀 Hybrid Communication Architecture** — Integrates Firebase Realtime Database for online messaging and low-level Bluetooth Socket Programming for fully offline P2P sessions. The app automatically handles the context of each connection type.

- **📡 RFCOMM Data Streaming** — Implements asynchronous multi-threading to manage device discovery, connection handshakes, and bidirectional data streams without degrading UI responsiveness.

- **🔒 Modern Security Compliance** — Includes a comprehensive Permission Management Framework specifically tailored for **Android 12+ (API 31)** security standards for hardware-level Bluetooth interactions (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`).

- **⚡ Reactive UI Engine** — Built using **MVVM** architecture combined with **LiveData** and **ListAdapter** for memory-efficient, lifecycle-aware data synchronization.

- **💉 Dependency Injection via Hilt** — All components are wired through Hilt (Dagger), enabling scalable, testable, and maintainable code architecture.

- **🧭 Single-Activity Navigation** — Leverages the Android Navigation Component for centralized, type-safe fragment management with a clean back stack.

---

## 🏛 Architecture

ChatApp follows the **MVVM (Model-View-ViewModel)** architectural pattern with a clean separation of concerns:

```
┌──────────────────────────────────────────────┐
│                    UI Layer                   │
│         Fragments + XML Layouts               │
└──────────────────┬───────────────────────────┘
                   │ observes LiveData
┌──────────────────▼───────────────────────────┐
│                ViewModel Layer                │
│     State management, business logic          │
└──────────────────┬───────────────────────────┘
                   │
       ┌───────────┴───────────┐
       │                       │
┌──────▼──────┐        ┌───────▼──────┐
│  Firebase   │        │  Bluetooth   │
│  Repository │        │  Repository  │
│  (Online)   │        │  (Offline)   │
└─────────────┘        └──────────────┘
```

---

## 🛠 Tech Stack

| Category | Technology |
|---|---|
| **Language** | Kotlin (with Coroutines for non-blocking operations) |
| **IDE** | Android Studio Iguana 2023.2.1 |
| **Min SDK** | 26 (Android 8.0 Oreo) |
| **Target SDK** | 34 (Android 14) |
| **Backend (Online)** | Firebase Realtime Database, Firebase Authentication |
| **Backend (Offline)** | Bluetooth Classic — RFCOMM / SPP |
| **Architecture** | MVVM + LiveData + Repository Pattern |
| **Dependency Injection** | Hilt (Dagger) |
| **Navigation** | Android Navigation Component |
| **Async** | Kotlin Coroutines |
| **UI** | ViewBinding, ListAdapter, RecyclerView |

---

## 📂 Project Structure

```
chatApp/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/chatapp/
│   │       │   ├── adapters/
│   │       │   │   ├── BluetoothChatMessageAdapter.kt
│   │       │   │   ├── BluetoothUserListAdapter.kt
│   │       │   │   ├── InternetChatMessageAdapter.kt
│   │       │   │   └── InternetUserListAdapter.kt
│   │       │   │
│   │       │   ├── data.entity/
│   │       │   │   ├── BluetoothDeviceListItem.kt
│   │       │   │   ├── BluetoothMessage.kt
│   │       │   │   ├── ChatMessage.kt
│   │       │   │   ├── ObjectConstants.kt
│   │       │   │   └── UserListItem.kt
│   │       │   │
│   │       │   ├── fragments/
│   │       │   │   ├── bluetooth/
│   │       │   │   │   ├── BluetoothChatPage.kt
│   │       │   │   │   └── BluetoothMainPage.kt
│   │       │   │   ├── internet/
│   │       │   │   │   ├── InternetChatPage.kt
│   │       │   │   │   ├── InternetMainPage.kt
│   │       │   │   │   ├── LoginPage.kt
│   │       │   │   │   └── SignUpPage.kt
│   │       │   │   └── main/
│   │       │   │       └── MainMenu.kt
│   │       │   │
│   │       │   ├── hilt/
│   │       │   │   ├── HiltAndroidApp.kt
│   │       │   │   └── Module.kt
│   │       │   │
│   │       │   ├── util/
│   │       │   │   ├── createBluetoothItem.kt
│   │       │   │   ├── dateFormatter.kt
│   │       │   │   └── permissionDataHandler.kt
│   │       │   │
│   │       │   ├── viewmodels/
│   │       │   │   ├── BluetoothModule/
│   │       │   │   │   └── BluetoothMessagingViewModel.kt
│   │       │   │   └── InternetModule/
│   │       │   │       ├── InternetChatPageViewModel.kt
│   │       │   │       ├── InternetMainPageViewModel.kt
│   │       │   │       └── SignUpViewModel.kt
│   │       │   │
│   │       │   └── MainActivity.kt
│   │       │
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── navigation/
│   │       │   └── values/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── .gitignore
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── google-services.json              # (not committed — add your own)
```

---

## 📦 Installation

### Prerequisites

- Android Studio **Iguana (2023.2.1)** or newer
- Android device or emulator running **API 26+**
- A Firebase project (see [Firebase Setup](#-firebase-setup))

### Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/mustafaerendalgic/chatApp.git
   cd chatApp
   ```

2. **Open in Android Studio:**
   - Launch Android Studio
   - Select **File → Open** and navigate to the cloned folder

3. **Add your Firebase config file:**
   - Place your `google-services.json` inside `app/`
   - (See [Firebase Setup](#-firebase-setup) below)

4. **Sync Gradle:**
   - Android Studio will prompt you — click **Sync Now**

5. **Build & Run:**
   - Connect a physical device (recommended for Bluetooth testing) or start an emulator
   - Press **Run ▶** or use `Shift + F10`

> ⚠️ **Note:** Bluetooth features require a **physical Android device**. Emulators do not support Bluetooth hardware.

---

## 🔥 Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Add an **Android app** with your package name (e.g., `com.example.chatapp`).
3. Download the `google-services.json` file and place it in the `app/` directory.
4. Enable the following Firebase services:
   - **Authentication** (Email/Password or Anonymous)
   - **Realtime Database** — set rules for development:
     ```json
     {
       "rules": {
         ".read": "auth != null",
         ".write": "auth != null"
       }
     }
     ```

---

## 🔐 Permissions

The app requests the following permissions, managed through a runtime permission framework:

| Permission | Purpose | Required API |
|---|---|---|
| `BLUETOOTH` | Basic Bluetooth operations | API < 31 |
| `BLUETOOTH_ADMIN` | Device discovery | API < 31 |
| `BLUETOOTH_SCAN` | Scan for nearby devices | API 31+ |
| `BLUETOOTH_CONNECT` | Connect to paired devices | API 31+ |
| `BLUETOOTH_ADVERTISE` | Make device discoverable | API 31+ |
| `ACCESS_FINE_LOCATION` | Required for BT scanning on older APIs | API < 31 |
| `INTERNET` | Firebase cloud messaging | All |

The app includes a comprehensive **Permission Management Framework** that gracefully handles both legacy (pre-API 31) and modern permission models.

---

## ⚙️ How It Works

### Online Mode (Firebase)
1. User authenticates via Firebase Authentication.
2. Messages are written to Firebase Realtime Database.
3. All connected clients receive updates in real-time via Firebase listeners.

### Offline Mode (Bluetooth P2P)
1. One device acts as a **server** — starts an `AcceptThread` listening on an RFCOMM socket.
2. The other device acts as a **client** — discovers nearby devices and connects via `ConnectThread`.
3. Once the connection is established, a **`ConnectedThread`** manages the bidirectional data stream.
4. All operations run on background threads to keep the UI responsive.

```
Device A (Server)              Device B (Client)
      │                               │
      │── AcceptThread (listening) ──▶│
      │                               │── ConnectThread (connect)
      │◀────── RFCOMM Connection ─────│
      │                               │
      │◀═══ ConnectedThread (R/W) ════│
```

## 👤 Author

**Mustafa Eren Dalgıç**
- GitHub: [@mustafaerendalgic](https://github.com/mustafaerendalgic)
- Çukurova University — Department of Computer Engineering

---

## 📄 License

This project is licensed under the **MIT License**

---

<p align="center">Made as a Graduation Thesis at Çukurova University</p>
