/**
 * AndroidScript API Client
 * Handles communication with the host-controller REST API and WebSocket
 */

class AndroidScriptAPI {
    constructor(baseUrl = 'http://localhost:8080') {
        this.baseUrl = baseUrl;
        this.ws = null;
        this.wsConnected = false;
        this.eventListeners = {};
    }

    /**
     * Set API base URL
     */
    setBaseUrl(url) {
        this.baseUrl = url;
        this.reconnectWebSocket();
    }

    /**
     * Get all connected devices
     */
    async getDevices() {
        try {
            const response = await fetch(`${this.baseUrl}/devices`);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('Failed to get devices:', error);
            throw error;
        }
    }

    /**
     * Get device information
     */
    async getDeviceInfo(deviceId) {
        try {
            const response = await fetch(`${this.baseUrl}/devices/${deviceId}`);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('Failed to get device info:', error);
            throw error;
        }
    }

    /**
     * Execute script on device
     */
    async executeScript(deviceId, script) {
        try {
            const response = await fetch(`${this.baseUrl}/devices/${deviceId}/execute`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ script })
            });

            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            return await response.json();
        } catch (error) {
            console.error('Failed to execute script:', error);
            throw error;
        }
    }

    /**
     * Execute script on all devices using JSON-RPC
     */
    async executeScriptOnAll(script) {
        try {
            const response = await fetch(`${this.baseUrl}/rpc`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    jsonrpc: '2.0',
                    method: 'script.executeAll',
                    params: { script },
                    id: Date.now()
                })
            });

            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const data = await response.json();

            if (data.error) {
                throw new Error(data.error.message);
            }

            return data.result;
        } catch (error) {
            console.error('Failed to execute script on all devices:', error);
            throw error;
        }
    }

    /**
     * Take screenshot from device
     */
    async takeScreenshot(deviceId) {
        try {
            const response = await fetch(`${this.baseUrl}/devices/${deviceId}/screenshot`);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);

            const blob = await response.blob();
            return URL.createObjectURL(blob);
        } catch (error) {
            console.error('Failed to take screenshot:', error);
            throw error;
        }
    }

    /**
     * Health check
     */
    async healthCheck() {
        try {
            const response = await fetch(`${this.baseUrl}/health`);
            return response.ok;
        } catch (error) {
            return false;
        }
    }

    /**
     * Connect to WebSocket for real-time updates
     */
    connectWebSocket() {
        const wsUrl = this.baseUrl.replace('http://', 'ws://').replace('https://', 'wss://');

        try {
            this.ws = new WebSocket(`${wsUrl}/ws`);

            this.ws.onopen = () => {
                console.log('WebSocket connected');
                this.wsConnected = true;
                this.emit('connected');
            };

            this.ws.onclose = () => {
                console.log('WebSocket disconnected');
                this.wsConnected = false;
                this.emit('disconnected');

                // Attempt reconnect after 5 seconds
                setTimeout(() => this.connectWebSocket(), 5000);
            };

            this.ws.onerror = (error) => {
                console.error('WebSocket error:', error);
                this.emit('error', error);
            };

            this.ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    this.handleWebSocketMessage(data);
                } catch (error) {
                    console.error('Failed to parse WebSocket message:', error);
                }
            };
        } catch (error) {
            console.error('Failed to connect WebSocket:', error);
        }
    }

    /**
     * Handle incoming WebSocket message
     */
    handleWebSocketMessage(data) {
        if (data.type === 'devices') {
            this.emit('devicesUpdated', data.data);
        } else if (data.type === 'event') {
            this.emit('deviceEvent', data.data);
        } else if (data.result !== undefined) {
            // JSON-RPC response
            this.emit('rpcResponse', data);
        }
    }

    /**
     * Disconnect WebSocket
     */
    disconnectWebSocket() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }

    /**
     * Reconnect WebSocket
     */
    reconnectWebSocket() {
        this.disconnectWebSocket();
        this.connectWebSocket();
    }

    /**
     * Send JSON-RPC request via WebSocket
     */
    sendRPC(method, params) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            throw new Error('WebSocket not connected');
        }

        const request = {
            jsonrpc: '2.0',
            method,
            params,
            id: Date.now()
        };

        this.ws.send(JSON.stringify(request));
    }

    /**
     * Event emitter - register listener
     */
    on(event, callback) {
        if (!this.eventListeners[event]) {
            this.eventListeners[event] = [];
        }
        this.eventListeners[event].push(callback);
    }

    /**
     * Event emitter - remove listener
     */
    off(event, callback) {
        if (this.eventListeners[event]) {
            this.eventListeners[event] = this.eventListeners[event].filter(
                cb => cb !== callback
            );
        }
    }

    /**
     * Event emitter - emit event
     */
    emit(event, data) {
        if (this.eventListeners[event]) {
            this.eventListeners[event].forEach(callback => {
                try {
                    callback(data);
                } catch (error) {
                    console.error(`Error in event listener for ${event}:`, error);
                }
            });
        }
    }
}

// Sample scripts for quick testing
const SAMPLE_SCRIPTS = {
    deviceInfo: `// Get device information
$device = GetDeviceInfo()
Print("Platform: " + $device.platform)
Print("Model: " + $device.model)
Print("Version: " + $device.version)
Print("Manufacturer: " + $device.manufacturer)
Print("Screen: " + $device.screenWidth + "x" + $device.screenHeight)`,

    tapTest: `// Tap test at screen center
$device = GetDeviceInfo()
$x = $device.screenWidth / 2
$y = $device.screenHeight / 2

Print("Tapping at: " + $x + ", " + $y)
Tap($x, $y)
Sleep(500)
Print("Tap completed!")`,

    findElement: `// Find and click element
Print("Searching for element...")

$button = FindByText("Submit")
if ($button != null) {
    Print("Found: " + $button.text)
    Click($button)
    Print("Element clicked!")
} else {
    Print("Element not found")
}`,

    automation: `// UI Automation example
Print("Starting automation...")

// Wait for UI to load
Sleep(1000)

// Find and fill username
$username = FindById("usernameField")
if ($username != null) {
    Click($username)
    InputText("testuser@example.com")
    Print("Username entered")
}

// Find and fill password
$password = FindById("passwordField")
if ($password != null) {
    Click($password)
    InputText("password123")
    Print("Password entered")
}

// Submit form
$login = FindByText("Login")
if ($login != null) {
    Click($login)
    Print("Form submitted")
}

Print("Automation complete!")`
};
