package com.androidscript.host.protocol

import com.androidscript.host.device.DeviceManager
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import mu.KotlinLogging
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * JSON-RPC 2.0 Server for remote device control
 * Provides REST API and WebSocket support
 */
class JsonRpcServer(
    private val deviceManager: DeviceManager,
    private val port: Int = 8080,
    private val host: String = "0.0.0.0"
) {
    private var server: NettyApplicationEngine? = null
    private val wsConnections = ConcurrentHashMap<String, DefaultWebSocketSession>()

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    /**
     * Start the server
     */
    fun start() {
        logger.info { "Starting JSON-RPC server on $host:$port" }

        server = embeddedServer(Netty, port = port, host = host) {
            install(ContentNegotiation) {
                json(json)
            }

            install(CORS) {
                anyHost()
                allowHeader("Content-Type")
            }

            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }

            routing {
                // Health check
                get("/health") {
                    call.respond(mapOf("status" to "ok"))
                }

                // JSON-RPC endpoint
                post("/rpc") {
                    val request = call.receive<JsonRpcRequest>()
                    val response = handleRequest(request)
                    call.respond(response)
                }

                // Device list
                get("/devices") {
                    val devices = deviceManager.getDevices().map { device ->
                        mapOf(
                            "id" to device.id,
                            "platform" to device.platform.name,
                            "model" to device.model,
                            "version" to device.version
                        )
                    }
                    call.respond(devices)
                }

                // Device info
                get("/devices/{id}") {
                    val deviceId = call.parameters["id"] ?: ""
                    val info = deviceManager.getDeviceInfo(deviceId)

                    if (info != null) {
                        call.respond(info)
                    } else {
                        call.respond(
                            io.ktor.http.HttpStatusCode.NotFound,
                            mapOf("error" to "Device not found")
                        )
                    }
                }

                // Execute script
                post("/devices/{id}/execute") {
                    val deviceId = call.parameters["id"] ?: ""
                    val request = call.receive<Map<String, String>>()
                    val script = request["script"] ?: ""

                    val result = deviceManager.executeScript(deviceId, script)

                    if (result != null) {
                        call.respond(result)
                    } else {
                        call.respond(
                            io.ktor.http.HttpStatusCode.NotFound,
                            mapOf("error" to "Device not found")
                        )
                    }
                }

                // Screenshot
                get("/devices/{id}/screenshot") {
                    val deviceId = call.parameters["id"] ?: ""
                    val screenshot = deviceManager.takeScreenshot(deviceId)

                    if (screenshot != null) {
                        call.respondBytes(screenshot.data, io.ktor.http.ContentType.Image.PNG)
                    } else {
                        call.respond(
                            io.ktor.http.HttpStatusCode.InternalServerError,
                            mapOf("error" to "Failed to take screenshot")
                        )
                    }
                }

                // WebSocket for real-time updates
                webSocket("/ws") {
                    val sessionId = System.currentTimeMillis().toString()
                    wsConnections[sessionId] = this

                    logger.info { "WebSocket client connected: $sessionId" }

                    try {
                        // Send initial device list
                        val devices = deviceManager.getDevices()
                        send(Frame.Text(json.encodeToString(
                            WsMessage.serializer(),
                            WsMessage(
                                type = "devices",
                                data = json.parseToJsonElement(json.encodeToString(
                                    kotlinx.serialization.builtins.ListSerializer(
                                        kotlinx.serialization.builtins.serializer()
                                    ),
                                    devices.map { it.id }
                                ))
                            )
                        )))

                        // Listen for device events
                        launch {
                            deviceManager.events.collect { event ->
                                send(Frame.Text(json.encodeToString(
                                    WsMessage.serializer(),
                                    WsMessage(
                                        type = "event",
                                        data = json.parseToJsonElement(event.toString())
                                    )
                                )))
                            }
                        }

                        // Handle incoming messages
                        incoming.consumeEach { frame ->
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                logger.debug { "Received WS message: $text" }

                                val request = json.decodeFromString<JsonRpcRequest>(text)
                                val response = handleRequest(request)

                                send(Frame.Text(json.encodeToString(
                                    JsonRpcResponse.serializer(),
                                    response
                                )))
                            }
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "WebSocket error: $sessionId" }
                    } finally {
                        wsConnections.remove(sessionId)
                        logger.info { "WebSocket client disconnected: $sessionId" }
                    }
                }
            }
        }.start(wait = false)

        logger.info { "JSON-RPC server started successfully" }
    }

    /**
     * Handle JSON-RPC request
     */
    private suspend fun handleRequest(request: JsonRpcRequest): JsonRpcResponse {
        return try {
            val result = when (request.method) {
                "devices.list" -> {
                    val devices = deviceManager.getDevices()
                    json.parseToJsonElement(json.encodeToString(
                        kotlinx.serialization.builtins.ListSerializer(
                            kotlinx.serialization.builtins.serializer()
                        ),
                        devices.map { device ->
                            mapOf(
                                "id" to device.id,
                                "platform" to device.platform.name,
                                "model" to device.model,
                                "version" to device.version
                            )
                        }
                    ))
                }

                "devices.get" -> {
                    val params = request.params?.jsonObject
                    val deviceId = params?.get("id")?.toString()?.removeSurrounding("\"") ?: ""
                    val info = deviceManager.getDeviceInfo(deviceId)

                    if (info != null) {
                        json.parseToJsonElement(json.encodeToString(
                            com.androidscript.host.device.DeviceInfo.serializer(),
                            info
                        ))
                    } else {
                        throw JsonRpcException(-32602, "Device not found")
                    }
                }

                "script.execute" -> {
                    val params = request.params?.jsonObject
                    val deviceId = params?.get("deviceId")?.toString()?.removeSurrounding("\"") ?: ""
                    val script = params?.get("script")?.toString()?.removeSurrounding("\"") ?: ""

                    val result = deviceManager.executeScript(deviceId, script)

                    if (result != null) {
                        json.parseToJsonElement(json.encodeToString(
                            com.androidscript.host.device.ExecutionResult.serializer(),
                            result
                        ))
                    } else {
                        throw JsonRpcException(-32602, "Device not found")
                    }
                }

                "script.executeAll" -> {
                    val params = request.params?.jsonObject
                    val script = params?.get("script")?.toString()?.removeSurrounding("\"") ?: ""

                    val results = deviceManager.executeScriptOnAll(script)
                    json.parseToJsonElement(json.encodeToString(
                        kotlinx.serialization.builtins.MapSerializer(
                            kotlinx.serialization.builtins.serializer(),
                            com.androidscript.host.device.ExecutionResult.serializer()
                        ),
                        results
                    ))
                }

                "device.screenshot" -> {
                    val params = request.params?.jsonObject
                    val deviceId = params?.get("id")?.toString()?.removeSurrounding("\"") ?: ""

                    val screenshot = deviceManager.takeScreenshot(deviceId)

                    if (screenshot != null) {
                        json.parseToJsonElement(mapOf(
                            "width" to screenshot.width,
                            "height" to screenshot.height,
                            "format" to screenshot.format,
                            "size" to screenshot.data.size
                        ).toString())
                    } else {
                        throw JsonRpcException(-32603, "Failed to take screenshot")
                    }
                }

                else -> throw JsonRpcException(-32601, "Method not found: ${request.method}")
            }

            JsonRpcResponse(
                jsonrpc = "2.0",
                result = result,
                error = null,
                id = request.id
            )
        } catch (e: JsonRpcException) {
            JsonRpcResponse(
                jsonrpc = "2.0",
                result = null,
                error = JsonRpcError(e.code, e.message ?: "Unknown error"),
                id = request.id
            )
        } catch (e: Exception) {
            logger.error(e) { "Error handling request: ${request.method}" }

            JsonRpcResponse(
                jsonrpc = "2.0",
                result = null,
                error = JsonRpcError(-32603, "Internal error: ${e.message}"),
                id = request.id
            )
        }
    }

    /**
     * Stop the server
     */
    fun stop() {
        logger.info { "Stopping JSON-RPC server" }
        server?.stop(1000, 5000)
        wsConnections.clear()
    }
}

/**
 * JSON-RPC 2.0 Request
 */
@Serializable
data class JsonRpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: JsonElement? = null,
    val id: Int
)

/**
 * JSON-RPC 2.0 Response
 */
@Serializable
data class JsonRpcResponse(
    val jsonrpc: String,
    val result: JsonElement?,
    val error: JsonRpcError?,
    val id: Int
)

/**
 * JSON-RPC Error
 */
@Serializable
data class JsonRpcError(
    val code: Int,
    val message: String
)

/**
 * JSON-RPC Exception
 */
class JsonRpcException(val code: Int, message: String) : Exception(message)

/**
 * WebSocket message
 */
@Serializable
data class WsMessage(
    val type: String,
    val data: JsonElement
)
