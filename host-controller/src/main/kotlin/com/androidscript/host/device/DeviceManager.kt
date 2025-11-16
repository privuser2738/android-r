package com.androidscript.host.device

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * Central device management system
 * Discovers, connects, and manages Android and iOS devices
 */
class DeviceManager(
    private val adbPath: String = "adb",
    private val idevicePath: String = "idevice",
    private val autoDiscovery: Boolean = true,
    private val discoveryInterval: Long = 5000  // 5 seconds
) {
    private val devices = ConcurrentHashMap<String, Device>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _events = MutableSharedFlow<DeviceEvent>()
    val events: SharedFlow<DeviceEvent> = _events

    private var discoveryJob: Job? = null

    init {
        if (autoDiscovery) {
            startAutoDiscovery()
        }
    }

    /**
     * Start automatic device discovery
     */
    fun startAutoDiscovery() {
        if (discoveryJob?.isActive == true) {
            logger.warn { "Auto-discovery already running" }
            return
        }

        logger.info { "Starting auto-discovery (interval: ${discoveryInterval}ms)" }

        discoveryJob = scope.launch {
            while (isActive) {
                try {
                    discoverDevices()
                } catch (e: Exception) {
                    logger.error(e) { "Error during device discovery" }
                }
                delay(discoveryInterval)
            }
        }
    }

    /**
     * Stop automatic device discovery
     */
    fun stopAutoDiscovery() {
        logger.info { "Stopping auto-discovery" }
        discoveryJob?.cancel()
        discoveryJob = null
    }

    /**
     * Discover all connected devices
     */
    suspend fun discoverDevices(): List<Device> = withContext(Dispatchers.IO) {
        logger.debug { "Discovering devices..." }

        val discovered = mutableListOf<Device>()

        // Discover Android devices
        try {
            val androidDevices = discoverAndroidDevices()
            discovered.addAll(androidDevices)
        } catch (e: Exception) {
            logger.error(e) { "Failed to discover Android devices" }
        }

        // Discover iOS devices
        try {
            val iosDevices = discoverIOSDevices()
            discovered.addAll(iosDevices)
        } catch (e: Exception) {
            logger.error(e) { "Failed to discover iOS devices" }
        }

        // Update device registry
        val currentIds = discovered.map { it.id }.toSet()
        val previousIds = devices.keys.toSet()

        // Add new devices
        for (device in discovered) {
            if (device.id !in previousIds) {
                devices[device.id] = device
                _events.emit(DeviceEvent.Connected(device))
                logger.info { "Device connected: ${device.model} (${device.id})" }
            }
        }

        // Remove disconnected devices
        for (id in previousIds - currentIds) {
            devices.remove(id)
            _events.emit(DeviceEvent.Disconnected(id))
            logger.info { "Device disconnected: $id" }
        }

        logger.info { "Discovery complete: ${discovered.size} devices found" }
        discovered
    }

    /**
     * Discover Android devices using ADB
     */
    private fun discoverAndroidDevices(): List<Device> {
        val devices = mutableListOf<Device>()

        try {
            val process = ProcessBuilder(adbPath, "devices")
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()

            // Parse ADB output
            val lines = output.split("\n")
            for (line in lines) {
                if (line.contains("\t") && !line.contains("List of devices")) {
                    val parts = line.split("\t")
                    if (parts.size >= 2) {
                        val deviceId = parts[0].trim()
                        val status = parts[1].trim()

                        if (status == "device") {
                            val device = AndroidDevice(deviceId, adbPath)
                            devices.add(device)
                        }
                    }
                }
            }

            logger.debug { "Found ${devices.size} Android devices" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute ADB" }
        }

        return devices
    }

    /**
     * Discover iOS devices using libimobiledevice
     */
    private fun discoverIOSDevices(): List<Device> {
        val devices = mutableListOf<Device>()

        try {
            val process = ProcessBuilder("${idevicePath}_id", "-l")
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                // Parse device IDs
                val lines = output.split("\n")
                for (line in lines) {
                    val deviceId = line.trim()
                    if (deviceId.isNotEmpty() && deviceId.length >= 20) {
                        val device = iOSDevice(deviceId, idevicePath)
                        devices.add(device)
                    }
                }

                logger.debug { "Found ${devices.size} iOS devices" }
            } else {
                logger.warn { "libimobiledevice not installed or no iOS devices connected" }
            }
        } catch (e: Exception) {
            logger.debug { "libimobiledevice not available: ${e.message}" }
        }

        return devices
    }

    /**
     * Get all connected devices
     */
    fun getDevices(): List<Device> = devices.values.toList()

    /**
     * Get device by ID
     */
    fun getDevice(id: String): Device? = devices[id]

    /**
     * Get devices by platform
     */
    fun getDevicesByPlatform(platform: Platform): List<Device> {
        return devices.values.filter { it.platform == platform }
    }

    /**
     * Execute script on specific device
     */
    suspend fun executeScript(deviceId: String, script: String): ExecutionResult? {
        val device = devices[deviceId] ?: return null

        _events.emit(DeviceEvent.ExecutionStarted(deviceId, script.hashCode().toString()))

        val result = device.executeScript(script)

        _events.emit(
            DeviceEvent.ExecutionCompleted(
                deviceId,
                script.hashCode().toString(),
                result
            )
        )

        return result
    }

    /**
     * Execute script on all connected devices
     */
    suspend fun executeScriptOnAll(script: String): Map<String, ExecutionResult> {
        return withContext(Dispatchers.IO) {
            val results = mutableMapOf<String, ExecutionResult>()

            devices.values.map { device ->
                async {
                    val result = executeScript(device.id, script)
                    if (result != null) {
                        results[device.id] = result
                    }
                }
            }.awaitAll()

            results
        }
    }

    /**
     * Execute script on devices matching platform
     */
    suspend fun executeScriptByPlatform(
        script: String,
        platform: Platform
    ): Map<String, ExecutionResult> {
        return withContext(Dispatchers.IO) {
            val results = mutableMapOf<String, ExecutionResult>()

            getDevicesByPlatform(platform).map { device ->
                async {
                    val result = executeScript(device.id, script)
                    if (result != null) {
                        results[device.id] = result
                    }
                }
            }.awaitAll()

            results
        }
    }

    /**
     * Take screenshot from device
     */
    suspend fun takeScreenshot(deviceId: String): Screenshot? {
        return devices[deviceId]?.takeScreenshot()
    }

    /**
     * Get device information
     */
    suspend fun getDeviceInfo(deviceId: String): DeviceInfo? {
        return devices[deviceId]?.getDeviceInfo()
    }

    /**
     * Disconnect device
     */
    suspend fun disconnectDevice(deviceId: String) {
        devices[deviceId]?.disconnect()
        devices.remove(deviceId)
        _events.emit(DeviceEvent.Disconnected(deviceId))
    }

    /**
     * Disconnect all devices and cleanup
     */
    fun shutdown() {
        logger.info { "Shutting down DeviceManager" }

        stopAutoDiscovery()

        runBlocking {
            devices.values.forEach { it.disconnect() }
        }

        devices.clear()
        scope.cancel()
    }

    /**
     * Get summary statistics
     */
    fun getStats(): DeviceStats {
        val androidCount = devices.values.count { it.platform == Platform.ANDROID }
        val iosCount = devices.values.count { it.platform == Platform.IOS }

        return DeviceStats(
            totalDevices = devices.size,
            androidDevices = androidCount,
            iosDevices = iosCount,
            connectedDevices = devices.size
        )
    }
}

/**
 * Device statistics
 */
data class DeviceStats(
    val totalDevices: Int,
    val androidDevices: Int,
    val iosDevices: Int,
    val connectedDevices: Int
)
