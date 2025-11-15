// App Management Example
// Demonstrates installing, configuring, and managing apps

$appPackage = "com.example.testapp"
$apkPath = "/sdcard/Download/testapp.apk"

// Check if app is installed
if (AppInstalled($appPackage)) {
    Log("App already installed, uninstalling...")
    UninstallApp($appPackage)
    Sleep(2000)
}

// Install the app
Log("Installing app from: " + $apkPath)
InstallApp($apkPath)
Sleep(5000)

// Grant necessary permissions
Log("Granting permissions...")
GrantPermission($appPackage, "android.permission.CAMERA")
GrantPermission($appPackage, "android.permission.LOCATION")
GrantPermission($appPackage, "android.permission.READ_EXTERNAL_STORAGE")
GrantPermission($appPackage, "android.permission.WRITE_EXTERNAL_STORAGE")

// Configure device settings
Log("Configuring device settings...")
SetWifi(true)
SetBluetooth(false)
SetBrightness(200)
SetVolume(50)

// Launch the app
Log("Launching app...")
LaunchApp($appPackage)
Sleep(3000)

// Perform initial setup
Tap(540, 1500)  // Tap "Get Started"
Sleep(1000)
Tap(540, 1500)  // Tap "Next"
Sleep(1000)
Tap(540, 1500)  // Tap "Finish"

// Take screenshot of initial state
Screenshot("app_initial_state.png")

Log("App setup completed successfully!")

// Optional: Clear app data for fresh state
// ClearAppData($appPackage)
