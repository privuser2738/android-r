// Image Recognition and OCR Example
// Demonstrates computer vision capabilities

// Launch game app
LaunchApp("com.example.game")
Sleep(5000)

// Wait for start button using image matching
Log("Looking for start button...")
$startButton = WaitForImage("templates/start_button.png", timeout: 10, confidence: 0.85)

if ($startButton != null) {
    Log("Start button found at: " + $startButton.centerX + "," + $startButton.centerY)
    Tap($startButton.centerX, $startButton.centerY)
} else {
    LogError("Start button not found!")
    Screenshot("error_no_start_button.png")
    return
}

Sleep(2000)

// Use OCR to read score from screen
$scoreRegion = [100, 50, 300, 100]  // x, y, width, height
$scoreText = ReadText(x: 100, y: 50, width: 300, height: 100)
Log("Current score: " + $scoreText)

// Find and tap level 1 icon
$level1 = FindImage("templates/level1_icon.png", confidence: 0.8)
if ($level1 != null) {
    Tap($level1.centerX, $level1.centerY)
    Log("Level 1 started")
}

// Monitor for "Victory" text using OCR
$attempts = 0
while ($attempts < 60) {
    $text = ReadText()
    if (TextExists("Victory")) {
        Log("Victory achieved!")
        Screenshot("victory.png")
        break
    }
    Sleep(1000)
    $attempts = $attempts + 1
}
