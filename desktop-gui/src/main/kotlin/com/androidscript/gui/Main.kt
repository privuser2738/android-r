package com.androidscript.gui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.androidscript.gui.api.ApiClient
import com.androidscript.gui.model.*
import com.androidscript.gui.ui.*
import com.androidscript.gui.ui.theme.DarkColorScheme
import kotlinx.coroutines.launch

fun main() = application {
    val windowState = rememberWindowState(
        size = DpSize(1400.dp, 900.dp)
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "AndroidScript Control Center",
        state = windowState
    ) {
        App()
    }
}

@Composable
fun App() {
    val apiClient = remember { ApiClient() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(Tab.EXECUTE) }
    var selectedDevice by remember { mutableStateOf<Device?>(null) }
    val devices by apiClient.devices.collectAsState()
    val connectionStatus by apiClient.connectionStatus.collectAsState()

    var scriptText by remember { mutableStateOf(SAMPLE_SCRIPT) }
    var outputText by remember { mutableStateOf("") }
    var isExecuting by remember { mutableStateOf(false) }

    val logs = remember { mutableStateListOf<LogEntry>() }
    var currentScreenshot by remember { mutableStateOf<Screenshot?>(null) }

    // Initialize API client
    LaunchedEffect(Unit) {
        apiClient.start()

        // Initial device refresh
        launch {
            apiClient.getDevices()
            addLog(logs, "Connected to AndroidScript server", LogLevel.SUCCESS)
        }

        // Periodic refresh
        while (true) {
            kotlinx.coroutines.delay(5000)
            apiClient.getDevices()
        }
    }

    MaterialTheme(
        colorScheme = DarkColorScheme
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                TopBar(connectionStatus, devices.size)

                Row(modifier = Modifier.fillMaxSize()) {
                    // Sidebar - Device List
                    DeviceList(
                        devices = devices,
                        selectedDevice = selectedDevice,
                        onDeviceSelected = { selectedDevice = it }
                    )

                    // Main Content
                    Column(modifier = Modifier.weight(1f)) {
                        // Tabs
                        TabRow(selectedTab) { selectedTab = it }

                        // Tab Content
                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedTab) {
                                Tab.EXECUTE -> ExecuteTab(
                                    scriptText = scriptText,
                                    onScriptTextChanged = { scriptText = it },
                                    outputText = outputText,
                                    isExecuting = isExecuting,
                                    selectedDevice = selectedDevice,
                                    devices = devices,
                                    onExecute = { script, target ->
                                        scope.launch {
                                            isExecuting = true
                                            outputText = "Executing script...\n\n"

                                            try {
                                                if (target == "all") {
                                                    // Execute on all devices
                                                    devices.forEach { device ->
                                                        val result = apiClient.executeScript(device.id, script)
                                                        if (result != null) {
                                                            outputText += formatExecutionResult(device, result)
                                                        }
                                                    }
                                                    addLog(logs, "Script executed on ${devices.size} devices", LogLevel.SUCCESS)
                                                } else {
                                                    // Execute on single device
                                                    val result = apiClient.executeScript(target, script)
                                                    if (result != null) {
                                                        val device = devices.find { it.id == target }
                                                        if (device != null) {
                                                            outputText += formatExecutionResult(device, result)
                                                        }

                                                        if (result.success) {
                                                            addLog(logs, "Script executed successfully", LogLevel.SUCCESS)
                                                        } else {
                                                            addLog(logs, "Script execution failed", LogLevel.ERROR)
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                outputText += "Error: ${e.message}\n"
                                                addLog(logs, "Execution error: ${e.message}", LogLevel.ERROR)
                                            } finally {
                                                isExecuting = false
                                            }
                                        }
                                    }
                                )

                                Tab.DEVICES -> DevicesTab(
                                    selectedDevice = selectedDevice,
                                    apiClient = apiClient
                                )

                                Tab.SCREENSHOT -> ScreenshotTab(
                                    selectedDevice = selectedDevice,
                                    screenshot = currentScreenshot,
                                    onCapture = {
                                        scope.launch {
                                            if (selectedDevice != null) {
                                                val data = apiClient.takeScreenshot(selectedDevice.id)
                                                if (data != null) {
                                                    currentScreenshot = Screenshot(
                                                        deviceId = selectedDevice.id,
                                                        data = data,
                                                        timestamp = System.currentTimeMillis()
                                                    )
                                                    addLog(logs, "Screenshot captured from ${selectedDevice.model}", LogLevel.SUCCESS)
                                                } else {
                                                    addLog(logs, "Failed to capture screenshot", LogLevel.ERROR)
                                                }
                                            }
                                        }
                                    }
                                )

                                Tab.LOGS -> LogsTab(logs)
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            apiClient.stop()
        }
    }
}

private fun formatExecutionResult(device: Device, result: ExecutionResult): String {
    return buildString {
        appendLine("=== ${device.model} (${device.id}) ===")
        appendLine("Status: ${if (result.success) "✓ Success" else "✗ Failed"}")
        appendLine("Time: ${result.executionTime}ms")

        if (result.output != null) {
            appendLine("\nOutput:")
            appendLine(result.output)
        }

        if (result.errors.isNotEmpty()) {
            appendLine("\nErrors:")
            result.errors.forEach { error ->
                appendLine("  • $error")
            }
        }

        appendLine()
    }
}

private fun addLog(logs: MutableList<LogEntry>, message: String, level: LogLevel) {
    logs.add(0, LogEntry(System.currentTimeMillis(), level, message))
    if (logs.size > 100) {
        logs.removeAt(logs.lastIndex)
    }
}

private const val SAMPLE_SCRIPT = """// AndroidScript Example
${"$"}device = GetDeviceInfo()
Print("Platform: " + ${"$"}device.platform)
Print("Model: " + ${"$"}device.model)
Print("Screen: " + ${"$"}device.screenWidth + "x" + ${"$"}device.screenHeight)
"""
