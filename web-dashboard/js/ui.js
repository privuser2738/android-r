/**
 * AndroidScript UI Helpers
 * Manages UI state and interactions
 */

class UIManager {
    constructor() {
        this.selectedDevice = null;
        this.devices = [];
    }

    /**
     * Update connection status indicator
     */
    updateConnectionStatus(connected) {
        const statusDot = document.getElementById('connectionStatus');
        const statusText = document.getElementById('connectionText');

        if (connected) {
            statusDot.classList.add('connected');
            statusDot.classList.remove('disconnected');
            statusText.textContent = 'Connected';
        } else {
            statusDot.classList.remove('connected');
            statusDot.classList.add('disconnected');
            statusText.textContent = 'Disconnected';
        }
    }

    /**
     * Update device list UI
     */
    updateDeviceList(devices) {
        this.devices = devices;

        const deviceList = document.getElementById('deviceList');
        const targetSelect = document.getElementById('targetDevice');

        // Update stats
        const androidCount = devices.filter(d => d.platform === 'ANDROID').length;
        const iosCount = devices.filter(d => d.platform === 'IOS').length;

        document.getElementById('androidCount').textContent = androidCount;
        document.getElementById('iosCount').textContent = iosCount;
        document.getElementById('totalCount').textContent = devices.length;

        // Clear and rebuild list
        if (devices.length === 0) {
            deviceList.innerHTML = `
                <div class="empty-state">
                    <p>No devices connected</p>
                    <small>Connect a device and click refresh</small>
                </div>
            `;
            targetSelect.innerHTML = '<option value="">No devices available</option>';
            return;
        }

        // Build device list
        deviceList.innerHTML = devices.map(device => `
            <div class="device-item ${this.selectedDevice?.id === device.id ? 'selected' : ''}"
                 data-device-id="${device.id}">
                <div class="device-platform platform-${device.platform.toLowerCase()}">
                    ${device.platform}
                </div>
                <div class="device-model">${device.model || 'Unknown Model'}</div>
                <div class="device-id">${device.id}</div>
            </div>
        `).join('');

        // Add click handlers
        deviceList.querySelectorAll('.device-item').forEach(item => {
            item.addEventListener('click', () => {
                const deviceId = item.dataset.deviceId;
                this.selectDevice(deviceId);
            });
        });

        // Update target select
        targetSelect.innerHTML = `
            <option value="">Select device...</option>
            <option value="all">All devices (${devices.length})</option>
            ${devices.map(d => `
                <option value="${d.id}">
                    ${d.platform} - ${d.model || d.id}
                </option>
            `).join('')}
        `;
    }

    /**
     * Select a device
     */
    selectDevice(deviceId) {
        const device = this.devices.find(d => d.id === deviceId);
        if (!device) return;

        this.selectedDevice = device;

        // Update UI
        document.querySelectorAll('.device-item').forEach(item => {
            if (item.dataset.deviceId === deviceId) {
                item.classList.add('selected');
            } else {
                item.classList.remove('selected');
            }
        });

        // Update target select
        document.getElementById('targetDevice').value = deviceId;

        // Log selection
        this.addLog(`Device selected: ${device.model} (${device.id})`, 'info');
    }

    /**
     * Show device info
     */
    async showDeviceInfo(api) {
        if (!this.selectedDevice) {
            this.showDeviceInfoEmpty();
            return;
        }

        try {
            const info = await api.getDeviceInfo(this.selectedDevice.id);
            const content = document.getElementById('deviceInfoContent');

            content.innerHTML = `
                <div class="info-grid">
                    <div class="info-card">
                        <h4>Platform Information</h4>
                        <div class="info-item">
                            <span class="info-label">Platform</span>
                            <span class="info-value">${info.platform}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Model</span>
                            <span class="info-value">${info.model}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Version</span>
                            <span class="info-value">${info.version}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Manufacturer</span>
                            <span class="info-value">${info.manufacturer}</span>
                        </div>
                    </div>

                    <div class="info-card">
                        <h4>Device Details</h4>
                        <div class="info-item">
                            <span class="info-label">Device ID</span>
                            <span class="info-value">${info.id}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Serial</span>
                            <span class="info-value">${info.serial}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">Screen Size</span>
                            <span class="info-value">${info.screenWidth}×${info.screenHeight}</span>
                        </div>
                    </div>

                    <div class="info-card">
                        <h4>Capabilities</h4>
                        ${info.capabilities.map(cap => `
                            <div class="info-item">
                                <span class="info-label">✓ ${cap}</span>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
        } catch (error) {
            this.addLog(`Failed to get device info: ${error.message}`, 'error');
        }
    }

    /**
     * Show empty device info state
     */
    showDeviceInfoEmpty() {
        const content = document.getElementById('deviceInfoContent');
        content.innerHTML = `
            <div class="empty-state">
                <p>Select a device from the list</p>
            </div>
        `;
    }

    /**
     * Clear output console
     */
    clearOutput() {
        const output = document.getElementById('outputConsole');
        output.innerHTML = '<div class="output-empty">No output yet. Run a script to see results.</div>';
    }

    /**
     * Add output line
     */
    addOutput(text, type = 'info') {
        const output = document.getElementById('outputConsole');

        // Remove empty state if present
        const empty = output.querySelector('.output-empty');
        if (empty) empty.remove();

        const line = document.createElement('div');
        line.className = `output-line output-${type}`;
        line.textContent = text;

        output.appendChild(line);
        output.scrollTop = output.scrollHeight;
    }

    /**
     * Show execution result
     */
    showExecutionResult(deviceId, result) {
        const device = this.devices.find(d => d.id === deviceId);
        const deviceName = device ? `${device.model} (${device.id})` : deviceId;

        this.addOutput(`\n=== Execution on ${deviceName} ===`, 'info');
        this.addOutput(`Status: ${result.success ? '✓ Success' : '✗ Failed'}`, result.success ? 'success' : 'error');
        this.addOutput(`Time: ${result.executionTime}ms`, 'info');

        if (result.output) {
            this.addOutput('\nOutput:', 'info');
            result.output.split('\n').forEach(line => {
                if (line.trim()) this.addOutput(line, 'info');
            });
        }

        if (result.errors && result.errors.length > 0) {
            this.addOutput('\nErrors:', 'error');
            result.errors.forEach(error => {
                this.addOutput(`  • ${error}`, 'error');
            });
        }

        this.addOutput('', 'info');  // Empty line
    }

    /**
     * Add log entry
     */
    addLog(message, type = 'info') {
        const container = document.getElementById('logsContainer');
        const time = new Date().toLocaleTimeString();

        const entry = document.createElement('div');
        entry.className = `log-entry ${type}`;
        entry.innerHTML = `
            <span class="log-time">${time}</span>
            <span class="log-message">${message}</span>
        `;

        container.appendChild(entry);
        container.scrollTop = container.scrollHeight;
    }

    /**
     * Clear logs
     */
    clearLogs() {
        const container = document.getElementById('logsContainer');
        container.innerHTML = `
            <div class="log-entry info">
                <span class="log-time">${new Date().toLocaleTimeString()}</span>
                <span class="log-message">Logs cleared</span>
            </div>
        `;
    }

    /**
     * Show screenshot
     */
    showScreenshot(imageUrl) {
        const content = document.getElementById('screenshotContent');
        content.innerHTML = `
            <div class="screenshot-container">
                <img src="${imageUrl}" alt="Device Screenshot">
            </div>
        `;
    }

    /**
     * Show screenshot empty state
     */
    showScreenshotEmpty(message = 'No screenshot captured') {
        const content = document.getElementById('screenshotContent');
        content.innerHTML = `
            <div class="empty-state">
                <p>${message}</p>
                <small>Select a device and click Capture Screenshot</small>
            </div>
        `;
    }

    /**
     * Switch tabs
     */
    switchTab(tabName) {
        // Update tab buttons
        document.querySelectorAll('.tab').forEach(tab => {
            if (tab.dataset.tab === tabName) {
                tab.classList.add('active');
            } else {
                tab.classList.remove('active');
            }
        });

        // Update tab content
        document.querySelectorAll('.tab-content').forEach(content => {
            if (content.id === `tab-${tabName}`) {
                content.classList.add('active');
            } else {
                content.classList.remove('active');
            }
        });
    }

    /**
     * Show loading state
     */
    showLoading(element, show = true) {
        if (show) {
            element.disabled = true;
            element.dataset.originalText = element.textContent;
            element.textContent = '⏳ Loading...';
        } else {
            element.disabled = false;
            element.textContent = element.dataset.originalText || element.textContent;
        }
    }

    /**
     * Show error message
     */
    showError(message) {
        this.addLog(`Error: ${message}`, 'error');
        this.addOutput(`Error: ${message}`, 'error');
    }
}
