# Host Controller - AndroidScript Multi-Device Orchestration

Central control system for managing and automating multiple Android and iOS devices simultaneously.

## Features

- **Multi-Device Management** - Control Android and iOS devices from one system
- **Auto-Discovery** - Automatic detection of connected devices via ADB and libimobiledevice
- **JSON-RPC 2.0 API** - RESTful API and WebSocket support for remote control
- **CLI Tool** - Command-line interface for device management and script execution
- **Concurrent Execution** - Run scripts on multiple devices in parallel
- **Real-Time Events** - WebSocket notifications for device status and execution results

## Architecture

```
host-controller/
├── src/main/kotlin/com/androidscript/host/
│   ├── device/                  # Device management
│   │   ├── Device.kt           # Unified device interface
│   │   ├── AndroidDevice.kt    # Android implementation (ADB)
│   │   ├── iOSDevice.kt        # iOS implementation (libimobiledevice)
│   │   └── DeviceManager.kt    # Device discovery & management
│   ├── protocol/                # Communication protocols
│   │   └── JsonRpcServer.kt    # JSON-RPC 2.0 server
│   ├── cli/                     # Command-line interface
│   │   └── Commands.kt         # CLI commands
│   └── Main.kt                 # Application entry point
└── build.gradle.kts            # Build configuration
```

## Prerequisites

### Android Support
- **ADB (Android Debug Bridge)** - Part of Android SDK platform-tools
- Devices must have USB debugging enabled

### iOS Support  
- **libimobiledevice** - iOS device communication library
  ```bash
  # macOS
  brew install libimobiledevice
  
  # Linux
  sudo apt-get install libimobiledevice-utils
  ```

## Building

```bash
cd host-controller
./gradlew build
```

Create standalone JAR:
```bash
./gradlew fatJar
```

## Usage

### Start Server

Start the JSON-RPC server for remote control:

```bash
./gradlew run --args="server"

# Or with custom port
./gradlew run --args="server --port 9090"

# Disable auto-discovery
./gradlew run --args="server --no-auto-discovery"
```

Using fat JAR:
```bash
java -jar build/libs/host-controller-all.jar server
```

### List Devices

```bash
# List all devices
./gradlew run --args="devices"

# Filter by platform
./gradlew run --args="devices --platform android"
./gradlew run --args="devices --platform ios"
```

Output:
```
Found 3 device(s):

  ANDROID  emulator-5554              Pixel 6 (13)
  ANDROID  R5CR10ABCDE               Galaxy S21 (12)
  IOS      00008030-001234567890001  iPhone 14 Pro (16.0)

Summary:
  Android: 2
  iOS:     1
  Total:   3
```

### Device Info

```bash
./gradlew run --args="info <device-id>"
```

Output:
```
Device Information:
  ID:           emulator-5554
  Platform:     Android
  Model:        Pixel 6
  Version:      13
  Manufacturer: Google
  Screen:       1080x2400

Capabilities:
  ✓ tap
  ✓ swipe
  ✓ text_input
  ✓ find_element
  ✓ screenshot
  ✓ accessibility_service
```

### Execute Script

Execute on specific device:
```bash
./gradlew run --args="execute --device emulator-5554 'Print(\"Hello\")'"
```

Execute on all devices:
```bash
./gradlew run --args="execute --all 'Print(\"Running on all devices\")'"
```

Execute on platform:
```bash
./gradlew run --args="execute --platform android --file test.as"
```

### Take Screenshot

```bash
./gradlew run --args="screenshot emulator-5554 --output screen.png"
```

## API Reference

### REST Endpoints

**Health Check**
```
GET /health
Response: {"status": "ok"}
```

**List Devices**
```
GET /devices
Response: [
  {
    "id": "emulator-5554",
    "platform": "ANDROID",
    "model": "Pixel 6",
    "version": "13"
  }
]
```

**Get Device Info**
```
GET /devices/{id}
Response: {
  "id": "emulator-5554",
  "platform": "Android",
  "model": "Pixel 6",
  "version": "13",
  "serial": "emulator-5554",
  "manufacturer": "Google",
  "screenWidth": 1080,
  "screenHeight": 2400,
  "capabilities": ["tap", "swipe", ...]
}
```

**Execute Script**
```
POST /devices/{id}/execute
Body: {
  "script": "Print('Hello')"
}
Response: {
  "success": true,
  "output": "Hello",
  "errors": [],
  "executionTime": 123
}
```

**Take Screenshot**
```
GET /devices/{id}/screenshot
Response: Binary PNG image data
```

### JSON-RPC Methods

Connect to `POST /rpc`

**devices.list**
```json
{
  "jsonrpc": "2.0",
  "method": "devices.list",
  "params": {},
  "id": 1
}
```

**devices.get**
```json
{
  "jsonrpc": "2.0",
  "method": "devices.get",
  "params": {"id": "emulator-5554"},
  "id": 2
}
```

**script.execute**
```json
{
  "jsonrpc": "2.0",
  "method": "script.execute",
  "params": {
    "deviceId": "emulator-5554",
    "script": "Print('Hello')"
  },
  "id": 3
}
```

**script.executeAll**
```json
{
  "jsonrpc": "2.0",
  "method": "script.executeAll",
  "params": {
    "script": "Print('Hello from all')"
  },
  "id": 4
}
```

**device.screenshot**
```json
{
  "jsonrpc": "2.0",
  "method": "device.screenshot",
  "params": {"id": "emulator-5554"},
  "id": 5
}
```

### WebSocket

Connect to `ws://localhost:8080/ws`

The server sends real-time events:

```json
{
  "type": "event",
  "data": "Connected(device=AndroidDevice(...))"
}
```

Send JSON-RPC requests through WebSocket for bidirectional communication.

## Development

### Project Structure

- **device/** - Device abstraction and implementations
- **protocol/** - Server and API implementation  
- **cli/** - Command-line interface
- **router/** - Script routing (future)

### Adding New Platform

1. Implement `Device` interface
2. Add discovery logic to `DeviceManager`
3. Test with platform-specific tools

### Testing

```bash
./gradlew test
```

## Configuration

Environment variables:
- `ADB_PATH` - Custom ADB path (default: `adb`)
- `IDEVICE_PATH` - Custom libimobiledevice path prefix (default: `idevice`)

## Status

✅ **Complete:**
- Device management interface
- Android device support (ADB)
- iOS device support (libimobiledevice)
- Device auto-discovery
- JSON-RPC 2.0 server
- REST API
- WebSocket support
- CLI tool

⏳ **Pending:**
- iOS script execution (requires app communication)
- Advanced routing strategies
- Authentication & security
- Rate limiting
- Cloud integration

## Lines of Code

- Device Management: ~800 lines
- Protocol Server: ~400 lines
- CLI: ~350 lines
- **Total: ~1,550 lines**

---

Part of the AndroidScript multi-platform automation framework.
