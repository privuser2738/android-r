import SwiftUI

/**
 * Main view for iOSAgent
 * Material Design-inspired interface matching Android app
 */
struct ContentView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            ExecutionView()
                .tabItem {
                    Label("Execute", systemImage: "play.circle.fill")
                }
                .tag(0)

            DeviceInfoView()
                .tabItem {
                    Label("Device", systemImage: "iphone")
                }
                .tag(1)

            SettingsView()
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
                .tag(2)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
