# TimeManagerForJob

**TimeManagerForJob** is an Android application for tracking work time with Google authentication support. It is designed for freelancers, self-employed professionals, and specialists who need to monitor actual working hours, manage weekends, and access detailed time statistics.

---

## 🧰 Key Features

* Google Sign-In authentication (all user data is linked to their email);
* Smart calendar:

  * Automatic highlighting of Saturdays and Sundays as weekends;
  * Ability for users to manually mark custom days off;
  * Current day is always highlighted;
* Work session tracking:

  * "Start Work" button launches a timer;
  * Supports pause/resume functionality;
  * Session finish saves data and updates statistics;
* Statistics screen:

  * Daily, weekly, and monthly views;
  * Displays total and average work time;
  * Tracks breaks;
  * Calculates potential earnings based on hourly rate;
* Settings screen:

  * Configure hourly rates for weekdays and weekends;
  * Switch account (sign out);
* Export statistics to Excel.

---

## 📁 Project Structure

```
timemanagerforjob/
├── auth/                 # Authentication (Google ID, repository, UI)
├── data/                 # Data source layer
│   ├── local/            # Local database (Room)
│   │   ├── dao/          # Data access interfaces
│   │   ├── database/     # DB configuration
│   │   ├── entity/       # Room entity models
│   │   ├── mapper/       # Entity ↔ Domain converters
│   ├── repository/       # Repository implementations
├── di/                   # Dagger Hilt modules
├── domain/               # Business logic layer
│   ├── model/            # Domain models
│   ├── repository/       # Repository interfaces
│   ├── usecases/         # Use case definitions
├── presentation/         # UI layer (Jetpack Compose)
│   ├── main/             # MainActivity and navigation
│   ├── navigation/       # Bottom navigation
│   ├── settings/         # Settings screen
│   ├── statistics/       # Statistics screen
│   ├── work/             # Work screen and calendar
│   │   ├── components/   # Reusable components for work screen
├── services/             # Supporting services (e.g., notifications)
├── ui/                   # App theming and design
│   ├── theme/            # Colors, typography, etc.
├── utils/                # Utility classes
│   ├── events/           # Event handling
│   ├── formatters/       # Date/time formatters
│   ├── notifications/    # Notification utilities
│   ├── preferences/      # SharedPreferences management
├── widget/               # Custom UI components (if any)
```

---

## ⚙️ Technologies Used

* **Kotlin**, **Jetpack Compose**;
* **Room** (local database);
* **Dagger Hilt** (dependency injection);
* **Google Identity Services** (authentication);
* **AndroidX**;
* **MVVM + Clean Architecture**;
* **Excel export** using Apache POI or similar.

---

## 🚀 Getting Started

1. Install Android Studio (latest stable version recommended);
2. Clone the repository:

   ```bash
   git clone https://github.com/Bphoenix134/TimekeepingForJob
   ```
3. Provide your `web_client_id` in `res/values/strings.xml`:

   ```xml
   <string name="web_client_id">your-google-client-id</string>
   ```
4. Build and run the application on an emulator or physical device.

---

## 📌 Notes

* All user data is stored locally and associated with the authenticated Google account (via email);
* This project does not include a license (you can optionally add one, e.g., MIT, Apache 2.0).

---

## 💬 Feedback

For questions, bugs, or suggestions, please create an issue or contact the developer via email.

---
