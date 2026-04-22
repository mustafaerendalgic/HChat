# 📱 ChatApp - Hybrid Android Messaging Platform

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)

[cite_start]**ChatApp** is a sophisticated Android communication ecosystem developed as a **Graduation Thesis** at **Çukurova University, Department of Computer Engineering**[cite: 8, 9, 11]. [cite_start]The project addresses critical communication gaps in environments with compromised or absent internet infrastructure, such as natural disasters or remote industrial areas, by providing a hybrid connectivity model[cite: 20, 71, 72].

## 📑 Project Overview

[cite_start]The core objective of this thesis is to engineer a resilient messaging platform that seamlessly transitions between traditional cloud-based messaging and autonomous, hardware-level peer-to-peer (P2P) communication[cite: 21, 72, 78]. [cite_start]By leveraging **Bluetooth RFCOMM** technology, ChatApp establishes stable, high-throughput serial port emulations for real-time text exchange without requiring an active internet connection[cite: 114, 116].

## 📸 Screenshots

| Main Menu | Scanning Screen | Bluetooth Scan | Chat Interface |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/215fee3a-0a90-48b6-ac1e-12c96458c5ed" width="200" /> | <img src="https://github.com/user-attachments/assets/3cc7e437-cdd6-4d76-bf71-c2b242315333" width="200" /> | <img src="https://github.com/user-attachments/assets/7a2bc9b2-2883-413b-99c9-7d5661e9b0e2" width="200" /> | <img src="https://github.com/user-attachments/assets/10ead6c9-bbdb-459e-8a83-b118f7132ab7" width="200" /> |
| <img src="https://github.com/user-attachments/assets/17fa258b-aa87-44a9-8c4e-227ece0d30ff" width="200" /> | <img src="https://github.com/user-attachments/assets/8f28c0ba-00ec-49f3-b4ed-262eccfe32d5" width="200" /> | | |

## ✨ Key Technical Features

- [cite_start]**Hybrid Communication Architecture:** Integrates **Firebase Realtime Database** for online messaging and low-level **Bluetooth Socket Programming** for offline P2P sessions[cite: 20, 23, 77].
- [cite_start]**RFCOMM Data Streaming:** Implements asynchronous multi-threading to manage device discovery, connection handshakes, and bidirectional data streams without degrading UI responsiveness[cite: 23, 142].
- [cite_start]**Modern Security Compliance:** Includes a comprehensive **Permission Management Framework** specifically tailored for **Android 12+ (API 31)** security standards regarding hardware-level interactions[cite: 26, 123].
- [cite_start]**Reactive UI Engine:** Built using **MVVM** architecture combined with **LiveData** and **ListAdapter** for memory-efficient, lifecycle-aware data synchronization[cite: 22, 29, 139].

## 🛠 Tech Stack

- **IDE & Tooling:** Android Studio Iguana | [cite_start]2023.2.1[cite: 130].
- [cite_start]**Language:** Kotlin (leveraging Coroutines for non-blocking operations)[cite: 131].
- [cite_start]**Dependency Injection:** **Hilt (Dagger)** for scalable component management[cite: 134].
- [cite_start]**Navigation:** **Android Navigation Component** for centralized fragment management[cite: 136].
- [cite_start]**Versioning:** Minimum SDK 26 (Android 8.0), Target SDK 34 (Android 14)[cite: 132].

## 📦 Installation

1. Clone the repository:
   ```bash
   git clone [https://github.com/mustafaerendalgic/chatApp.git](https://github.com/mustafaerendalgic/chatApp.git)
