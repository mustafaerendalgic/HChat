# 📱 ChatApp — Hybrid Android Messaging & Network Utility Platform

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge\&logo=kotlin\&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge\&logo=android\&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge\&logo=firebase)
![Bluetooth](https://img.shields.io/badge/Bluetooth-0082FC?style=for-the-badge\&logo=bluetooth\&logoColor=white)
![Room](https://img.shields.io/badge/Room_DB-4285F4?style=for-the-badge\&logo=android\&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-DI-orange?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)

> **Graduation Thesis Project** — Çukurova University, Department of Computer Engineering

**ChatApp** is a hybrid Android application built around three separate modules: **Firebase** for cloud-based real-time messaging, **Bluetooth Classic RFCOMM** for offline peer-to-peer messaging, and a **Network Scanner** for discovering active devices on the local Wi-Fi network.

The app dynamically manages Firebase authentication and real-time message synchronization, BLE advertising and RFCOMM socket communication, local Room-based Bluetooth chat persistence, and Wi-Fi subnet scanning. The project is structured with MVVM architecture, Repository pattern, XML ViewBinding, Kotlin Coroutines, StateFlow/LiveData, and Hilt dependency injection.

> ⚠️ This project was developed as a graduation thesis at Çukurova University.
> Feel free to use it for learning purposes. If you reference it in academic work, please cite appropriately.

---

## 📑 Table of Contents

* [Project Overview](#-project-overview)
* [Screenshots](#-screenshots)
* [Architecture](#-architecture)
* [Key Technical Features](#-key-technical-features)
* [Tech Stack & Dependencies](#-tech-stack--dependencies)
* [Project Structure](#-project-structure)
* [Data Flow](#-data-flow)
* [Installation](#-installation)
* [Firebase Setup](#-firebase-setup)
* [Firestore Data Model](#-firestore-data-model)
* [Permissions](#-permissions)
* [How It Works](#-how-it-works)
* [Known Limitations](#-known-limitations)
* [Author](#-author)
* [License](#-license)

---

## 📋 Project Overview

The app launches to a **MainMenu** where the user selects one of the available modules. Each module has its own navigation path, UI screens, ViewModel/data layer, and technical responsibility.

| Module                     | Technology                                                       | Use Case                                           |
| -------------------------- | ---------------------------------------------------------------- | -------------------------------------------------- |
| **Firebase Module**        | Firebase Auth + Firestore + Realtime Database + Firebase Storage | Internet-based real-time chat                      |
| **Bluetooth Module**       | BLE Advertising + Bluetooth Classic RFCOMM Sockets + Room DB     | Offline peer-to-peer chat with local persistence   |
| **Network Scanner Module** | WifiManager + subnet probing + RecyclerView                      | Discover active devices on the local Wi-Fi network |

---

## 📸 Screenshots

### Firebase Module

|                                                   Login                                                  |                                                 User List                                                |                                                   Chat                                                   |
| :------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------: |
| <img src="https://github.com/user-attachments/assets/215fee3a-0a90-48b6-ac1e-12c96458c5ed" width="200"/> | <img src="https://github.com/user-attachments/assets/3cc7e437-cdd6-4d76-bf71-c2b242315333" width="200"/> | <img src="https://github.com/user-attachments/assets/17fa258b-aa87-44a9-8c4e-227ece0d30ff" width="200"/> |

### Bluetooth Module

|                                                Device Scan                                               |                                                   Chat                                                   |
| :------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------: |
| <img src="https://github.com/user-attachments/assets/7a2bc9b2-2883-413b-99c9-7d5661e9b0e2" width="200"/> | <img src="https://github.com/user-attachments/assets/8f28c0ba-00ec-49f3-b4ed-262eccfe32d5" width="200"/> |

### Network Scanner Module

|             Scan Screen             |             Detected Devices             |
| :---------------------------------: | :--------------------------------------: |
| Add network scanner screenshot here | Add detected-device list screenshot here |

---

## 🏛 Architecture

ChatApp follows **MVVM** with a Repository pattern. The three feature modules are selected from the MainMenu and handled through separate UI and data flows.

```text
┌──────────────────────────────────────────────────────────────────────┐
│                              UI Layer                                 │
│        MainMenu ──► InternetModule / BluetoothModule / ScanModule      │
│                    Fragments + XML ViewBinding                        │
└──────────────────────────────┬───────────────────────────────────────┘
                               │ observes StateFlow / LiveData
┌──────────────────────────────▼───────────────────────────────────────┐
│                          ViewModel Layer                              │
│  InternetChatPageViewModel      BluetoothMessagingViewModel           │
│  InternetMainPageViewModel      NetworkScanViewModel                  │
│  SignUpViewModel                                                       │
└───────────────┬──────────────────────┬───────────────────────────────┘
                │                      │
┌───────────────▼──────────────┐ ┌─────▼────────────────────┐ ┌────────▼───────────────┐
│        Firebase Stack         │ │     Bluetooth Stack       │ │   Network Scan Stack   │
│                               │ │                           │ │                        │
│  FirebaseAuth                 │ │  BluetoothRepo            │ │  NetworkScanRepo       │
│  FirebaseFirestore            │ │  ├─ BLE Advertiser        │ │  ├─ WifiManager        │
│  FirebaseRealtime Database    │ │  ├─ BLE Scanner           │ │  ├─ Local IP resolver  │
│  FirebaseStorage              │ │  ├─ RFCOMM ServerSocket   │ │  ├─ Subnet probing     │
│                               │ │  ├─ RFCOMM ClientSocket   │ │  ├─ ARP table lookup   │
│                               │ │  ├─ ConnectedThread       │ │  └─ ScanResultObject   │
│                               │ │  └─ Room DAO              │ │                        │
└───────────────────────────────┘ └───────────────────────────┘ └────────────────────────┘
```

### Event-Driven ViewModel — Bluetooth

The Bluetooth module uses a **sealed interface event system** to route user actions:

```kotlin
sealed interface BluetoothEvent

sealed interface GeneralBluetoothEvent : BluetoothEvent
// scan, tapToChat, sendMessage, endConnection

sealed interface ClientBluetoothEvent : BluetoothEvent
// connectToDevice
```

`BluetoothMessagingViewModel.onEvent(event)` dispatches to either `GeneralHandlerImp` or `ClientHandlerImp`, keeping the ViewModel thin.

### Event-Driven ViewModel — Network Scanner

The Network Scanner module uses a simple event model for starting and stopping scans:

```kotlin
sealed interface NetworkEvent {
    data object PerformScan : NetworkEvent
    data object StopScan : NetworkEvent
}
```

`NetworkScanViewModel.onEvent(event)` starts or cancels the scan coroutine job while exposing scan state and discovered devices through `StateFlow`.

---

## ✨ Key Technical Features

### Firebase Module

* **Firebase Authentication** — Users register and log in with Firebase Auth using email/password authentication.

* **Dual Firebase Backend** — User profiles and recent-chat metadata are stored in **Firestore**, while actual chat messages are stored in **Firebase Realtime Database** under a deterministic chat path: `min(uid, partnerUid) + "-" + max(uid, partnerUid)`.

* **Real-Time User + Message Streams** — `InternetMainPageViewModel` combines user and recent-chat streams using Kotlin Flow operators, then exposes the result as observable UI state.

* **Unseen Message Counter** — When a message is sent, the recipient's unseen-message counter is updated in Firestore. When the chat is opened, the count is reduced and messages are marked as seen.

* **Seen Status per Message** — Each `ChatMessage` has a `seen` flag that is updated when the recipient opens the conversation.

* **Profile Picture Support** — User profile images are stored with Firebase Storage and loaded into the UI with Glide.

* **Lottie Animations** — Loading and empty-state screens use Lottie animations for smoother user feedback.

### Bluetooth Module

* **Hybrid BLE Discovery + RFCOMM Messaging** — BLE advertising is used to broadcast the user's nickname, while BLE scanning is used to discover nearby peers. After discovery, actual message transfer is handled through Bluetooth Classic RFCOMM sockets.

* **Offline Peer-to-Peer Communication** — The Bluetooth module works without Firebase or internet connectivity, making it suitable for nearby device-to-device chat.

* **Role-Based Connection Management** — Each device has one runtime role: `IDLE`, `SERVER`, or `CLIENT`. Role state is tracked and used to control scan, advertise, connect, and accept behavior.

* **Custom Binary Packet Protocol** — Long messages are split into 1000-byte chunks and sent with a compact 5-byte header: `[messageID (2B)] [totalParts (1B)] [nickSize (1B)] [partIndex (1B)]`.

* **Packet Reassembly** — The receiver stores incoming chunks in an `assemblyMap` keyed by `messageID`, sorts parts by index, rebuilds the message body, and then persists it.

* **Room DB for Message Persistence** — Bluetooth chat history is stored locally in Room. Conversation records are keyed using an MD5 hash of the peer's MAC address.

* **Per-Connection Coroutine Scopes** — Each `BluetoothConnection` carries its own coroutine scope, so connection failures are isolated from the rest of the application.

* **Nickname via SharedPreferences** — The user's Bluetooth display name is saved locally and included in outgoing Bluetooth packets.

### Network Scanner Module

* **Local Wi-Fi Device Discovery** — The scanner resolves the device's local IPv4 address, derives the subnet prefix, and scans the local range for active devices.

* **Subnet-Based Scanning** — The scanner checks addresses from `.0` to `.255` in the current local subnet.

* **Multiple Reachability Strategies** — Device detection uses ping, socket connection attempts on ports `80` and `443`, and `InetAddress.isReachable()` as fallback methods.

* **Host Name Resolution** — When a reachable IP address is found, the scanner attempts to resolve a hostname. If no hostname is available, the device is displayed as an active device.

* **MAC Address Lookup** — The scanner attempts to read `/proc/net/arp` to resolve MAC addresses where Android allows access.

* **StateFlow-Based UI Updates** — `NetworkScanViewModel` exposes discovered devices through `StateFlow`, allowing the RecyclerView to update while the scan is running.

* **Duplicate Filtering** — Devices are added to the list only if the same IP address has not already been emitted.

* **Timed Scan UI** — `ScanNetworkFragment` starts a scan animation and stops scanning after a fixed 10-second scan duration.

---

## 🛠 Tech Stack & Dependencies

| Category                 | Library / Tool                               | Version                   |
| ------------------------ | -------------------------------------------- | ------------------------- |
| **Language**             | Kotlin                                       | 2.0.21                    |
| **Build System**         | Gradle KTS + KSP                             | AGP 8.10.1                |
| **Min / Target SDK**     | API 26 / API 36                              | Android 8.0 – Android 14+ |
| **Firebase BOM**         | firebase-bom                                 | 34.6.0                    |
| **Firebase Auth**        | firebase-auth                                | via BOM                   |
| **Firebase Realtime DB** | firebase-database                            | via BOM                   |
| **Firebase Firestore**   | firebase-firestore                           | via BOM                   |
| **Firebase Storage**     | firebase-storage                             | via BOM                   |
| **Dependency Injection** | Hilt / Dagger                                | 2.56                      |
| **Navigation**           | Navigation Component + Safe Args             | 2.9.6                     |
| **Local Database**       | Room                                         | 2.8.4                     |
| **Preferences**          | DataStore Preferences / SharedPreferences    | 1.2.1 / Android API       |
| **Image Loading**        | Glide                                        | 5.0.5                     |
| **Animations**           | Lottie                                       | 6.7.1                     |
| **Architecture**         | MVVM + Repository + LiveData + StateFlow     | —                         |
| **Async**                | Kotlin Coroutines + Flow                     | —                         |
| **UI**                   | XML ViewBinding + RecyclerView + ListAdapter | —                         |
| **Network Scanning**     | WifiManager + InetAddress + Socket probing   | Android API               |

---

## 📂 Project Structure

```text
HChat/
├── app/
│   ├── build.gradle.kts                  # App dependencies and Android build config
│   └── src/main/
│       ├── AndroidManifest.xml           # App permissions, features, and activity declaration
│       └── java/com/example/chatapp/
│           │
│           ├── MainActivity.kt           # Single-activity host with NavController
│           ├── MainMenu.kt               # Module selector: Firebase, Bluetooth, Network Scanner
│           │
│           ├── hilt/
│           │   ├── HiltAndroidApp.kt     # @HiltAndroidApp entry point
│           │   └── Module.kt             # @Singleton providers: BluetoothAdapter,
│           │                             # BluetoothRepo, Room DB, DAOs, handlers,
│           │                             # WifiManager, NetworkScanRepo
│           │
│           ├── bluetooth/
│           │   ├── adapters/
│           │   │   ├── BluetoothChatMessageAdapter.kt
│           │   │   └── BluetoothUserListAdapter.kt
│           │   │
│           │   ├── data/
│           │   │   ├── entity/
│           │   │   │   ├── BluetoothConnection.kt       # Socket, streams, and CoroutineScope
│           │   │   │   ├── BluetoothDeviceListItem.kt   # Discovered Bluetooth device item
│           │   │   │   ├── BluetoothMessage.kt          # Room entity for Bluetooth messages
│           │   │   │   └── ObjectConstants.kt           # UUID, buffer size, DB name, role codes
│           │   │   │
│           │   │   └── repo/
│           │   │       ├── BluetoothRepo.kt             # BLE scan/advertise, RFCOMM lifecycle,
│           │   │       │                                # packet assembly, Room writes
│           │   │       ├── client/
│           │   │       │   ├── ClientHandler.kt
│           │   │       │   └── ClientHandlerImp.kt      # ConnectToDevice event handler
│           │   │       └── general/
│           │   │           ├── GeneralHandler.kt
│           │   │           └── GeneralHandlerImp.kt     # Scan, TapToChat, Send, End handlers
│           │   │
│           │   ├── event/
│           │   │   ├── BluetoothEvent.kt                # Sealed Bluetooth event hierarchy
│           │   │   └── DeviceRole.kt                    # IDLE, SERVER, CLIENT role model
│           │   │
│           │   ├── fragments/
│           │   │   ├── BluetoothMainPage.kt             # Bluetooth device discovery UI
│           │   │   └── BluetoothChatPage.kt             # Bluetooth chat UI
│           │   │
│           │   ├── room/
│           │   │   ├── BluetoothDao.kt                  # Chat history queries and upserts
│           │   │   └── RoomDatabaseForBluetooth.kt      # Room database definition
│           │   │
│           │   ├── util/
│           │   │   ├── bluetoothMessageFormatter.kt     # Message formatting helpers
│           │   │   ├── createBluetoothItem.kt           # Bluetooth list item builder
│           │   │   ├── hasher.kt                        # MD5 MAC address hashing
│           │   │   ├── permissionDataHandler.kt         # Bluetooth permission logic
│           │   │   └── saveName.kt                      # SharedPreferences nickname store
│           │   │
│           │   └── viewmodel/
│           │       └── BluetoothMessagingViewModel.kt   # Bluetooth UI state and events
│           │
│           ├── internet/
│           │   ├── adapters/
│           │   │   ├── InternetChatMessageAdapter.kt
│           │   │   └── InternetUserListAdapter.kt
│           │   │
│           │   ├── entity/
│           │   │   ├── ChatMessage.kt                   # Firebase Realtime DB message model
│           │   │   └── UserListItem.kt                  # Firestore user + recent chat model
│           │   │
│           │   ├── fragments/
│           │   │   ├── LoginPage.kt                     # Firebase login screen
│           │   │   ├── SignUpPage.kt                    # Firebase registration screen
│           │   │   ├── InternetMainPage.kt              # User list and recent chats
│           │   │   └── InternetChatPage.kt              # Real-time Firebase chat screen
│           │   │
│           │   ├── util/
│           │   │   └── dateFormatter.kt                 # Chat timestamp formatting
│           │   │
│           │   └── viewmodels/
│           │       ├── SignUpViewModel.kt               # Registration logic
│           │       ├── InternetMainPageViewModel.kt     # User list + recent-chat flow logic
│           │       └── InternetChatPageViewModel.kt     # Message send/listen + seen logic
│           │
│           └── scan/
│               ├── adapters/
│               │   └── NetworkScanAdapter.kt            # RecyclerView adapter for scan results
│               │
│               ├── data/
│               │   ├── entity/
│               │   │   └── ScanResultObject.kt          # IP, hostname, and MAC result model
│               │   ├── repo/
│               │   │   └── NetworkScanRepo.kt           # Local IP resolution and subnet scan
│               │   └── state/
│               │       └── NetworkScanState.kt          # Scan state holder
│               │
│               ├── fragment/
│               │   └── ScanNetworkFragment.kt           # Network scanner UI and scan timer
│               │
│               └── viewmodel/
│                   └── NetworkScanViewModel.kt           # Scan events, coroutine job, StateFlow list
│
├── gradle/
│   └── libs.versions.toml                              # Version catalog
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 🔄 Data Flow

### Firebase Module — Sending a Message

```text
User sends message
       │
InternetChatPageViewModel.addMessageToChat()
       │
  ┌────┴──────────────────────────────────────┐
  │  1. Build ChatMessage object              │
  │  2. Push message to Realtime Database      │
  │     /chats/{minUID}-{maxUID}/{pushKey}    │
  │  3. Update recent-chat metadata            │
  │     in Firestore                           │
  │  4. Increment recipient unseen counter     │
  └───────────────────────────────────────────┘
       │
Realtime Database listener
       │
ViewModel chat state
       │
RecyclerView UI
```

### Firebase Module — Reading Recent Chats

```text
InternetMainPageViewModel
       │
       ├── listens to all users
       │
       └── listens to recent_chats metadata
              │
        combine() user + recent chat streams
              │
        sort by latest timestamp
              │
        expose List<UserListItem>
              │
        InternetMainPage RecyclerView
```

### Bluetooth Module — Sending a Message

```text
User types message
       │
BluetoothChatPage.kt ──onEvent(SendMessage)──► BluetoothMessagingViewModel
       │
GeneralHandlerImp.handleGeneralEvents()
       │
BluetoothRepo.sendMessage()
       │
  ┌────┴──────────────────────────────────┐
  │  1. Convert message to byte payload    │
  │  2. Chunk message into 1000B parts     │
  │  3. Build 5-byte header per chunk      │
  │     [msgID][parts][nickSize][partIdx]  │
  │  4. Write chunks to RFCOMM stream      │
  │  5. Upsert message to Room DB          │
  └───────────────────────────────────────┘
       │
Room Flow
       │
BluetoothMessagingViewModel._messageList
       │
BluetoothChatPage RecyclerView
```

### Bluetooth Module — Receiving a Message

```text
ConnectedThread reads inputStream
       │
Parse packet header
       │
assemblyMap[messageID].add(packet)
       │
When all message parts arrive:
       │
sort by partIndex
       │
organizeBytesIntoMessageItem()
       │
BluetoothDao.upsertMessage()
       │
Room Flow ──► ViewModel ──► UI
```

### Network Scanner Module — Scanning Local Network

```text
User taps scan
       │
ScanNetworkFragment
       │
NetworkScanViewModel.onEvent(NetworkEvent.PerformScan)
       │
NetworkScanRepo.scanNetwork()
       │
  ┌────┴──────────────────────────────────────────────┐
  │  1. Resolve local IPv4 address                    │
  │  2. Build subnet prefix from local IP             │
  │  3. Probe addresses from subnet.0 to subnet.255   │
  │  4. Check reachability with ping/socket/fallback  │
  │  5. Resolve hostname where possible               │
  │  6. Try to resolve MAC address from ARP table     │
  │  7. Emit ScanResultObject                         │
  └───────────────────────────────────────────────────┘
       │
StateFlow<List<ScanResultObject>>
       │
NetworkScanAdapter
       │
RecyclerView UI
```

---

## 📦 Installation

### Prerequisites

* Android Studio **Iguana (2023.2.1)** or newer
* JDK 11 or newer
* Physical Android device running **API 26+**
* A configured Firebase project for the Firebase module
* Wi-Fi connection for the Network Scanner module
* Two physical Android devices for end-to-end Bluetooth testing

> ⚠️ Bluetooth functionality requires real Android Bluetooth hardware. Emulators are not suitable for full Bluetooth testing.

### Steps

1. **Clone the repository:**

   ```bash
   git clone https://github.com/mustafaerendalgic/HChat.git
   cd HChat
   ```

2. **Open in Android Studio:**

   Select **File → Open** and choose the cloned project folder.

3. **Add Firebase config:**

   Place your `google-services.json` file inside the `app/` directory.

4. **Sync Gradle:**

   Android Studio should prompt you to sync. Click **Sync Now**.

5. **Build and run:**

   Connect a physical Android device and press **Run ▶**.

6. **Module-specific testing:**

   * Use the Firebase module with an active internet connection.
   * Use the Bluetooth module with two physical Android devices.
   * Use the Network Scanner module while connected to a Wi-Fi network.

---

## 🔥 Firebase Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.

2. Add an **Android app** with package name:

   ```text
   com.example.chatapp
   ```

3. Download `google-services.json`.

4. Place `google-services.json` inside the `app/` directory.

5. Enable the following Firebase services:

### Authentication

Enable **Email/Password** sign-in.

### Firestore

Create a `users` collection. For development only, you can use:

```json
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Realtime Database

Realtime Database stores chat messages. For development only, you can use:

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

### Storage

Firebase Storage is used for profile pictures. For development only, you can use:

```text
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

> ⚠️ The sample Firebase rules above are suitable for development and testing. They should be tightened before production deployment.

---

## 🗄 Firestore Data Model

```text
users/
  {uid}/
    nickname: String
    profile_pic: String

    recent_chats/
      {partnerUid}/
        lastMessage: String
        lastMessageBy: String
        timestamp: Timestamp
        unseenMessageCount: Int
```

Realtime Database message path:

```text
/chats/{minUID}-{maxUID}/
  {pushKey}/
    messageID: String
    senderUID: String
    message: String
    date: String
    seen: Boolean
    nickname: String
```

---

## 🔐 Permissions

The app uses different permissions depending on the selected module and Android API level.

| Permission                   | Purpose                                                    | API Scope        |
| ---------------------------- | ---------------------------------------------------------- | ---------------- |
| `INTERNET`                   | Firebase connectivity and online messaging                 | All              |
| `ACCESS_NETWORK_STATE`       | Check network connectivity state                           | All              |
| `ACCESS_WIFI_STATE`          | Read Wi-Fi connection information for local IP discovery   | All              |
| `BLUETOOTH`                  | Basic Bluetooth operations                                 | maxSdkVersion 30 |
| `BLUETOOTH_ADMIN`            | Device discovery and pairing                               | maxSdkVersion 30 |
| `ACCESS_FINE_LOCATION`       | Required for Bluetooth scanning on legacy Android versions | maxSdkVersion 30 |
| `ACCESS_COARSE_LOCATION`     | Required for Bluetooth scanning on legacy Android versions | maxSdkVersion 30 |
| `ACCESS_BACKGROUND_LOCATION` | Background Bluetooth scanning on legacy Android versions   | maxSdkVersion 30 |
| `BLUETOOTH_SCAN`             | BLE scanning on Android 12+                                | API 31+          |
| `BLUETOOTH_CONNECT`          | Connect to discovered Bluetooth devices on Android 12+     | API 31+          |
| `BLUETOOTH_ADVERTISE`        | BLE advertising on Android 12+                             | API 31+          |

The app also declares Bluetooth Low Energy hardware support through:

```xml
<uses-feature
    android:name="android.hardware.bluetooth_le"
    android:required="true" />
```

---

## ⚙️ How It Works

### Firebase Module

1. The user registers or logs in with **Firebase Authentication**.
2. User profile data is stored in **Firestore** under `/users/{uid}`.
3. The main internet chat screen loads users and recent-chat metadata.
4. Opening a chat starts a **Realtime Database** listener.
5. New messages are pushed to `/chats/{minUID}-{maxUID}`.
6. Firestore recent-chat metadata is updated after each sent message.
7. Unseen-message counters and seen flags are updated when the recipient opens the conversation.
8. The UI updates automatically through Firebase listeners and ViewModel state.

### Bluetooth Module

#### Discovery Phase — BLE

Both devices can advertise and scan while in `IDLE` mode. The advertiser includes the user's nickname as BLE service data, allowing nearby devices to show a readable name in the Bluetooth device list.

#### Connection Phase — RFCOMM

One device acts as the server and listens for an incoming RFCOMM connection. The other device acts as the client and initiates the socket connection.

```text
Device A                                Device B
(Server)                                (Client)

BLE advertise + RFCOMM listen()         BLE scan + discover A
        │                                      │
        │◄──────── RFCOMM connect() ──────────│
        │                                      │
        ╔══════════════════════════════════════╗
        ║ BluetoothConnection established      ║
        ║ socket + inputStream + outputStream  ║
        ╚══════════════════════════════════════╝
        │                                      │
        │◄════════ ConnectedThread R/W ═══════►│
```

#### Messaging Phase

* Messages are converted to byte arrays.
* Large messages are chunked into smaller packet parts.
* Each packet carries a custom header.
* The receiver reassembles packets using `messageID`.
* Completed messages are saved into Room.
* The chat screen observes Room data and updates the RecyclerView.

### Network Scanner Module

1. The user opens the Network Scanner from the MainMenu.
2. `ScanNetworkFragment` initializes the RecyclerView and scan animation.
3. The user taps the scan button or scan text.
4. `NetworkScanViewModel` receives `NetworkEvent.PerformScan`.
5. `NetworkScanRepo` resolves the device's local IPv4 address.
6. The subnet prefix is derived from the local IP address.
7. The scanner probes addresses from `.0` to `.255`.
8. Reachable devices are emitted as `ScanResultObject`.
9. The ViewModel filters duplicate IP addresses.
10. The RecyclerView displays detected IP address, hostname, and MAC address when available.
11. After the timed scan completes, `NetworkEvent.StopScan` cancels the scan job and preserves the displayed results.

---

## ⚠️ Known Limitations & Potential Improvements

* **Single active Bluetooth connection** — The current Bluetooth role system is designed around one active peer connection at a time.

* **No Bluetooth delivery confirmation** — The Bluetooth module does not implement message ACKs or retry logic for dropped packets.

* **No application-layer end-to-end encryption** — Bluetooth RFCOMM may benefit from Android Bluetooth link-layer protection after pairing, but message payloads are not encrypted at the application layer.

* **Firebase rules are open for development** — The sample Firebase rules allow broad authenticated access and should be restricted before production use.

* **Release build minification disabled** — `isMinifyEnabled = false` is suitable for debugging, but ProGuard/R8 rules should be configured before release.

* **Network scanner is local-network only** — The scanner is intended for devices on the current Wi-Fi subnet and does not scan external networks.

* **Subnet assumption** — The current scanner derives a `/24`-style subnet and scans `.0` to `.255`.

* **MAC address availability is limited** — Modern Android versions may restrict or hide ARP table data, so MAC addresses can appear as unavailable.

* **Reachability checks are best-effort** — Some devices block ping or ports `80` and `443`, so not every active device may respond.

* **No persistent network scan history** — Network scan results are displayed in memory and are not currently saved to a database.

---

## 👤 Author

**Mustafa Eren Dalgıç**

* GitHub: [@mustafaerendalgic](https://github.com/mustafaerendalgic)
* Çukurova University — Department of Computer Engineering

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<p align="center">Made as a Graduation Thesis at Çukurova University 🎓</p>

