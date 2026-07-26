# MyRuns - Android Exercise Tracker

MyRuns is a comprehensive Android application designed to help users track their physical activities. Whether manually logging a gym session or using GPS for a cross-country run, MyRuns provides a seamless experience for monitoring progress and history.

## 🚀 Features

### 1. Activity Tracking
- **Manual Entry**: Log activities like Swimming, Cycling, or Yoga by entering duration, distance, and calories manually.
- **GPS Tracking**: Real-time tracking of outdoor activities (Running, Walking, Hiking) with live map visualization using **Google Maps SDK**.
- **Automatic Recognition**: Leverages on-device sensors (Accelerometer) and machine learning (Weka Classifier) to automatically detect whether you are Standing, Walking, or Running.

### 2. History & Insights
- **Exercise History**: A centralized list of all past activities, displaying key stats like distance, duration, and pace.
- **Detailed View**: Revisit any past activity to see the specific path taken on the map or review manual notes.
- **Statistics**: Automatic conversion between Metric (km) and Imperial (miles) units based on user preference.

### 3. User Personalization
- **Profile Management**: Customize your user profile with name, email, phone number, and a profile picture (captured via Camera or Gallery).
- **Settings**: Toggle privacy settings and manage unit preferences.

---

## 🛠️ Architecture & Tech Stack

The project follows the **MVVM (Model-View-ViewModel)** architecture pattern to ensure a clean separation of concerns and maintainability.

- **Language**: Kotlin
- **UI Framework**: XML Layouts with Material Design 3 components.
- **Navigation**: ViewPager2 with TabLayout for main screen navigation.
- **Database**: **Room Persistence Library** for local data storage and offline access.
- **Concurrency**: **Kotlin Coroutines** and **Flow** for asynchronous database operations and location updates.
- **Background Processing**: **Foreground Services** for reliable activity tracking while the app is in the background (Android 14+ compliant).
- **ML/Signal Processing**: Custom FFT implementation and Weka Classifier for sensor-based activity recognition.
- **API**: Google Maps Platform for location visualization.

---

## 📸 Screenshots

| Start Screen | History | Profile |
| :---: | :---: | :---: |
| ![Start](/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png) | ![History](/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png) | ![Profile](/app/src/main/res/mipmap-xxxhdpi/ic_launcher.png) |
*(Note: Placeholder icons used. Replace with actual screenshots in the root directory)*

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Ladybug (or newer)
- Android SDK 35
- Google Maps API Key

### Configuration
1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/MyRuns.git
   ```
2. **Add API Key**:
   Create a `local.properties` file in the root directory and add your Google Maps API key:
   ```properties
   MAPS_API_KEY=YOUR_API_KEY_HERE
   ```
3. **Build & Run**:
   Open the project in Android Studio, sync Gradle, and run on an emulator or physical device.

---

## 👨‍💻 Developer
**Nam Nguyen**

---
*Developed as part of the Android Development curriculum.*
