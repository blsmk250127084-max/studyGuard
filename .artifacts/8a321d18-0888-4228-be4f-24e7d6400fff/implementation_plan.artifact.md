# StudyGuard: Comprehensive UI/UX Redesign - Implementation Plan

StudyGuard will undergo a complete UI/UX redesign into a premium, modern dark-themed productivity application inspired by Forest, Todoist, and Notion. All existing business logic (MVVM, Room Database, StateFlows, Proximity & Motion sensors, Grace Period countdown, and Alarms) will remain 100% preserved and fully functional.

## Design Specifications & Color Palette

- **Primary Background**: `#0B1020`
- **Secondary Background**: `#111827`
- **Card Background**: `#182235`
- **Primary Blue**: `#7C9DFF`
- **Secondary Purple**: `#9B8CFF`
- **Accent Cyan**: `#5DD6E8`
- **Text Primary**: `#F8FAFC`
- **Text Secondary**: `#94A3B8`
- **Success Green**: `#4ADE80`
- **Warning Yellow**: `#FBBF24`
- **Violation Red**: `#EF4444`
- **Primary Action Gradient**: `Brush.horizontalGradient(listOf(Color(0xFF7C9DFF), Color(0xFF9B8CFF)))`

---

## Proposed Changes

### UI & Theme Layer

#### [MODIFY] [Color.kt](file:///C:/Users/zanra/AndroidStudioProjects/studyGuard/app/src/main/java/com/example/studyguard/ui/theme/Color.kt)
- Define new dark productivity color palette (BgMain, BgSecondary, CardBg, PrimaryBlue, SecondaryPurple, AccentCyan, TextPrimary, TextSecondary, SuccessGreen, WarningYellow, ViolationRed).

#### [MODIFY] [Theme.kt](file:///C:/Users/zanra/AndroidStudioProjects/studyGuard/app/src/main/java/com/example/studyguard/ui/theme/Theme.kt)
- Update `StudyGuardTheme` to enforce the modern dark color scheme across the entire app with proper surface, card, and background colors.

#### [NEW] [HomeScreen.kt](file:///C:/Users/zanra/AndroidStudioProjects/studyGuard/app/src/main/java/com/example/studyguard/ui/screens/HomeScreen.kt)
- Create Dashboard Screen featuring:
  - Greeting header ("Halo, Pejuang Fokus! 👋") & Today's date.
  - Daily Study Stats Card (Total minutes studied today, completed sessions count).
  - Daily Goal Progress Card with circular/linear progress indicator.
  - Recent Study Activity list from Room database.
  - Quick Action button to launch a focus session (navigates to Focus tab).

#### [MODIFY] [FocusScreen.kt](file:///C:/Users/zanra/AndroidStudioProjects/studyGuard/app/src/main/java/com/example/studyguard/ui/screens/FocusScreen.kt)
- Comprehensive redesign:
  - **IDLE State (Focus Setup)**:
    - Shield logo header + "StudyGuard" typography + subtitle "Your personal study companion" + "Ready to focus?" greeting.
    - Topic Input Card ("WHAT ARE YOU STUDYING?") with notebook icon and placeholder.
    - Duration Selector Chips (15m, 25m, 45m, 60m + 1m Demo for quick testing) with gradient selection highlights.
    - Focus Summary Card (Topic, Duration, Estimated completion time).
    - Primary Gradient Button ("Mulai Sesi Fokus") with Play icon and click elevation.
  - **PREPARING State**: 3-second countdown overlay with phone placement guidance.
  - **FOCUSING State**: Large animated gradient timer ring, MM:SS countdown, sensor status pills (Proximity & Motion), Pause/Resume, and Cancel actions.
  - **VIOLATION State**: Bold red warning overlay with warning icon, breach cause, and 5-second grace period circular countdown.
  - **COMPLETED / FAILED States**: Victory/Failure summary cards with action buttons.

#### [MODIFY] [HistoryScreen.kt](file:///C:/Users/zanra/AndroidStudioProjects/studyGuard/app/src/main/java/com/example/studyguard/ui/screens/HistoryScreen.kt)
- Redesign history screen to match the dark productivity palette:
  - Summary cards (Total Minutes, Success Rate %, Completed vs Failed sessions).
  - Elegant list items with subject, timestamp, target vs achieved duration, alarm count, and status badges.
  - History deletion controls.

#### [NEW] [SettingsScreen.kt](file:///C:/Users/zanra/AndroidStudioProjects/studyGuard/app/src/main/java/com/example/studyguard/ui/screens/SettingsScreen.kt)
- Create Settings / App Info screen:
  - App Version & Description.
  - Sensor explanation cards (Proximity Sensor & Linear Acceleration Motion Sensor).
  - Option to clear all study history.

#### [MODIFY] [MainScreen.kt](file:///C:/Users/zanra/AndroidStudioProjects/studyGuard/app/src/main/java/com/example/studyguard/ui/navigation/MainScreen.kt)
- Update Bottom Navigation Scaffold to 4 tabs:
  1. `Home` (Dashboard) -> `HomeScreen`
  2. `Focus` (Timer Setup) -> `FocusScreen`
  3. `History` (Stats & Logs) -> `HistoryScreen`
  4. `Settings` (Options & Info) -> `SettingsScreen`
- Custom dark navigation bar with active primary highlights and smooth tab switching.

---

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleDebug`) to ensure error-free compilation.

### Manual Verification
- Deploy to device/emulator.
- Test 4-tab bottom navigation flow.
- Verify Focus setup, topic input, duration selection, summary card, 3s prep countdown, active timer ring, real-time sensor triggers, 5s grace period, alarm/vibration, database logging, dashboard stats, history list, and settings tab.
