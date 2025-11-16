import SwiftUI

/**
 * Script execution view
 * Allows users to input and run AndroidScript code
 */
struct ExecutionView: View {
    @State private var scriptText: String = """
// Example AndroidScript
$device = GetDeviceInfo()
Print("Running on: " + $device.platform)
Print("Model: " + $device.model)
Print("Screen: " + $device.screenWidth + "x" + $device.screenHeight)
"""

    @State private var outputText: String = ""
    @State private var isExecuting: Bool = false
    @State private var selectedSample: String = "Custom"

    private let scriptRunner = ScriptRunner()

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Sample script picker
                Picker("Sample Scripts", selection: $selectedSample) {
                    Text("Custom").tag("Custom")
                    Text("Device Info").tag("DeviceInfo")
                    Text("Tap Test").tag("TapTest")
                    Text("Find Element").tag("FindElement")
                }
                .pickerStyle(.segmented)
                .padding()
                .onChange(of: selectedSample) { newValue in
                    loadSampleScript(newValue)
                }

                // Script input area
                VStack(alignment: .leading, spacing: 8) {
                    Text("Script")
                        .font(.headline)
                        .foregroundColor(.secondary)

                    TextEditor(text: $scriptText)
                        .font(.system(.body, design: .monospaced))
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                        .border(Color.gray.opacity(0.3))
                }
                .padding()
                .frame(maxHeight: 250)

                // Execute button
                Button(action: executeScript) {
                    HStack {
                        if isExecuting {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .scaleEffect(0.8)
                        } else {
                            Image(systemName: "play.fill")
                        }
                        Text(isExecuting ? "Executing..." : "Execute Script")
                            .fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(isExecuting ? Color.gray : Color.blue)
                    .foregroundColor(.white)
                    .cornerRadius(10)
                }
                .disabled(isExecuting)
                .padding(.horizontal)

                // Output area
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Text("Output")
                            .font(.headline)
                            .foregroundColor(.secondary)

                        Spacer()

                        Button(action: { outputText = "" }) {
                            Text("Clear")
                                .font(.caption)
                                .foregroundColor(.blue)
                        }
                    }

                    ScrollView {
                        Text(outputText.isEmpty ? "No output yet" : outputText)
                            .font(.system(.body, design: .monospaced))
                            .foregroundColor(outputText.isEmpty ? .secondary : .primary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(8)
                    }
                    .frame(maxHeight: .infinity)
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(8)
                }
                .padding()
                .frame(maxHeight: .infinity)
            }
            .navigationTitle("AndroidScript")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private func executeScript() {
        isExecuting = true
        outputText = "Executing script...\n\n"

        DispatchQueue.global(qos: .userInitiated).async {
            let result = scriptRunner.execute(source: scriptText)

            DispatchQueue.main.async {
                isExecuting = false

                if result.success {
                    outputText += "✓ Execution completed successfully\n"
                    if let output = result.output {
                        outputText += "\n\(output)"
                    }
                } else {
                    outputText += "✗ Execution failed\n\n"
                    outputText += "Errors:\n"
                    for error in result.errors {
                        outputText += "  • \(error)\n"
                    }
                }
            }
        }
    }

    private func loadSampleScript(_ sample: String) {
        switch sample {
        case "DeviceInfo":
            scriptText = """
// Get device information
$device = GetDeviceInfo()
Print("Platform: " + $device.platform)
Print("Model: " + $device.model)
Print("Version: " + $device.version)
Print("Manufacturer: " + $device.manufacturer)
Print("Screen: " + $device.screenWidth + "x" + $device.screenHeight)
"""

        case "TapTest":
            scriptText = """
// Tap at screen coordinates
$screenWidth = 375
$screenHeight = 812

// Tap at center
$x = $screenWidth / 2
$y = $screenHeight / 2
Print("Tapping at: " + $x + ", " + $y)
Tap($x, $y)

Sleep(1000)
Print("Tap completed")
"""

        case "FindElement":
            scriptText = """
// Find and interact with elements
$button = FindByText("Submit")
if ($button != null) {
    Print("Found button: " + $button.text)
    Click($button)
    Print("Button clicked!")
} else {
    Print("Button not found")
}
"""

        default:
            break
        }
    }
}

struct ExecutionView_Previews: PreviewProvider {
    static var previews: some View {
        ExecutionView()
    }
}
