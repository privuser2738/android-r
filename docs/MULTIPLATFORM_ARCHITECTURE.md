# Multi-Platform Architecture

## Overview

AndroidScript is being evolved into a **cross-platform automation framework** supporting:
- ✅ **Android** (via AccessibilityService)
- 🔄 **iOS** (via XCTest/Accessibility)
- 🔄 **Other platforms** (extensible design)

## Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                    Host Controller                       │
│  (Desktop/Server - manages all connected devices)       │
├─────────────────────────────────────────────────────────┤
│              Platform Detection & Routing                │
│           (ADB for Android, libimobiledevice for iOS)   │
├─────────────────────────────────────────────────────────┤
│                  Unified Protocol Layer                  │
│        (JSON-RPC over TCP/USB for communication)        │
├──────────────────┬──────────────────┬───────────────────┤
│  Android Agent   │    iOS Agent     │   Future Agents   │
│  (Kotlin/Java)   │  (Swift/ObjC)    │   (...)          │
├──────────────────┼──────────────────┼───────────────────┤
│                Script Interpreter (Shared)               │
│       (Core engine - portable across platforms)         │
├──────────────────┼──────────────────┼───────────────────┤
│ Platform Bridge  │ Platform Bridge  │ Platform Bridge   │
│ (Accessibility)  │ (XCTest/XCUIAut) │   (...)          │
└──────────────────┴──────────────────┴───────────────────┘
```

## Component Breakdown

### 1. Host Controller (New)
- **Location**: `/host-controller/`
- **Language**: Kotlin/C++
- **Responsibilities**:
  - Detect connected devices (Android via ADB, iOS via libimobiledevice)
  - Route scripts to appropriate platform agents
  - Aggregate results from multiple devices
  - Provide unified API for scripting

### 2. Platform Abstraction Layer
- **Location**: `/common/platform-interface/`
- **Language**: Protocol definitions (JSON)
- **Defines**:
  - Standard automation primitives (tap, swipe, find, etc.)
  - Common data structures (Element, Bounds, etc.)
  - Error codes and responses

### 3. Android Agent (Existing)
- **Location**: `/android-agent/`
- **Status**: ✅ Complete
- **Features**: Full accessibility-based automation

### 4. iOS Agent (New)
- **Location**: `/ios-agent/`
- **Language**: Swift + Objective-C
- **Implementation**: XCTest + Accessibility APIs
- **Requires**:
  - iOS 13+ for full accessibility
  - Developer certificate for installation
  - Optional: WebDriverAgent integration

### 5. Script Interpreter (Portable)
- **Location**: `/core/` (C++) and platform-specific ports
- **Status**: ✅ Android (Kotlin), 🔄 iOS (Swift)
- **Design**: Core interpreter logic is platform-agnostic

## Platform-Specific Considerations

### Android
- ✅ Accessibility Service (system-level automation)
- ✅ Works on all Android versions (API 21+)
- ✅ No root required
- ✅ Can automate any app

### iOS
- 🔄 XCTest Framework (requires app bundling)
- 🔄 UIAutomation (deprecated but still works)
- 🔄 WebDriverAgent (Appium backend)
- ⚠️ Requires developer certificate
- ⚠️ More restrictive than Android

### Future Platforms
- HarmonyOS (Huawei)
- KaiOS (feature phones)
- Web (browser automation)

## Communication Protocol

### JSON-RPC 2.0 Format

**Request:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "automation.tap",
  "params": {
    "x": 500,
    "y": 1000
  }
}
```

**Response:**
```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "success": true
  }
}
```

### Standard Methods

| Method | Description | Params | Platforms |
|--------|-------------|--------|-----------|
| `automation.tap` | Tap at coordinates | x, y | All |
| `automation.swipe` | Swipe gesture | x1, y1, x2, y2, duration | All |
| `automation.findByText` | Find element by text | text | All |
| `automation.click` | Click element | elementId | All |
| `automation.inputText` | Type text | text | All |
| `device.getInfo` | Get device info | - | All |
| `script.execute` | Run script | source | All |

## Unified Script Example

```androidscript
// This script runs on ANY platform!

// Get device info
$device = GetDeviceInfo()
Print("Running on: " + $device.platform)

// Find and click button (works on Android, iOS, etc.)
$button = FindByText("Submit")
if ($button != null) {
    Click($button)
    Print("Clicked submit button")
}

// Platform-specific behavior
if ($device.platform == "iOS") {
    // iOS-specific action
    PressHome()
} else if ($device.platform == "Android") {
    // Android-specific action
    PressBack()
}
```

## Device Discovery

### Android (ADB)
```bash
adb devices -l
# List[0015F9C711001234] = {
#   platform: "Android",
#   model: "Pixel 6",
#   version: "14"
# }
```

### iOS (libimobiledevice)
```bash
idevice_id -l
# List[00008030-001234567890ABCD] = {
#   platform: "iOS",
#   model: "iPhone 14",
#   version: "17.2"
# }
```

## Installation & Deployment

### Android Agent
```bash
# Build and install
./gradlew assembleDebug
adb install -r app-debug.apk
# Enable accessibility service in settings
```

### iOS Agent
```bash
# Build with Xcode
cd ios-agent
xcodebuild -scheme iOSAgent -configuration Debug
# Install via Xcode or ios-deploy
ios-deploy --bundle iOSAgent.app
```

### Host Controller
```bash
# Run on desktop/server
./host-controller --listen 0.0.0.0:8080
# Discovers all connected devices automatically
```

## Development Roadmap

### Phase 1: Foundation (Current)
- [x] Android agent with interpreter
- [x] Download manager
- [ ] Protocol definition
- [ ] Host controller base

### Phase 2: iOS Support
- [ ] iOS agent app structure
- [ ] Swift interpreter port
- [ ] XCTest automation bridge
- [ ] iOS-specific APIs

### Phase 3: Unified Control
- [ ] Multi-device orchestration
- [ ] Cross-platform scripts
- [ ] Device farm support
- [ ] Web dashboard

### Phase 4: Advanced Features
- [ ] Computer vision (screen analysis)
- [ ] AI-powered element detection
- [ ] Cloud deployment
- [ ] Plugin system

## Directory Structure

```
android-r/
├── android-agent/          # Android app (Kotlin)
├── ios-agent/              # iOS app (Swift) [NEW]
├── core/                   # C++ interpreter core
├── host-controller/        # Desktop controller [NEW]
├── common/
│   ├── protocol/           # Protocol definitions [NEW]
│   └── models/             # Shared data models [NEW]
├── scripts/                # Example scripts
└── docs/                   # Documentation
```

## Contributing

When adding a new platform:

1. Create `/[platform]-agent/` directory
2. Implement platform bridge (extend `PlatformBridge` interface)
3. Port/integrate interpreter
4. Register platform in host controller
5. Add platform-specific tests
6. Update documentation
