# iOSAgent - AndroidScript for iOS

Complete iOS implementation of the AndroidScript automation framework with native Swift runtime.

## 📱 Overview

iOSAgent brings the full AndroidScript automation capabilities to iOS devices using:
- **Native Swift Runtime** - Complete interpreter ported from Kotlin (~2,850 lines)
- **XCTest Integration** - UI automation using iOS native APIs
- **SwiftUI Interface** - Modern Material Design-inspired UI
- **Cross-Platform Protocol** - Same scripts run on both Android and iOS

## 🏗️ Architecture

```
iOSAgent/
├── iOSAgentApp.swift          # App entry point
├── Info.plist                 # App configuration & permissions
├── UI/                        # SwiftUI views
│   ├── ContentView.swift      # Main tab view
│   ├── ExecutionView.swift    # Script execution interface
│   ├── DeviceInfoView.swift   # Device information display
│   └── SettingsView.swift     # Configuration options
├── Runtime/                   # AndroidScript interpreter
│   ├── Token.swift            # Token types & lexer tokens
│   ├── Lexer.swift            # Lexical analysis
│   ├── Parser.swift           # Syntax analysis & AST generation
│   ├── AST.swift              # Abstract Syntax Tree definitions
│   ├── Value.swift            # Runtime value system
│   ├── Interpreter.swift      # AST execution engine
│   └── ScriptRunner.swift     # Main execution orchestrator
└── Bridge/                    # Platform integration
    ├── iOSPlatformBridge.swift   # XCTest automation APIs
    └── iOSNativeBridge.swift     # Built-in function bindings
```

## 🚀 Building the Project

### Prerequisites

1. **macOS** with Xcode 15.0 or later
2. **iOS 13.0+** deployment target
3. **Swift 5.7+**
4. Apple Developer account (for device testing)

### Build Steps

#### Option 1: Using Xcode

1. Open the project:
   ```bash
   cd ios-agent
   open iOSAgent.xcodeproj
   ```

2. In Xcode:
   - Select your development team in **Signing & Capabilities**
   - Choose a target device or simulator
   - Press **⌘R** to build and run

#### Option 2: Using Command Line

1. List available simulators:
   ```bash
   xcrun simctl list devices
   ```

2. Build for simulator:
   ```bash
   xcodebuild -project iOSAgent.xcodeproj \
              -scheme iOSAgent \
              -configuration Debug \
              -sdk iphonesimulator \
              -destination 'platform=iOS Simulator,name=iPhone 15,OS=17.0'
   ```

3. Build for device:
   ```bash
   xcodebuild -project iOSAgent.xcodeproj \
              -scheme iOSAgent \
              -configuration Release \
              -sdk iphoneos \
              CODE_SIGN_IDENTITY="iPhone Developer" \
              DEVELOPMENT_TEAM="YOUR_TEAM_ID"
   ```

## 🎯 Using the App

### Execution View

The main interface for running AndroidScript code with pre-loaded examples and real-time execution feedback.

### Device Info View

Displays device capabilities, platform details, and runtime status.

### Settings View

Configure execution behavior, timeouts, and retry logic.

## 📝 Example Scripts

### Basic Device Info
```javascript
$device = GetDeviceInfo()
Print("Platform: " + $device.platform)
Print("Model: " + $device.model)
```

### UI Automation
```javascript
$button = FindByText("Submit")
if ($button != null) {
    Click($button)
}
```

## 📊 Status

**Status**: ✅ iOS Runtime Complete | 🔄 UI Complete | ⏳ Device Testing Pending

**Lines of Code**: ~3,450 total (2,850 runtime + 600 UI)
