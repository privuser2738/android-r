#include "adb_client.h"
#include <cstdlib>
#include <sstream>
#include <iostream>
#include <array>
#include <memory>
#include <stdexcept>

#ifdef _WIN32
#include <windows.h>
#else
#include <unistd.h>
#include <sys/wait.h>
#endif

namespace androidscript {

AdbClient::AdbClient() {
    adb_path_ = findAdbPath();
    if (adb_path_.empty()) {
        std::cerr << "Warning: ADB not found in PATH. Please install Android SDK Platform Tools." << std::endl;
    }
}

AdbClient::~AdbClient() = default;

std::string AdbClient::findAdbPath() {
    // Try to find adb in PATH
#ifdef _WIN32
    const char* adb_name = "adb.exe";
#else
    const char* adb_name = "adb";
#endif

    // First check if ADB_PATH environment variable is set
    const char* env_path = std::getenv("ADB_PATH");
    if (env_path) {
        return std::string(env_path);
    }

    // Try just "adb" - let the system find it in PATH
    return adb_name;
}

AdbResult AdbClient::executeCommand(const std::vector<std::string>& args) {
    if (adb_path_.empty()) {
        return {-1, "", "ADB not found"};
    }

    // Build command string
    std::ostringstream cmd;
    cmd << adb_path_;
    for (const auto& arg : args) {
        cmd << " " << arg;
    }

    std::string command = cmd.str();

    // Execute command and capture output
#ifdef _WIN32
    // Windows implementation
    FILE* pipe = _popen(command.c_str(), "r");
#else
    // Unix/Linux implementation
    FILE* pipe = popen(command.c_str(), "r");
#endif

    if (!pipe) {
        return {-1, "", "Failed to execute command"};
    }

    // Read output
    std::string output;
    std::array<char, 128> buffer;
    while (fgets(buffer.data(), buffer.size(), pipe) != nullptr) {
        output += buffer.data();
    }

    // Close pipe and get exit code
#ifdef _WIN32
    int exit_code = _pclose(pipe);
#else
    int exit_code = pclose(pipe);
    if (WIFEXITED(exit_code)) {
        exit_code = WEXITSTATUS(exit_code);
    }
#endif

    return {exit_code, output, ""};
}

// Device discovery

std::vector<DeviceInfo> AdbClient::getDevices() {
    auto result = executeCommand({"devices", "-l"});

    std::vector<DeviceInfo> devices;
    if (!result.success()) {
        return devices;
    }

    std::istringstream iss(result.output);
    std::string line;

    // Skip first line ("List of devices attached")
    std::getline(iss, line);

    while (std::getline(iss, line)) {
        if (line.empty()) continue;

        DeviceInfo device;
        std::istringstream line_stream(line);

        // Parse: serial state product:xxx model:yyy device:zzz transport_id:nnn
        line_stream >> device.serial >> device.state;

        std::string token;
        while (line_stream >> token) {
            size_t colon = token.find(':');
            if (colon != std::string::npos) {
                std::string key = token.substr(0, colon);
                std::string value = token.substr(colon + 1);

                if (key == "model") device.model = value;
                else if (key == "product") device.product = value;
                else if (key == "transport_id") device.transport_id = value;
            }
        }

        devices.push_back(device);
    }

    return devices;
}

DeviceInfo AdbClient::getDevice(const std::string& serial) {
    auto devices = getDevices();
    for (const auto& device : devices) {
        if (device.serial == serial) {
            return device;
        }
    }
    throw std::runtime_error("Device not found: " + serial);
}

bool AdbClient::deviceExists(const std::string& serial) {
    auto devices = getDevices();
    for (const auto& device : devices) {
        if (device.serial == serial) {
            return true;
        }
    }
    return false;
}

// Shell commands

AdbResult AdbClient::shell(const std::string& serial, const std::string& command) {
    return executeCommand({"-s", serial, "shell", command});
}

AdbResult AdbClient::shellNoOutput(const std::string& serial, const std::string& command) {
    return executeCommand({"-s", serial, "shell", command, ">/dev/null", "2>&1"});
}

// UI Automation

AdbResult AdbClient::tap(const std::string& serial, int x, int y) {
    std::ostringstream cmd;
    cmd << "input tap " << x << " " << y;
    return shell(serial, cmd.str());
}

AdbResult AdbClient::swipe(const std::string& serial, int x1, int y1, int x2, int y2, int duration_ms) {
    std::ostringstream cmd;
    cmd << "input swipe " << x1 << " " << y1 << " " << x2 << " " << y2 << " " << duration_ms;
    return shell(serial, cmd.str());
}

AdbResult AdbClient::input(const std::string& serial, const std::string& text) {
    // Escape special characters for shell
    std::string escaped = escapeShellArg(text);
    std::ostringstream cmd;
    cmd << "input text " << escaped;
    return shell(serial, cmd.str());
}

AdbResult AdbClient::keyevent(const std::string& serial, const std::string& keycode) {
    std::ostringstream cmd;
    cmd << "input keyevent " << keycode;
    return shell(serial, cmd.str());
}

// App management

AdbResult AdbClient::launchApp(const std::string& serial, const std::string& package) {
    std::ostringstream cmd;
    cmd << "monkey -p " << package << " -c android.intent.category.LAUNCHER 1";
    return shell(serial, cmd.str());
}

AdbResult AdbClient::stopApp(const std::string& serial, const std::string& package) {
    std::ostringstream cmd;
    cmd << "am force-stop " << package;
    return shell(serial, cmd.str());
}

AdbResult AdbClient::installApk(const std::string& serial, const std::string& apk_path) {
    return executeCommand({"-s", serial, "install", "-r", apk_path});
}

AdbResult AdbClient::uninstallApp(const std::string& serial, const std::string& package) {
    return executeCommand({"-s", serial, "uninstall", package});
}

AdbResult AdbClient::clearAppData(const std::string& serial, const std::string& package) {
    std::ostringstream cmd;
    cmd << "pm clear " << package;
    return shell(serial, cmd.str());
}

// File operations

AdbResult AdbClient::push(const std::string& serial, const std::string& local_path, const std::string& remote_path) {
    return executeCommand({"-s", serial, "push", local_path, remote_path});
}

AdbResult AdbClient::pull(const std::string& serial, const std::string& remote_path, const std::string& local_path) {
    return executeCommand({"-s", serial, "pull", remote_path, local_path});
}

AdbResult AdbClient::screenshot(const std::string& serial, const std::string& output_path) {
    // Take screenshot on device
    auto result1 = shell(serial, "screencap -p /sdcard/screenshot.png");
    if (!result1.success()) {
        return result1;
    }

    // Pull screenshot to local
    auto result2 = pull(serial, "/sdcard/screenshot.png", output_path);

    // Clean up remote file
    shell(serial, "rm /sdcard/screenshot.png");

    return result2;
}

// Device info

std::string AdbClient::getDeviceModel(const std::string& serial) {
    auto result = shell(serial, "getprop ro.product.model");
    if (result.success() && !result.output.empty()) {
        // Remove trailing newline
        std::string model = result.output;
        if (!model.empty() && model.back() == '\n') {
            model.pop_back();
        }
        return model;
    }
    return "Unknown";
}

std::string AdbClient::getAndroidVersion(const std::string& serial) {
    auto result = shell(serial, "getprop ro.build.version.release");
    if (result.success() && !result.output.empty()) {
        std::string version = result.output;
        if (!version.empty() && version.back() == '\n') {
            version.pop_back();
        }
        return version;
    }
    return "Unknown";
}

std::pair<int, int> AdbClient::getScreenSize(const std::string& serial) {
    auto result = shell(serial, "wm size");
    if (result.success()) {
        // Parse output like "Physical size: 1080x1920"
        std::string output = result.output;
        size_t x_pos = output.find('x');
        if (x_pos != std::string::npos) {
            // Find the last space before 'x'
            size_t space_pos = output.rfind(' ', x_pos);
            if (space_pos != std::string::npos) {
                std::string width_str = output.substr(space_pos + 1, x_pos - space_pos - 1);
                std::string height_str = output.substr(x_pos + 1);

                // Remove trailing newline from height
                size_t newline = height_str.find('\n');
                if (newline != std::string::npos) {
                    height_str = height_str.substr(0, newline);
                }

                try {
                    int width = std::stoi(width_str);
                    int height = std::stoi(height_str);
                    return {width, height};
                } catch (...) {
                    // Fall through to default
                }
            }
        }
    }
    return {1080, 1920}; // Default
}

// ADB server

bool AdbClient::startServer() {
    auto result = executeCommand({"start-server"});
    return result.success();
}

bool AdbClient::killServer() {
    auto result = executeCommand({"kill-server"});
    return result.success();
}

// Helper methods

std::string AdbClient::escapeShellArg(const std::string& arg) {
    // Replace spaces with escaped spaces for shell
    std::string escaped;
    for (char c : arg) {
        if (c == ' ') {
            escaped += "%s";
        } else if (c == '\'' || c == '"' || c == '\\') {
            escaped += '\\';
            escaped += c;
        } else {
            escaped += c;
        }
    }
    return escaped;
}

} // namespace androidscript
