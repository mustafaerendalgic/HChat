# 📱 ChatApp — Android Messaging Platform

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)
![Bluetooth](https://img.shields.io/badge/Bluetooth-0082FC?style=for-the-badge&logo=bluetooth&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)

> **Graduation Thesis Project** — Çukurova University, Department of Computer Engineering

**ChatApp** is an Android messaging application built around two fully independent communication modules — **Firebase** for internet-based real-time chat and **Bluetooth Classic RFCOMM** for offline peer-to-peer communication. The user selects a module from the main menu; each has its own data layer, ViewModels, and UI flow with no shared state between them.

---

> ⚠️ This project was developed as a graduation thesis at Çukurova University.
> Feel free to use it for learning purposes. If you reference it in academic work, please cite appropriately.

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
- [License](#-license)

---

## 📋 Project Overview

ChatApp provides two independent messaging modules, each designed for a different connectivity context:

| Module | Technology | Requirement |
|---|---|---|
| **Firebase Module** | Firebase Realtime Database + Auth | Active internet connection |
| **Bluetooth Module** | Bluetooth Classic RFCOMM | No internet needed — device proximity only |

The modules are selected from a main menu and operate entirely independently — different screens, different data sources, different ViewModels.

---

## 📸 Screenshots

### Firebase Module
| Login Screen | Chat List | Chat Interface |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/215fee3a-0a90-48b6-ac1e-12c96458c5ed" width="200" /> | <img src="https://github.com/user-attachments/assets/3cc7e437-cdd6-4d76-bf71-c2b242315333" width="200" /> | <img src="https://github.com/user-attachments/assets/17fa258b-aa87-44a9-8c4e-227ece0d30ff" width="200" /> |

### Bluetooth Module
| Device Scan | Chat Interface |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/7a2bc9b2-2883-413b-99c9-7d5661e9b0e2" width="200" /> | <img src="https://github.com/user-attachments/assets/8f28c0ba-00ec-49f3-b4ed-262eccfe32d5" width="200" /> |

---

## ✨ Key Technical Features

- **🔀 Two Independent Communication Modules** — Firebase module handles cloud-based real-time messaging; Bluetooth module handles fully offline P2P sessions via RFCOMM sockets. Each module has its own architecture with no shared state.

- **📡 RFCOMM Data Streaming** — Implements asynchronous multi-threading (AcceptThread / ConnectThread / ConnectedThread) to manage device discovery, connection handshakes, and bidirectional data streams without blocking the UI.

- **🔒 Modern Permission Compliance** — Includes a runtime Permission Management Framework tailored for **Android 12+ (API 31)** Bluetooth permission standards (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`), with graceful fallback for legacy APIs (pre-API 31).

- **⚡ Reactive UI** — Built with **MVVM** architecture combined with **LiveData** and **ListAdapter** for memory-efficient, lifecycle-aware data synchronization.

- **💉 Dependency Injection via Hilt** — All components are wired through Hilt (Dagger), enabling scalable and maintainable code architecture.

- **🧭 Single-Activity Navigation** — Leverages the Android Navigation Component for centralized, type-safe fragment management with a clean back stack.

---

## 🏛 Architecture

ChatApp follows the **MVVM** architectural pattern with a clean separation of concerns. The two modules share the same ViewModel layer structure but have completely separate repositories and data flows:

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

| Category | Technology | Version |
|---|---|---|
| **Language** | Kotlin | 2.2.10 |
| **IDE** | Android Studio Iguana | 2023.2.1 |
| **Min SDK** | Android 8.0 Oreo | API 26 |
| **Target SDK** | Android 14 | API 34 |
| **Build System** | Gradle (KTS) + KSP | KSP 2.3.1 |
| **Firebase Module** | Firebase Realtime Database + Auth | google-services 4.4.4 |
| **Bluetooth Module** | Bluetooth Classic — RFCOMM / SPP | — |
| **Architecture** | MVVM + LiveData + Repository Pattern | — |
| **Dependency Injection** | Hilt (Dagger) | 2.56 |
| **Navigation** | Android Navigation Component + Safe Args | 2.7.7 |
| **Async** | Kotlin Coroutines | — |
| **UI** | ViewBinding, ListAdapter, RecyclerView | — |

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

| Permission | Purpose | API Scope |
|---|---|---|
| `INTERNET` | Firebase cloud messaging | All |
| `BLUETOOTH` | Basic Bluetooth operations | maxSdkVersion 30 |
| `BLUETOOTH_ADMIN` | Device discovery & pairing | maxSdkVersion 30 |
| `ACCESS_FINE_LOCATION` | Required for BT scanning on legacy APIs | maxSdkVersion 30 |
| `ACCESS_COARSE_LOCATION` | Required for BT scanning on legacy APIs | maxSdkVersion 30 |
| `ACCESS_BACKGROUND_LOCATION` | Background BT scanning on legacy APIs | maxSdkVersion 30 |
| `BLUETOOTH_SCAN` | Scan for nearby devices (`neverForLocation`) | API 31+ |
| `BLUETOOTH_CONNECT` | Connect to paired/discovered devices | API 31+ |
| `BLUETOOTH_ADVERTISE` | Make device discoverable to peers | API 31+ |

The app includes a Permission Management Framework that gracefully handles both legacy (pre-API 31) and modern (API 31+) Bluetooth permission models.

---

## ⚙️ How It Works

### Firebase Module (Online)
1. User authenticates via Firebase Authentication.
2. Messages are written to Firebase Realtime Database.
3. All connected clients receive updates in real-time via Firebase listeners.

### Bluetooth Module (Offline P2P)
1. One device acts as a **server** — starts an `AcceptThread` listening on an RFCOMM socket.
2. The other device acts as a **client** — discovers nearby devices and connects via `ConnectThread`.
3. Once connected, a `ConnectedThread` manages the bidirectional data stream.
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

---

## 👤 Author

**Mustafa Eren Dalgıç**
- GitHub: [@mustafaerendalgic](https://github.com/mustafaerendalgic)
- Çukurova University — Department of Computer Engineering

---

## 📄 License

This project is licensed under the **MIT License**

---

<p align="center">Made as a Graduation Thesis at Çukurova University</p>
