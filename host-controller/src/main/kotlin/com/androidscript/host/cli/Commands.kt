package com.androidscript.host.cli

import com.androidscript.host.device.DeviceManager
import com.androidscript.host.device.Platform
import com.androidscript.host.protocol.JsonRpcServer
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Main CLI command
 */
class AndroidScriptCLI : CliktCommand(
    name = "androidscript",
    help = "AndroidScript multi-platform automation framework"
) {
    override fun run() = Unit
}

/**
 * Server command - start the protocol server
 */
class ServerCommand : CliktCommand(
    name = "server",
    help = "Start the JSON-RPC protocol server"
) {
    private val port by option("-p", "--port", help = "Server port").int().default(8080)
    private val host by option("-h", "--host", help = "Server host").default("0.0.0.0")
    private val noAutoDiscovery by option("--no-auto-discovery", help = "Disable auto-discovery").flag()

    override fun run() = runBlocking {
        echo("Starting AndroidScript Host Controller")
        echo("Server: http://$host:$port")
        echo()

        val deviceManager = DeviceManager(autoDiscovery = !noAutoDiscovery)
        val server = JsonRpcServer(deviceManager, port, host)

        Runtime.getRuntime().addShutdownHook(Thread {
            echo("\nShutting down...")
            server.stop()
            deviceManager.shutdown()
        })

        server.start()

        echo("✓ Server started successfully")
        echo("  REST API: http://$host:$port/devices")
        echo("  WebSocket: ws://$host:$port/ws")
        echo()
        echo("Press Ctrl+C to stop")

        // Keep running
        while (true) {
            Thread.sleep(1000)
        }
    }
}

/**
 * Devices command - list connected devices
 */
class DevicesCommand : CliktCommand(
    name = "devices",
    help = "List all connected devices"
) {
    private val platform by option("-p", "--platform", help = "Filter by platform (android|ios)")

    override fun run() = runBlocking {
        val deviceManager = DeviceManager(autoDiscovery = false)

        echo("Discovering devices...")
        deviceManager.discoverDevices()

        val devices = when (platform?.lowercase()) {
            "android" -> deviceManager.getDevicesByPlatform(Platform.ANDROID)
            "ios" -> deviceManager.getDevicesByPlatform(Platform.IOS)
            else -> deviceManager.getDevices()
        }

        echo()

        if (devices.isEmpty()) {
            echo("No devices found")
        } else {
            echo("Found ${devices.size} device(s):")
            echo()

            devices.forEach { device ->
                echo("  ${device.platform.name.padEnd(8)} ${device.id.padEnd(25)} ${device.model} (${device.version})")
            }
        }

        echo()

        val stats = deviceManager.getStats()
        echo("Summary:")
        echo("  Android: ${stats.androidDevices}")
        echo("  iOS:     ${stats.iosDevices}")
        echo("  Total:   ${stats.totalDevices}")

        deviceManager.shutdown()
    }
}

/**
 * Info command - get device information
 */
class InfoCommand : CliktCommand(
    name = "info",
    help = "Get detailed device information"
) {
    private val deviceId by argument(name = "device-id", help = "Device ID")

    override fun run() = runBlocking {
        val deviceManager = DeviceManager(autoDiscovery = false)

        echo("Discovering devices...")
        deviceManager.discoverDevices()

        val device = deviceManager.getDevice(deviceId)

        if (device == null) {
            echo("Error: Device not found: $deviceId")
            return@runBlocking
        }

        val info = device.getDeviceInfo()

        echo()
        echo("Device Information:")
        echo("  ID:           ${info.id}")
        echo("  Platform:     ${info.platform}")
        echo("  Model:        ${info.model}")
        echo("  Version:      ${info.version}")
        echo("  Manufacturer: ${info.manufacturer}")
        echo("  Screen:       ${info.screenWidth}x${info.screenHeight}")
        echo()
        echo("Capabilities:")
        info.capabilities.forEach { capability ->
            echo("  ✓ $capability")
        }

        deviceManager.shutdown()
    }
}

/**
 * Execute command - run a script on devices
 */
class ExecuteCommand : CliktCommand(
    name = "execute",
    help = "Execute AndroidScript on devices"
) {
    private val deviceId by option("-d", "--device", help = "Target device ID").optional()
    private val all by option("-a", "--all", help = "Execute on all devices").flag()
    private val platform by option("-p", "--platform", help = "Execute on platform (android|ios)")
    private val scriptFile by option("-f", "--file", help = "Script file").file()
    private val script by argument(name = "script", help = "Script to execute").optional()

    override fun run() = runBlocking {
        val scriptText = when {
            scriptFile != null -> scriptFile!!.readText()
            script != null -> script!!
            else -> {
                echo("Error: Provide either --file or script argument")
                return@runBlocking
            }
        }

        val deviceManager = DeviceManager(autoDiscovery = false)

        echo("Discovering devices...")
        deviceManager.discoverDevices()

        val results = when {
            deviceId != null -> {
                echo("Executing on device: $deviceId")
                val result = deviceManager.executeScript(deviceId!!, scriptText)
                if (result != null) mapOf(deviceId!! to result) else emptyMap()
            }

            all -> {
                echo("Executing on all devices...")
                deviceManager.executeScriptOnAll(scriptText)
            }

            platform != null -> {
                val p = when (platform?.lowercase()) {
                    "android" -> Platform.ANDROID
                    "ios" -> Platform.IOS
                    else -> {
                        echo("Error: Unknown platform: $platform")
                        return@runBlocking
                    }
                }
                echo("Executing on $platform devices...")
                deviceManager.executeScriptByPlatform(scriptText, p)
            }

            else -> {
                echo("Error: Specify --device, --all, or --platform")
                return@runBlocking
            }
        }

        echo()
        echo("Results:")
        echo()

        results.forEach { (id, result) ->
            echo("Device: $id")
            echo("  Status: ${if (result.success) "✓ Success" else "✗ Failed"}")
            echo("  Time:   ${result.executionTime}ms")

            if (result.output != null) {
                echo("  Output:")
                result.output.lines().forEach { line ->
                    echo("    $line")
                }
            }

            if (result.errors.isNotEmpty()) {
                echo("  Errors:")
                result.errors.forEach { error ->
                    echo("    • $error")
                }
            }

            echo()
        }

        deviceManager.shutdown()
    }
}

/**
 * Screenshot command - take screenshot from device
 */
class ScreenshotCommand : CliktCommand(
    name = "screenshot",
    help = "Take screenshot from device"
) {
    private val deviceId by argument(name = "device-id", help = "Device ID")
    private val output by option("-o", "--output", help = "Output file").file().default(File("screenshot.png"))

    override fun run() = runBlocking {
        val deviceManager = DeviceManager(autoDiscovery = false)

        echo("Discovering devices...")
        deviceManager.discoverDevices()

        val screenshot = deviceManager.takeScreenshot(deviceId)

        if (screenshot == null) {
            echo("Error: Failed to take screenshot")
            return@runBlocking
        }

        output.writeBytes(screenshot.data)

        echo("✓ Screenshot saved: ${output.absolutePath}")
        echo("  Size: ${screenshot.width}x${screenshot.height}")
        echo("  Format: ${screenshot.format}")
        echo("  File size: ${screenshot.data.size / 1024} KB")

        deviceManager.shutdown()
    }
}
