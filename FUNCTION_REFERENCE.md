# AndroidScript Function Reference

Complete reference for all built-in functions with real ADB integration.

---

## 📱 Device Management

### `Device()` / `Device(serial)`
Connect to an Android device.

**Usage:**
```androidscript
# Auto-detect first online device
$dev = Device()

# Connect to specific device
$dev = Device("emulator-5554")
```

**Returns:** Device object with model, Android version, screen size

**Errors:**
- No devices found
- Device offline/unauthorized
- Specified serial not found

---

### `GetAllDevices()`
Get list of all connected Android devices.

**Usage:**
```androidscript
$devices = GetAllDevices()
$count = Count($devices)
Print("Found " + $count + " devices")
```

**Returns:** Array of device objects

---

## 🖱️ UI Automation

### `Tap(x, y)`
Tap at screen coordinates.

**Usage:**
```androidscript
Tap(500, 1000)  # Tap center of screen
```

**Parameters:**
- `x` (int): X coordinate in pixels
- `y` (int): Y coordinate in pixels

**ADB Command:** `adb shell input tap x y`

---

### `Swipe(x1, y1, x2, y2, duration)`
Swipe gesture from point A to point B.

**Usage:**
```androidscript
# Swipe up (app drawer)
Swipe(500, 1500, 500, 500, 300)

# Swipe right (next screen)
Swipe(100, 800, 900, 800, 250)
```

**Parameters:**
- `x1, y1` (int): Start coordinates
- `x2, y2` (int): End coordinates
- `duration` (int): Duration in milliseconds

**ADB Command:** `adb shell input swipe x1 y1 x2 y2 duration`

---

### `Input(text)`
Type text into focused input field.

**Usage:**
```androidscript
Input("Hello World")
Input("user@example.com")
```

**Note:** Spaces are escaped automatically. Special characters may need handling.

**ADB Command:** `adb shell input text "..."`

---

### `KeyEvent(keycode)`
Send hardware/software key event.

**Usage:**
```androidscript
KeyEvent("KEYCODE_HOME")    # Press Home button
KeyEvent("KEYCODE_BACK")    # Press Back button
KeyEvent("KEYCODE_ENTER")   # Press Enter
KeyEvent("3")               # HOME key by number
```

**Common Keycodes:**
- `KEYCODE_HOME` (3) - Home button
- `KEYCODE_BACK` (4) - Back button
- `KEYCODE_MENU` (82) - Menu button
- `KEYCODE_ENTER` (66) - Enter key
- `KEYCODE_DEL` (67) - Backspace
- `KEYCODE_VOLUME_UP` (24)
- `KEYCODE_VOLUME_DOWN` (25)
- `KEYCODE_POWER` (26)

**ADB Command:** `adb shell input keyevent <code>`

---

### `Screenshot(path)`
Capture screenshot and save to file.

**Usage:**
```androidscript
Screenshot("screenshot.png")
Screenshot("/tmp/device_screen.png")
```

**Parameters:**
- `path` (string): Local file path to save screenshot

**Process:**
1. Capture screenshot on device: `/sdcard/screenshot.png`
2. Pull to local path
3. Delete temporary file from device

**ADB Commands:**
```bash
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png <local_path>
adb shell rm /sdcard/screenshot.png
```

---

## 📦 App Management

### `LaunchApp(package)`
Launch app by package name.

**Usage:**
```androidscript
LaunchApp("com.android.settings")
LaunchApp("com.google.android.youtube")
LaunchApp("com.android.chrome")
```

**ADB Command:** `adb shell monkey -p <package> -c android.intent.category.LAUNCHER 1`

---

### `StopApp(package)`
Force stop running app.

**Usage:**
```androidscript
StopApp("com.android.settings")
```

**ADB Command:** `adb shell am force-stop <package>`

---

### `InstallApp(apk_path)`
Install APK file to device.

**Usage:**
```androidscript
InstallApp("app-release.apk")
InstallApp("/path/to/myapp.apk")
```

**Flags:** `-r` (reinstall/replace existing app)

**ADB Command:** `adb install -r <apk_path>`

---

### `UninstallApp(package)`
Uninstall app from device.

**Usage:**
```androidscript
UninstallApp("com.example.myapp")
```

**ADB Command:** `adb uninstall <package>`

---

### `ClearAppData(package)`
Clear app data and cache.

**Usage:**
```androidscript
ClearAppData("com.android.chrome")
```

**ADB Command:** `adb shell pm clear <package>`

---

## 📁 File Operations

### `PushFile(local_path, remote_path)`
Copy file from PC to device.

**Usage:**
```androidscript
PushFile("data.txt", "/sdcard/Download/data.txt")
PushFile("video.mp4", "/sdcard/Movies/video.mp4")
```

**Parameters:**
- `local_path` (string): File path on PC
- `remote_path` (string): Destination path on device

**ADB Command:** `adb push <local> <remote>`

---

### `PullFile(remote_path, local_path)`
Copy file from device to PC.

**Usage:**
```androidscript
PullFile("/sdcard/DCIM/Camera/IMG_001.jpg", "photo.jpg")
PullFile("/data/local/tmp/log.txt", "device_log.txt")
```

**Parameters:**
- `remote_path` (string): File path on device
- `local_path` (string): Destination path on PC

**ADB Command:** `adb pull <remote> <local>`

---

## 📝 String Functions

### `Length(str)`
Get string/array length.

**Usage:**
```androidscript
$len = Length("Hello")  # Returns 5
```

---

### `Substring(str, start, end)`
Extract substring.

**Usage:**
```androidscript
$sub = Substring("Hello World", 0, 5)  # "Hello"
```

---

### `ToUpper(str)` / `ToLower(str)`
Convert case.

**Usage:**
```androidscript
$upper = ToUpper("hello")  # "HELLO"
$lower = ToLower("WORLD")  # "world"
```

---

### `Contains(str, substr)`
Check if string contains substring.

**Usage:**
```androidscript
$found = Contains("Hello World", "World")  # true
```

---

### `Replace(str, old, new)`
Replace all occurrences.

**Usage:**
```androidscript
$result = Replace("Hello World", "World", "Android")  # "Hello Android"
```

---

## 🔢 Type Conversion

### `ToString(value)`
Convert any value to string.

**Usage:**
```androidscript
$str = ToString(123)      # "123"
$str = ToString(3.14)     # "3.14"
$str = ToString(true)     # "true"
```

---

### `ToInt(value)`
Convert to integer.

**Usage:**
```androidscript
$num = ToInt("123")     # 123
$num = ToInt(3.14)      # 3
```

---

### `ToFloat(value)`
Convert to floating point.

**Usage:**
```androidscript
$num = ToFloat("3.14")  # 3.14
$num = ToFloat(5)       # 5.0
```

---

## 🛠️ Utility Functions

### `Print(...)`
Print to console.

**Usage:**
```androidscript
Print("Hello")
Print("X:", $x, "Y:", $y)
```

---

### `Log(...)` / `LogError(...)`
Log with prefix.

**Usage:**
```androidscript
Log("Debug message")
LogError("Error occurred")
```

**Output:**
```
[LOG] Debug message
[ERROR] Error occurred
```

---

### `Sleep(milliseconds)`
Pause execution.

**Usage:**
```androidscript
Sleep(1000)   # Wait 1 second
Sleep(500)    # Wait 0.5 seconds
```

---

### `Assert(condition, message)`
Runtime assertion.

**Usage:**
```androidscript
Assert($x > 0, "X must be positive")
Assert(FileExists("data.txt"), "File not found")
```

---

## 💾 File I/O (Local PC)

### `FileExists(path)`
Check if file exists on PC.

**Usage:**
```androidscript
if (FileExists("config.txt")) {
    Print("Config found")
}
```

---

### `ReadFile(path)`
Read file contents from PC.

**Usage:**
```androidscript
$content = ReadFile("data.txt")
Print($content)
```

---

### `WriteFile(path, content)`
Write file to PC.

**Usage:**
```androidscript
WriteFile("output.txt", "Hello World")
```

---

## 📊 Array Functions

### `Count(array)`
Get array size.

**Usage:**
```androidscript
$arr = [1, 2, 3]
$size = Count($arr)  # 3
```

---

### `Push(array, value)`
Add element to array.

**Usage:**
```androidscript
$arr = [1, 2]
$arr = Push($arr, 3)  # [1, 2, 3]
```

---

### `Pop(array)`
Remove and return last element.

**Usage:**
```androidscript
$last = Pop($arr)
```

---

### `Join(array, separator)`
Join array elements into string.

**Usage:**
```androidscript
$arr = ["Hello", "World"]
$str = Join($arr, " ")  # "Hello World"
```

---

## 🎯 Complete Example

```androidscript
Print("=== Android Automation Script ===")

# Connect to device
$dev = Device()
Sleep(1000)

# Navigate to home screen
KeyEvent("KEYCODE_HOME")
Sleep(500)

# Open app drawer
Swipe(500, 1500, 500, 500, 300)
Sleep(1000)

# Search for Settings
Tap(500, 200)
Sleep(500)
Input("Settings")
Sleep(500)

# Open Settings app
KeyEvent("KEYCODE_ENTER")
Sleep(2000)

# Take screenshot
Screenshot("settings_screen.png")

# Go back to home
KeyEvent("KEYCODE_HOME")

Print("Script complete!")
```

---

## 🚨 Error Handling

All functions throw errors if they fail. Use try-catch when implemented:

```androidscript
# When try-catch is implemented:
try {
    Tap(500, 500)
} catch {
    Print("Tap failed")
}
```

Current behavior: Script stops with error message if command fails.

---

## 📏 Coordinate Tips

### Finding Coordinates:
1. Enable "Show taps" in Developer Options
2. Enable "Pointer location" to see coordinates
3. Use `adb shell getevent` to capture touch events
4. Use screenshot and image editor to measure pixels

### Screen Density:
- Coordinates are in pixels, not DP
- Different devices have different resolutions
- Use `$dev.screen_width` and `$dev.screen_height` for dynamic positioning

### Example Dynamic Positioning:
```androidscript
$dev = Device()
$center_x = $dev.screen_width / 2
$center_y = $dev.screen_height / 2
Tap($center_x, $center_y)
```

---

**Total Functions:** 35+
**ADB Integration:** 15 functions
**Status:** ✅ All working with real devices!
