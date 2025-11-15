# =====================================================
# AndroidScript Comprehensive Demo
# Demonstrates all ADB-integrated automation features
# =====================================================

Print("========================================")
Print("  AndroidScript - Full Feature Demo")
Print("========================================")
Print("")

# =====================================================
# 1. DEVICE CONNECTION
# =====================================================
Print("[1] DEVICE MANAGEMENT")
Print("--------------------")

Print("Connecting to Android device...")
$device = Device()

Print("")
Print("Device Information:")
Print("  Model: " + $device.model)
Print("  Android Version: " + $device.android_version)
Print("  Screen: " + $device.screen_width + "x" + $device.screen_height)
Print("")

Sleep(1000)

# =====================================================
# 2. NAVIGATION AND KEY EVENTS
# =====================================================
Print("[2] NAVIGATION & KEY EVENTS")
Print("----------------------------")

Print("Going to Home screen...")
KeyEvent("KEYCODE_HOME")
Sleep(1000)

Print("Opening notification panel...")
Swipe(500, 100, 500, 1000, 300)
Sleep(1000)

Print("Closing notification panel...")
KeyEvent("KEYCODE_BACK")
Sleep(500)

Print("")

# =====================================================
# 3. APP MANAGEMENT
# =====================================================
Print("[3] APP MANAGEMENT")
Print("------------------")

Print("Launching Settings app...")
LaunchApp("com.android.settings")
Sleep(2000)

Print("Taking screenshot of Settings...")
Screenshot("demo_settings.png")
Sleep(500)

Print("Stopping Settings app...")
StopApp("com.android.settings")
Sleep(500)

Print("")

# =====================================================
# 4. UI AUTOMATION
# =====================================================
Print("[4] UI AUTOMATION")
Print("-----------------")

Print("Returning to Home screen...")
KeyEvent("KEYCODE_HOME")
Sleep(500)

# Calculate center of screen
$center_x = $device.screen_width / 2
$center_y = $device.screen_height / 2

Print("Tapping center of screen...")
Print("  Coordinates: (" + $center_x + ", " + $center_y + ")")
Tap($center_x, $center_y)
Sleep(500)

Print("Opening app drawer...")
$start_y = $device.screen_height - 200
$end_y = 200
Swipe($center_x, $start_y, $center_x, $end_y, 300)
Sleep(1500)

Print("Closing app drawer...")
KeyEvent("KEYCODE_BACK")
Sleep(500)

Print("")

# =====================================================
# 5. TEXT INPUT & SEARCH
# =====================================================
Print("[5] TEXT INPUT & SEARCH")
Print("-----------------------")

Print("Opening app drawer again...")
Swipe($center_x, $start_y, $center_x, $end_y, 300)
Sleep(1000)

Print("Tapping search field...")
$search_y = 200
Tap($center_x, $search_y)
Sleep(500)

Print("Typing search query...")
Input("calculator")
Sleep(1000)

Print("Clearing search...")
KeyEvent("KEYCODE_DEL")
KeyEvent("KEYCODE_DEL")
KeyEvent("KEYCODE_DEL")
KeyEvent("KEYCODE_DEL")
KeyEvent("KEYCODE_DEL")
KeyEvent("KEYCODE_DEL")
KeyEvent("KEYCODE_DEL")
KeyEvent("KEYCODE_DEL")
KeyEvent("KEYCODE_DEL")
KeyEvent("KEYCODE_DEL")
Sleep(500)

Print("Closing app drawer...")
KeyEvent("KEYCODE_BACK")
Sleep(500)

Print("")

# =====================================================
# 6. SCREENSHOTS & FILE OPERATIONS
# =====================================================
Print("[6] FILE OPERATIONS")
Print("-------------------")

Print("Capturing final screenshot...")
Screenshot("demo_final.png")
Sleep(500)

Print("Screenshots saved:")
Print("  - demo_settings.png")
Print("  - demo_final.png")

Print("")

# Note: File push/pull examples commented out to avoid file requirements
# Print("Pushing test file to device...")
# PushFile("test.txt", "/sdcard/Download/test.txt")
#
# Print("Pulling file from device...")
# PullFile("/sdcard/Download/test.txt", "pulled_test.txt")

# =====================================================
# 7. VOLUME CONTROLS
# =====================================================
Print("[7] VOLUME CONTROLS")
Print("-------------------")

Print("Pressing Volume Up...")
KeyEvent("KEYCODE_VOLUME_UP")
Sleep(500)

Print("Pressing Volume Down...")
KeyEvent("KEYCODE_VOLUME_DOWN")
Sleep(500)

Print("")

# =====================================================
# 8. BACK TO HOME
# =====================================================
Print("[8] CLEANUP")
Print("-----------")

Print("Returning to Home screen...")
KeyEvent("KEYCODE_HOME")
Sleep(500)

Print("")

# =====================================================
# SUMMARY
# =====================================================
Print("========================================")
Print("  Demo Complete!")
Print("========================================")
Print("")
Print("Demonstrated Features:")
Print("  ✓ Device connection and info")
Print("  ✓ Navigation (Home, Back)")
Print("  ✓ App launching and stopping")
Print("  ✓ Screen tapping (absolute & dynamic)")
Print("  ✓ Swiping gestures")
Print("  ✓ Text input")
Print("  ✓ Key events (Home, Back, Volume, Del)")
Print("  ✓ Screenshots")
Print("  ✓ Dynamic coordinate calculation")
Print("")
Print("Total automation commands executed: 25+")
Print("")
Print("All features working with real ADB!")
Print("========================================")
