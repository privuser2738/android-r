# AndroidScript Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         User                                │
│                    (Writes Scripts)                         │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        │ .as script files
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    Host Runtime (PC)                        │
│  ┌───────────────────────────────────────────────────────┐ │
│  │  CLI Tool (host-runtime/)                              │ │
│  │  • Load scripts                                        │ │
│  │  • Parse command line args                            │ │
│  │  • Coordinate execution                               │ │
│  └──────────────┬────────────────────────────────────────┘ │
│                 │                                           │
│  ┌──────────────▼────────────────────────────────────────┐ │
│  │  Script Engine (core/)                                 │ │
│  │  • Lexer → Tokenization                               │ │
│  │  │ Parser → AST Generation                            │ │
│  │  │ Interpreter → Execution                            │ │
│  │  └─→ Built-in Functions                               │ │
│  └──────────────┬────────────────────────────────────────┘ │
│                 │                                           │
│  ┌──────────────▼────────────────────────────────────────┐ │
│  │  Device Orchestrator                                   │ │
│  │  • Multi-device management                            │ │
│  │  • Parallel execution                                 │ │
│  │  • Synchronization                                    │ │
│  └──────────────┬────────────────────────────────────────┘ │
│                 │                                           │
│  ┌──────────────▼────────────────────────────────────────┐ │
│  │  ADB Bridge (bridge/)                                  │ │
│  │  • Device discovery                                   │ │
│  │  • Command transmission                               │ │
│  │  • Response handling                                  │ │
│  └──────────────┬────────────────────────────────────────┘ │
└─────────────────┼─────────────────────────────────────────┘
                  │
                  │ ADB Protocol / Network
                  │ (USB or WiFi)
                  │
       ┌──────────┴────────────┐
       │                       │
       ▼                       ▼
┌─────────────────┐     ┌─────────────────┐
│  Android Device │     │  Android Device │  ... N devices
│                 │     │                 │
│ ┌─────────────┐ │     │ ┌─────────────┐ │
│ │ Agent App   │ │     │ │ Agent App   │ │
│ └─────────────┘ │     │ └─────────────┘ │
│                 │     │                 │
│ ┌─────────────┐ │     │ ┌─────────────┐ │
│ │Accessibility│ │     │ │Accessibility│ │
│ │  Service    │ │     │ │  Service    │ │
│ │             │ │     │ │             │ │
│ │ • UI        │ │     │ │ • UI        │ │
│ │   Automation│ │     │ │   Automation│ │
│ │ • Gestures  │ │     │ │ • Gestures  │ │
│ │ • Input     │ │     │ │ • Input     │ │
│ └─────────────┘ │     │ └─────────────┘ │
│                 │     │                 │
│ ┌─────────────┐ │     │ ┌─────────────┐ │
│ │ Computer    │ │     │ │ Computer    │ │
│ │ Vision      │ │     │ │ Vision      │ │
│ │             │ │     │ │             │ │
│ │ • OpenCV    │ │     │ │ • OpenCV    │ │
│ │ • Tesseract │ │     │ │ • Tesseract │ │
│ │ • OCR       │ │     │ │ • OCR       │ │
│ └─────────────┘ │     │ └─────────────┘ │
│                 │     │                 │
│ ┌─────────────┐ │     │ ┌─────────────┐ │
│ │ App Manager │ │     │ │ App Manager │ │
│ │             │ │     │ │             │ │
│ │ • Install   │ │     │ │ • Install   │ │
│ │ • Uninstall │ │     │ │ • Uninstall │ │
│ │ • Permissions│     │ │ • Permissions│ │
│ └─────────────┘ │     │ └─────────────┘ │
└─────────────────┘     └─────────────────┘
```

## Execution Modes

### Mode 1: Host-Based Execution
```
Script.as → Host Runtime → ADB → Android Device → Execute → Results
                                                              │
                                                              ▼
                                                        Host Runtime
```

**Advantages:**
- Easy development on PC
- Powerful host resources
- No device modification needed
- Multi-device orchestration

**Use Cases:**
- Testing labs
- CI/CD integration
- Multi-device testing
- Development and debugging

### Mode 2: On-Device Execution
```
Script.as → Copy to Device → Android Agent → Execute Locally
                                   │
                                   ▼
                              Local Results
```

**Advantages:**
- No PC connection required
- Faster execution
- Standalone operation
- Lower latency

**Use Cases:**
- Field testing
- Automated demos
- Standalone automation
- Offline scenarios

---

## Component Details

### 1. Core Script Engine

**Lexer** (`core/src/lexer.cpp`)
```
Source Code → [Lexer] → Token Stream
"Tap(500, 1000)" → [IDENTIFIER("Tap"), LPAREN, INTEGER(500), COMMA, INTEGER(1000), RPAREN]
```

**Parser** (`core/src/parser.cpp`)
```
Token Stream → [Parser] → Abstract Syntax Tree (AST)
[Tokens] → CallExpr { callee: "Tap", args: [500, 1000] }
```

**Interpreter** (`core/src/interpreter.cpp`)
```
AST → [Interpreter] → Execution
CallExpr → Find function "Tap" → Execute with args [500, 1000]
```

**Value System** (`core/src/value.cpp`)
```cpp
class Value {
    enum Type { INT, FLOAT, STRING, BOOL, ARRAY, OBJECT, DEVICE, NULL };
    // Runtime representation of all script values
};
```

---

### 2. ADB Bridge

**Device Discovery**
```
[Host] → "adb devices" → [ADB Server] → Device List
       ← "emulator-5554\nRF8N12345"
```

**Command Execution**
```
[Host] → "shell input tap 500 1000" → [ADB] → [Device]
       ← "Success" or error
```

**File Transfer**
```
[Host] → "push file.png /sdcard/" → [ADB] → [Device]
[Host] ← "pull /sdcard/screenshot.png" ← [ADB] ← [Device]
```

---

### 3. Android Agent

**Accessibility Service Flow**
```
Script Command
    │
    ▼
AccessibilityService
    │
    ├─→ findElement(id)
    │       │
    │       ▼
    │   NodeInfo Tree
    │       │
    │       ▼
    │   Target Node
    │
    ├─→ performAction(CLICK)
    │       │
    │       ▼
    │   UI Event
    │
    └─→ Result
```

**UI Automation Layers**
```
┌─────────────────────────────────────┐
│     Script Command (Tap)            │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  UIAutomator.performTap(x, y)       │
│  • Validate coordinates             │
│  • Check permissions                │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  GestureController.tap(x, y)        │
│  • Create gesture path              │
│  • Set duration                     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  AccessibilityService               │
│  .dispatchGesture()                 │
└──────────────┬──────────────────────┘
               │
               ▼
         Android System
         (Performs tap)
```

---

### 4. Image Recognition Pipeline

```
┌──────────────────────────────────────────┐
│  Script: FindImage("button.png")        │
└────────────────┬─────────────────────────┘
                 │
┌────────────────▼─────────────────────────┐
│  1. Capture screenshot                   │
│     • AccessibilityService               │
│     • Bitmap capture                     │
└────────────────┬─────────────────────────┘
                 │
┌────────────────▼─────────────────────────┐
│  2. Load template image                  │
│     • Read from assets/storage           │
│     • Decode to Mat                      │
└────────────────┬─────────────────────────┘
                 │
┌────────────────▼─────────────────────────┐
│  3. OpenCV Template Matching             │
│     • Convert to grayscale               │
│     • Match template                     │
│     • Calculate confidence               │
└────────────────┬─────────────────────────┘
                 │
┌────────────────▼─────────────────────────┐
│  4. Find best match                      │
│     • Threshold by confidence (0.85)     │
│     • Get coordinates                    │
│     • Return match region                │
└────────────────┬─────────────────────────┘
                 │
                 ▼
          Match { x, y, width, height }
```

---

### 5. Multi-Device Orchestration

```
Script with ForEach($device in $devices)
    │
    ▼
Device Orchestrator
    │
    ├─→ Thread 1: Device emulator-5554
    │       │
    │       ├─→ LaunchApp("com.app")
    │       ├─→ Tap(500, 1000)
    │       └─→ Screenshot("d1.png")
    │
    ├─→ Thread 2: Device RF8N12345
    │       │
    │       ├─→ LaunchApp("com.app")
    │       ├─→ Tap(500, 1000)
    │       └─→ Screenshot("d2.png")
    │
    └─→ Thread 3: Device 192.168.1.100
            │
            ├─→ LaunchApp("com.app")
            ├─→ Tap(500, 1000)
            └─→ Screenshot("d3.png")

    ↓ SyncDevices() - Wait for all

All devices finished
```

---

## Data Flow

### Script Execution Flow

```
1. User writes script.as
    ↓
2. Host Runtime loads script
    ↓
3. Lexer tokenizes source code
    ↓
4. Parser generates AST
    ↓
5. Interpreter executes AST
    ↓
6. Built-in function called (e.g., Tap)
    ↓
7. Command sent to Device Orchestrator
    ↓
8. Command serialized to protocol format
    ↓
9. ADB Bridge transmits to device
    ↓
10. Android Agent receives command
    ↓
11. Accessibility Service performs action
    ↓
12. Result captured
    ↓
13. Response sent back to host
    ↓
14. Result returned to script
    ↓
15. Script continues execution
```

### Example: Tap Command

```
Script:  Tap(500, 1000)
           ↓
Lexer:   [IDENTIFIER("Tap"), LPAREN, INT(500), COMMA, INT(1000), RPAREN]
           ↓
Parser:  CallExpr { callee: "Tap", args: [IntExpr(500), IntExpr(1000)] }
           ↓
Interpreter: Execute builtin_Tap([Value(500), Value(1000)])
           ↓
Built-in: Create TapCommand { x: 500, y: 1000, device: current }
           ↓
Orchestrator: Route to device thread
           ↓
ADB Bridge: Serialize to JSON: {"type":"tap","x":500,"y":1000}
           ↓
Network: Send via socket/ADB
           ↓
Android Agent: Receive and parse command
           ↓
UIAutomator: performTap(500, 1000)
           ↓
Accessibility: dispatchGesture(path)
           ↓
Android System: Executes tap at (500, 1000)
           ↓
Agent: Return success result
           ↓
ADB Bridge: Receive response
           ↓
Orchestrator: Route back to thread
           ↓
Interpreter: Continue execution
```

---

## Protocol Format

### Command Structure (JSON)
```json
{
  "id": "cmd_12345",
  "type": "tap",
  "timestamp": 1699876543000,
  "params": {
    "x": 500,
    "y": 1000,
    "duration": 100
  }
}
```

### Response Structure
```json
{
  "id": "cmd_12345",
  "status": "success",
  "timestamp": 1699876543100,
  "result": {
    "executed": true,
    "duration_ms": 150
  },
  "error": null
}
```

### Command Types
- `tap` - Single tap
- `long_press` - Long press
- `swipe` - Swipe gesture
- `input` - Text input
- `key_press` - Hardware key
- `launch_app` - Launch application
- `find_element` - Find UI element
- `screenshot` - Capture screen
- `find_image` - Image recognition
- `ocr_read` - OCR text extraction

---

## Security Considerations

### Permissions Required

**Android Agent:**
- `BIND_ACCESSIBILITY_SERVICE` - UI automation
- `SYSTEM_ALERT_WINDOW` - Overlay windows
- `QUERY_ALL_PACKAGES` - List apps
- `REQUEST_INSTALL_PACKAGES` - Install APKs
- `WRITE_EXTERNAL_STORAGE` - Save screenshots
- `INTERNET` - Network communication

**Host Runtime:**
- ADB debugging enabled on device
- USB debugging or wireless ADB

### Safety Features
- Input validation on all commands
- Timeout limits to prevent hangs
- Resource cleanup on errors
- Permission checks before actions
- Sandboxed script execution

---

## Performance Characteristics

### Latency
- **Lexer/Parser:** < 10ms for typical script
- **Interpreter:** < 1ms per statement
- **ADB Command:** 20-100ms (USB), 50-200ms (WiFi)
- **UI Action:** 50-500ms (depends on animation)
- **Image Recognition:** 100-1000ms (depends on resolution)
- **OCR:** 200-2000ms (depends on text amount)

### Scalability
- **Devices:** Tested up to 10 concurrent devices
- **Script Size:** No practical limit
- **Memory:** ~50MB per device thread
- **CPU:** Multi-threaded, scales with cores

---

## Extension Points

### 1. Custom Built-in Functions
```cpp
// core/src/builtins.cpp
Value builtin_CustomFunction(const std::vector<Value>& args) {
    // Implementation
}

// Register in interpreter
env.define("CustomFunction", Value::makeBuiltin(builtin_CustomFunction));
```

### 2. Custom Device Actions
```kotlin
// android-agent/.../automation/CustomAction.kt
class CustomAction(private val service: AccessibilityService) {
    fun performCustomAction(params: JSONObject): Result {
        // Implementation
    }
}
```

### 3. Script Libraries
```androidscript
// stdlib/mylib.as
function MyHelper($param) {
    // Reusable logic
}

// Script includes library
#include "stdlib/mylib.as"
MyHelper("value")
```

---

## Deployment Architecture

### Development
```
Developer PC → USB → Test Device (1-2 devices)
```

### Testing Lab
```
Test Server → USB Hub → Multiple Devices (5-20 devices)
     ↓
CI/CD Pipeline
```

### Cloud Farm
```
                ┌─→ Device Farm 1 (10 devices)
Control Server ─┼─→ Device Farm 2 (10 devices)
                └─→ Device Farm 3 (10 devices)

Web Dashboard for monitoring
```

---

## Technology Stack

### Host Runtime (C++)
- **Language:** C++17
- **Build:** CMake 3.15+
- **Libraries:**
  - STL (Standard Template Library)
  - Optional: OpenCV, Tesseract

### Android Agent (Kotlin/Java)
- **Language:** Kotlin 1.9
- **Build:** Gradle 8.2
- **Android:** API 21-34 (Android 5.0+)
- **Libraries:**
  - AndroidX
  - OkHttp (networking)
  - Gson (JSON)
  - OpenCV for Android
  - Tess-Two (Tesseract)

### Communication
- **Protocol:** JSON over TCP/IP
- **Transport:** ADB (USB/Network)
- **Alternative:** WebSocket, gRPC

---

**Architecture Version:** 1.0
**Last Updated:** 2025-11-14
