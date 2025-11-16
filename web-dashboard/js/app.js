/**
 * AndroidScript Control Center - Main Application
 */

// Initialize API and UI
const api = new AndroidScriptAPI();
const ui = new UIManager();

// Settings
let settings = {
    serverUrl: 'http://localhost:8080',
    autoRefresh: true,
    refreshInterval: 5
};

// Auto-refresh timer
let refreshTimer = null;

/**
 * Initialize application
 */
async function init() {
    ui.addLog('Initializing AndroidScript Control Center...', 'info');

    // Load settings from localStorage
    loadSettings();

    // Setup event listeners
    setupEventListeners();

    // Setup API event handlers
    setupAPIEventHandlers();

    // Connect to server
    await connectToServer();

    // Initial device refresh
    await refreshDevices();

    // Start auto-refresh if enabled
    if (settings.autoRefresh) {
        startAutoRefresh();
    }

    ui.addLog('Initialization complete', 'success');
}

/**
 * Connect to server
 */
async function connectToServer() {
    ui.addLog(`Connecting to server: ${settings.serverUrl}`, 'info');

    try {
        // Health check
        const healthy = await api.healthCheck();

        if (healthy) {
            ui.updateConnectionStatus(true);
            ui.addLog('Connected to server', 'success');

            // Connect WebSocket
            api.connectWebSocket();
        } else {
            ui.updateConnectionStatus(false);
            ui.addLog('Server not responding', 'error');
        }
    } catch (error) {
        ui.updateConnectionStatus(false);
        ui.addLog(`Connection failed: ${error.message}`, 'error');
    }
}

/**
 * Refresh device list
 */
async function refreshDevices() {
    try {
        const devices = await api.getDevices();
        ui.updateDeviceList(devices);
        ui.addLog(`Refreshed device list: ${devices.length} device(s) found`, 'info');
    } catch (error) {
        ui.showError(`Failed to refresh devices: ${error.message}`);
    }
}

/**
 * Execute script
 */
async function executeScript() {
    const script = document.getElementById('scriptEditor').value.trim();
    const target = document.getElementById('targetDevice').value;
    const executeBtn = document.getElementById('executeBtn');

    if (!script) {
        ui.showError('Please enter a script to execute');
        return;
    }

    if (!target) {
        ui.showError('Please select a target device');
        return;
    }

    ui.showLoading(executeBtn);
    ui.clearOutput();
    ui.addLog(`Executing script on: ${target === 'all' ? 'all devices' : target}`, 'info');

    try {
        if (target === 'all') {
            // Execute on all devices
            const results = await api.executeScriptOnAll(script);

            for (const [deviceId, result] of Object.entries(results)) {
                ui.showExecutionResult(deviceId, result);
            }

            ui.addLog(`Script executed on ${Object.keys(results).length} device(s)`, 'success');
        } else {
            // Execute on single device
            const result = await api.executeScript(target, script);
            ui.showExecutionResult(target, result);

            if (result.success) {
                ui.addLog('Script executed successfully', 'success');
            } else {
                ui.addLog('Script execution failed', 'error');
            }
        }
    } catch (error) {
        ui.showError(`Execution failed: ${error.message}`);
    } finally {
        ui.showLoading(executeBtn, false);
    }
}

/**
 * Capture screenshot
 */
async function captureScreenshot() {
    if (!ui.selectedDevice) {
        ui.showError('Please select a device first');
        return;
    }

    const captureBtn = document.getElementById('captureBtn');
    ui.showLoading(captureBtn);
    ui.addLog(`Capturing screenshot from: ${ui.selectedDevice.id}`, 'info');

    try {
        const imageUrl = await api.takeScreenshot(ui.selectedDevice.id);
        ui.showScreenshot(imageUrl);
        ui.addLog('Screenshot captured successfully', 'success');
    } catch (error) {
        ui.showScreenshotEmpty('Failed to capture screenshot');
        ui.showError(`Screenshot failed: ${error.message}`);
    } finally {
        ui.showLoading(captureBtn, false);
    }
}

/**
 * Load sample script
 */
function loadSample() {
    const sampleId = document.getElementById('sampleScripts').value;
    if (!sampleId || !SAMPLE_SCRIPTS[sampleId]) return;

    document.getElementById('scriptEditor').value = SAMPLE_SCRIPTS[sampleId];
    ui.addLog(`Loaded sample script: ${sampleId}`, 'info');
}

/**
 * Setup event listeners
 */
function setupEventListeners() {
    // Refresh devices
    document.getElementById('refreshDevices').addEventListener('click', refreshDevices);

    // Execute script
    document.getElementById('executeBtn').addEventListener('click', executeScript);

    // Sample scripts
    document.getElementById('loadSample').addEventListener('click', loadSample);
    document.getElementById('sampleScripts').addEventListener('change', loadSample);

    // Clear script
    document.getElementById('clearScript').addEventListener('click', () => {
        document.getElementById('scriptEditor').value = '';
    });

    // Clear output
    document.getElementById('clearOutput').addEventListener('click', () => {
        ui.clearOutput();
    });

    // Clear logs
    document.getElementById('clearLogs').addEventListener('click', () => {
        ui.clearLogs();
    });

    // Capture screenshot
    document.getElementById('captureBtn').addEventListener('click', captureScreenshot);

    // Tab switching
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => {
            const tabName = tab.dataset.tab;
            ui.switchTab(tabName);

            // Load data for specific tabs
            if (tabName === 'info') {
                ui.showDeviceInfo(api);
            } else if (tabName === 'screenshot') {
                if (!ui.selectedDevice) {
                    ui.showScreenshotEmpty();
                }
            }
        });
    });

    // Keyboard shortcuts
    document.addEventListener('keydown', (e) => {
        // Ctrl+Enter or Cmd+Enter to execute
        if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
            if (document.activeElement.id === 'scriptEditor') {
                e.preventDefault();
                executeScript();
            }
        }
    });
}

/**
 * Setup API event handlers
 */
function setupAPIEventHandlers() {
    // WebSocket connected
    api.on('connected', () => {
        ui.updateConnectionStatus(true);
        ui.addLog('WebSocket connected', 'success');
    });

    // WebSocket disconnected
    api.on('disconnected', () => {
        ui.updateConnectionStatus(false);
        ui.addLog('WebSocket disconnected', 'warning');
    });

    // Devices updated
    api.on('devicesUpdated', (devices) => {
        ui.updateDeviceList(devices);
        ui.addLog('Device list updated', 'info');
    });

    // Device event
    api.on('deviceEvent', (event) => {
        ui.addLog(`Device event: ${event}`, 'info');
    });
}

/**
 * Start auto-refresh
 */
function startAutoRefresh() {
    stopAutoRefresh();

    const interval = settings.refreshInterval * 1000;
    refreshTimer = setInterval(refreshDevices, interval);

    ui.addLog(`Auto-refresh enabled (${settings.refreshInterval}s)`, 'info');
}

/**
 * Stop auto-refresh
 */
function stopAutoRefresh() {
    if (refreshTimer) {
        clearInterval(refreshTimer);
        refreshTimer = null;
    }
}

/**
 * Load settings from localStorage
 */
function loadSettings() {
    const saved = localStorage.getItem('androidscript-settings');
    if (saved) {
        try {
            settings = { ...settings, ...JSON.parse(saved) };
        } catch (e) {
            console.error('Failed to parse saved settings:', e);
        }
    }

    // Apply loaded settings
    api.setBaseUrl(settings.serverUrl);
}

/**
 * Save settings to localStorage
 */
function saveSettings() {
    localStorage.setItem('androidscript-settings', JSON.stringify(settings));
}

// Initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}
