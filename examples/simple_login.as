// Simple Login Automation Example
// This script demonstrates basic UI automation

// Launch the target app
LaunchApp("com.example.app")
Sleep(2000)

// Wait for login screen to appear
Log("Waiting for login screen...")
WaitForElement("id/username", timeout: 10)

// Input credentials
Log("Entering credentials...")
Tap(500, 800)  // Tap username field
Input("testuser@example.com")

Tap(500, 1000)  // Tap password field
Input("SecurePassword123")

// Tap login button
Log("Tapping login button...")
TapText("Login")

// Verify successful login
if (WaitForImage("home_screen.png", timeout: 15) != null) {
    Log("Login successful!")
    Screenshot("login_success.png")
} else {
    LogError("Login failed - home screen not found")
    Screenshot("login_failure.png")
}
