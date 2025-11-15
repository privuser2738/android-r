# AndroidScript Quick Start Guide

Get started with Android automation in 5 minutes!

---

## Prerequisites

### 1. Install ADB (Android Debug Bridge)

**Linux (Manjaro/Arch):**
```bash
sudo pacman -S android-tools
```

**Ubuntu/Debian:**
```bash
sudo apt install adb
```

**Windows:**
- Download [Android SDK Platform Tools](https://developer.android.com/tools/releases/platform-tools)
- Extract and add to PATH

**Mac:**
```bash
brew install android-platform-tools
```

**Verify Installation:**
```bash
adb version
```

### 2. Enable USB Debugging on Android

1. Go to **Settings → About Phone**
2. Tap **Build Number** 7 times
3. Go to **Settings → Developer Options**
4. Enable **USB Debugging**
5. Connect device via USB
6. Accept "Allow USB Debugging" prompt

### 3. Verify Device Connection

```bash
adb devices -l
```

Expected output:
```
List of devices attached
ABC123DEF456    device product:model model:SM_G973F
```

---

## Building AndroidScript

```bash
cd android-r
mkdir build
cd build
cmake ..
cmake --build . --config Release
```

Executable location: `./build/bin/androidscript`

---

## Your First Script

### 1. Create `hello_android.as`:

```androidscript
Print("=== Hello Android! ===")

# Connect to device
$dev = Device()

Print("Connected to: " + $dev.model)

# Go to home screen
KeyEvent("KEYCODE_HOME")
Sleep(1000)

# Take a screenshot
Screenshot("my_screenshot.png")

Print("Screenshot saved!")
Print("=== Complete! ===")
```

### 2. Run the script:

```bash
./build/bin/androidscript hello_android.as
```

### 3. Expected Output:

```
=== Hello Android! ===
[DEVICE] Connected to SM-G973F (Android 12) [1080x2280]
Connected to: SM-G973F
[AUTOMATION] KeyEvent("KEYCODE_HOME")
[AUTOMATION] Screenshot("my_screenshot.png")
Screenshot saved!
=== Complete! ===
```

---

## 5-Minute Automation Examples

### Example 1: Take Screenshot
```androidscript
$dev = Device()
KeyEvent("KEYCODE_HOME")
Sleep(500)
Screenshot("home_screen.png")
Print("Screenshot saved!")
```

### Example 2: Open Settings
```androidscript
$dev = Device()
LaunchApp("com.android.settings")
Sleep(2000)
Screenshot("settings.png")
StopApp("com.android.settings")
```

### Example 3: Tap Screen Center
```androidscript
$dev = Device()
$x = $dev.screen_width / 2
$y = $dev.screen_height / 2
Tap($x, $y)
Print("Tapped center!")
```

### Example 4: Open App Drawer
```androidscript
$dev = Device()
KeyEvent("KEYCODE_HOME")
Sleep(500)

# Swipe up to open app drawer
$center_x = $dev.screen_width / 2
Swipe($center_x, 1500, $center_x, 500, 300)
Sleep(1000)

# Close it
KeyEvent("KEYCODE_BACK")
```

### Example 5: Search for App
```androidscript
$dev = Device()

# Open app drawer
KeyEvent("KEYCODE_HOME")
Sleep(500)
Swipe(500, 1500, 500, 500, 300)
Sleep(1000)

# Tap search
Tap(500, 200)
Sleep(500)

# Type app name
Input("chrome")
Sleep(500)

# Close
KeyEvent("KEYCODE_BACK")
KeyEvent("KEYCODE_BACK")
```

---

## Common Tasks

### Take Multiple Screenshots
```androidscript
$dev = Device()

KeyEvent("KEYCODE_HOME")
Screenshot("screen1.png")
Sleep(500)

LaunchApp("com.android.settings")
Sleep(2000)
Screenshot("screen2.png")

StopApp("com.android.settings")
```

### Navigate Through Screens
```androidscript
$dev = Device()

# Go home
KeyEvent("KEYCODE_HOME")
Sleep(500)

# Open app drawer
Swipe(500, 1500, 500, 500, 300)
Sleep(1000)

# Go back
KeyEvent("KEYCODE_BACK")
Sleep(500)

# Open notifications
Swipe(500, 100, 500, 1000, 300)
Sleep(1000)

# Close
KeyEvent("KEYCODE_BACK")
```

### Install and Launch App
```androidscript
$dev = Device()

# Install APK
Print("Installing app...")
InstallApp("myapp.apk")
Sleep(2000)

# Launch it
Print("Launching app...")
LaunchApp("com.example.myapp")
Sleep(3000)

# Screenshot
Screenshot("app_launched.png")

# Stop it
StopApp("com.example.myapp")
```

---

## Key Codes Reference

### Most Used:
- `KEYCODE_HOME` - Home button
- `KEYCODE_BACK` - Back button
- `KEYCODE_MENU` - Menu button
- `KEYCODE_ENTER` - Enter key
- `KEYCODE_DEL` - Backspace

### Volume:
- `KEYCODE_VOLUME_UP`
- `KEYCODE_VOLUME_DOWN`
- `KEYCODE_VOLUME_MUTE`

### Navigation:
- `KEYCODE_DPAD_UP`
- `KEYCODE_DPAD_DOWN`
- `KEYCODE_DPAD_LEFT`
- `KEYCODE_DPAD_RIGHT`
- `KEYCODE_DPAD_CENTER`

### Media:
- `KEYCODE_MEDIA_PLAY`
- `KEYCODE_MEDIA_PAUSE`
- `KEYCODE_MEDIA_NEXT`
- `KEYCODE_MEDIA_PREVIOUS`

[Full list](https://developer.android.com/reference/android/view/KeyEvent)

---

## Finding Screen Coordinates

### Method 1: Developer Options
1. Enable **Show taps** in Developer Options
2. Enable **Pointer location**
3. Touch screen to see coordinates in real-time

### Method 2: Screenshot + Image Editor
1. Take screenshot: `Screenshot("screen.png")`
2. Open in image editor (GIMP, Photoshop, etc.)
3. Hover over location to see pixel coordinates

### Method 3: Dynamic Calculation
```androidscript
$dev = Device()

# Center
$center_x = $dev.screen_width / 2
$center_y = $dev.screen_height / 2

# Top
$top_y = $dev.screen_height / 4

# Bottom
$bottom_y = $dev.screen_height * 3 / 4
```

---

## Common App Packages

### System Apps:
- Settings: `com.android.settings`
- Chrome: `com.android.chrome`
- Camera: `com.android.camera2`
- Calculator: `com.android.calculator2`
- Clock: `com.android.deskclock`
- Contacts: `com.android.contacts`
- Phone: `com.android.dialer`
- Messages: `com.android.messaging`

### Finding App Package:
```bash
# List all packages
adb shell pm list packages

# Find specific app
adb shell pm list packages | grep chrome
```

---

## Troubleshooting

### "No Android devices found"
- Check USB cable is connected
- USB debugging enabled?
- Run `adb devices` to verify
- Try `adb kill-server && adb start-server`
- Accept USB debugging prompt on device

### "Device offline" or "unauthorized"
- Accept USB debugging prompt on device
- Check USB cable (try different cable/port)
- Restart ADB: `adb kill-server && adb start-server`
- Re-enable USB debugging

### "ADB not found"
- Install Android Platform Tools
- Add to PATH environment variable
- Or set `ADB_PATH` environment variable

### Script Errors
- Multi-argument calls don't work yet (parser bug)
- Use single argument for now: `Tap(100)` works, `Tap(100, 200)` fails
- Parser fix coming soon!

### Permission Denied (Linux)
```bash
# Add user to plugdev group
sudo usermod -aG plugdev $USER

# Or create udev rules
sudo nano /etc/udev/rules.d/51-android.rules
# Add: SUBSYSTEM=="usb", ATTR{idVendor}=="xxxx", MODE="0666"

sudo udevadm control --reload-rules
```

---

## Next Steps

### Learn More:
- Read `FUNCTION_REFERENCE.md` for all available functions
- Check `examples/` directory for more scripts
- See `ADB_INTEGRATION_COMPLETE.md` for technical details

### Try Examples:
```bash
# Device info
./build/bin/androidscript device_info_test.as

# Full integration test
./build/bin/androidscript adb_integration_test.as

# Comprehensive demo
./build/bin/androidscript examples/comprehensive_demo.as
```

### Create Your Own:
1. Identify the task you want to automate
2. Find necessary coordinates (enable pointer location)
3. Find app packages (`adb shell pm list packages`)
4. Write script step by step
5. Test and refine

---

## Example: Automated App Testing

```androidscript
Print("=== Automated App Testing ===")

# Connect to device
$dev = Device()

# Launch app
Print("Launching app...")
LaunchApp("com.example.myapp")
Sleep(3000)

# Take screenshot of main screen
Screenshot("test_main.png")
Sleep(500)

# Tap button at (500, 1000)
Print("Tapping button...")
Tap(500, 1000)
Sleep(1000)

# Screenshot after action
Screenshot("test_after_tap.png")
Sleep(500)

# Swipe to next screen
Print("Swiping to next screen...")
Swipe(900, 1000, 100, 1000, 300)
Sleep(1000)

# Screenshot
Screenshot("test_screen2.png")

# Enter text
Print("Entering text...")
Tap(500, 800)
Sleep(500)
Input("test@example.com")
Sleep(500)

# Screenshot
Screenshot("test_with_text.png")

# Go back
KeyEvent("KEYCODE_BACK")
Sleep(500)

# Stop app
Print("Stopping app...")
StopApp("com.example.myapp")

Print("=== Test Complete ===")
Print("Screenshots saved:")
Print("  - test_main.png")
Print("  - test_after_tap.png")
Print("  - test_screen2.png")
Print("  - test_with_text.png")
```

---

## Tips & Best Practices

1. **Always add Sleep() between actions**
   - Gives UI time to respond
   - Prevents race conditions
   - 500-1000ms is usually good

2. **Use dynamic coordinates when possible**
   ```androidscript
   $x = $dev.screen_width / 2  # Works on any screen
   ```

3. **Take screenshots for debugging**
   - Helps verify each step
   - Useful for documentation

4. **Handle errors gracefully**
   - Check if device is connected
   - Verify app package names
   - Test coordinates on your device

5. **Start simple, then expand**
   - Get one action working first
   - Then build up to complex workflows

---

**You're ready to automate Android!** 🚀

Start with simple scripts and gradually build more complex automation.
Check the function reference for all available commands.

Happy automating!
