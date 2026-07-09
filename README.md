# HChat

[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Bluetooth](https://img.shields.io/badge/Bluetooth-0082FC?logo=bluetooth&logoColor=white)](https://developer.android.com/develop/connectivity/bluetooth)
[![Room](https://img.shields.io/badge/Room-4285F4?logo=android&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Hilt](https://img.shields.io/badge/Hilt-Dependency%20Injection-orange)](https://dagger.dev/hilt/)

**HChat** is a native Android messaging application that provides two communication modes within a single app:

- **Cloud chat** using Firebase services
- **Offline peer-to-peer chat** using Bluetooth discovery and L2CAP sockets

The project was developed as a graduation thesis at Çukurova University, Department of Computer Engineering. It explores real-time cloud communication, internet-independent device-to-device messaging, local persistence, asynchronous state management, and Android API-level compatibility.

> The cloud and Bluetooth features are independently structured application features, not separate Gradle or Android Dynamic Feature modules.

---

## Contents

- [Overview](#overview)
- [Main Features](#main-features)
- [Bluetooth Architecture](#bluetooth-architecture)
- [Android Version Compatibility](#android-version-compatibility)
- [Cloud Chat Architecture](#cloud-chat-architecture)
- [Application Architecture](#application-architecture)
- [Message Framing Protocol](#message-framing-protocol)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Installation](#installation)
- [Firebase Configuration](#firebase-configuration)
- [Permissions](#permissions)
- [Known Limitations](#known-limitations)
- [Author](#author)
- [License](#license)

---

## Overview

HChat opens with a main menu that allows the user to select one of two communication modes.

| Feature | Communication stack | Persistence | Internet required |
|---|---|---|---|
| Cloud chat | Firebase Authentication, Cloud Firestore, Realtime Database, Firebase Storage | Firebase | Yes |
| Bluetooth chat | BLE advertising/scanning and L2CAP Connection-Oriented Channels | Room | No |

Both features follow the same overall architectural approach while maintaining separate UI, state, and data-handling components.

---

## Main Features

### Cloud chat

- Email/password authentication with Firebase Authentication
- Real-time message exchange through Firebase Realtime Database
- User profiles and recent-chat metadata stored in Cloud Firestore
- Profile image storage through Firebase Storage
- Live user and conversation updates
- Unseen-message counters
- Per-message seen status
- Recent conversations ordered by activity

### Offline Bluetooth chat

- Internet-independent peer discovery and messaging
- BLE advertising and scanning for discovery and session negotiation
- L2CAP Connection-Oriented Channel sockets for message transport
- Dynamic PSM exchange through BLE service data
- Application-level UUID handshake after socket establishment
- Role-based connection management
- Custom binary message chunking and reassembly
- Local chat history with Room
- Per-connection coroutine scopes for failure isolation
- Explicit remote-disconnection detection

---

## Bluetooth Architecture

The Bluetooth implementation separates **peer discovery** from **message transport**.

### 1. Discovery and negotiation

BLE is used to advertise and discover nearby HChat peers. An advertisement contains the session information required to establish the later socket connection:

```text
[PSM] [role] [session UUID] [nickname]
```

The payload is stored in BLE `ServiceData` under a fixed `ParcelUuid`.

The advertised PSM must be obtained only after the L2CAP server socket has been opened because Android assigns the PSM dynamically. Advertising a cached or stale PSM would cause connection attempts to fail.

### 2. L2CAP connection

The hosting device opens an insecure L2CAP server socket:

```kotlin
bluetoothAdapter.listenUsingInsecureL2capChannel()
```

The joining device reads the advertised PSM and opens a matching client socket:

```kotlin
device.createInsecureL2capChannel(psm)
```

The host blocks on `accept()`, while the client calls `connect()`.

### 3. Application-level handshake

An L2CAP socket does not use an application service UUID in the same way as the project's earlier RFCOMM implementation. HChat therefore performs a lightweight identity handshake after the socket is connected:

1. The host sends one ready byte.
2. The client reads the ready byte.
3. The client sends its 16-byte session UUID.
4. The host validates and stores the UUID.
5. The socket is promoted to a managed `BluetoothConnection`.

### 4. Managed connection

Each active peer connection stores:

- `BluetoothSocket`
- Input and output streams
- Remote `BluetoothDevice`
- Dynamic PSM
- Session UUID
- A dedicated `CoroutineScope`
- A `SupervisorJob` for fault isolation

A failed connection can therefore be closed and removed without cancelling unrelated active connections.

---

## RFCOMM-to-L2CAP Migration

The Bluetooth transport was originally implemented with RFCOMM and later migrated to L2CAP Connection-Oriented Channels.

The migration required several architectural changes:

- Replacing a fixed RFCOMM service UUID with a dynamically assigned L2CAP PSM
- Advertising the PSM before clients attempt to connect
- Waiting for the listener socket to expose a valid PSM
- Adding an application-level UUID handshake
- Handling L2CAP stream closure explicitly
- Raising the Bluetooth feature's minimum supported API level to Android 10

The custom message framing protocol is transport-independent and was retained during the migration.

---

## Android Version Compatibility

Bluetooth permissions changed significantly in Android 12.

HChat implements API-level-aware permission handling for both permission models:

### Android 10 and Android 11 — API 29 and 30

BLE discovery uses the legacy Bluetooth and location permission model:

- `BLUETOOTH`
- `BLUETOOTH_ADMIN`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `ACCESS_BACKGROUND_LOCATION`, where required by the application's scanning behavior

These legacy permissions are restricted with `maxSdkVersion="30"` where applicable.

### Android 12 and later — API 31+

The app requests the newer runtime Bluetooth permissions:

- `BLUETOOTH_SCAN`
- `BLUETOOTH_CONNECT`
- `BLUETOOTH_ADVERTISE`

`BLUETOOTH_SCAN` is declared with `neverForLocation` because scanning is used to discover HChat peers rather than infer physical location.

This version-aware implementation avoids requesting obsolete permissions on newer Android releases while preserving compatibility with the Android 10–11 permission model.

> L2CAP Connection-Oriented Channel APIs used by this project were introduced in API 29. Consequently, the Bluetooth chat feature requires Android 10 or later.

---

## Cloud Chat Architecture

The cloud feature uses multiple Firebase products for distinct responsibilities.

### Firebase Authentication

Handles registration, login, and authenticated user sessions.

### Cloud Firestore

Stores user profiles and recent-conversation metadata, including:

- Nickname
- Profile image URL
- Last message
- Last sender
- Last activity timestamp
- Unseen-message count

### Firebase Realtime Database

Stores the actual chat messages and delivers real-time updates.

A deterministic conversation key is generated from the two user IDs so both participants resolve to the same message path:

```text
min(uidA, uidB) + "-" + max(uidA, uidB)
```

### Firebase Storage

Stores profile images referenced by user documents.

### Reactive updates

Firebase listeners are adapted into reactive streams. The ViewModels combine user and recent-chat data to produce a live, ordered conversation list for the UI.

---

## Application Architecture

HChat uses a single Android application module with feature-oriented packages.

The project follows:

- MVVM
- Repository pattern
- Hilt dependency injection
- Kotlin Coroutines
- `StateFlow`
- `SharedFlow`
- `LiveData`
- Room
- ViewBinding
- Navigation Component

```text
UI
├── Fragments
├── RecyclerView adapters
└── ViewBinding
        │
        ▼
ViewModels
├── UI state
├── Events
└── Coroutine collection
        │
        ▼
Repositories and managers
├── Firebase data operations
├── BLE discovery
├── L2CAP connection management
├── Message parsing
└── Room persistence
```

The Bluetooth ViewModel uses sealed event types and delegates operations to specialized handlers. This keeps UI event routing separate from discovery, connection, transport, and persistence logic.

---

## Message Framing Protocol

Bluetooth streams do not preserve application-level message boundaries. HChat therefore defines a binary framing protocol above the socket layer.

Each chunk starts with a five-byte header:

```text
[message ID: 2 bytes]
[total parts: 1 byte]
[nickname size: 1 byte]
[part index: 1 byte]
[payload: remaining bytes]
```

### Sending

1. The message is encoded as bytes.
2. The payload is divided into buffer-sized chunks.
3. Each chunk receives the binary header.
4. Chunks are written to the connection output stream.
5. The sent message is persisted in Room.

### Receiving

1. Bytes are read from the socket input stream.
2. The parser extracts the header.
3. Chunks are grouped by message ID.
4. Parts are ordered by part index.
5. The nickname and message body are reconstructed.
6. The completed message is persisted in Room.
7. The UI receives the updated history through a Room `Flow`.

The read loop also checks for `InputStream.read(...) == -1`, which indicates that the remote peer has closed the stream. The connection is then cleaned up and removed from the active-device state.

---

## Technology Stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| Platform | Android |
| Architecture | MVVM and Repository pattern |
| Dependency injection | Hilt |
| Asynchronous programming | Kotlin Coroutines |
| Reactive state | StateFlow, SharedFlow, LiveData |
| Cloud authentication | Firebase Authentication |
| Cloud metadata | Cloud Firestore |
| Cloud messages | Firebase Realtime Database |
| Media storage | Firebase Storage |
| Peer discovery | Bluetooth Low Energy advertising and scanning |
| Offline transport | L2CAP Connection-Oriented Channels |
| Local persistence | Room |
| Navigation | Android Navigation Component and Safe Args |
| UI | XML layouts, ViewBinding, RecyclerView |
| Image loading | Glide |
| Animation | Lottie |
| Build configuration | Gradle Kotlin DSL and KSP |

Dependency versions should be treated as defined by the repository's Gradle files and version catalog rather than duplicated here, since they may change independently of this document.

---

## Project Structure

```text
HChat/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/chatapp/
│       │   ├── MainActivity.kt
│       │   ├── hilt/
│       │   ├── internet/
│       │   │   ├── adapters/
│       │   │   ├── entity/
│       │   │   ├── fragments/
│       │   │   ├── util/
│       │   │   └── viewmodels/
│       │   └── bluetooth/
│       │       ├── adapters/
│       │       ├── data/
│       │       │   ├── entity/
│       │       │   └── repo/
│       │       ├── event/
│       │       ├── fragments/
│       │       ├── room/
│       │       ├── util/
│       │       └── viewmodel/
│       └── res/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

Important Bluetooth components include:

- `BluetoothRepo` — coordinates discovery, connection state, message transport, parsing, and persistence
- `BleDiscoveryManager` — manages BLE advertising and scanning
- `BleConnectionManager` — manages L2CAP server/client sockets and connection read loops
- `BluetoothMessageParser` — reconstructs framed message chunks
- `BluetoothMessagingViewModel` — exposes Bluetooth UI state and events
- `BluetoothDao` — reads and writes local chat history

---

## Requirements

- Android Studio compatible with the project's Android Gradle Plugin
- A supported JDK version for the configured Android Gradle Plugin
- Android SDK packages required by `compileSdk`
- Two physical Android devices running Android 10 or later for Bluetooth testing
- Bluetooth and BLE hardware support on both devices
- A Firebase project for cloud-chat functionality

> Android emulators do not provide the physical Bluetooth environment required to validate BLE advertising, scanning, and L2CAP socket communication.

---

## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/mustafaerendalgic/HChat.git
   cd HChat
   ```

2. Open the project in Android Studio.

3. Add your Firebase Android configuration file:

   ```text
   app/google-services.json
   ```

4. Sync the Gradle project.

5. Build and install the app on one or more physical Android devices.

6. Grant the Bluetooth permissions requested for the device's Android version.

7. For Bluetooth chat testing, install and run the app on two Android 10+ devices.

---

## Firebase Configuration

Create a Firebase project and register an Android application using the package/application ID configured in `app/build.gradle.kts`.

Enable:

- Email/password authentication
- Cloud Firestore
- Realtime Database
- Firebase Storage

Download `google-services.json` and place it in:

```text
app/google-services.json
```

### Simplified data layout

```text
users/
└── {uid}/
    ├── nickname
    ├── profile_pic
    └── recent_chats/
        └── {partnerUid}/
            ├── lastMessage
            ├── lastMessageBy
            ├── timestamp
            └── unseenMessageCount

chats/
└── {conversationKey}/
    └── {messageKey}/
        ├── messageID
        ├── senderUID
        ├── message
        ├── date
        ├── seen
        └── nickname
```

Do not use permissive development rules in a production deployment. Firestore, Realtime Database, and Storage rules should restrict access to authenticated users and validate ownership, participants, and permitted fields.

---

## Permissions

The manifest uses different declarations for legacy and modern Android Bluetooth permission models.

| Permission | Purpose | Android scope |
|---|---|---|
| `INTERNET` | Firebase connectivity | All supported versions |
| `BLUETOOTH` | Legacy Bluetooth operations | API 30 and below |
| `BLUETOOTH_ADMIN` | Legacy discovery/administration | API 30 and below |
| `ACCESS_FINE_LOCATION` | Legacy BLE scanning requirement | API 30 and below |
| `ACCESS_COARSE_LOCATION` | Legacy location compatibility | API 30 and below |
| `ACCESS_BACKGROUND_LOCATION` | Legacy background-location requirement, where applicable | API 29–30 |
| `BLUETOOTH_SCAN` | Discover nearby HChat peers | API 31+ |
| `BLUETOOTH_CONNECT` | Communicate with Bluetooth devices and sockets | API 31+ |
| `BLUETOOTH_ADVERTISE` | Advertise the local HChat session | API 31+ |

Runtime permission requests are handled according to the active Android API level.

---

## Known Limitations

- Bluetooth chat requires Android 10 or later because the selected L2CAP APIs are unavailable on older versions.
- L2CAP sockets are created with the insecure channel APIs; the application does not add end-to-end encryption.
- The custom Bluetooth protocol does not currently provide acknowledgements, delivery receipts, retransmission, or message-level integrity checks.
- A dropped connection can interrupt an in-flight multipart message.
- Concurrent connection negotiation and multi-peer edge cases may require additional coordination.
- Bluetooth behavior can vary between device vendors and Android Bluetooth stack implementations.
- Cloud-chat security depends on correctly configured Firebase rules.
- Automated tests and end-to-end Bluetooth instrumentation coverage can be expanded.
- Production hardening should include stricter validation, security rules, error reporting, and release-build optimization.

---

## Author

**Mustafa Eren Dalgıç**

- GitHub: [@mustafaerendalgic](https://github.com/mustafaerendalgic)
- Çukurova University, Department of Computer Engineering

---

## License

This repository does not currently include a license file. Without an explicit license, reuse, modification, and redistribution are not automatically granted.

Add a `LICENSE` file before describing the project as MIT-licensed or otherwise open source.
