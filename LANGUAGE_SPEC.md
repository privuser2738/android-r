# AndroidScript Language Specification

## Overview
AndroidScript is a simple, AutoIt-like scripting language for Android automation. It supports both host-based (PC) and on-device execution with multi-device orchestration.

## Basic Syntax

### Comments
```
// Single line comment
/* Multi-line
   comment */
```

### Variables
```
$var = "value"              // String
$num = 123                  // Integer
$float = 3.14               // Float
$bool = true                // Boolean
$device = Device("serial")  // Object
```

### Data Types
- String: "text" or 'text'
- Integer: 123
- Float: 3.14
- Boolean: true/false
- Array: [1, 2, 3]
- Device: Device object reference

## Device Management

### Single Device
```
// Connect to device (auto-detect if only one connected)
$device = Device()

// Connect to specific device by serial
$device = Device("emulator-5554")

// Connect via IP (wireless ADB)
$device = Device("192.168.1.100:5555")
```

### Multi-Device
```
// Get all connected devices
$devices = GetAllDevices()

// Run on all devices in parallel
ForEach($device in $devices) {
    $device.Tap(500, 1000)
}

// Synchronize devices
SyncDevices($devices)  // Wait for all devices to reach this point
```

## UI Automation

### Touch Actions
```
Tap(x, y)                    // Single tap at coordinates
Tap(x, y, $device)           // Tap on specific device
LongPress(x, y, duration)    // Long press (duration in ms)
DoubleTap(x, y)              // Double tap
Swipe(x1, y1, x2, y2, duration)  // Swipe gesture
```

### Text Input
```
Input("text")                // Input text to focused field
Input("text", clearFirst: true)  // Clear before input
PressKey("KEYCODE_HOME")     // Press hardware key
PressKey("BACK")             // Navigate back
```

### Element Finding
```
// By resource ID
$element = FindElement("com.example:id/button")
if ($element != null) {
    $element.Tap()
}

// By text
$element = FindByText("Login")
$element.Tap()

// By image (template matching)
$element = FindByImage("button_template.png")
if ($element != null) {
    $element.Tap()
}

// By OCR (find text in image)
$element = FindByOCR("Submit")
$element.Tap()

// Wait for element (timeout in seconds)
$element = WaitForElement("id/button", timeout: 10)
```

### Screen Interaction
```
Screenshot("output.png")      // Take screenshot
GetScreenSize()               // Returns [width, height]
GetPixelColor(x, y)          // Get color at position
ScrollDown(pixels)           // Scroll down
ScrollUp(pixels)             // Scroll up
```

## App Management

### App Control
```
LaunchApp("com.example.app")              // Launch app by package
LaunchApp("com.example.app", activity: ".MainActivity")
StopApp("com.example.app")                // Force stop
ClearAppData("com.example.app")           // Clear app data
UninstallApp("com.example.app")           // Uninstall
InstallApp("/path/to/app.apk")            // Install APK
```

### Permissions
```
GrantPermission("com.example.app", "android.permission.CAMERA")
RevokePermission("com.example.app", "android.permission.CAMERA")
```

### Device Settings
```
SetWifi(true)                 // Enable/disable WiFi
SetBluetooth(false)           // Enable/disable Bluetooth
SetAirplaneMode(true)         // Toggle airplane mode
SetBrightness(128)            // Set brightness (0-255)
SetVolume(50)                 // Set volume (0-100)
UnlockScreen("1234")          // Unlock with PIN/password
```

## Control Flow

### Conditionals
```
if (condition) {
    // code
} else if (condition) {
    // code
} else {
    // code
}
```

### Loops
```
// For loop
for ($i = 0; $i < 10; $i++) {
    Tap(100, 100)
}

// While loop
while (condition) {
    // code
}

// ForEach loop
ForEach($item in $array) {
    // code
}

// Repeat-until
repeat {
    // code
} until (condition)
```

### Functions
```
// Define function
function TapButton($x, $y, $count) {
    for ($i = 0; $i < $count; $i++) {
        Tap($x, $y)
        Sleep(500)
    }
}

// Call function
TapButton(500, 1000, 3)
```

## Image Recognition & OCR

### Template Matching
```
// Find image on screen
$found = FindImage("template.png", confidence: 0.8)
if ($found != null) {
    Tap($found.centerX, $found.centerY)
}

// Wait for image to appear
$found = WaitForImage("loading_complete.png", timeout: 30)
```

### OCR
```
// Read all text from screen
$text = ReadText()
Print($text)

// Read text from region
$text = ReadText(x: 100, y: 200, width: 500, height: 100)

// Find and tap text
if (TextExists("Login")) {
    TapText("Login")
}
```

## Utility Functions

### Timing
```
Sleep(1000)               // Sleep in milliseconds
WaitForIdle(timeout: 5)   // Wait for UI to be idle
Timeout(10) {             // Set timeout for block
    WaitForElement("id/button")
}
```

### Logging
```
Print("Message")          // Print to console
Log("Info message")       // Log message
LogError("Error!")        // Log error
Assert(condition, "Message")  // Assert condition
```

### File Operations
```
FileExists("/path/file")
ReadFile("/path/file")
WriteFile("/path/file", content)
```

## Example Scripts

### Example 1: Simple Login
```
// Launch app and login
LaunchApp("com.example.app")
Sleep(2000)

// Wait for login screen
WaitForElement("id/username", timeout: 10)

// Input credentials
Tap(500, 800)
Input("testuser@example.com")
Tap(500, 1000)
Input("password123")

// Tap login button
TapText("Login")

// Verify login success
if (WaitForImage("home_screen.png", timeout: 10) != null) {
    Log("Login successful!")
} else {
    LogError("Login failed!")
}
```

### Example 2: Multi-Device Testing
```
// Get all connected devices
$devices = GetAllDevices()
Print("Found " + Count($devices) + " devices")

// Run test on all devices in parallel
ForEach($device in $devices) {
    $device.LaunchApp("com.example.app")
    $device.Sleep(2000)
    $device.Tap(500, 1000)
}

// Wait for all devices to finish
SyncDevices($devices)
Print("All devices completed!")
```

### Example 3: Image-Based Navigation
```
// Navigate using image recognition
LaunchApp("com.example.game")
Sleep(3000)

// Wait for start button
$startBtn = WaitForImage("start_button.png", timeout: 10)
if ($startBtn != null) {
    Tap($startBtn.centerX, $startBtn.centerY)
}

// Wait for level select
Sleep(2000)
$level1 = FindImage("level1_icon.png", confidence: 0.85)
if ($level1 != null) {
    Tap($level1.centerX, $level1.centerY)
}
```

## Runtime Modes

### Host Mode (PC-based)
```
// Explicitly target host mode
#mode host

$device = Device("emulator-5554")
$device.LaunchApp("com.example.app")
```

### On-Device Mode
```
// Run directly on Android device
#mode device

// No device selection needed - runs on local device
LaunchApp("com.example.app")
Tap(500, 1000)
```

### Auto Mode (Default)
```
// Automatically detect and use best mode
#mode auto
```

## Error Handling

```
try {
    $element = FindElement("id/button")
    $element.Tap()
} catch ($error) {
    LogError("Failed: " + $error.message)
    Screenshot("error.png")
} finally {
    // Cleanup code
}
```

## Built-in Constants

```
SCREEN_WIDTH    // Current device screen width
SCREEN_HEIGHT   // Current device screen height
DEVICE_MODEL    // Device model name
ANDROID_VERSION // Android OS version
SCRIPT_DIR      // Script directory path
```

## Compiler Directives

```
#include "library.as"          // Include another script
#import "opencv"               // Import native library
#timeout 30                    // Set default timeout
#retry 3                       // Set default retry count
```
