#include "builtins.h"
#include "interpreter.h"
#include "adb_client.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <thread>
#include <chrono>
#include <cstdio>

#ifdef _WIN32
#include <windows.h>
#else
#include <unistd.h>
#endif

namespace androidscript {

// Global ADB client and current device context
static AdbClient g_adb_client;
static std::string g_current_device_serial;

void registerBuiltins(Interpreter& interpreter) {
    auto env = interpreter.getGlobalEnvironment();

    // Utility functions
    env->define("Print", Value::makeNativeFunction(builtin_Print));
    env->define("Log", Value::makeNativeFunction(builtin_Log));
    env->define("LogError", Value::makeNativeFunction(builtin_LogError));
    env->define("Sleep", Value::makeNativeFunction(builtin_Sleep));
    env->define("Assert", Value::makeNativeFunction(builtin_Assert));

    // String functions
    env->define("Length", Value::makeNativeFunction(builtin_Length));
    env->define("Substring", Value::makeNativeFunction(builtin_Substring));
    env->define("ToUpper", Value::makeNativeFunction(builtin_ToUpper));
    env->define("ToLower", Value::makeNativeFunction(builtin_ToLower));
    env->define("Contains", Value::makeNativeFunction(builtin_Contains));
    env->define("Replace", Value::makeNativeFunction(builtin_Replace));

    // Array functions
    env->define("Count", Value::makeNativeFunction(builtin_Count));
    env->define("Push", Value::makeNativeFunction(builtin_Push));
    env->define("Pop", Value::makeNativeFunction(builtin_Pop));
    env->define("Join", Value::makeNativeFunction(builtin_Join));

    // Type conversion
    env->define("ToString", Value::makeNativeFunction(builtin_ToString));
    env->define("ToInt", Value::makeNativeFunction(builtin_ToInt));
    env->define("ToFloat", Value::makeNativeFunction(builtin_ToFloat));

    // Device management
    env->define("Device", Value::makeNativeFunction(builtin_Device));
    env->define("GetAllDevices", Value::makeNativeFunction(builtin_GetAllDevices));

    // File operations
    env->define("FileExists", Value::makeNativeFunction(builtin_FileExists));
    env->define("ReadFile", Value::makeNativeFunction(builtin_ReadFile));
    env->define("WriteFile", Value::makeNativeFunction(builtin_WriteFile));

    // UI Automation
    env->define("Tap", Value::makeNativeFunction(builtin_Tap));
    env->define("Swipe", Value::makeNativeFunction(builtin_Swipe));
    env->define("Input", Value::makeNativeFunction(builtin_Input));
    env->define("KeyEvent", Value::makeNativeFunction(builtin_KeyEvent));
    env->define("Screenshot", Value::makeNativeFunction(builtin_Screenshot));

    // App Management
    env->define("LaunchApp", Value::makeNativeFunction(builtin_LaunchApp));
    env->define("StopApp", Value::makeNativeFunction(builtin_StopApp));
    env->define("InstallApp", Value::makeNativeFunction(builtin_InstallApp));
    env->define("UninstallApp", Value::makeNativeFunction(builtin_UninstallApp));
    env->define("ClearAppData", Value::makeNativeFunction(builtin_ClearAppData));

    // Device File Operations
    env->define("PushFile", Value::makeNativeFunction(builtin_PushFile));
    env->define("PullFile", Value::makeNativeFunction(builtin_PullFile));
}

// Utility functions

Value builtin_Print(const std::vector<Value>& args) {
    for (size_t i = 0; i < args.size(); ++i) {
        if (i > 0) std::cout << " ";
        std::cout << args[i].toString();
    }
    std::cout << std::endl;
    return Value::makeNil();
}

Value builtin_Log(const std::vector<Value>& args) {
    std::cout << "[LOG] ";
    for (size_t i = 0; i < args.size(); ++i) {
        if (i > 0) std::cout << " ";
        std::cout << args[i].toString();
    }
    std::cout << std::endl;
    return Value::makeNil();
}

Value builtin_LogError(const std::vector<Value>& args) {
    std::cerr << "[ERROR] ";
    for (size_t i = 0; i < args.size(); ++i) {
        if (i > 0) std::cerr << " ";
        std::cerr << args[i].toString();
    }
    std::cerr << std::endl;
    return Value::makeNil();
}

Value builtin_Sleep(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("Sleep() requires 1 argument (milliseconds)");
    }

    int64_t ms = args[0].asInt();
    if (ms < 0) {
        throw std::runtime_error("Sleep() duration cannot be negative");
    }

    std::this_thread::sleep_for(std::chrono::milliseconds(ms));
    return Value::makeNil();
}

Value builtin_Assert(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("Assert() requires at least 1 argument");
    }

    if (!args[0].isTruthy()) {
        std::string message = "Assertion failed";
        if (args.size() > 1) {
            message += ": " + args[1].toString();
        }
        throw std::runtime_error(message);
    }

    return Value::makeNil();
}

// String functions

Value builtin_Length(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("Length() requires 1 argument");
    }

    return Value(static_cast<int64_t>(args[0].length()));
}

Value builtin_Substring(const std::vector<Value>& args) {
    if (args.size() < 3) {
        throw std::runtime_error("Substring() requires 3 arguments (string, start, end)");
    }

    std::string str = args[0].asString();
    size_t start = static_cast<size_t>(args[1].asInt());
    size_t end = static_cast<size_t>(args[2].asInt());

    if (start > str.length() || end > str.length() || start > end) {
        throw std::runtime_error("Invalid substring indices");
    }

    return Value(str.substr(start, end - start));
}

Value builtin_ToUpper(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("ToUpper() requires 1 argument");
    }

    std::string str = args[0].asString();
    std::transform(str.begin(), str.end(), str.begin(), ::toupper);
    return Value(str);
}

Value builtin_ToLower(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("ToLower() requires 1 argument");
    }

    std::string str = args[0].asString();
    std::transform(str.begin(), str.end(), str.begin(), ::tolower);
    return Value(str);
}

Value builtin_Contains(const std::vector<Value>& args) {
    if (args.size() < 2) {
        throw std::runtime_error("Contains() requires 2 arguments (string, substring)");
    }

    std::string str = args[0].asString();
    std::string substr = args[1].asString();

    return Value(str.find(substr) != std::string::npos);
}

Value builtin_Replace(const std::vector<Value>& args) {
    if (args.size() < 3) {
        throw std::runtime_error("Replace() requires 3 arguments (string, old, new)");
    }

    std::string str = args[0].asString();
    std::string old_str = args[1].asString();
    std::string new_str = args[2].asString();

    size_t pos = 0;
    while ((pos = str.find(old_str, pos)) != std::string::npos) {
        str.replace(pos, old_str.length(), new_str);
        pos += new_str.length();
    }

    return Value(str);
}

// Array functions

Value builtin_Count(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("Count() requires 1 argument");
    }

    return Value(static_cast<int64_t>(args[0].length()));
}

Value builtin_Push(const std::vector<Value>& args) {
    if (args.size() < 2) {
        throw std::runtime_error("Push() requires 2 arguments (array, value)");
    }

    // Note: This modifies the original array
    Value arr = args[0];
    arr.push(args[1]);
    return arr;
}

Value builtin_Pop(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("Pop() requires 1 argument");
    }

    // Note: This modifies the original array
    Value arr = args[0];
    return arr.pop();
}

Value builtin_Join(const std::vector<Value>& args) {
    if (args.size() < 2) {
        throw std::runtime_error("Join() requires 2 arguments (array, separator)");
    }

    const ValueArray& arr = args[0].asArray();
    std::string sep = args[1].asString();
    std::ostringstream oss;

    for (size_t i = 0; i < arr.size(); ++i) {
        if (i > 0) oss << sep;
        oss << arr[i].toString();
    }

    return Value(oss.str());
}

// Type conversion

Value builtin_ToString(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("ToString() requires 1 argument");
    }

    return Value(args[0].toString());
}

Value builtin_ToInt(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("ToInt() requires 1 argument");
    }

    if (args[0].isInt()) {
        return args[0];
    } else if (args[0].isFloat()) {
        return Value(static_cast<int64_t>(args[0].asFloat()));
    } else if (args[0].isString()) {
        try {
            return Value(static_cast<int64_t>(std::stoll(args[0].asString())));
        } catch (...) {
            throw std::runtime_error("Cannot convert string to integer");
        }
    }

    throw std::runtime_error("Cannot convert to integer");
}

Value builtin_ToFloat(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("ToFloat() requires 1 argument");
    }

    if (args[0].isFloat()) {
        return args[0];
    } else if (args[0].isInt()) {
        return Value(static_cast<double>(args[0].asInt()));
    } else if (args[0].isString()) {
        try {
            return Value(std::stod(args[0].asString()));
        } catch (...) {
            throw std::runtime_error("Cannot convert string to float");
        }
    }

    throw std::runtime_error("Cannot convert to float");
}

// Device management

Value builtin_Device(const std::vector<Value>& args) {
    DeviceRef dev;

    if (!args.empty()) {
        // Use specified device serial
        dev.serial = args[0].asString();

        // Verify device exists
        if (!g_adb_client.deviceExists(dev.serial)) {
            throw std::runtime_error("Device not found: " + dev.serial);
        }
    } else {
        // Auto-detect first available device
        auto devices = g_adb_client.getDevices();
        if (devices.empty()) {
            throw std::runtime_error("No Android devices found. Make sure USB debugging is enabled.");
        }

        // Use first online device
        bool found = false;
        for (const auto& d : devices) {
            if (d.isOnline()) {
                dev.serial = d.serial;
                found = true;
                break;
            }
        }

        if (!found) {
            throw std::runtime_error("No online devices found. Device state: " + devices[0].state);
        }
    }

    // Set this as the current device for automation commands
    g_current_device_serial = dev.serial;

    // Get device info from ADB
    dev.model = g_adb_client.getDeviceModel(dev.serial);
    dev.android_version = g_adb_client.getAndroidVersion(dev.serial);

    auto screen_size = g_adb_client.getScreenSize(dev.serial);
    dev.screen_width = screen_size.first;
    dev.screen_height = screen_size.second;

    std::cout << "[DEVICE] Connected to " << dev.model
              << " (Android " << dev.android_version << ")"
              << " [" << dev.screen_width << "x" << dev.screen_height << "]" << std::endl;

    return Value::makeDevice(dev);
}

Value builtin_GetAllDevices(const std::vector<Value>& args) {
    auto adb_devices = g_adb_client.getDevices();
    ValueArray devices;

    for (const auto& d : adb_devices) {
        DeviceRef dev;
        dev.serial = d.serial;
        dev.model = d.model.empty() ? g_adb_client.getDeviceModel(d.serial) : d.model;
        dev.android_version = g_adb_client.getAndroidVersion(d.serial);

        auto screen_size = g_adb_client.getScreenSize(d.serial);
        dev.screen_width = screen_size.first;
        dev.screen_height = screen_size.second;

        devices.push_back(Value::makeDevice(dev));
    }

    return Value::makeArray(devices);
}

// File operations

Value builtin_FileExists(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("FileExists() requires 1 argument");
    }

    std::string path = args[0].asString();
    std::ifstream file(path);
    return Value(file.good());
}

Value builtin_ReadFile(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("ReadFile() requires 1 argument");
    }

    std::string path = args[0].asString();
    std::ifstream file(path);

    if (!file) {
        throw std::runtime_error("Cannot open file: " + path);
    }

    std::ostringstream oss;
    oss << file.rdbuf();
    return Value(oss.str());
}

Value builtin_WriteFile(const std::vector<Value>& args) {
    if (args.size() < 2) {
        throw std::runtime_error("WriteFile() requires 2 arguments (path, content)");
    }

    std::string path = args[0].asString();
    std::string content = args[1].asString();

    std::ofstream file(path);
    if (!file) {
        throw std::runtime_error("Cannot write to file: " + path);
    }

    file << content;
    return Value::makeNil();
}

// UI Automation - Using real ADB commands

Value builtin_Tap(const std::vector<Value>& args) {
    if (args.size() < 2) {
        throw std::runtime_error("Tap() requires 2 arguments (x, y)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    int x = static_cast<int>(args[0].asInt());
    int y = static_cast<int>(args[1].asInt());

    std::cout << "[AUTOMATION] Tap(" << x << ", " << y << ") on " << g_current_device_serial << std::endl;

    auto result = g_adb_client.tap(g_current_device_serial, x, y);
    if (!result.success()) {
        throw std::runtime_error("Tap failed: " + result.error);
    }

    return Value::makeNil();
}

Value builtin_Swipe(const std::vector<Value>& args) {
    if (args.size() < 5) {
        throw std::runtime_error("Swipe() requires 5 arguments (x1, y1, x2, y2, duration)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    int x1 = static_cast<int>(args[0].asInt());
    int y1 = static_cast<int>(args[1].asInt());
    int x2 = static_cast<int>(args[2].asInt());
    int y2 = static_cast<int>(args[3].asInt());
    int duration = static_cast<int>(args[4].asInt());

    std::cout << "[AUTOMATION] Swipe(" << x1 << ", " << y1 << " -> "
              << x2 << ", " << y2 << ", " << duration << "ms)" << std::endl;

    auto result = g_adb_client.swipe(g_current_device_serial, x1, y1, x2, y2, duration);
    if (!result.success()) {
        throw std::runtime_error("Swipe failed: " + result.error);
    }

    return Value::makeNil();
}

Value builtin_Input(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("Input() requires 1 argument");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    std::string text = args[0].asString();
    std::cout << "[AUTOMATION] Input(\"" << text << "\")" << std::endl;

    auto result = g_adb_client.input(g_current_device_serial, text);
    if (!result.success()) {
        throw std::runtime_error("Input failed: " + result.error);
    }

    return Value::makeNil();
}

Value builtin_Screenshot(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("Screenshot() requires 1 argument (path)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    std::string path = args[0].asString();
    std::cout << "[AUTOMATION] Screenshot(\"" << path << "\")" << std::endl;

    auto result = g_adb_client.screenshot(g_current_device_serial, path);
    if (!result.success()) {
        throw std::runtime_error("Screenshot failed: " + result.error);
    }

    std::cout << "[AUTOMATION] Screenshot saved to: " << path << std::endl;
    return Value::makeNil();
}

Value builtin_KeyEvent(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("KeyEvent() requires 1 argument (keycode)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    std::string keycode = args[0].asString();
    std::cout << "[AUTOMATION] KeyEvent(\"" << keycode << "\")" << std::endl;

    auto result = g_adb_client.keyevent(g_current_device_serial, keycode);
    if (!result.success()) {
        throw std::runtime_error("KeyEvent failed: " + result.error);
    }

    return Value::makeNil();
}

// App Management

Value builtin_LaunchApp(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("LaunchApp() requires 1 argument (package)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    std::string package = args[0].asString();
    std::cout << "[APP] LaunchApp(\"" << package << "\")" << std::endl;

    auto result = g_adb_client.launchApp(g_current_device_serial, package);
    if (!result.success()) {
        throw std::runtime_error("LaunchApp failed: " + result.error);
    }

    return Value::makeNil();
}

Value builtin_StopApp(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("StopApp() requires 1 argument (package)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    std::string package = args[0].asString();
    std::cout << "[APP] StopApp(\"" << package << "\")" << std::endl;

    auto result = g_adb_client.stopApp(g_current_device_serial, package);
    if (!result.success()) {
        throw std::runtime_error("StopApp failed: " + result.error);
    }

    return Value::makeNil();
}

Value builtin_InstallApp(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("InstallApp() requires 1 argument (apk_path)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    std::string apk_path = args[0].asString();
    std::cout << "[APP] InstallApp(\"" << apk_path << "\")" << std::endl;

    auto result = g_adb_client.installApk(g_current_device_serial, apk_path);
    if (!result.success()) {
        throw std::runtime_error("InstallApp failed: " + result.error);
    }

    std::cout << "[APP] App installed successfully" << std::endl;
    return Value::makeNil();
}

Value builtin_UninstallApp(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("UninstallApp() requires 1 argument (package)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    std::string package = args[0].asString();
    std::cout << "[APP] UninstallApp(\"" << package << "\")" << std::endl;

    auto result = g_adb_client.uninstallApp(g_current_device_serial, package);
    if (!result.success()) {
        throw std::runtime_error("UninstallApp failed: " + result.error);
    }

    std::cout << "[APP] App uninstalled successfully" << std::endl;
    return Value::makeNil();
}

Value builtin_ClearAppData(const std::vector<Value>& args) {
    if (args.empty()) {
        throw std::runtime_error("ClearAppData() requires 1 argument (package)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    std::string package = args[0].asString();
    std::cout << "[APP] ClearAppData(\"" << package << "\")" << std::endl;

    auto result = g_adb_client.clearAppData(g_current_device_serial, package);
    if (!result.success()) {
        throw std::runtime_error("ClearAppData failed: " + result.error);
    }

    std::cout << "[APP] App data cleared successfully" << std::endl;
    return Value::makeNil();
}

// Device File Operations

Value builtin_PushFile(const std::vector<Value>& args) {
    if (args.size() < 2) {
        throw std::runtime_error("PushFile() requires 2 arguments (local_path, remote_path)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    std::string local_path = args[0].asString();
    std::string remote_path = args[1].asString();

    std::cout << "[FILE] PushFile(\"" << local_path << "\" -> \"" << remote_path << "\")" << std::endl;

    auto result = g_adb_client.push(g_current_device_serial, local_path, remote_path);
    if (!result.success()) {
        throw std::runtime_error("PushFile failed: " + result.error);
    }

    std::cout << "[FILE] File pushed successfully" << std::endl;
    return Value::makeNil();
}

Value builtin_PullFile(const std::vector<Value>& args) {
    if (args.size() < 2) {
        throw std::runtime_error("PullFile() requires 2 arguments (remote_path, local_path)");
    }

    if (g_current_device_serial.empty()) {
        throw std::runtime_error("No device selected. Call Device() first.");
    }

    std::string remote_path = args[0].asString();
    std::string local_path = args[1].asString();

    std::cout << "[FILE] PullFile(\"" << remote_path << "\" -> \"" << local_path << "\")" << std::endl;

    auto result = g_adb_client.pull(g_current_device_serial, remote_path, local_path);
    if (!result.success()) {
        throw std::runtime_error("PullFile failed: " + result.error);
    }

    std::cout << "[FILE] File pulled successfully" << std::endl;
    return Value::makeNil();
}

} // namespace androidscript
