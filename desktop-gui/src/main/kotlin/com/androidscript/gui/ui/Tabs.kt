package com.androidscript.gui.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidscript.gui.api.ApiClient
import com.androidscript.gui.model.*
import com.androidscript.gui.ui.theme.SuccessColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Execute Script Tab
 */
@Composable
fun ExecuteTab(
    scriptText: String,
    onScriptTextChanged: (String) -> Unit,
    outputText: String,
    isExecuting: Boolean,
    selectedDevice: Device?,
    devices: List<Device>,
    onExecute: (script: String, target: String) -> Unit
) {
    var targetDevice by remember { mutableStateOf(selectedDevice?.id ?: "") }

    LaunchedEffect(selectedDevice) {
        if (selectedDevice != null) {
            targetDevice = selectedDevice.id
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Execution controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Script Execution",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Target selection
                var expanded by remember { mutableStateOf(false) }

                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.width(250.dp)
                ) {
                    Icon(Icons.Default.Devices, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            targetDevice == "all" -> "All Devices (${devices.size})"
                            targetDevice.isEmpty() -> "Select device..."
                            else -> devices.find { it.id == targetDevice }?.model ?: targetDevice
                        }
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All devices (${devices.size})") },
                        onClick = {
                            targetDevice = "all"
                            expanded = false
                        },
                        leadingIcon = { Icon(Icons.Default.Devices, null) }
                    )
                    if (devices.isNotEmpty()) {
                        Divider()
                        devices.forEach { device ->
                            DropdownMenuItem(
                                text = { Text("${device.platform} - ${device.model}") },
                                onClick = {
                                    targetDevice = device.id
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        if (device.platform == "ANDROID") Icons.Default.Android else Icons.Default.Apple,
                                        null
                                    )
                                }
                            )
                        }
                    }
                }

                // Execute button
                Button(
                    onClick = {
                        if (targetDevice.isNotEmpty()) {
                            onExecute(scriptText, targetDevice)
                        }
                    },
                    enabled = !isExecuting && targetDevice.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessColor
                    )
                ) {
                    Icon(
                        if (isExecuting) Icons.Default.HourglassEmpty else Icons.Default.PlayArrow,
                        null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (isExecuting) "Executing..." else "Execute")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Script editor
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(0.6f).fillMaxWidth()
        ) {
            Column {
                // Toolbar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { onScriptTextChanged(SAMPLE_DEVICE_INFO) }
                        ) {
                            Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Device Info", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = { onScriptTextChanged(SAMPLE_TAP_TEST) }
                        ) {
                            Icon(Icons.Default.TouchApp, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Tap Test", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = { onScriptTextChanged(SAMPLE_AUTOMATION) }
                        ) {
                            Icon(Icons.Default.AutoMode, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Automation", fontSize = 12.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = { onScriptTextChanged("") }
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear", fontSize = 12.sp)
                        }
                    }
                }

                // Editor
                OutlinedTextField(
                    value = scriptText,
                    onValueChange = onScriptTextChanged,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Output console
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.weight(0.4f).fillMaxWidth()
        ) {
            Column {
                // Header
                Surface(
                    color = Color(0xFF2D2D2D),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Output",
                            color = Color(0xFFE0E0E0),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Console
                val scrollState = rememberScrollState()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(12.dp)
                ) {
                    if (outputText.isEmpty()) {
                        Text(
                            "No output yet. Run a script to see results.",
                            color = Color(0xFF808080),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            outputText,
                            color = Color(0xFFE0E0E0),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }

                LaunchedEffect(outputText) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            }
        }
    }
}

/**
 * Device Info Tab
 */
@Composable
fun DevicesTab(selectedDevice: Device?, apiClient: ApiClient) {
    val scope = rememberCoroutineScope()
    var deviceInfo by remember { mutableStateOf<DeviceInfo?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDevice) {
        if (selectedDevice != null) {
            isLoading = true
            deviceInfo = apiClient.getDeviceInfo(selectedDevice.id)
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when {
            selectedDevice == null -> {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Select a device from the list",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            deviceInfo != null -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            "Device Information",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        InfoCard("Platform") {
                            InfoRow("Platform", deviceInfo!!.platform)
                            InfoRow("Model", deviceInfo!!.model)
                            InfoRow("Version", deviceInfo!!.version)
                            InfoRow("Manufacturer", deviceInfo!!.manufacturer)
                        }
                    }

                    item {
                        InfoCard("Device Details") {
                            InfoRow("Device ID", deviceInfo!!.id)
                            InfoRow("Serial", deviceInfo!!.serial)
                            InfoRow("Screen Size", "${deviceInfo!!.screenWidth}×${deviceInfo!!.screenHeight}")
                        }
                    }

                    item {
                        InfoCard("Capabilities") {
                            deviceInfo!!.capabilities.forEach { capability ->
                                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        null,
                                        tint = SuccessColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(capability)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            value,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Screenshot Tab
 */
@Composable
fun ScreenshotTab(
    selectedDevice: Device?,
    screenshot: Screenshot?,
    onCapture: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Screenshot",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onCapture,
                enabled = selectedDevice != null
            ) {
                Icon(Icons.Default.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Capture Screenshot")
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                selectedDevice == null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Screenshot,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Select a device to capture screenshots",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                screenshot != null -> {
                    val image = remember(screenshot) { screenshot.data.toImageBitmap() }
                    if (image != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = 4.dp
                        ) {
                            Image(
                                bitmap = image,
                                contentDescription = "Screenshot",
                                modifier = Modifier.fillMaxSize(0.9f)
                            )
                        }
                    } else {
                        Text("Failed to load screenshot")
                    }
                }

                else -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Image,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No screenshot captured yet",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Logs Tab
 */
@Composable
fun LogsTab(logs: List<LogEntry>) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Activity Log",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { log ->
                    LogEntryItem(log, dateFormat)
                }

                if (logs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No logs yet",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogEntryItem(log: LogEntry, dateFormat: SimpleDateFormat) {
    val color = when (log.level) {
        LogLevel.INFO -> MaterialTheme.colorScheme.primary
        LogLevel.SUCCESS -> SuccessColor
        LogLevel.WARNING -> Color(0xFFFF9800)
        LogLevel.ERROR -> MaterialTheme.colorScheme.error
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                dateFormat.format(Date(log.timestamp)),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = color,
                modifier = Modifier.width(80.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                log.message,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Sample scripts
private val SAMPLE_DEVICE_INFO = """// Get device information
${"$"}device = GetDeviceInfo()
Print("Platform: " + ${"$"}device.platform)
Print("Model: " + ${"$"}device.model)
Print("Version: " + ${"$"}device.version)
Print("Screen: " + ${"$"}device.screenWidth + "x" + ${"$"}device.screenHeight)"""

private val SAMPLE_TAP_TEST = """// Tap test
${"$"}device = GetDeviceInfo()
${"$"}x = ${"$"}device.screenWidth / 2
${"$"}y = ${"$"}device.screenHeight / 2

Print("Tapping at: " + ${"$"}x + ", " + ${"$"}y)
Tap(${"$"}x, ${"$"}y)
Sleep(500)
Print("Tap completed!")"""

private val SAMPLE_AUTOMATION = """// UI Automation
Print("Starting automation...")

${"$"}button = FindByText("Submit")
if (${"$"}button != null) {
    Print("Found: " + ${"$"}button.text)
    Click(${"$"}button)
    Print("Button clicked!")
} else {
    Print("Button not found")
}

Print("Complete!")"""
