package com.androidscript.host.device

import kotlinx.serialization.Serializable

/**
 * Unified device interface for cross-platform automation
 * Supports Android, iOS, and future platforms
 */
interface Device {
    val id: String
    val platform: Platform
    val model: String
    val version: String
    val manufacturer: String
    val screenWidth: Int
    val screenHeight: Int

    suspend fun isConnected(): Boolean
    suspend fun executeScript(script: String): ExecutionResult
    suspend fun takeScreenshot(): Screenshot?
    suspend fun getDeviceInfo(): DeviceInfo
    suspend fun disconnect()
}

/**
 * Supported platforms
 */
enum class Platform {
    ANDROID,
    IOS,
    UNKNOWN
}

/**
 * Device information
 */
@Serializable
data class DeviceInfo(
    val id: String,
    val platform: String,
    val model: String,
    val version: String,
    val serial: String,
    val manufacturer: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val capabilities: List<String>
)

/**
 * Script execution result
 */
@Serializable
data class ExecutionResult(
    val success: Boolean,
    val output: String?,
    val errors: List<String>,
    val executionTime: Long
)

/**
 * Screenshot data
 */
@Serializable
data class Screenshot(
    val width: Int,
    val height: Int,
    val format: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Screenshot

        if (width != other.width) return false
        if (height != other.height) return false
        if (format != other.format) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + format.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

/**
 * Device connection status
 */
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    UNAUTHORIZED,
    OFFLINE,
    ERROR
}

/**
 * Device event
 */
sealed class DeviceEvent {
    data class Connected(val device: Device) : DeviceEvent()
    data class Disconnected(val deviceId: String) : DeviceEvent()
    data class StatusChanged(val deviceId: String, val status: ConnectionStatus) : DeviceEvent()
    data class ExecutionStarted(val deviceId: String, val scriptId: String) : DeviceEvent()
    data class ExecutionCompleted(val deviceId: String, val scriptId: String, val result: ExecutionResult) : DeviceEvent()
}
