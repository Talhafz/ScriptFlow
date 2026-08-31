# ScriptFlow

ScriptFlow is a professional-grade, native Android teleprompter application designed for video creators who require reliability and precision. Built with a modern technical stack, it provides a high-performance scrolling engine and a distraction-free environment to help speakers deliver their message naturally and effectively.

## Core Philosophy

Most teleprompter apps suffer from stuttering text or complex interfaces that distract the speaker. ScriptFlow was engineered with a "Performance First" mindset. By utilizing hardware-accelerated rendering and frame-accurate timing, the app ensures that text movement is perfectly smooth, regardless of script length or device hardware.

## Key Features

### Professional Playback Engine
- Horizontal News-Ticker Mode: Optimized for landscape recording where text slides smoothly from right to left in a single line.
- Frame-Accurate Scrolling: Implemented using a monotonic frame clock to eliminate jitter and ensure consistent speed.
- Reading Zone Guides: Integrated vertical markers to help the speaker maintain a consistent eye line.
- Mirror Mode: Built-in horizontal graphic transformation for professional beam-splitter glass hardware.

### Intelligent Script Management
- Live Metadata: Real-time calculation of word count, character count, and estimated duration as you type.
- Auto-Save Engine: A debounced persistence layer that secures your work without interrupting your creative flow.
- Reactive Dashboard: A centralized home for your scripts with support for instant search, duplication, and management.

### Deep Customization
- Real-Time Settings Preview: An interactive configuration screen that shows exactly how your text will look before you start recording.
- Theme Presets: High-contrast, dark, and classic modes designed for various lighting conditions.
- Granular Control: Adjust WPM (Words Per Minute), font size, line spacing, and text alignment to suit your reading style.

## Technical Architecture

The project follows Clean Architecture principles combined with the MVVM (Model-View-ViewModel) pattern and Unidirectional Data Flow (UDF). This ensures the codebase is testable, maintainable, and scalable.

### Layer Breakdown
- Domain Layer: Contains pure Kotlin business logic, models, and use cases, free from Android platform dependencies.
- Data Layer: Manages local persistence using Room (SQL) for scripts and Jetpack DataStore for user preferences.
- UI Layer: Built entirely with Jetpack Compose and Material 3, utilizing StateFlow for reactive UI updates.

### Tech Stack
- Language: Kotlin
- UI Framework: Jetpack Compose (Material 3)
- Dependency Injection: Hilt
- Database: Room
- Preferences: Jetpack DataStore
- Concurrency: Kotlin Coroutines and Flow
- Navigation: Navigation Compose
- Optimization: R8/ProGuard and GPU-accelerated graphics layers

## Installation

1. Clone this repository.
2. Open the project in the latest version of Android Studio.
3. Synchronize the Gradle files to download the required dependencies.
4. Build and run the app on an Android device or emulator (API 24 or higher).

## Development Roadmap

While the core engine is complete, future iterations will focus on:
- Bluetooth Remote Support: Allowing external controllers to pause or adjust speed.
- Cloud Sync: Optional synchronization across devices.
- Video Overlay: A floating teleprompter window for recording directly on the mobile device.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
