import SwiftUI

/**
 * Settings view
 * Configuration options for iOSAgent
 */
struct SettingsView: View {
    @AppStorage("enableLogging") private var enableLogging = true
    @AppStorage("autoRetry") private var autoRetry = true
    @AppStorage("retryDelay") private var retryDelay = 1.0
    @AppStorage("executionTimeout") private var executionTimeout = 30.0

    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Execution")) {
                    Toggle("Enable Logging", isOn: $enableLogging)
                    Toggle("Auto Retry on Failure", isOn: $autoRetry)

                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text("Retry Delay")
                            Spacer()
                            Text("\(Int(retryDelay))s")
                                .foregroundColor(.secondary)
                        }
                        Slider(value: $retryDelay, in: 1...10, step: 1)
                    }
                    .disabled(!autoRetry)

                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text("Execution Timeout")
                            Spacer()
                            Text("\(Int(executionTimeout))s")
                                .foregroundColor(.secondary)
                        }
                        Slider(value: $executionTimeout, in: 10...120, step: 10)
                    }
                }

                Section(header: Text("Automation")) {
                    NavigationLink(destination: Text("Gesture Settings")) {
                        SettingRow(icon: "hand.tap", label: "Gesture Settings", value: "Default")
                    }
                    NavigationLink(destination: Text("Element Finding")) {
                        SettingRow(icon: "magnifyingglass", label: "Element Finding", value: "XCTest")
                    }
                    NavigationLink(destination: Text("Screenshot Settings")) {
                        SettingRow(icon: "camera", label: "Screenshot Settings", value: "PNG")
                    }
                }

                Section(header: Text("About")) {
                    InfoRow(label: "Version", value: "1.0.0")
                    InfoRow(label: "Build", value: "1")
                    InfoRow(label: "Framework", value: "AndroidScript")

                    HStack {
                        Text("Runtime")
                        Spacer()
                        Text("iOS Native")
                            .foregroundColor(.secondary)
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(.green)
                    }
                }

                Section(header: Text("Documentation")) {
                    Link(destination: URL(string: "https://github.com")!) {
                        HStack {
                            Image(systemName: "book")
                            Text("User Guide")
                            Spacer()
                            Image(systemName: "arrow.up.right")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }

                    Link(destination: URL(string: "https://github.com")!) {
                        HStack {
                            Image(systemName: "doc.text")
                            Text("API Reference")
                            Spacer()
                            Image(systemName: "arrow.up.right")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
            .navigationTitle("Settings")
        }
    }
}

struct SettingRow: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        HStack {
            Image(systemName: icon)
                .frame(width: 24)
                .foregroundColor(.blue)
            Text(label)
            Spacer()
            Text(value)
                .foregroundColor(.secondary)
        }
    }
}

struct SettingsView_Previews: PreviewProvider {
    static var previews: some View {
        SettingsView()
    }
}
