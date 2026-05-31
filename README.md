# 📱 ChatApp — Hybrid Android Messaging Platform

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)
![Bluetooth](https://img.shields.io/badge/Bluetooth-0082FC?style=for-the-badge&logo=bluetooth&logoColor=white)
![Room](https://img.shields.io/badge/Room_DB-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-DI-orange?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)

> **Graduation Thesis Project** — Çukurova University, Department of Computer Engineering

**ChatApp** is a hybrid Android messaging application that combines two fully independent communication modules: **Firebase** for cloud-based real-time messaging and **Bluetooth Classic RFCOMM** for offline peer-to-peer communication — no internet required. The app dynamically manages BLE advertising, RFCOMM socket connections, a local Room database for chat persistence, and real-time Firestore/Realtime Database sync, all wired together with Hilt dependency injection and MVVM architecture.

> ⚠️ This project was developed as a graduation thesis at Çukurova University.
> Feel free to use it for learning purposes. If you reference it in academic work, please cite appropriately.

---

## 📑 Table of Contents

- [Project Overview](#-project-overview)
- [Screenshots](#-screenshots)
- [Architecture](#-architecture)
- [Key Technical Features](#-key-technical-features)
- [Tech Stack & Dependencies](#-tech-stack--dependencies)
- [Project Structure](#-project-structure)
- [Data Flow](#-data-flow)
- [Installation](#-installation)
- [Firebase Setup](#-firebase-setup)
- [Firestore Data Model](#-firestore-data-model)
- [Permissions](#-permissions)
- [How It Works](#-how-it-works)
- [Known Limitations](#-known-limitations)
- [Author](#-author)
- [License](#-license)

---

## 📋 Project Overview

The app launches to a **MainMenu** where the user picks a communication mode. Each mode is completely isolated — separate data sources, ViewModels, fragments, and adapters.

| Module | Technology | Use Case |
|---|---|---|
| **Firebase Module** | Firestore + Realtime Database + Firebase Auth | Internet-based real-time chat |
| **Bluetooth Module** | BLE Advertising + RFCOMM Classic Sockets + Room DB | Offline P2P chat with message persistence |

---

## 📸 Screenshots

### Firebase Module

| Login | User List | Chat |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/215fee3a-0a90-48b6-ac1e-12c96458c5ed" width="200"/> | <img src="https://github.com/user-attachments/assets/3cc7e437-cdd6-4d76-bf71-c2b242315333" width="200"/> | <img src="https://github.com/user-attachments/assets/17fa258b-aa87-44a9-8c4e-227ece0d30ff" width="200"/> |

### Bluetooth Module

| Device Scan | Chat |
| :---: | :---: |
| <img src="https://github.com/user-attachments/assets/7a2bc9b2-2883-413b-99c9-7d5661e9b0e2" width="200"/> | <img src="https://github.com/user-attachments/assets/8f28c0ba-00ec-49f3-b4ed-262eccfe32d5" width="200"/> |

---

## 🏛 Architecture

ChatApp follows **MVVM** with a Repository pattern. The two modules share no state — each has its own ViewModel(s), repository/data layer, and UI fragments.

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                          │
│   MainMenu ──► InternetModule  /  BluetoothModule        │
│                  Fragments + XML ViewBinding             │
└──────────────────────┬──────────────────────────────────┘
                       │  observes StateFlow / LiveData
┌──────────────────────▼──────────────────────────────────┐
│                    ViewModel Layer                        │
│  InternetChatPageViewModel   BluetoothMessagingViewModel │
│  InternetMainPageViewModel   (StateFlow + SharedFlow)    │
│  SignUpViewModel                                         │
└──────────┬───────────────────────────┬──────────────────┘
           │                           │
┌──────────▼──────────┐   ┌────────────▼────────────────┐
│   Firebase Stack    │   │      Bluetooth Stack         │
│                     │   │                              │
│  FirebaseAuth       │   │  BluetoothRepo               │
│  FirebaseFirestore  │   │  ├─ BLE Advertiser/Scanner   │
│  FirebaseRealtime   │   │  ├─ RFCOMM ServerSocket      │
│    Database         │   │  ├─ RFCOMM ClientSocket      │
│  FirebaseStorage    │   │  ├─ ConnectedThread (R/W)    │
└─────────────────────┘   │  └─ Room DAO                 │
                          └─────────────────────────────-┘
```

### Event-Driven ViewModel (Bluetooth)

The Bluetooth module uses a **sealed interface event system** to route user actions:

```kotlin
sealed interface BluetoothEvent
sealed interface GeneralBluetoothEvent : BluetoothEvent  // scan, tapToChat, sendMessage, endConnection
sealed interface ClientBluetoothEvent  : BluetoothEvent  // connectToDevice
```

`BluetoothMessagingViewModel.onEvent(event)` dispatches to either `GeneralHandlerImp` or `ClientHandlerImp`, keeping the ViewModel thin.

---

## ✨ Key Technical Features

### Bluetooth Module

- **Hybrid BLE Discovery + RFCOMM Connection** — Uses BLE advertising (`BluetoothLeAdvertiser`) to broadcast the user's nickname via `ServiceData`, and BLE scanning (`BluetoothLeScanner`) to discover peers. Once a peer is identified, a classic **RFCOMM socket** is used for the actual data channel.

- **Role-Based Connection Management** — Each device has one of three roles at runtime: `IDLE`, `SERVER`, or `CLIENT` (tracked with an `AtomicInteger`). Role is set when a connection is accepted or initiated and drives how scan/advertise events are handled.

- **Custom Binary Packet Protocol** — Messages are split into 1000-byte chunks and sent with a custom 5-byte header: `[messageID (2B)] [totalParts (1B)] [nickSize (1B)] [partIndex (1B)]`. The receiver reassembles packets in an `assemblyMap` keyed by `messageID` before persisting the full message.

- **Room DB for Message Persistence** — All Bluetooth chat history is persisted locally in a Room database (`message_table`). Chat history is keyed by an **MD5 hash of the peer's MAC address**, ensuring conversations survive app restarts.

- **Per-Connection Coroutine Scopes** — Each `BluetoothConnection` carries its own `CoroutineScope(Dispatchers.IO + SupervisorJob())`, so connection failures are isolated and don't affect other active connections.

- **Nickname via SharedPreferences** — The user's display name is persisted with `SharedPreferences` (via `saveName` / `getNameFromMemory` utils) and included in every Bluetooth packet header.

### Firebase Module

- **Dual Firebase Backend** — User profiles and recent-chat metadata (last message, unseen count, timestamps) live in **Firestore**. Actual chat messages are stored in **Firebase Realtime Database** under a deterministic filename: `min(uid, partnerUid) + "-" + max(uid, partnerUid)`.

- **Real-Time User + Message Streams via `callbackFlow`** — `InternetMainPageViewModel` merges two Firestore/RTDB snapshot listeners into a single `LiveData<List<UserListItem>>` using `combine`, sorting the list by most recent message timestamp.

- **Unseen Message Counter** — When a message is sent, the partner's `unseenMessageCount` field in Firestore is incremented atomically. When the chat is opened, `updateSeenCount()` decrements it with a Firestore `FieldValue.increment`.

- **Seen Status per Message** — Each `ChatMessage` in the Realtime Database has a `seen` boolean that is updated via `updateSeenStatus()` once the recipient opens the chat.

- **Glide for Profile Pictures** — User avatars are loaded from Firebase Storage URLs using Glide 5.

- **Lottie Animations** — Loading and empty-state animations are handled by the Lottie library.

---

## 🛠 Tech Stack & Dependencies

| Category | Library / Tool | Version |
|---|---|---|
| **Language** | Kotlin | 2.0.21 |
| **Build System** | Gradle KTS + KSP | AGP 8.10.1 |
| **Min / Target SDK** | API 26 / API 36 | Android 8.0 – Android 14 |
| **Firebase BOM** | firebase-bom | 34.6.0 |
| **Firebase Auth** | firebase-auth | via BOM |
| **Firebase Realtime DB** | firebase-database | via BOM |
| **Firebase Firestore** | firebase-firestore | via BOM |
| **Firebase Storage** | firebase-storage | via BOM |
| **Dependency Injection** | Hilt (Dagger) | 2.56 |
| **Navigation** | Navigation Component + Safe Args | 2.9.6 |
| **Local Database** | Room | 2.8.4 |
| **Preferences** | DataStore Preferences | 1.2.1 |
| **Image Loading** | Glide | 5.0.5 |
| **Animations** | Lottie | 6.7.1 |
| **Architecture** | MVVM + LiveData + StateFlow + Repository | — |
| **Async** | Kotlin Coroutines | — |
| **UI** | ViewBinding + ListAdapter + RecyclerView | — |

---

## 📂 Project Structure

```
chatApp/
├── app/
│   ├── build.gradle.kts                  # All dependencies & build config
│   └── src/main/java/com/example/chatapp/
│       │
│       ├── MainActivity.kt               # Single-activity host (NavController)
│       │
│       ├── hilt/
│       │   ├── HiltAndroidApp.kt         # @HiltAndroidApp entry point
│       │   └── Module.kt                 # @Singleton providers: BluetoothAdapter,
│       │                                 #   BluetoothRepo, Room DB, DAOs, Handlers
│       │
│       ├── bluetooth/
│       │   ├── adapters/
│       │   │   ├── BluetoothChatMessageAdapter.kt
│       │   │   └── BluetoothUserListAdapter.kt
│       │   ├── data/
│       │   │   ├── entity/
│       │   │   │   ├── BluetoothConnection.kt   # Socket + streams + CoroutineScope
│       │   │   │   ├── BluetoothDeviceListItem.kt
│       │   │   │   ├── BluetoothMessage.kt      # @Entity for Room (composite PK)
│       │   │   │   └── ObjectConstants.kt       # UUID, buffer size, DB name, role codes
│       │   │   └── repo/
│       │   │       ├── BluetoothRepo.kt          # BLE scan/advertise, RFCOMM lifecycle,
│       │   │       │                             #   packet assembly, Room writes
│       │   │       ├── client/
│       │   │       │   ├── ClientHandler.kt
│       │   │       │   └── ClientHandlerImp.kt   # ConnectToDevices event handler
│       │   │       └── general/
│       │   │           ├── GeneralHandler.kt
│       │   │           └── GeneralHandlerImp.kt  # Scan, TapToChat, Send, End handlers
│       │   ├── event/
│       │   │   ├── BluetoothEvent.kt             # Sealed interface hierarchy
│       │   │   └── DeviceRole.kt
│       │   ├── fragments/
│       │   │   ├── BluetoothMainPage.kt          # Device discovery UI
│       │   │   └── BluetoothChatPage.kt          # Bluetooth chat UI
│       │   ├── room/
│       │   │   ├── BluetoothDao.kt               # getTheChatHistory, upsert, delete
│       │   │   └── RoomDatabaseForBluetooth.kt
│       │   ├── util/
│       │   │   ├── bluetoothMessageFormatter.kt
│       │   │   ├── createBluetoothItem.kt
│       │   │   ├── hasher.kt                     # MD5 MAC address hashing
│       │   │   ├── permissionDataHandler.kt      # API 31+ permission logic
│       │   │   └── saveName.kt                   # SharedPreferences nickname store
│       │   └── viewmodel/
│       │       └── BluetoothMessagingViewModel.kt
│       │
│       └── internet/
│           ├── adapters/
│           │   ├── InternetChatMessageAdapter.kt
│           │   └── InternetUserListAdapter.kt
│           ├── entity/
│           │   ├── ChatMessage.kt                # Firebase Realtime DB message model
│           │   └── UserListItem.kt               # Firestore user + recent chat model
│           ├── fragments/
│           │   ├── LoginPage.kt
│           │   ├── SignUpPage.kt
│           │   ├── InternetMainPage.kt           # User list with last message + unseen count
│           │   ├── InternetChatPage.kt           # Real-time chat with seen tracking
│           │   └── MainMenu.kt                   # Module selector
│           ├── util/
│           │   └── dateFormatter.kt
│           └── viewmodels/
│               ├── SignUpViewModel.kt
│               ├── InternetMainPageViewModel.kt  # Combines allUsers + recentChats flows
│               └── InternetChatPageViewModel.kt  # Message send/listen + seen management
│
├── gradle/
│   └── libs.versions.toml                        # Version catalog
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🔄 Data Flow

### Bluetooth Module — Sending a Message

```
User types message
       │
BluetoothChatPage.kt ──onEvent(SendMessage)──► BluetoothMessagingViewModel
       │
GeneralHandlerImp.handleGeneralEvents()
       │
BluetoothRepo.sendMessage()
       │
  ┌────┴──────────────────────────────────┐
  │  1. Chunk message into 1000B parts    │
  │  2. Build 5-byte header per chunk:    │
  │     [msgID(2B)][parts(1B)]            │
  │     [nickSize(1B)][partIdx(1B)]       │
  │  3. Write to BluetoothConnection      │
  │     .outputStream                     │
  │  4. Upsert BluetoothMessage to Room   │
  └───────────────────────────────────────┘
       │
Room DB ──(Flow)──► ViewModel._messageList ──► UI RecyclerView
```

### Bluetooth Module — Receiving a Message

```
ConnectedThread reads inputStream
       │
  assemblyMap[messageID].add(packet)
       │
  When lastPacketList.size == totalParts:
       │
  organizeBytesIntoMessageItem()
  (sort by partIndex, extract nick + body)
       │
  BluetoothDao.upsertMessage()
       │
Room Flow ──► ViewModel ──► UI
```

### Firebase Module — Sending a Message

```
User sends message
       │
InternetChatPageViewModel.addMessageToChat()
       │
  ┌────┴──────────────────────────────────────┐
  │  1. Push ChatMessage to RTDB              │
  │     /chats/{minUID}-{maxUID}/{pushKey}    │
  │  2. Batch-update Firestore:               │
  │     /users/{uid}/recent_chats/{partner}   │
  │     → lastMessage, lastMessageBy,         │
  │       timestamp, unseenMessageCount+1     │
  └───────────────────────────────────────────┘
       │
RTDB ValueEventListener ──► ViewModel._chat ──► UI
```

---

## 📦 Installation

### Prerequisites

- Android Studio **Iguana (2023.2.1)** or newer
- JDK 11
- Physical Android device running **API 26+** (required for Bluetooth testing — emulators don't support BT hardware)
- A configured Firebase project (see [Firebase Setup](#-firebase-setup))

### Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/mustafaerendalgic/chatApp.git
   cd chatApp
   ```

2. **Open in Android Studio:**
   Select **File → Open** and point to the cloned folder.

3. **Add your Firebase config:**
   Place `google-services.json` inside the `app/` directory (see below).

4. **Sync Gradle:**
   Android Studio will prompt you — click **Sync Now**.

5. **Build & Run:**
   Connect a physical device and press **Run ▶** (`Shift + F10`).

> ⚠️ **Bluetooth requires a physical device.** Test with two real Android phones for end-to-end Bluetooth verification.

---

## 🔥 Firebase Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Add an **Android app** with package name `com.example.chatapp`.
3. Download `google-services.json` and place it in `app/`.
4. Enable the following services:

**Authentication** — Enable Email/Password sign-in.

**Firestore** — Create a `users` collection. Set development rules:
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

**Realtime Database** — Used for messages. Set development rules:
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

**Storage** — Used for profile pictures. Set development rules:
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## 🗄 Firestore Data Model

```
users/
  {uid}/
    nickname: String
    profile_pic: String           # Firebase Storage URL

    recent_chats/
      {partnerUid}/
        lastMessage: String
        lastMessageBy: String     # uid of sender
        timestamp: Timestamp
        unseenMessageCount: Int
```

```
/chats/{minUID}-{maxUID}/
  {pushKey}/
    messageID: String
    senderUID: String
    message: String
    date: String
    seen: Boolean (default "0")
    nickname: String
```

---

## 🔐 Permissions

All permissions are handled by `permissionDataHandler.kt` with graceful fallback between legacy and modern APIs.

| Permission | Purpose | API Scope |
|---|---|---|
| `INTERNET` | Firebase connectivity | All |
| `BLUETOOTH` | Basic Bluetooth operations | maxSdkVersion 30 |
| `BLUETOOTH_ADMIN` | Device discovery & pairing | maxSdkVersion 30 |
| `ACCESS_FINE_LOCATION` | Required for BT scanning (legacy) | maxSdkVersion 30 |
| `ACCESS_COARSE_LOCATION` | Required for BT scanning (legacy) | maxSdkVersion 30 |
| `ACCESS_BACKGROUND_LOCATION` | Background BT scanning (legacy) | maxSdkVersion 30 |
| `BLUETOOTH_SCAN` | BLE scan (`neverForLocation`) | API 31+ |
| `BLUETOOTH_CONNECT` | Connect to discovered devices | API 31+ |
| `BLUETOOTH_ADVERTISE` | BLE advertising (discoverable) | API 31+ |

The app also declares `android.hardware.bluetooth_le` as a required hardware feature.

---

## ⚙️ How It Works

### Firebase Module

1. User registers/logs in via **Firebase Authentication**.
2. On login, the user is registered in **Firestore** under `/users/{uid}`.
3. `InternetMainPageViewModel` combines two Flows — all users and recent chats — using `combine()`, producing a sorted `LiveData<List<UserListItem>>` with unseen message badges.
4. Opening a chat starts a **Realtime Database listener**. Messages arrive as a `DataSnapshot` and are rendered in real-time.
5. Each message sent is pushed to RTDB and the partner's `unseenMessageCount` is incremented in a **Firestore batch write**.
6. On opening a chat, `updateSeenCount()` decrements the counter and `updateSeenStatus()` marks individual messages as seen.

### Bluetooth Module

#### Discovery Phase (BLE)

Both devices advertise and scan simultaneously in `IDLE` mode. The advertiser includes the user's nickname as `ServiceData` in the BLE advertisement packet, keyed by a fixed `ParcelUuid`. The scanner parses this to display human-readable device names in the list.

#### Connection Phase (RFCOMM)

One device acts as **server** (`AcceptThread`), one as **client** (`ConnectThread`). Role assignment is automatic: the first accepted connection makes the device a server; the first outgoing connection makes it a client.

```
Device A (Server)                  Device B (Client)
        │                                  │
BLE advertise + RFCOMM listen()    BLE scan + discover A
        │                                  │
        │◄────── RFCOMM connect() ─────────│
        │                                  │
        ╔══════════════════════════════════╗
        ║   BluetoothConnection established║
        ║   (socket + inputStream          ║
        ║    + outputStream + scope)       ║
        ╚══════════════════════════════════╝
        │                                  │
        │◄══ ConnectedThread read loop ═══►│
```

#### Messaging Phase

- Messages are chunked into ≤1000-byte parts with a 5-byte binary header.
- The receiver reassembles parts using an `assemblyMap<messageID, List<ByteArray>>`.
- Completed messages are upserted into Room via `BluetoothDao.updateMessage()`.
- The UI observes the Room `Flow<List<BluetoothMessage>>` through `ViewModel._messageList`.
- Chat history is keyed by `MD5(peerMacAddress)`, making it addressable and persistent.

---

## ⚠️ Known Limitations & Potential Improvements

- **Single active Bluetooth connection** — The current role system supports one peer connection at a time. The server accepts one client and breaks out of the accept loop.
- **No message delivery confirmation** — The Bluetooth module does not implement ACKs; packet loss on a dropped connection is not retried.
- **Release build minification disabled** — `isMinifyEnabled = false` in the release build type. ProGuard rules should be added before production release.
- **Firebase rules are open** — The sample rules allow any authenticated user to read/write all data. Tighten rules before deploying to production.
- **No end-to-end encryption** — Bluetooth RFCOMM communication benefits from underlying link-layer encryption provided by the Android Bluetooth stack after pairing. However, message payloads are not encrypted at the application layer. Implementing end-to-end encryption (e.g., using a shared key or public-key exchange) would ensure message confidentiality independent of the transport layer.

---

## 👤 Author

**Mustafa Eren Dalgıç**
- GitHub: [@mustafaerendalgic](https://github.com/mustafaerendalgic)
- Çukurova University — Department of Computer Engineering

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<p align="center">Made as a Graduation Thesis at Çukurova University 🎓</p>
