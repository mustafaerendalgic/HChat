# 📱 ChatApp — Hybrid Android Messaging Platform

[![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)](https://firebase.google.com)
[![Bluetooth](https://img.shields.io/badge/Bluetooth-0082FC?style=for-the-badge&logo=bluetooth&logoColor=white)](https://developer.android.com/guide/topics/connectivity/bluetooth)
[![Room](https://img.shields.io/badge/Room_DB-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Hilt](https://img.shields.io/badge/Hilt-DI-orange?style=for-the-badge)](https://dagger.dev/hilt/)
[![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)](#-license)

> **Graduation Thesis Project** — Çukurova University, Department of Computer Engineering

**ChatApp** is a hybrid Android messaging application that combines two fully independent communication modules: **Firebase** for cloud-based real-time messaging and **Bluetooth (BLE discovery + L2CAP data channel)** for offline peer-to-peer communication — no internet required. The app dynamically manages BLE advertising/scanning, L2CAP Connection-Oriented Channel (CoC) sockets, a local Room database for chat persistence, and real-time Firestore/Realtime Database sync, all wired together with Hilt dependency injection and MVVM architecture.

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
- [How It Works](#️-how-it-works)
- [Known Limitations](#️-known-limitations--potential-improvements)
- [Author](#-author)
- [License](#-license)

---

## 📋 Project Overview

The app launches to a **MainMenu** where the user picks a communication mode. Each mode is completely isolated — separate data sources, ViewModels, fragments, and adapters.

| Module               | Technology                                                     | Use Case                                  |
| -------------------- | --------------------------------------------------------------- | ------------------------------------------ |
| **Firebase Module**  | Firestore + Realtime Database + Firebase Auth                  | Internet-based real-time chat             |
| **Bluetooth Module** | BLE Advertising/Scanning + L2CAP CoC Sockets + Room DB          | Offline P2P chat with message persistence |

> **Migration note:** the Bluetooth transport was migrated from **RFCOMM** to **L2CAP Connection-Oriented Channels** (`BluetoothServerSocket`/`BluetoothSocket` via `listenUsingInsecureL2capChannel()` / `createInsecureL2capChannel(psm)`). This requires **API 29 (Android 10, `Build.VERSION_CODES.Q`)** or higher and changes how peers discover and establish a data channel — see [Connection Phase (L2CAP)](#connection-phase-l2cap) below.

---

## 📸 Screenshots

### Firebase Module

|                                                   Login                                                  |                                                 User List                                                |                                                   Chat                                                   |
| :------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------: |
| <img src="https://github.com/user-attachments/assets/215fee3a-0a90-48b6-ac1e-12c96458c5ed" width="200"/> | <img src="https://github.com/user-attachments/assets/3cc7e437-cdd6-4d76-bf71-c2b242315333" width="200"/> | <img src="https://github.com/user-attachments/assets/17fa258b-aa87-44a9-8c4e-227ece0d30ff" width="200"/> |

### Bluetooth Module

|                                                Device Scan                                               |                                                   Chat                                                   |
| :------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------: |
| <img src="https://github.com/user-attachments/assets/2c5c84dd-c14b-480e-be4b-a4be35b1d70a" width="200"/> | <img src="https://github.com/user-attachments/assets/e3d4e5d6-17fc-4aa2-b416-1f31f1751569" width="200"/> |

---

## 🏛 Architecture

ChatApp follows **MVVM** with a Repository pattern. The two modules share no state — each has its own ViewModel(s), repository/data layer, and UI fragments.

```
┌─────────────────────────────────────────────────────────┐
│                        UI Layer                          │
│   MainMenu ──► InternetModule  /  BluetoothModule        │
│                  Fragments + XML ViewBinding             │
└──────────────────────┬───────────────────────────────────┘
                        │  observes StateFlow / LiveData
┌───────────────────────▼───────────────────────────────────┐
│                    ViewModel Layer                          │
│  InternetChatPageViewModel   BluetoothMessagingViewModel   │
│  InternetMainPageViewModel   (StateFlow + SharedFlow)      │
│  SignUpViewModel                                            │
└──────────┬────────────────────────────────┬─────────────────┘
           │                                │
┌──────────▼──────────┐   ┌─────────────────▼──────────────────┐
│   Firebase Stack     │   │         Bluetooth Stack             │
│                      │   │                                     │
│  FirebaseAuth        │   │  BluetoothRepo                      │
│  FirebaseFirestore   │   │  ├─ BleDiscoveryManager             │
│  FirebaseRealtime    │   │  │   ├─ BLE Advertiser (PSM+role+   │
│    Database          │   │  │   │  uuid+nick in ServiceData)  │
│  FirebaseStorage      │   │  │   └─ BLE Scanner                │
│                      │   │  ├─ BleConnectionManager            │
│                      │   │  │   ├─ L2CAP ServerSocket (host)  │
│                      │   │  │   ├─ L2CAP ClientSocket (join)  │
│                      │   │  │   └─ per-connection read loop    │
│                      │   │  └─ Room DAO                        │
└──────────────────────┘   └──────────────────────────────────---┘
```

### Event-Driven ViewModel (Bluetooth)

The Bluetooth module uses a **sealed interface event system** to route user actions:

```
sealed interface BluetoothEvent
sealed interface GeneralBluetoothEvent : BluetoothEvent  // scan, tapToChat, sendMessage, endConnection
sealed interface ClientBluetoothEvent  : BluetoothEvent  // connectToDevice
```

`BluetoothMessagingViewModel.onEvent(event)` dispatches to either `GeneralHandlerImp` or `ClientHandlerImp`, keeping the ViewModel thin.

---

## ✨ Key Technical Features

### Bluetooth Module

- **BLE Discovery + L2CAP Data Channel** — BLE advertising (`BluetoothLeAdvertiser`) and scanning (`BluetoothLeScanner`) are used purely for peer *discovery* and *session negotiation*. The actual data channel is a classic Bluetooth **L2CAP Connection-Oriented Channel (CoC)**, opened with `BluetoothAdapter.listenUsingInsecureL2capChannel()` on the hosting side and `BluetoothDevice.createInsecureL2capChannel(psm)` on the joining side. L2CAP CoC requires **API 29+**.

- **Dynamic PSM Exchange via BLE Advertisement** — Unlike RFCOMM (which binds to a fixed, well-known UUID), L2CAP assigns a **dynamic PSM (Protocol/Service Multiplexer) port** each time a listener socket is opened. This PSM has no fixed value across sessions, so it must be broadcast every time a device starts hosting. `BleDiscoveryManager.startAdvertising()` packs `psm (4B) + role (1B) + sessionUUID (16B) + nickname bytes` into a single `ServiceData` payload, keyed by a fixed `ParcelUuid`. Peers scanning for devices decode this payload to learn the current PSM before attempting to connect — this coupling is new relative to RFCOMM and is the reason PSM retrieval must be awaited (see `startHostingAndGetPsm()`) before advertising begins, to avoid broadcasting a stale or null PSM from a previous session.

- **Application-Layer UUID Handshake Over L2CAP** — Because a bare L2CAP socket carries no identity information the way an RFCOMM UUID does, the app performs its own handshake immediately after `socket.connect()`/`accept()` succeeds: the host writes a single "ready" byte, the client responds with its 16-byte session UUID, and the host validates the byte count before promoting the socket to a managed `BluetoothConnection`. This replaces the implicit identity that RFCOMM's service UUID used to provide.

- **Role-Based Connection Management** — Each device has one of three roles at runtime: `IDLE`, `SERVER`, or `CLIENT`. Role is set once a connection is actually accepted/established (not merely attempted) and drives whether the device advertises+hosts, scans+joins, or both, on the next scan cycle.

- **Custom Binary Packet Protocol (transport-agnostic)** — Messages are split into buffer-sized chunks and sent with a custom 5-byte header: `[messageID (2B)] [totalParts (1B)] [nickSize (1B)] [partIndex (1B)]`. The receiver reassembles packets in an `assemblyMap` keyed by `messageID` before persisting the full message. This framing lives entirely above the socket layer, so it carried over unchanged from RFCOMM to L2CAP.

- **Explicit EOF / Remote-Close Detection** — Because L2CAP sockets (like any stream socket) return `-1` from `InputStream.read()` on a graceful remote close rather than throwing, `manageConnectedSocket()` explicitly checks for `-1` and raises a controlled `IOException` — ensuring a peer closing its socket is detected and cleaned up (role reset, list removal) instead of silently corrupting the reassembly buffer.

- **Room DB for Message Persistence** — All Bluetooth chat history is persisted locally in a Room database (`message_table`). Chat history is keyed by a hash derived from the peer's discovered session UUID/MAC address, ensuring conversations survive app restarts.

- **Per-Connection Coroutine Scopes** — Each `BluetoothConnection` carries its own `CoroutineScope(Dispatchers.IO + SupervisorJob())`, so connection failures are isolated and don't affect other active connections. The scope is torn down only after the socket and streams are closed, so cleanup callbacks (`removeDeviceFromMemory`, `emitError`) reliably fire before cancellation — as opposed to cancelling first and losing the ability to run suspend cleanup logic.

- **Nickname via SharedPreferences** — The user's display name is persisted with `SharedPreferences` and included in every Bluetooth packet header and BLE advertisement.

### Firebase Module

- **Dual Firebase Backend** — User profiles and recent-chat metadata (last message, unseen count, timestamps) live in **Firestore**. Actual chat messages are stored in **Firebase Realtime Database** under a deterministic filename: `min(uid, partnerUid) + "-" + max(uid, partnerUid)`.

- **Real-Time User + Message Streams via `callbackFlow`** — `InternetMainPageViewModel` merges two Firestore/RTDB snapshot listeners into a single `LiveData<List<UserListItem>>` using `combine`, sorting the list by most recent message timestamp.

- **Unseen Message Counter** — When a message is sent, the partner's `unseenMessageCount` field in Firestore is incremented atomically. When the chat is opened, `updateSeenCount()` decrements it with a Firestore `FieldValue.increment`.

- **Seen Status per Message** — Each `ChatMessage` in the Realtime Database has a `seen` boolean that is updated via `updateSeenStatus()` once the recipient opens the chat.

- **Lottie Animations** — Loading and empty-state animations are handled by the Lottie library.

---

## 🛠 Tech Stack & Dependencies

| Category                 | Library / Tool                           | Version                  |
| ------------------------ | ----------------------------------------- | ------------------------- |
| **Language**             | Kotlin                                    | 2.0.21                    |
| **Build System**         | Gradle KTS + KSP                          | AGP 8.10.1                |
| **Min / Target SDK**     | API 29¹ / API 36                          | Android 10 – Android 14   |
| **Firebase BOM**         | firebase-bom                              | 34.6.0                    |
| **Firebase Auth**        | firebase-auth                             | via BOM                   |
| **Firebase Realtime DB** | firebase-database                         | via BOM                   |
| **Firebase Firestore**   | firebase-firestore                        | via BOM                   |
| **Firebase Storage**     | firebase-storage                          | via BOM                   |
| **Dependency Injection** | Hilt (Dagger)                             | 2.56                      |
| **Navigation**           | Navigation Component + Safe Args          | 2.9.6                     |
| **Local Database**       | Room                                      | 2.8.4                     |
| **Preferences**          | DataStore Preferences                     | 1.2.1                     |
| **Image Loading**        | Glide                                     | 5.0.5                     |
| **Animations**           | Lottie                                    | 6.7.1                     |
| **Architecture**         | MVVM + LiveData + StateFlow + Repository  | —                         |
| **Async**                | Kotlin Coroutines                         | —                         |
| **UI**                   | ViewBinding + ListAdapter + RecyclerView  | —                         |

¹ **API 29 (`Build.VERSION_CODES.Q`) is a hard floor for the Bluetooth module**, since `listenUsingInsecureL2capChannel()` / `createInsecureL2capChannel()` were introduced in Android 10. All L2CAP entry points in `BleConnectionManager` are annotated `@RequiresApi(Build.VERSION_CODES.Q)`. The Firebase module has no such constraint.

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
│       │   │   │   ├── BluetoothConnection.kt   # socket + inputStream + outputStream +
│       │   │   │   │                             #   remoteDevice + CoroutineScope + psm + uuid
│       │   │   │   ├── BluetoothDeviceListItem.kt
│       │   │   │   ├── BluetoothMessage.kt      # @Entity for Room (composite PK)
│       │   │   │   ├── DeviceRole.kt             # IDLE / SERVER / CLIENT role codes
│       │   │   │   └── ObjectConstants.kt        # ParcelUuid, buffer size, scan window, DB name
│       │   │   └── repo/
│       │   │       ├── BluetoothRepo.kt          # Orchestrates scan/host cycle, role state,
│       │   │       │                             #   packet chunking/assembly, Room writes
│       │   │       ├── BleDiscoveryManager.kt    # BLE advertise (PSM+role+uuid+nick) & scan
│       │   │       ├── BleConnectionManager.kt   # L2CAP listener/accept loop, L2CAP connect,
│       │   │       │                             #   per-connection read loop, teardown
│       │   │       ├── BluetoothMessageParser.kt # messageID parsing + packet reassembly
│       │   │       ├── client/
│       │   │       │   ├── ClientHandler.kt
│       │   │       │   └── ClientHandlerImp.kt   # ConnectToDevice event handler
│       │   │       └── general/
│       │   │           ├── GeneralHandler.kt
│       │   │           └── GeneralHandlerImp.kt  # Scan, TapToChat, Send, End handlers
│       │   ├── event/
│       │   │   ├── BluetoothEvent.kt             # Sealed interface hierarchy
│       │   │   └── GeneralBluetoothEvent.kt
│       │   ├── fragments/
│       │   │   ├── BluetoothMainPage.kt          # Device discovery UI
│       │   │   └── BluetoothChatPage.kt          # Bluetooth chat UI
│       │   ├── room/
│       │   │   ├── BluetoothDao.kt               # getTheChatHistory, upsert, delete
│       │   │   └── RoomDatabaseForBluetooth.kt
│       │   ├── util/
│       │   │   ├── byteArrayToUuidString.kt      # 16-byte UUID handshake payload → String
│       │   │   ├── convertUuidToByteArray.kt      # UUID String → 16-byte handshake payload
│       │   │   ├── createBluetoothItem.kt
│       │   │   ├── getChatFileName.kt
│       │   │   └── permissionDataHandler.kt      # API 31+ permission logic
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

### Bluetooth Module — Discovery & Connection Setup (L2CAP)

```
performScan() called (role == IDLE, SERVER, or CLIENT)
       │
       ├── shouldHost? ─────────────────────────────────────────────┐
       │                                                            │
       │   1. startHostingAndGetPsm()                               │
       │      → bluetoothAdapter.listenUsingInsecureL2capChannel()  │
       │      → suspends until the listener is bound and a real,    │
       │        non-null PSM is available (prevents advertising a   │
       │        stale/closed PSM from a previous cycle)              │
       │   2. acceptConnections(role) launched in the background    │
       │      → blocks on _listener.accept()                        │
       │   3. startAdvertising(psm, role, sessionUUID, nickname)     │
       │      → BLE ServiceData: [psm|role|uuid|nick]                │
       │                                                            │
       └── shouldScan? ─────────────────────────────────────────────┤
           BLE scan decodes ServiceData → BluetoothDeviceListItem   │
           (device, nick, psm, role, uuid) added to scanResults      │
                                                                     │
User taps a discovered device ──► establishConnectionAsClient()     │
       │                                                            │
       │  device.device.createInsecureL2capChannel(device.psm)      │
       │  socket.connect()                                          │
       │  read 1 "ready" byte from host ──► write 16-byte UUID       │
       │                                                            │
       └────────────────────────────────────────────────────────────┘
                                │
              Host: accept() returns socket
                     │
              write 1 "ready" byte ──► read 16-byte UUID from client
                     │
              BluetoothConnection(socket, in, out, device, scope, psm, uuid)
                     │
              saveDeviceToMemory() ──► setDeviceRole() + manageConnectedSocket()
```

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
  │  1. Chunk message into buffer-sized   │
  │     parts                             │
  │  2. Build 5-byte header per chunk:    │
  │     [msgID(2B)][parts(1B)]            │
  │     [nickSize(1B)][partIdx(1B)]       │
  │  3. Write to BluetoothConnection      │
  │     .outputStream (L2CAP socket)      │
  │  4. Upsert BluetoothMessage to Room   │
  └────────────────────────────────────────┘
       │
Room DB ──(Flow)──► ViewModel._messageList ──► UI RecyclerView
```

### Bluetooth Module — Receiving a Message / Detecting Disconnects

```
manageConnectedSocket() read loop on connection.scope
       │
  socket.inputStream.read(buffer)
       │
       ├── byteSize == -1 ──► remote peer closed the socket (graceful FIN)
       │                       → throw IOException → socketError()
       │                       → endConnection() → removeDeviceFromMemory()
       │
       └── byteSize > 0
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
  └─────────────────────────────────────────────┘
       │
RTDB ValueEventListener ──► ViewModel._chat ──► UI
```

---

## 📦 Installation

### Prerequisites

- Android Studio **Iguana (2023.2.1)** or newer
- JDK 11
- Two physical Android devices running **API 29+ (Android 10)** — required for L2CAP testing; emulators don't support Bluetooth hardware, and devices below API 29 cannot use the L2CAP data channel at all
- A configured Firebase project (see [Firebase Setup](#-firebase-setup))

### Steps

1. **Clone the repository:**

   ```
   git clone https://github.com/mustafaerendalgic/HChat.git
   cd HChat
   ```

2. **Open in Android Studio:** Select **File → Open** and point to the cloned folder.

3. **Add your Firebase config:** Place `google-services.json` inside the `app/` directory (see below).

4. **Sync Gradle:** Android Studio will prompt you — click **Sync Now**.

5. **Build & Run:** Connect two physical devices (API 29+) and press **Run ▶** (`Shift + F10`) on each.

> ⚠️ **Bluetooth requires two physical devices on API 29+.** The L2CAP channel will fail to open on emulators and on devices below Android 10.

---

## 🔥 Firebase Setup

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Add an **Android app** with package name `com.example.chatapp`.
3. Download `google-services.json` and place it in `app/`.
4. Enable the following services:

**Authentication** — Enable Email/Password sign-in.

**Firestore** — Create a `users` collection. Set development rules:

```
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

```
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

| Permission                   | Purpose                           | API Scope        |
| ----------------------------- | ---------------------------------- | ----------------- |
| `INTERNET`                    | Firebase connectivity              | All               |
| `BLUETOOTH`                   | Basic Bluetooth operations         | maxSdkVersion 30  |
| `BLUETOOTH_ADMIN`             | Device discovery & pairing         | maxSdkVersion 30  |
| `ACCESS_FINE_LOCATION`        | Required for BT scanning (legacy)  | maxSdkVersion 30  |
| `ACCESS_COARSE_LOCATION`      | Required for BT scanning (legacy)  | maxSdkVersion 30  |
| `ACCESS_BACKGROUND_LOCATION`  | Background BT scanning (legacy)    | maxSdkVersion 30  |
| `BLUETOOTH_SCAN`               | BLE scan (`neverForLocation`)      | API 31+           |
| `BLUETOOTH_CONNECT`           | Connect to discovered devices; required for L2CAP `accept()`/`connect()` | API 31+ |
| `BLUETOOTH_ADVERTISE`         | BLE advertising (discoverable)     | API 31+           |

The app also declares `android.hardware.bluetooth_le` as a required hardware feature. Because L2CAP CoC is a classic-Bluetooth transport exposed through the `BluetoothAdapter`/`BluetoothDevice` APIs (not GATT), no additional BLE-specific permission is required beyond what BLE advertising/scanning already needs — `BLUETOOTH_CONNECT` covers the L2CAP socket calls.

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

Both devices advertise and scan simultaneously while `IDLE`. Unlike a static discoverability flag, every advertisement packet carries **session-specific data**: the device's current L2CAP PSM, its intended role, a session UUID, and its nickname — all packed into one `ServiceData` blob under a fixed `ParcelUuid`. This is necessary because L2CAP has no fixed, well-known channel to connect to; the PSM is assigned by the OS at listen-time and changes across app runs, so it has to be discovered fresh every session.

#### Connection Phase (L2CAP)

One device hosts (`acceptConnections`, backed by `BluetoothServerSocket.accept()`), one device joins (`establishConnectionAsClient`, backed by `BluetoothSocket.connect()`). Role assignment happens only once a connection is actually accepted or established — not merely attempted — to avoid a device prematurely believing it holds a role it hasn't secured yet.

```
Device A (Host)                              Device B (Joiner)
        │                                            │
listenUsingInsecureL2capChannel()                    │
   → dynamic PSM assigned                            │
        │                                            │
BLE advertise: [psm|role|uuid|nick] ──────────►  BLE scan decodes PSM
        │                                            │
   accept() blocks                                   │
        │                                            │
        │◄──────── createInsecureL2capChannel(psm) ──│
        │                socket.connect()             │
        │                                            │
   write 1 "ready" byte ──────────────────────────►  read 1 byte
        │                                            │
        │◄──────────────────── write 16-byte uuid ───│
   read 16-byte uuid                                  │
        │                                            │
        ╔════════════════════════════════════════════╗
        ║        BluetoothConnection established       ║
        ║   (socket + inputStream + outputStream        ║
        ║    + per-connection CoroutineScope + psm)     ║
        ╚════════════════════════════════════════════╝
        │                                            │
        │◄══════ per-connection read loop (both) ═══►│
```

A few consequences of moving to L2CAP that are worth calling out explicitly:

- **No implicit peer identity.** RFCOMM connections were tied to a service UUID that doubled as an identity hint. L2CAP sockets carry none of that, so the app performs its own lightweight handshake (ready-byte + UUID exchange) immediately after the socket opens, before the connection is considered "established" and handed off to the rest of the repo.
- **PSM must be fresh, not cached.** Because the PSM changes every time `listenUsingInsecureL2capChannel()` is called, `BluetoothRepo.performScan()` explicitly `await`s a suspend function (`startHostingAndGetPsm()`) that returns only after the listener socket is bound, rather than reading a possibly-stale `_listener?.psm` synchronously right after firing off the host coroutine.
- **Graceful close needs an explicit check.** `InputStream.read()` on an L2CAP socket returns `-1` on remote close instead of throwing, so `manageConnectedSocket()` checks for `-1` explicitly and raises a controlled error, ensuring the peer's disconnect is always detected and reflected in the connected-devices list on both ends.

#### Messaging Phase

- Messages are chunked into fixed-size parts with a 5-byte binary header, unchanged from the RFCOMM implementation since this framing sits above the socket abstraction.
- The receiver reassembles parts using an `assemblyMap<messageID, List<ByteArray>>`.
- Completed messages are upserted into Room via `BluetoothDao.updateMessage()`.
- The UI observes the Room `Flow<List<BluetoothMessage>>` through `ViewModel._messageList`.
- Chat history is keyed by the peer's session identifier, making it addressable and persistent across reconnects.

---

## ⚠️ Known Limitations & Potential Improvements

- **API 29 floor for Bluetooth.** L2CAP CoC (`listenUsingInsecureL2capChannel` / `createInsecureL2capChannel`) is unavailable below Android 10. Devices on API 26–28 can still use the Firebase module but cannot use Bluetooth chat at all — this is a stricter floor than the RFCOMM implementation had.
- **Single active Bluetooth connection** — The current role system supports one peer connection at a time. The host accepts one client and breaks out of the accept loop once a connection is established.
- **No PSM/session collision handling for concurrent hosts** — if multiple nearby devices are hosting simultaneously, a scanner will see multiple advertisements and connect to whichever it taps; there's no negotiation to prevent a device from accidentally attempting two connections at once.
- **No message delivery confirmation** — The Bluetooth module does not implement ACKs; packet loss on a dropped connection is not retried.
- **No proactive dead-connection detection** — remote closes are detected via `read() == -1`, but an abrupt link loss (e.g., devices moving out of range without a clean socket close) may leave a connection appearing open until the next failed read/write. A heartbeat or read-timeout mechanism would make disconnect detection more robust.
- **Release build minification disabled** — `isMinifyEnabled = false` in the release build type. ProGuard rules should be added before production release.
- **Firebase rules are open** — The sample rules allow any authenticated user to read/write all data. Tighten rules before deploying to production.
- **No end-to-end encryption** — Bluetooth communication benefits from underlying link-layer encryption provided by the Android Bluetooth stack after pairing (L2CAP sockets here are opened *insecure*, i.e. without pairing/encryption). Message payloads are not encrypted at the application layer. Implementing end-to-end encryption (e.g., using a shared key or public-key exchange) would ensure message confidentiality independent of the transport layer, and switching to `listenUsingL2capChannel()` / `createL2capChannel()` (the *secure*, non-"Insecure" variants) would add link-layer authentication and encryption.

---

## 👤 Author

**Mustafa Eren Dalgıç**

- GitHub: [@mustafaerendalgic](https://github.com/mustafaerendalgic)
- Çukurova University — Department of Computer Engineering

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](https://github.com/mustafaerendalgic/HChat/blob/main/LICENSE) file for details.

---

Made as a Graduation Thesis at Çukurova University 🎓
