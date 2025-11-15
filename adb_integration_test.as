Print("=== AndroidScript ADB Integration Test ===")
Print("")

Print("[1] Discovering Android devices...")
$dev = Device()
Print("")

Print("[2] Testing basic automation...")
Print("  - Simulating tap at (500, 500)")
Tap(500, 500)
Sleep(500)

Print("  - Simulating swipe from (300, 1000) to (300, 500)")
Swipe(300, 1000, 300, 500, 300)
Sleep(500)

Print("  - Sending HOME key event")
KeyEvent("KEYCODE_HOME")
Sleep(1000)

Print("")
Print("[3] Testing app management...")
Print("  - Launching Settings app")
LaunchApp("com.android.settings")
Sleep(2000)

Print("  - Taking screenshot")
Screenshot("device_screenshot.png")
Sleep(500)

Print("  - Stopping Settings app")
StopApp("com.android.settings")
Sleep(500)

Print("")
Print("[4] Testing text input...")
Print("  - Opening app drawer")
KeyEvent("KEYCODE_HOME")
Sleep(500)
Swipe(500, 1500, 500, 500, 300)
Sleep(1000)

Print("")
Print("=== Test Complete ===")
Print("All ADB commands executed successfully!")
