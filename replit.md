# BlockRemote — Sentinel Defense System

## Overview
BlockRemote is a cybersecurity Android app built with Kotlin/Jetpack Compose, designed to detect and block unauthorized remote access attempts on Android devices. The project includes the full Android source code, a FastAPI backend integration layer, and an interactive web-based preview.

## Architecture
**Clean Architecture (MVVM)** with the following package structure:

### Kotlin Source (`app/src/main/java/com/blockremote/`)
- `ui/theme/` — Material 3 Cyber theme (Color, Typography, Theme)
- `ui/components/` — Reusable Composables: NeonCard, CyberButton, SentinelSwitch, SubscriptionOverlay
- `ui/screens/` — 5 screens: Dashboard, SensorMonitor, AppShield, AuditLogs, Settings
- `data/sensors/` — SensorRepository (accelerometer, gyroscope, magnetometer via Flow)
- `data/network/` — ApiClient (Retrofit + auth interceptor), BlockRemoteApi, SentinelWebSocket (OkHttp + Zero-Trust auth)
- `data/local/` — SessionManager (JWT token + device ID + local trial tracking via SharedPreferences)
- `data/billing/` — BillingRepository (HTTP 402/403 handling, offline fallback)
- `domain/usecases/` — DetectThreatUseCase, MonitorSensorsUseCase
- `viewmodel/` — BlockRemoteViewModel (AndroidViewModel) with billing check, WebSocket lifecycle, 30s heartbeat, server command handlers
- `navigation/` — NavGraph with animated transitions

### Backend Integration
- **API Base**: `https://api.blockremote.io/v1/` (configurable via BuildConfig)
- **Auth**: JWT Bearer token via OkHttp interceptor
- **WebSocket**: `wss://api.blockremote.io/v1/signals` with Zero-Trust auth handshake
- **Billing Middleware**: HTTP 402 (payment required) / 403 (forbidden) handling
- **Server Commands**: LOCK_SYSTEM, FORCE_LOGOUT, BILLING_EXPIRED, STATUS_SAFE, STATUS_AUDIT
- **License States**: LOADING, ACTIVE, TRIAL, PAYWALL, LOCKED, OFFLINE
- **Dependencies**: Retrofit 2.9.0, OkHttp 4.12.0, Gson 2.10.1

### Web Preview (`preview/`)
- Express.js server serving an interactive HTML/CSS/JS preview of the app
- Mimics all 5 screens with real-time sensor wave animations using Canvas API
- Includes WebSocket status bar, subscription overlay, billing simulation, heartbeat
- Runs on port 5000

## Design System
- **Background**: Pure Black `#000000`
- **Surface**: Carbon Grey `#0d1117`
- **Primary Neon**: Matrix Green `#00ff41`
- **Secondary Neon**: Cyber Lime `#adff2f`
- **Text**: Off-White `#f1f2f1`
- **Alert**: Red `#ff0040`
- **Font**: JetBrains Mono

## Dependencies
- Node.js 20 (for web preview server)
- Express (web framework for preview)
- Kotlin (Android source code)
- Jetpack Compose BOM 2024.01.00
- Navigation Compose 2.7.6
- Lifecycle ViewModel Compose 2.7.0
- Retrofit 2.9.0 + Gson Converter
- OkHttp 4.12.0 + Logging Interceptor

## Build Configuration
- `build.gradle.kts` — Root Gradle config (AGP 8.2.0, Kotlin 1.9.20)
- `app/build.gradle.kts` — App module (minSdk 26, targetSdk 34, Compose 1.5.4, buildConfig enabled)
- `settings.gradle.kts` — Project settings
- `gradle.properties` — JVM args and AndroidX config

## AndroidManifest Permissions
- `INTERNET` — Network access for API and WebSocket
- `ACCESS_NETWORK_STATE` — Network connectivity checks
- `HIGH_SAMPLING_RATE_SENSORS` — High-frequency sensor polling
- `usesCleartextTraffic="false"` — HTTPS enforced

## Screens
1. **Dashboard** — Animated radar (Canvas) with sweep beam, concentric rings, compass markers, signal blips, pulse rings, bearing/range HUD. Billing status check on launch. Threat simulation with haptic feedback.
2. **Sensor Monitor** — Real-time accelerometer/gyroscope waveforms via Canvas
3. **App Shield** — Permission list with SentinelSwitch toggle and risk badges
4. **Audit Logs** — Terminal-style log viewer with color-coded severity levels
5. **Settings** — Threshold sliders, module toggles, Connection card (relay/license/heartbeat), system info
