# 📱 BChat - Hybrid Android Messaging Platform

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)

**BChat** is a modular communication platform designed to provide seamless messaging through both network-dependent (Cloud) and autonomous hardware-level (Bluetooth RFCOMM) protocols. It serves as a resilient solution for environments with compromised or absent internet infrastructure.

> ⚠️ **Note:** This project is currently an active Graduation Thesis (Interim Phase 2).

## 📸 Screenshots

| Main Menu | Scanning Screen | Bluetooth Scan | Chat Interface |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/215fee3a-0a90-48b6-ac1e-12c96458c5ed" width="200" /> | <img src="https://github.com/user-attachments/assets/3cc7e437-cdd6-4d76-bf71-c2b242315333" width="200" /> | <img src="https://github.com/user-attachments/assets/7a2bc9b2-2883-413b-99c9-7d5661e9b0e2" width="200" /> | <img src="https://github.com/user-attachments/assets/10ead6c9-bbdb-459e-8a83-b118f7132ab7" width="200" /> |
| <img src="https://github.com/user-attachments/assets/17fa258b-aa87-44a9-8c4e-227ece0d30ff" width="200" /> | <img src="https://github.com/user-attachments/assets/8f28c0ba-00ec-49f3-b4ed-262eccfe32d5" width="200" /> | | |

## ✨ Key Features

- **Hybrid Connectivity:** Seamlessly transition between Firebase-powered cloud messaging and offline Bluetooth P2P communication.
- **Bluetooth RFCOMM Engine:** Low-level socket programming for stable, real-time bidirectional data streaming.
- **Permission Management Framework:** Granular handling of hardware-level permissions tailored for Android 12+ (API 31).
- **Reactive Architecture:** Utilizes **LiveData** and **ListAdapter** for optimized, lifecycle-aware UI updates.

## 🛠 Tech Stack & Architecture

- **Architecture:** MVVM (Model-View-ViewModel) for clean separation of business logic and UI.
- **View Logic:** XML Layouts with **View Binding** for type-safe and null-safe interactions.
- **Dependency Injection:** **Hilt (Dagger)** for efficient management of Bluetooth adapters and application lifecycle.
- **Asynchronous Execution:** Dedicated multi-threaded socket management to ensure a non-blocking Main Thread.

## 🚀 Roadmap

- [x] **Bluetooth Core:** Stable RFCOMM socket layer and device discovery.
- [ ] **Data Persistence:** Integration of **Room Database** for offline message logs.
- [ ] **Security Enhancement:** Application-level AES-256 encryption for end-to-end privacy.
- [ ] **Mesh Networking:** Extending operational range via daisy-chaining multiple devices.

## 📦 Installation

To run this project locally:

1. Clone the repository:
   ```bash
   git clone [https://github.com/mustafaerendalgic/chatApp.git](https://github.com/mustafaerendalgic/chatApp.git)
