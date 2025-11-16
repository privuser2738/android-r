package com.androidscript.host.device

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Android device implementation using ADB
 */
class AndroidDevice(
    override val id: String,
    private val adbPath: String = "adb"
) : Device {

    override val platform = Platform.ANDROID
    override var model: String = "Unknown"
    override var version: String = "Unknown"
    override var manufacturer: String = "Unknown"
    override var screenWidth: Int = 0
    override var screenHeight: Int = 0

    private var connected = false

    init {
        // Initialize device info
        refreshDeviceInfo()
    }

    /**
     * Refresh device information from ADB
     */
    private fun refreshDeviceInfo() {
        try {
            model = getProperty("ro.product.model") ?: "Unknown"
            version = getProperty("ro.build.version.release") ?: "Unknown"
            manufacturer = getProperty("ro.product.manufacturer") ?: "Unknown"

            // Get screen size
            val wm = executeAdb("shell wm size")
            val sizeMatch = Regex("Physical size: (\\d+)x(\\d+)").find(wm)
            if (sizeMatch != null) {
                screenWidth = sizeMatch.groupValues[1].toIntOrNull() ?: 0
                screenHeight = sizeMatch.groupValues[2].toIntOrNull() ?: 0
            }

            connected = true
            logger.info { "Android device initialized: $model ($id)" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize Android device: $id" }
            connected = false
        }
    }

    /**
     * Get device property using getprop
     */
    private fun getProperty(key: String): String? {
        return try {
            val result = executeAdb("shell getprop $key")
            result.trim().takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Execute ADB command for this device
     */
    private fun executeAdb(command: String): String {
        val fullCommand = "$adbPath -s $id $command"
        logger.debug { "Executing: $fullCommand" }

        val process = ProcessBuilder(fullCommand.split(" "))
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            throw RuntimeException("ADB command failed: $fullCommand\nOutput: $output")
        }

        return output
    }

    override suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            val devices = executeAdb("devices")
            connected = devices.contains(id) && !devices.contains("offline")
            connected
        } catch (e: Exception) {
            logger.error(e) { "Failed to check connection for device: $id" }
            connected = false
            false
        }
    }

    override suspend fun executeScript(script: String): ExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            logger.info { "Executing script on Android device: $id" }

            // Push script to device
            val tempScriptFile = File.createTempFile("script_", ".as")
            tempScriptFile.writeText(script)

            val deviceScriptPath = "/data/local/tmp/script.as"
            executeAdb("push ${tempScriptFile.absolutePath} $deviceScriptPath")

            // Execute via automation service (assuming agent is installed)
            val packageName = "com.androidscript.agent"
            val output = executeAdb(
                "shell am broadcast " +
                "-a com.androidscript.EXECUTE_SCRIPT " +
                "-e script_path $deviceScriptPath " +
                "$packageName"
            )

            tempScriptFile.delete()

            val executionTime = System.currentTimeMillis() - startTime

            ExecutionResult(
                success = true,
                output = output,
                errors = emptyList(),
                executionTime = executionTime
            )
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            logger.error(e) { "Script execution failed on device: $id" }

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
            logger.info { "Taking screenshot on Android device: $id" }

            val devicePath = "/data/local/tmp/screenshot.png"
            executeAdb("shell screencap -p $devicePath")

            val tempFile = File.createTempFile("screenshot_", ".png")
            executeAdb("pull $devicePath ${tempFile.absolutePath}")

            val data = tempFile.readBytes()
            tempFile.delete()

            Screenshot(
                width = screenWidth,
                height = screenHeight,
                format = "png",
                data = data
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to take screenshot on device: $id" }
            null
        }
    }

    override suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        DeviceInfo(
            id = id,
            platform = "Android",
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
                "accessibility_service"
            )
        )
    }

    override suspend fun disconnect() {
        logger.info { "Disconnecting Android device: $id" }
        connected = false
    }

    override fun toString(): String {
        return "AndroidDevice(id=$id, model=$model, version=$version)"
    }
}
