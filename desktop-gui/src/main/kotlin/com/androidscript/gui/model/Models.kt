package com.androidscript.gui.model

import kotlinx.serialization.Serializable

/**
 * Device model
 */
@Serializable
data class Device(
    val id: String,
    val platform: String,
    val model: String,
    val version: String
)

/**
 * Device information (detailed)
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
 * Connection status
 */
enum class ConnectionStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    ERROR
}

/**
 * Tab selection
 */
enum class Tab {
    EXECUTE,
    DEVICES,
    SCREENSHOT,
    LOGS
}

/**
 * Log entry
 */
data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val message: String
)

enum class LogLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

/**
 * Screenshot data
 */
data class Screenshot(
    val deviceId: String,
    val data: ByteArray,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Screenshot

        if (deviceId != other.deviceId) return false
        if (!data.contentEquals(other.data)) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = deviceId.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
