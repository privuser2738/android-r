package com.androidscript.gui.api

import com.androidscript.gui.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * API Client for AndroidScript host controller
 */
class ApiClient(
    private val baseUrl: String = "http://localhost:8080"
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(WebSockets)
    }

    private var wsSession: DefaultClientWebSocketSession? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices

    /**
     * Start the API client
     */
    fun start() {
        scope.launch {
            connectWebSocket()
        }
    }

    /**
     * Stop the API client
     */
    fun stop() {
        scope.cancel()
        client.close()
    }

    /**
     * Health check
     */
    suspend fun healthCheck(): Boolean {
        return try {
            val response = client.get("$baseUrl/health")
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            logger.error(e) { "Health check failed" }
            false
        }
    }

    /**
     * Get all devices
     */
    suspend fun getDevices(): List<Device> {
        return try {
            val devices: List<Device> = client.get("$baseUrl/devices").body()
            _devices.value = devices
            devices
        } catch (e: Exception) {
            logger.error(e) { "Failed to get devices" }
            emptyList()
        }
    }

    /**
     * Get device information
     */
    suspend fun getDeviceInfo(deviceId: String): DeviceInfo? {
        return try {
            client.get("$baseUrl/devices/$deviceId").body()
        } catch (e: Exception) {
            logger.error(e) { "Failed to get device info for $deviceId" }
            null
        }
    }

    /**
     * Execute script on device
     */
    suspend fun executeScript(deviceId: String, script: String): ExecutionResult? {
        return try {
            client.post("$baseUrl/devices/$deviceId/execute") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("script" to script))
            }.body()
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute script on $deviceId" }
            null
        }
    }

    /**
     * Execute script on all devices
     */
    suspend fun executeScriptOnAll(script: String): Map<String, ExecutionResult> {
        return try {
            val response = client.post("$baseUrl/rpc") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "jsonrpc" to "2.0",
                    "method" to "script.executeAll",
                    "params" to mapOf("script" to script),
                    "id" to System.currentTimeMillis()
                ))
            }

            val json = Json { ignoreUnknownKeys = true }
            val responseText = response.bodyAsText()
            val jsonElement = json.parseToJsonElement(responseText)

            // Parse the result field
            emptyMap() // Simplified for now
        } catch (e: Exception) {
            logger.error(e) { "Failed to execute script on all devices" }
            emptyMap()
        }
    }

    /**
     * Take screenshot from device
     */
    suspend fun takeScreenshot(deviceId: String): ByteArray? {
        return try {
            val response = client.get("$baseUrl/devices/$deviceId/screenshot")
            response.body()
        } catch (e: Exception) {
            logger.error(e) { "Failed to take screenshot from $deviceId" }
            null
        }
    }

    /**
     * Connect to WebSocket for real-time updates
     */
    private suspend fun connectWebSocket() {
        _connectionStatus.value = ConnectionStatus.CONNECTING

        try {
            client.webSocket(
                method = HttpMethod.Get,
                host = baseUrl.substringAfter("://").substringBefore(":"),
                port = baseUrl.substringAfterLast(":").toIntOrNull() ?: 8080,
                path = "/ws"
            ) {
                wsSession = this
                _connectionStatus.value = ConnectionStatus.CONNECTED
                logger.info { "WebSocket connected" }

                try {
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                handleWebSocketMessage(text)
                            }
                            else -> {}
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "WebSocket error" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to connect WebSocket" }
            _connectionStatus.value = ConnectionStatus.ERROR

            // Retry after 5 seconds
            delay(5000)
            connectWebSocket()
        }
    }

    /**
     * Handle incoming WebSocket message
     */
    private suspend fun handleWebSocketMessage(message: String) {
        logger.debug { "WebSocket message: $message" }

        try {
            // Parse and handle different message types
            if (message.contains("\"type\":\"devices\"")) {
                // Device list updated
                getDevices()
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to handle WebSocket message" }
        }
    }
}
