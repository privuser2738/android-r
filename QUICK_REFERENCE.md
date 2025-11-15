# AndroidScript Quick Reference

## Command Line Usage

### Host Runtime
```bash
# Run script on auto-detected device
androidscript run script.as

# Run on specific device
androidscript run script.as --device emulator-5554

# Run on all connected devices
androidscript run script.as --all-devices

# Validate script syntax
androidscript validate script.as

# List connected devices
androidscript devices

# Interactive REPL mode
androidscript repl

# Debug mode with verbose logging
androidscript run script.as --debug --verbose
```

---

## Language Syntax

### Variables
```androidscript
$var = "string"
$num = 123
$float = 3.14
$bool = true
$array = [1, 2, 3]
$device = Device("emulator-5554")
```

### Operators
```androidscript
$a + $b          // Addition
$a - $b          // Subtraction
$a * $b          // Multiplication
$a / $b          // Division
$a % $b          // Modulo
$a == $b         // Equal
$a != $b         // Not equal
$a < $b          // Less than
$a > $b          // Greater than
$a && $b         // Logical AND
$a || $b         // Logical OR
!$a              // Logical NOT
```

### Control Flow
```androidscript
// If statement
if ($condition) {
    // code
} else if ($other) {
    // code
} else {
    // code
}

// While loop
while ($condition) {
    // code
}

// For loop
for ($i = 0; $i < 10; $i++) {
    // code
}

// ForEach loop
ForEach($item in $array) {
    // code
}

// Repeat-until
repeat {
    // code
} until ($condition)
```

### Functions
```androidscript
function MyFunction($param1, $param2) {
    // code
    return $result
}

// Call function
$result = MyFunction("value1", "value2")
```

---

## Built-in Functions

### UI Automation
```androidscript
Tap(x, y)                              // Tap at coordinates
Tap(x, y, $device)                     // Tap on specific device
LongPress(x, y, duration)              // Long press
DoubleTap(x, y)                        // Double tap
Swipe(x1, y1, x2, y2, duration)        // Swipe gesture
Input("text")                          // Input text
Input("text", clearFirst: true)        // Clear then input
PressKey("KEYCODE_HOME")               // Press hardware key
PressKey("BACK")                       // Back button
ScrollDown(pixels)                     // Scroll down
ScrollUp(pixels)                       // Scroll up
```

### Element Finding
```androidscript
FindElement("id")                      // Find by resource ID
FindByText("text")                     // Find by text
FindByImage("template.png")            // Find by image
FindByOCR("text")                      // Find by OCR
WaitForElement("id", timeout: 10)      // Wait for element
```

### Screen Interaction
```androidscript
Screenshot("path.png")                 // Take screenshot
GetScreenSize()                        // Get [width, height]
GetPixelColor(x, y)                    // Get color at position
```

### Image Recognition
```androidscript
FindImage("template.png", confidence: 0.85)
WaitForImage("template.png", timeout: 10)
TextExists("text")                     // Check if text visible
ReadText()                             // Read all screen text
ReadText(x: 100, y: 100, width: 200, height: 50)  // Read region
TapText("text")                        // Tap text if found
```

### App Management
```androidscript
LaunchApp("package.name")              // Launch app
LaunchApp("pkg", activity: ".Main")    // Launch with activity
StopApp("package.name")                // Force stop
ClearAppData("package.name")           // Clear app data
InstallApp("/path/to/app.apk")         // Install APK
UninstallApp("package.name")           // Uninstall
AppInstalled("package.name")           // Check if installed
```

### Permissions
```androidscript
GrantPermission("pkg", "android.permission.CAMERA")
RevokePermission("pkg", "android.permission.CAMERA")
CheckPermission("pkg", "android.permission.CAMERA")
```

### Device Control
```androidscript
SetWifi(true)                          // Enable WiFi
SetBluetooth(false)                    // Disable Bluetooth
SetAirplaneMode(true)                  // Toggle airplane mode
SetBrightness(128)                     // Set brightness (0-255)
SetVolume(50)                          // Set volume (0-100)
UnlockScreen("1234")                   // Unlock with PIN
LockScreen()                           // Lock screen
```

### Device Management
```androidscript
Device()                               // Auto-detect device
Device("emulator-5554")                // Specific device
Device("192.168.1.100:5555")           // Network device
GetAllDevices()                        // Get all connected
SyncDevices($devices)                  // Wait for all
```

### Utility Functions
```androidscript
Sleep(ms)                              // Sleep in milliseconds
Print(message)                         // Print to console
Log(message)                           // Log message
LogError(message)                      // Log error
Assert(condition, "message")           // Assert condition
WaitForIdle(timeout: 5)                // Wait for UI idle
```

### File Operations
```androidscript
FileExists("/path/file")               // Check file exists
ReadFile("/path/file")                 // Read file content
WriteFile("/path/file", content)       // Write to file
DeleteFile("/path/file")               // Delete file
```

### String Functions
```androidscript
Length($str)                           // Get string length
Substring($str, start, end)            // Get substring
ToUpper($str)                          // Convert to uppercase
ToLower($str)                          // Convert to lowercase
Contains($str, $substring)             // Check contains
Replace($str, $old, $new)              // Replace text
```

### Array Functions
```androidscript
Count($array)                          // Get array length
Push($array, value)                    // Add to end
Pop($array)                            // Remove from end
Join($array, ",")                      // Join to string
```

---

## Common Patterns

### Login Automation
```androidscript
LaunchApp("com.example.app")
Sleep(2000)
WaitForElement("id/username", timeout: 10)
Tap(500, 800)
Input("user@example.com")
Tap(500, 1000)
Input("password")
TapText("Login")
```

### Multi-Device Testing
```androidscript
$devices = GetAllDevices()
ForEach($device in $devices) {
    $device.LaunchApp("com.example.app")
    $device.Tap(540, 960)
    $device.Screenshot("result_" + $device.serial + ".png")
}
SyncDevices($devices)
```

### Image-Based Navigation
```androidscript
$button = WaitForImage("start_button.png", timeout: 10)
if ($button != null) {
    Tap($button.centerX, $button.centerY)
}
```

### Error Handling
```androidscript
try {
    $element = FindElement("id/button")
    $element.Tap()
} catch ($error) {
    LogError("Failed: " + $error.message)
    Screenshot("error.png")
} finally {
    // Cleanup
}
```

### Conditional Actions
```androidscript
if (TextExists("Allow")) {
    TapText("Allow")
} else if (TextExists("OK")) {
    TapText("OK")
}
```

### Waiting with Timeout
```androidscript
$attempts = 0
while ($attempts < 30) {
    if (TextExists("Success")) {
        break
    }
    Sleep(1000)
    $attempts = $attempts + 1
}
```

---

## Hardware Key Codes

```androidscript
PressKey("KEYCODE_HOME")               // Home button
PressKey("KEYCODE_BACK")               // Back button
PressKey("KEYCODE_APP_SWITCH")         // Recent apps
PressKey("KEYCODE_POWER")              // Power button
PressKey("KEYCODE_VOLUME_UP")          // Volume up
PressKey("KEYCODE_VOLUME_DOWN")        // Volume down
PressKey("KEYCODE_MENU")               // Menu button
PressKey("KEYCODE_ENTER")              // Enter key
PressKey("KEYCODE_DEL")                // Delete key
```

---

## Common Permissions

```androidscript
// Camera
"android.permission.CAMERA"

// Location
"android.permission.ACCESS_FINE_LOCATION"
"android.permission.ACCESS_COARSE_LOCATION"

// Storage
"android.permission.READ_EXTERNAL_STORAGE"
"android.permission.WRITE_EXTERNAL_STORAGE"

// Phone
"android.permission.CALL_PHONE"
"android.permission.READ_PHONE_STATE"

// Contacts
"android.permission.READ_CONTACTS"
"android.permission.WRITE_CONTACTS"

// Microphone
"android.permission.RECORD_AUDIO"

// SMS
"android.permission.SEND_SMS"
"android.permission.READ_SMS"
```

---

## Device Object Properties

```androidscript
$device = Device("emulator-5554")

$device.serial                         // Device serial number
$device.model                          // Device model
$device.androidVersion                 // Android version
$device.screenWidth                    // Screen width
$device.screenHeight                   // Screen height

// Device methods
$device.Tap(x, y)
$device.LaunchApp("pkg")
$device.Screenshot("path.png")
```

---

## Special Variables

```androidscript
SCREEN_WIDTH                           // Current screen width
SCREEN_HEIGHT                          // Current screen height
DEVICE_MODEL                           // Device model name
ANDROID_VERSION                        // Android OS version
SCRIPT_DIR                             // Script directory path
```

---

## Directives

```androidscript
#include "library.as"                  // Include another script
#import "opencv"                       // Import native library
#timeout 30                            // Set default timeout
#retry 3                               // Set default retry count
#mode host                             // Force host mode
#mode device                           // Force device mode
#mode auto                             // Auto-detect mode
```

---

## Exit Codes

```
0   - Success
1   - Syntax error
2   - Runtime error
3   - Device not found
4   - Connection timeout
5   - Permission denied
6   - File not found
7   - Element not found
8   - Assertion failed
```

---

## Tips & Best Practices

### Performance
- Use element IDs instead of image recognition when possible
- Cache image templates
- Minimize Sleep() calls, use WaitForElement() instead
- Batch operations when possible

### Reliability
- Always add timeouts to waits
- Handle errors with try-catch
- Take screenshots on failure for debugging
- Use Assert() to validate assumptions

### Maintainability
- Use functions for repeated actions
- Add comments for complex logic
- Use descriptive variable names
- Keep scripts modular

### Multi-Device
- Test on different screen sizes
- Use relative coordinates or element IDs
- Handle device-specific variations
- Monitor device resources

---

## Common Issues

### Element Not Found
```androidscript
// Bad: No timeout
$element = FindElement("id/button")

// Good: With timeout and error handling
$element = WaitForElement("id/button", timeout: 10)
if ($element == null) {
    LogError("Button not found")
    Screenshot("error.png")
}
```

### Race Conditions
```androidscript
// Bad: Fixed sleep
Tap(500, 1000)
Sleep(2000)
Tap(500, 1200)

// Good: Wait for UI to update
Tap(500, 1000)
WaitForElement("id/next_screen", timeout: 5)
Tap(500, 1200)
```

### Hardcoded Coordinates
```androidscript
// Bad: Hardcoded for one device
Tap(540, 960)

// Good: Find element
$button = FindElement("id/submit")
$button.Tap()

// Alternative: Use screen size
Tap(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2)
```

---

## Debugging

### Enable Debug Output
```bash
androidscript run script.as --debug
```

### Add Debug Statements
```androidscript
Print("Debug: Current state = " + $state)
Log("Checkpoint reached at line 42")
Screenshot("debug_checkpoint.png")
```

### Validate Script
```bash
androidscript validate script.as
```

---

**Quick Reference Version:** 1.0
**For Full Documentation:** See LANGUAGE_SPEC.md
