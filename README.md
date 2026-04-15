# 🚘 Telematics Scanner

A sleek and efficient Android application designed to read, display, and log vehicle telematics and diagnostic data. This app serves as a digital dashboard and OBD (On-Board Diagnostics) scanner interface, allowing users to monitor vehicle performance and troubleshoot engine codes on the go.

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Language-Java-007396?style=for-the-badge&logo=java&logoColor=white)
![Room Database](https://img.shields.io/badge/Database-Room-0081CB?style=for-the-badge&logo=sqlite&logoColor=white)

## ✨ Key Features

The application is built with a modern Bottom Navigation architecture, divided into three core functionalities:

* **📊 Dashboard (`DashboardFragment`):** Provides a real-time interface for viewing live vehicle telemetry data (e.g., Speed, RPM, Engine Load).
* **🛠️ Diagnostics (`DiagnosticsFragment`):** Scans and lists Diagnostic Trouble Codes (DTCs). Includes a custom `DiagnosticAdapter` to present fault codes clearly so users can identify specific vehicle issues.
* **📜 History Log (`HistoryFragment`):** Maintains a historical record of previous scans and telemetry data. Utilizes an offline **Room Database** (`AppDatabase`, `TelemetryDao`, `TelemetryLog`) to ensure users can review past performance and faults even without an internet connection.

## 🛠️ Technology Stack

* **Language:** Java
* **UI Architecture:** Fragments & Bottom Navigation View
* **Local Storage:** Room Persistence Library (SQLite abstraction)
* **View Components:** RecyclerView (for efficient list rendering in History and Diagnostics)
* **Build System:** Gradle

## 📂 Project Architecture

The codebase follows a clean, modular structure:

```text
app/src/main/java/com/example/telematicsscanner/
├── activity/       # Contains MainActivity hosting the fragments
├── adapter/        # RecyclerView adapters (e.g., DiagnosticAdapter)
├── database/       # Room DB components (Entities, DAOs, Database class)
├── fragments/      # UI controllers for Dashboard, Diagnostics, and History
└── model/          # POJO data models (e.g., DiagnosticCode)
```
🚀 Getting Started
Prerequisites
Android Studio: Hedgehog or newer recommended.

Android SDK: API Level 24 (Nougat) or higher.

Java SDK: Java 8 or higher.

Installation & Setup
Clone the repository:

Bash
git clone [https://github.com/mvinduwara/telematics-scanner.git](https://github.com/mvinduwara/telematics-scanner.git)
cd telematics-scanner
Open in Android Studio:

Launch Android Studio.

Select File > Open and navigate to the cloned telematics-scanner directory.

Wait for Gradle to finish syncing.

Build and Run:

Connect a physical Android device via USB (with USB Debugging enabled) or start an Android Virtual Device (AVD).

Click the Run button (Shift + F10) to compile and install the app.

👨‍💻 Author
Manilka Vinduwara

GitHub Profile

Email: dev.manilkavinduwara@gmail.com
