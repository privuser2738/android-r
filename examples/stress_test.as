// Stress Test Example
// Performs repeated actions to test app stability

$appPackage = "com.example.app"
$iterations = 100

Log("Starting stress test: " + $iterations + " iterations")

for ($i = 0; $i < $iterations; $i = $i + 1) {
    Log("Iteration: " + ($i + 1) + "/" + $iterations)

    // Launch app
    LaunchApp($appPackage)
    Sleep(2000)

    // Perform series of actions
    Tap(540, 800)
    Sleep(500)

    Swipe(540, 1500, 540, 500, 300)  // Swipe up
    Sleep(500)

    Swipe(540, 500, 540, 1500, 300)  // Swipe down
    Sleep(500)

    Tap(200, 1000)
    Sleep(500)

    PressKey("BACK")
    Sleep(500)

    // Check for ANR or crash
    if (TextExists("isn't responding")) {
        LogError("ANR detected at iteration " + ($i + 1))
        Screenshot("anr_iteration_" + ($i + 1) + ".png")
        TapText("Close app")
        Sleep(2000)
    }

    // Stop app
    StopApp($appPackage)
    Sleep(1000)

    // Take periodic screenshots
    if (($i + 1) % 10 == 0) {
        Screenshot("checkpoint_" + ($i + 1) + ".png")
    }
}

Log("Stress test completed: " + $iterations + " iterations")
