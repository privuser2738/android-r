// Multi-Device Testing Example
// This script runs the same test on multiple devices simultaneously

// Get all connected devices
$devices = GetAllDevices()
Print("Found " + Count($devices) + " connected devices")

// Define test steps
function RunTest($device) {
    Log("Starting test on device: " + $device.serial)

    // Launch app
    $device.LaunchApp("com.example.app")
    $device.Sleep(3000)

    // Perform test actions
    $device.Tap(540, 960)
    $device.Sleep(500)
    $device.Input("Test Input")

    // Take screenshot
    $device.Screenshot("device_" + $device.serial + "_result.png")

    Log("Test completed on device: " + $device.serial)
}

// Run test on all devices in parallel
ForEach($device in $devices) {
    RunTest($device)
}

// Wait for all devices to complete
SyncDevices($devices)

Print("All device tests completed!")
