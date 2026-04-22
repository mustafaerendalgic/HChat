# 📱 BChat - Hybrid Android Messaging Platform

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)

[cite_start]**BChat** is a modular communication platform designed to provide seamless messaging through both network-dependent (Cloud) and autonomous hardware-level (Bluetooth RFCOMM) protocols[cite: 20, 21]. [cite_start]It serves as a resilient solution for environments with compromised or absent internet infrastructure[cite: 21].

> [cite_start]⚠️ **Note:** This project is currently an active Graduation Thesis (Interim Phase 2)[cite: 9, 10].

## 📸 Screenshots

| Main Menu | Scanning Screen | Chat Interface | P2P Connectivity |
| :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/215fee3a-0a90-48b6-ac1e-12c96458c5ed" width="200"> | <img src="https://github.com/user-attachments/assets/3cc7e437-cdd6-4d76-bf71-c2b242315333" width="200"> | <img src="https://github.com/user-attachments/assets/10ead6c9-bbdb-459e-8a83-b118f7132ab7" width="200"> | <img src="YOUR_IMAGE_URL_HERE" width="200"> |
| <img src="https://github.com/user-attachments/assets/17fa258b-aa87-44a9-8c4e-227ece0d30ff" width="200"> | <img src="YOUR_IMAGE_URL_HERE" width="200"> | | |

## ✨ Key Features

- [cite_start]**Hybrid Connectivity:** Seamlessly transition between Firebase-powered cloud messaging and offline Bluetooth P2P communication[cite: 11, 21].
- [cite_start]**Bluetooth RFCOMM Engine:** Low-level socket programming for stable, real-time bidirectional data streaming[cite: 23, 28].
- [cite_start]**Permission Management Framework:** Granular handling of hardware-level permissions tailored for Android 12+ (API 31)[cite: 26, 253].
- [cite_start]**Reactive Architecture:** Utilizes **LiveData** and **ListAdapter** for optimized, lifecycle-aware UI updates[cite: 29, 30].

## 🛠 Tech Stack & Architecture

- [cite_start]**Architecture:** MVVM (Model-View-ViewModel) for clean separation of business logic and UI[cite: 22, 139].
- [cite_start]**View Logic:** XML Layouts with **View Binding** for type-safe and null-safe interactions[cite: 137, 138].
- [cite_start]**Dependency Injection:** **Hilt (Dagger)** for efficient management of Bluetooth adapters and application lifecycle[cite: 134].
- [cite_start]**Asynchronous Execution:** Dedicated multi-threaded socket management to ensure a non-blocking Main Thread[cite: 23, 142].

## 🚀 Roadmap

- [x] [cite_start]**Bluetooth Core:** Stable RFCOMM socket layer and device discovery[cite: 27, 28].
- [ ] [cite_start]**Data Persistence:** Integration of **Room Database** for offline message logs[cite: 219].
- [ ] **Security Enhancement:** Application-level AES-256 encryption for end-to-end privacy.
- [ ] [cite_start]**Mesh Networking:** Extending operational range via daisy-chaining multiple devices[cite: 206].

## 📦 Installation

To run this project locally:

1. Clone the repository:
   ```bash
   git clone [https://github.com/mustafaerendalgic/chatApp.git](https://github.com/mustafaerendalgic/chatApp.git)
