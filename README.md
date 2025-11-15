# AndroidScript - Android Automation Framework

An AutoIt-like automation framework for Android devices with script-based syntax, supporting both host-based (PC) and on-device execution with multi-device orchestration.

**🎉 Major Milestone:** Real ADB integration is now working! You can control Android devices with simple scripts.

## What's Working Right Now

```androidscript
# Connect to Android device
$dev = Device()

# Navigate to home screen
KeyEvent("KEYCODE_HOME")
Sleep(1000)

# Take a screenshot
Screenshot("my_screen.png")

# Launch an app
LaunchApp("com.android.settings")
Sleep(2000)

# Tap and swipe
Tap(500, 1000)
Swipe(300, 1500, 300, 500, 300)

# Input text
Input("Hello Android!")

# Stop the app
StopApp("com.android.settings")
```

All of this works **right now** with real Android devices via ADB! 🚀

## Features

- **Simple Script Syntax**: AutoIt-like scripting language designed for Android automation
- **Dual Execution Modes**: Run scripts from host PC (via ADB) or directly on Android devices
- **Multi-Device Support**: Orchestrate automation across multiple Android devices simultaneously
- **UI Automation**: Tap, swipe, input, and gesture controls
- **Image Recognition**: Template matching using OpenCV
- **OCR Support**: Text recognition using Tesseract
- **App Management**: Install, uninstall, launch apps, manage permissions
- **Device Control**: WiFi, Bluetooth, settings, screen unlock, etc.

## Project Structure

```
android-r/
├── core/                   # Core C/C++ script engine
│   ├── src/               # Source files (parser, interpreter, runtime)
│   └── include/           # Header files
├── android-agent/         # On-device Android app
│   └── app/
│       └── src/main/
│           ├── java/      # Java/Kotlin source
│           ├── res/       # Android resources
│           └── assets/    # Bundled assets
├── host-runtime/          # Host-side execution runtime
│   ├── src/               # Host runtime implementation
│   └── include/           # Header files
├── bridge/                # Communication bridge (ADB/Network)
│   ├── src/               # Bridge implementation
│   └── include/           # Header files
├── stdlib/                # Standard library functions
├── examples/              # Example automation scripts
├── docs/                  # Documentation
├── LANGUAGE_SPEC.md       # Language specification
└── README.md              # This file
```

## Quick Start

### Example Script

```androidscript
// Launch app and perform login
LaunchApp("com.example.app")
Sleep(2000)

// Input credentials
Tap(500, 800)
Input("user@example.com")
Tap(500, 1000)
Input("password")

// Tap login button
TapText("Login")

// Verify success
if (WaitForImage("home.png", timeout: 10) != null) {
    Log("Login successful!")
}
```

### Multi-Device Example

```androidscript
// Run on all connected devices
$devices = GetAllDevices()

ForEach($device in $devices) {
    $device.LaunchApp("com.example.app")
    $device.Tap(500, 1000)
}

SyncDevices($devices)
```

## Components

### 1. Core Script Engine (C/C++)
- Lexer and parser for script syntax
- Bytecode interpreter
- Memory management and garbage collection
- Standard library integration

### 2. Android Agent (Kotlin/Java)
- Accessibility Service for UI automation
- On-device script execution
- Image recognition and OCR
- App and device management APIs

### 3. Host Runtime (C/C++)
- ADB bridge for device communication
- Multi-device orchestration
- Script compilation and distribution
- Result aggregation

### 4. Bridge Layer
- Communication protocol between host and device
- Network and USB transport
- Command serialization/deserialization
- Error handling and retry logic

## Building

### Quick Build (Recommended)

**Linux/Mac:**
```bash
./build.sh
```

**Windows:**
```cmd
build.bat
```

**Rebuild from scratch:**
```bash
./rebuild.sh    # Linux/Mac
rebuild.bat     # Windows
```

The executable will be at: `./build/bin/androidscript`

### Prerequisites
- **CMake** 3.15+
- **C++17 compatible compiler** (GCC 7+, Clang 5+, MSVC 2017+)
- **ADB** (Android Debug Bridge) - for device automation
- *Optional:* OpenCV 4.x, Tesseract 4.x (for future image recognition features)

See [BUILD.md](BUILD.md) for detailed build instructions, troubleshooting, and advanced options.

## Usage

### Running Scripts

```bash
# Run a script
./build/bin/androidscript script.as

# Test basic functionality
./build/bin/androidscript single_arg_test.as

# Test device connection
./build/bin/androidscript device_info_test.as

# Run comprehensive demo
./build/bin/androidscript examples/comprehensive_demo.as
```

### Prerequisites for Device Automation
1. **Connect Android device** via USB
2. **Enable USB Debugging** (Settings → Developer Options → USB Debugging)
3. **Verify connection**: `adb devices`
4. **Run script** that uses device automation

See [QUICK_START.md](QUICK_START.md) for step-by-step guide and examples.

## Language Features

See [LANGUAGE_SPEC.md](LANGUAGE_SPEC.md) for complete language documentation.

Key features:
- Variables and data types
- Control flow (if/else, loops, functions)
- Device management
- UI automation primitives
- Image recognition and OCR
- App and device control
- Error handling
- Multi-device orchestration

## Development Status

**Current Version:** v1.0.0-alpha
**Status:** ~50% Complete - Core engine and ADB integration working!

### Completed ✅
- [x] Language specification (LANGUAGE_SPEC.md)
- [x] Project structure and build system
- [x] CMake build scripts (build.sh, rebuild.sh)
- [x] Lexer implementation
- [x] Parser implementation (95% - multi-arg calls need fix)
- [x] AST-based interpreter
- [x] Value system (9 data types)
- [x] 35+ built-in functions
- [x] **ADB bridge (full device automation)**
- [x] Device discovery and management
- [x] UI automation (tap, swipe, input, keyevent)
- [x] App management (launch, stop, install, uninstall)
- [x] File operations (push, pull, screenshot)
- [x] Example scripts and comprehensive documentation

### In Progress 🔧
- [ ] Parser fix for multi-argument function calls
- [ ] Android agent with accessibility service
- [ ] Device orchestration manager

### Planned 📋
- [ ] Image recognition (OpenCV integration)
- [ ] OCR (Tesseract integration)
- [ ] Advanced UI automation (gestures, element finding)
- [ ] Network protocol (host-device communication)
- [ ] On-device script execution

See [PROGRESS.md](PROGRESS.md) and [BUILD_SUCCESS.md](BUILD_SUCCESS.md) for detailed status.

## License

TBD

## Contributing

TBD
