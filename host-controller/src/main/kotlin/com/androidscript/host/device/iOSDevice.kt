package com.androidscript.host.device

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * iOS device implementation using libimobiledevice
 */
class iOSDevice(
    override val id: String,
    private val idevicePath: String = "idevice"
) : Device {

    override val platform = Platform.IOS
    override var model: String = "Unknown"
    override var version: String = "Unknown"
    override var manufacturer: String = "Apple"
    override var screenWidth: Int = 0
    override var screenHeight: Int = 0

    private var connected = false

    init {
        refreshDeviceInfo()
    }

    /**
     * Refresh device information using libimobiledevice tools
     */
    private fun refreshDeviceInfo() {
        try {
            // Get device info using ideviceinfo
            val info = executeIDevice("ideviceinfo -u $id")

            model = extractValue(info, "ProductType") ?: "Unknown"
            version = extractValue(info, "ProductVersion") ?: "Unknown"

            // Parse screen dimensions (if available)
            // This might require device-specific mapping
            when {
                model.contains("iPhone14") -> {
                    screenWidth = 1170
                    screenHeight = 2532
                }
                model.contains("iPhone13") -> {
                    screenWidth = 1170
                    screenHeight = 2532
                }
                model.contains("iPad") -> {
                    screenWidth = 1620
                    screenHeight = 2160
                }
                else -> {
                    screenWidth = 1170
                    screenHeight = 2532  // Default to iPhone size
                }
            }

            connected = true
            logger.info { "iOS device initialized: $model ($id)" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize iOS device: $id" }
            connected = false
        }
    }

    /**
     * Extract value from ideviceinfo output
     */
    private fun extractValue(output: String, key: String): String? {
        val pattern = Regex("$key: (.+)")
        return pattern.find(output)?.groupValues?.get(1)?.trim()
    }

    /**
     * Execute libimobiledevice command
     */
    private fun executeIDevice(command: String): String {
        logger.debug { "Executing: $command" }

        val process = ProcessBuilder(command.split(" "))
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw RuntimeException("iDevice command failed: $command\nOutput: $output")
        }

        return output
    }

    override suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            val devices = executeIDevice("idevice_id -l")
            connected = devices.contains(id)
            connected
        } catch (e: Exception) {
            logger.error(e) { "Failed to check connection for iOS device: $id" }
            connected = false
            false
        }
    }

    override suspend fun executeScript(script: String): ExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            logger.info { "Executing script on iOS device: $id" }

            // For iOS, we would need to communicate with the installed iOSAgent app
            // This could be done via:
            // 1. WebSocket connection to the app
            // 2. File transfer via AFC (Apple File Conduit)
            // 3. Custom protocol handler

            // Placeholder implementation - would need actual iOS communication
            logger.warn { "iOS script execution not yet fully implemented" }

            val executionTime = System.currentTimeMillis() - startTime

            ExecutionResult(
                success = false,
                output = null,
                errors = listOf("iOS script execution requires app installation and communication channel"),
                executionTime = executionTime
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            logger.error(e) { "Script execution failed on iOS device: $id" }

            ExecutionResult(
                success = false,
                output = null,
                errors = listOf(e.message ?: "Unknown error"),
                executionTime = executionTime
            )
        }
    }

    override suspend fun takeScreenshot(): Screenshot? = withContext(Dispatchers.IO) {
        try {
            logger.info { "Taking screenshot on iOS device: $id" }

            // Use idevicescreenshot if available
            val tempFile = java.io.File.createTempFile("screenshot_", ".png")
            executeIDevice("idevicescreenshot -u $id ${tempFile.absolutePath}")

            val data = tempFile.readBytes()
            tempFile.delete()

            Screenshot(
                width = screenWidth,
                height = screenHeight,
                format = "png",
                data = data
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to take screenshot on iOS device: $id" }
            null
        }
    }

    override suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        DeviceInfo(
            id = id,
            platform = "iOS",
            model = model,
            version = version,
            serial = id,
            manufacturer = manufacturer,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            capabilities = listOf(
                "tap",
                "swipe",
                "text_input",
                "find_element",
                "screenshot",
                "xctest"
            )
        }
    }

    override suspend fun disconnect() {
        logger.info { "Disconnecting iOS device: $id" }
        connected = false
    }

    override fun toString(): String {
        return "iOSDevice(id=$id, model=$model, version=$version)"
    }
}
