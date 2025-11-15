#ifndef ANDROIDSCRIPT_ADB_CLIENT_H
#define ANDROIDSCRIPT_ADB_CLIENT_H

#include <string>
#include <vector>
#include <memory>

namespace androidscript {

// Device information
struct DeviceInfo {
    std::string serial;
    std::string state;  // "device", "offline", "unauthorized", etc.
    std::string model;
    std::string product;
    std::string transport_id;

    bool isOnline() const { return state == "device"; }
};

// ADB command result
struct AdbResult {
    int exit_code;
    std::string output;
    std::string error;

    bool success() const { return exit_code == 0; }
};

// ADB Client for device communication
class AdbClient {
public:
    AdbClient();
    ~AdbClient();

    // Device discovery
    std::vector<DeviceInfo> getDevices();
    DeviceInfo getDevice(const std::string& serial);
    bool deviceExists(const std::string& serial);

    // Shell commands
    AdbResult shell(const std::string& serial, const std::string& command);
    AdbResult shellNoOutput(const std::string& serial, const std::string& command);

    // UI Automation
    AdbResult tap(const std::string& serial, int x, int y);
    AdbResult swipe(const std::string& serial, int x1, int y1, int x2, int y2, int duration_ms);
    AdbResult input(const std::string& serial, const std::string& text);
    AdbResult keyevent(const std::string& serial, const std::string& keycode);

    // App management
    AdbResult launchApp(const std::string& serial, const std::string& package);
    AdbResult stopApp(const std::string& serial, const std::string& package);
    AdbResult installApk(const std::string& serial, const std::string& apk_path);
    AdbResult uninstallApp(const std::string& serial, const std::string& package);
    AdbResult clearAppData(const std::string& serial, const std::string& package);

    // File operations
    AdbResult push(const std::string& serial, const std::string& local_path, const std::string& remote_path);
    AdbResult pull(const std::string& serial, const std::string& remote_path, const std::string& local_path);
    AdbResult screenshot(const std::string& serial, const std::string& output_path);

    // Device info
    std::string getDeviceModel(const std::string& serial);
    std::string getAndroidVersion(const std::string& serial);
    std::pair<int, int> getScreenSize(const std::string& serial);

    // ADB server
    bool startServer();
    bool killServer();
    std::string getAdbPath() const { return adb_path_; }
    void setAdbPath(const std::string& path) { adb_path_ = path; }

private:
    std::string adb_path_;

    // Helper methods
    AdbResult executeCommand(const std::vector<std::string>& args);
    std::string findAdbPath();
    std::string escapeShellArg(const std::string& arg);
    std::vector<std::string> parseDeviceList(const std::string& output);
};

} // namespace androidscript

#endif // ANDROIDSCRIPT_ADB_CLIENT_H
