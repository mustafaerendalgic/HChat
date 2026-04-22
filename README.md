# 📱 Umbrella - Hybrid Android Messaging Platform

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/firebase-%23039BE5.svg?style=for-the-badge&logo=firebase)

**Umbrella** is a modular Android messaging application built with **Kotlin**, designed to support communication through both **cloud-based messaging** and **Bluetooth peer-to-peer messaging**. The goal of the project is to provide a more resilient communication experience in situations where internet access is limited or unavailable.

> ⚠️ **Project Status:** This project is currently being developed as an active **Graduation Thesis (Interim Phase 2)**.

---

## 📖 Overview

BChat combines two messaging approaches in a single Android application:

- **Cloud Chat** for network-based messaging
- **Bluetooth Chat** for short-range offline communication between nearby devices

The project is structured around modern Android development practices, including **MVVM**, **Hilt**, **ViewModels**, **RecyclerView adapters**, and **modular feature separation**.

---

## 📸 Screenshots

| | | | | |
|---|---|---|---|---|
| <img src="https://github.com/user-attachments/assets/215fee3a-0a90-48b6-ac1e-12c96458c5ed" width="200" /> | <img src="https://github.com/user-attachments/assets/3cc7e437-cdd6-4d76-bf71-c2b242315333" width="200" /> | <img src="https://github.com/user-attachments/assets/17fa258b-aa87-44a9-8c4e-227ece0d30ff" width="200" /> | <img src="https://github.com/user-attachments/assets/10ead6c9-bbdb-459e-8a83-b118f7132ab7" width="200" /> | <img src="https://github.com/user-attachments/assets/7a2bc9b2-2883-413b-99c9-7d5661e9b0e2" width="200" /> |
| <img src="https://github.com/user-attachments/assets/8f28c0ba-00ec-49f3-b4ed-262eccfe32d5" width="200" /> |  |  |  |  |

---

## ✨ Key Features

- **Hybrid Messaging Architecture**  
  Supports both internet-based messaging and offline Bluetooth communication inside one application.

- **Bluetooth RFCOMM Communication**  
  Uses Bluetooth socket-based communication for real-time message exchange between nearby Android devices.

- **Authentication and Online Messaging Flow**  
  Includes separate screens for login, signup, user listing, and internet chat flow.

- **Permission Handling for Modern Android Versions**  
  Handles runtime permissions required for Bluetooth operations, especially on newer Android versions.

- **Reactive UI Updates**  
  Uses lifecycle-aware components and adapter-based list rendering for chat messages and device/user lists.

- **Modular Code Structure**  
  Clear separation between Bluetooth, internet, and shared app components.

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **Platform:** Android
- **Architecture:** MVVM
- **UI:** XML Layouts, Fragments, RecyclerView, View Binding
- **Dependency Injection:** Hilt
- **State / Lifecycle:** ViewModel, LiveData
- **Connectivity:**
  - Bluetooth RFCOMM
  - Cloud-based messaging flow
- **Utilities:** Permission handling, date formatting, item mapping helpers

> If your internet chat module is backed by Firebase, the Firebase badge and mention are accurate. If not, remove the Firebase badge to avoid confusion.

---

## 🧠 Architecture

BChat follows the **MVVM** pattern to separate presentation logic from UI code and keep the application easier to maintain.

### Layers

- **UI Layer**
  - `MainActivity`
  - Fragments for main menu, Bluetooth screens, login/signup, and internet chat

- **Presentation Layer**
  - ViewModels manage UI state, business logic, and chat-related workflows

- **Data Layer**
  - Entity classes represent users, Bluetooth devices, and chat messages

- **Dependency Injection Layer**
  - Hilt manages shared dependencies across the application

- **Utility Layer**
  - Handles permissions, date formatting, and Bluetooth item creation

---

## 🔄 Application Flow

### Main Navigation
1. The app launches from `MainActivity`
2. The user is directed to the main menu
3. The user chooses one of the available communication modes:
   - **Bluetooth Chat**
   - **Internet Chat**

### Bluetooth Chat Flow
1. Open the Bluetooth main page
2. Discover or list nearby devices
3. Select a device
4. Open the Bluetooth chat page
5. Send and receive messages through Bluetooth

### Internet Chat Flow
1. Log in or create an account
2. Open the internet main page
3. Select a user or conversation
4. Open the internet chat page
5. Exchange messages online

---

## 📂 Core Components

### Main
- `MainActivity.kt`  
  Entry point of the application.

- `MainMenu.kt`  
  Provides navigation between Bluetooth and Internet messaging modules.

### Bluetooth Module
- `BluetoothMainPage.kt`  
  Handles device listing and Bluetooth-related interactions.

- `BluetoothChatPage.kt`  
  Displays Bluetooth conversations.

- `BluetoothMessagingViewModel.kt`  
  Manages Bluetooth chat logic and message state.

### Internet Module
- `LoginPage.kt`  
  Handles user authentication.

- `SignUpPage.kt`  
  Handles account registration.

- `InternetMainPage.kt`  
  Displays the main internet chat interface or user list.

- `InternetChatPage.kt`  
  Displays online chat conversations.

- `InternetMainPageViewModel.kt`  
  Manages internet main page logic.

- `InternetChatPageViewModel.kt`  
  Manages internet chat logic.

### Data Models
- `BluetoothMessage.kt`
- `ChatMessage.kt`
- `BluetoothDeviceListItem.kt`
- `UserListItem.kt`

These classes define the core data structures used throughout the app.

### Utilities
- `permissionDataHandler.kt`  
  Handles required runtime permissions for Bluetooth and related functionality.

- `dateFormatter.kt`  
  Formats timestamps for chat messages.

- `createBluetoothItem.kt`  
  Helps map Bluetooth-related data into UI-friendly items.

---

## 🔐 Permissions

This project may require the following permissions depending on the Android version:

- `BLUETOOTH`
- `BLUETOOTH_ADMIN`
- `BLUETOOTH_CONNECT`
- `BLUETOOTH_SCAN`
- `ACCESS_FINE_LOCATION`
- `INTERNET`

Make sure the correct permissions are declared in the manifest and requested at runtime where necessary.

---

## 🚀 Installation

1. Clone the repository:

   ```git
   git clone https://github.com/mustafaerendalgic/chatApp.git
   ```

2. Open the project in **Android Studio**

3. Let **Gradle** sync all dependencies

4. Build and run the application on an Android device or emulator

> For Bluetooth testing, a real Android device is strongly recommended.

---

## ✅ Requirements

- Android Studio
- Kotlin support
- Android SDK
- Internet connection for cloud messaging tests
- Bluetooth-enabled Android device for offline messaging tests

---




- MIT License
- Apache 2.0
- GPL v3
