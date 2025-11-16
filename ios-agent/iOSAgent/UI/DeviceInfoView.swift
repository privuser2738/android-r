import SwiftUI

/**
 * Device information view
 * Displays platform details and capabilities
 */
struct DeviceInfoView: View {
    @State private var deviceInfo: DeviceInfo?

    private let bridge = iOSPlatformBridge()

    var body: some View {
        NavigationView {
            List {
                if let info = deviceInfo {
                    Section(header: Text("Device Information")) {
                        InfoRow(label: "Platform", value: info.platform)
                        InfoRow(label: "Model", value: info.model)
                        InfoRow(label: "Version", value: info.version)
                        InfoRow(label: "Manufacturer", value: info.manufacturer)
                    }

                    Section(header: Text("Screen")) {
                        InfoRow(label: "Width", value: "\(info.screenWidth)px")
                        InfoRow(label: "Height", value: "\(info.screenHeight)px")
                        InfoRow(label: "Scale", value: String(format: "%.1fx", UIScreen.main.scale))
                    }

                    Section(header: Text("Capabilities")) {
                        CapabilityRow(icon: "hand.tap", label: "Touch Automation", enabled: true)
                        CapabilityRow(icon: "magnifyingglass", label: "Element Finding", enabled: true)
                        CapabilityRow(icon: "camera", label: "Screenshots", enabled: true)
                        CapabilityRow(icon: "text.cursor", label: "Text Input", enabled: true)
                        CapabilityRow(icon: "arrow.up.and.down.and.arrow.left.and.right", label: "Gestures", enabled: true)
                    }

                    Section(header: Text("Status")) {
                        HStack {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(.green)
                            Text("AndroidScript Runtime Ready")
                                .fontWeight(.medium)
                        }
                    }
                } else {
                    Section {
                        HStack {
                            Spacer()
                            ProgressView()
                            Spacer()
                        }
                    }
                }
            }
            .navigationTitle("Device Info")
            .onAppear {
                loadDeviceInfo()
            }
            .refreshable {
                loadDeviceInfo()
            }
        }
    }

    private func loadDeviceInfo() {
        deviceInfo = bridge.getDeviceInfo()
    }
}

struct InfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
                .fontWeight(.medium)
        }
    }
}

struct CapabilityRow: View {
    let icon: String
    let label: String
    let enabled: Bool

    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(enabled ? .blue : .gray)
                .frame(width: 24)
            Text(label)
            Spacer()
            Image(systemName: enabled ? "checkmark.circle.fill" : "xmark.circle.fill")
                .foregroundColor(enabled ? .green : .red)
        }
    }
}

struct DeviceInfoView_Previews: PreviewProvider {
    static var previews: some View {
        DeviceInfoView()
    }
}
