import Foundation
import UIKit
import XCTest

/**
 * iOS Platform Bridge
 * Implements cross-platform automation interface for iOS
 */
class iOSPlatformBridge {

    // XCTest application instance
    private var app: XCUIApplication

    // Element cache for performance
    private var elementCache = [String: XCUIElement]()
    private var nextElementId = 1

    init() {
        self.app = XCUIApplication()
    }

    // MARK: - Device Information

    func getDeviceInfo() -> DeviceInfo {
        let device = UIDevice.current
        let screen = UIScreen.main

        return DeviceInfo(
            platform: "iOS",
            model: device.model,
            version: device.systemVersion,
            serial: getDeviceSerial(),
            manufacturer: "Apple",
            screenWidth: Int(screen.bounds.width * screen.scale),
            screenHeight: Int(screen.bounds.height * screen.scale),
            dpi: Int(screen.scale * 160), // Convert to DPI
            language: Locale.current.languageCode ?? "en",
            region: Locale.current.regionCode ?? "US"
        )
    }

    private func getDeviceSerial() -> String {
        // iOS doesn't expose serial number directly
        // Use UUID as unique identifier
        return UIDevice.current.identifierForVendor?.uuidString ?? "unknown"
    }

    // MARK: - Core Actions

    func tap(x: Int, y: Int) -> Bool {
        let coordinate = app.coordinate(withNormalizedOffset: CGVector(dx: 0, dy: 0))
            .withOffset(CGVector(dx: x, dy: y))
        coordinate.tap()
        return true
    }

    func longPress(x: Int, y: Int, duration: TimeInterval = 1.0) -> Bool {
        let coordinate = app.coordinate(withNormalizedOffset: CGVector(dx: 0, dy: 0))
            .withOffset(CGVector(dx: x, dy: y))
        coordinate.press(forDuration: duration)
        return true
    }

    func swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: TimeInterval = 0.3) -> Bool {
        let start = app.coordinate(withNormalizedOffset: CGVector(dx: 0, dy: 0))
            .withOffset(CGVector(dx: x1, dy: y1))
        let end = app.coordinate(withNormalizedOffset: CGVector(dx: 0, dy: 0))
            .withOffset(CGVector(dx: x2, dy: y2))

        start.press(forDuration: 0.1, thenDragTo: end)
        return true
    }

    func pinch(centerX: Int, centerY: Int, scale: Float) -> Bool {
        // XCTest doesn't directly support pinch at coordinates
        // Use element-based pinch instead
        let element = app.coordinate(withNormalizedOffset: CGVector(dx: 0, dy: 0))
            .withOffset(CGVector(dx: centerX, dy: centerY))

        // Simulate pinch using two-finger gesture
        if scale > 1.0 {
            // Zoom in
            app.pinch(withScale: CGFloat(scale), velocity: 1.0)
        } else {
            // Zoom out
            app.pinch(withScale: CGFloat(scale), velocity: -1.0)
        }
        return true
    }

    // MARK: - Element Finding

    func findByText(_ text: String, exact: Bool = false) -> UIElement? {
        if exact {
            // Exact match
            let element = app.staticTexts[text]
            if element.exists {
                return wrapElement(element, text: text)
            }

            // Also check buttons
            let button = app.buttons[text]
            if button.exists {
                return wrapElement(button, text: text)
            }
        } else {
            // Contains match
            let predicate = NSPredicate(format: "label CONTAINS[c] %@", text)
            let elements = app.descendants(matching: .any).containing(predicate)

            if let first = elements.allElementsBoundByIndex.first {
                return wrapElement(first, text: text)
            }
        }

        return nil
    }

    func findById(_ id: String) -> UIElement? {
        let element = app.descendants(matching: .any).matching(identifier: id).firstMatch
        if element.exists {
            return wrapElement(element, id: id)
        }
        return nil
    }

    func findByType(_ type: String) -> [UIElement] {
        let elementType = mapElementType(type)
        let elements = app.descendants(matching: elementType)

        var results = [UIElement]()
        for i in 0..<min(elements.count, 100) { // Limit to 100 elements
            let element = elements.element(boundBy: i)
            if element.exists {
                results.append(wrapElement(element, type: type))
            }
        }

        return results
    }

    func findByContentDescription(_ description: String) -> UIElement? {
        // iOS uses accessibilityLabel instead of contentDescription
        let element = app.descendants(matching: .any)
            .containing(NSPredicate(format: "label == %@", description))
            .firstMatch

        if element.exists {
            return wrapElement(element, contentDesc: description)
        }
        return nil
    }

    func getAllElements() -> [UIElement] {
        let elements = app.descendants(matching: .any)

        var results = [UIElement]()
        for i in 0..<min(elements.count, 100) {
            let element = elements.element(boundBy: i)
            if element.exists && element.isHittable {
                results.append(wrapElement(element, index: i))
            }
        }

        return results
    }

    // MARK: - Element Interaction

    func clickElement(_ elementId: String) -> Bool {
        guard let element = elementCache[elementId] else { return false }
        element.tap()
        return true
    }

    func longClickElement(_ elementId: String, duration: TimeInterval = 1.0) -> Bool {
        guard let element = elementCache[elementId] else { return false }
        element.press(forDuration: duration)
        return true
    }

    func inputText(_ elementId: String, text: String) -> Bool {
        guard let element = elementCache[elementId] else { return false }

        // Tap to focus
        element.tap()

        // Type text
        element.typeText(text)
        return true
    }

    func clearText(_ elementId: String) -> Bool {
        guard let element = elementCache[elementId] else { return false }

        // Get current value
        guard let currentValue = element.value as? String else { return false }

        // Tap to focus
        element.tap()

        // Delete all characters
        let deleteString = String(repeating: XCUIKeyboardKey.delete.rawValue, count: currentValue.count)
        element.typeText(deleteString)

        return true
    }

    func getText(_ elementId: String) -> String? {
        guard let element = elementCache[elementId] else { return nil }

        // Try label first
        if !element.label.isEmpty {
            return element.label
        }

        // Try value
        if let value = element.value as? String {
            return value
        }

        return nil
    }

    func getBounds(_ elementId: String) -> Bounds? {
        guard let element = elementCache[elementId] else { return nil }

        let frame = element.frame
        return Bounds(
            left: Int(frame.minX),
            top: Int(frame.minY),
            right: Int(frame.maxX),
            bottom: Int(frame.maxY)
        )
    }

    func isVisible(_ elementId: String) -> Bool {
        guard let element = elementCache[elementId] else { return false }
        return element.exists && element.isHittable
    }

    func scroll(_ elementId: String, direction: String) -> Bool {
        guard let element = elementCache[elementId] else { return false }

        switch direction.lowercased() {
        case "up":
            element.swipeUp()
        case "down":
            element.swipeDown()
        case "left":
            element.swipeLeft()
        case "right":
            element.swipeRight()
        default:
            return false
        }

        return true
    }

    // MARK: - System Actions

    func pressHome() -> Bool {
        XCUIDevice.shared.press(.home)
        return true
    }

    func pressBack() -> Bool {
        // iOS doesn't have a back button
        // Swipe from left edge to go back
        let leftEdge = app.coordinate(withNormalizedOffset: CGVector(dx: 0.01, dy: 0.5))
        let middle = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
        leftEdge.press(forDuration: 0.1, thenDragTo: middle)
        return true
    }

    func pressRecents() -> Bool {
        // Double-tap home button (not available on newer iPhones)
        // Use app switcher gesture instead
        XCUIDevice.shared.press(.home)
        Thread.sleep(forTimeInterval: 0.1)
        XCUIDevice.shared.press(.home)
        return true
    }

    func openNotifications() -> Bool {
        // Swipe down from top of screen
        let topCenter = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.01))
        let middle = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
        topCenter.press(forDuration: 0.1, thenDragTo: middle)
        return true
    }

    func openQuickSettings() -> Bool {
        // Swipe down from top-right on iPhone X+
        let topRight = app.coordinate(withNormalizedOffset: CGVector(dx: 0.95, dy: 0.01))
        let middle = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
        topRight.press(forDuration: 0.1, thenDragTo: middle)
        return true
    }

    func lockScreen() -> Bool {
        // iOS doesn't provide API to lock screen programmatically
        // Would require private APIs or jailbreak
        return false
    }

    func takeScreenshot() -> Screenshot? {
        let screenshot = XCUIScreen.main.screenshot()

        return Screenshot(
            data: screenshot.pngRepresentation,
            format: "PNG",
            width: Int(screenshot.image.size.width),
            height: Int(screenshot.image.size.height)
        )
    }

    func getScreenSize() -> ScreenSize {
        let screen = UIScreen.main
        return ScreenSize(
            width: Int(screen.bounds.width * screen.scale),
            height: Int(screen.bounds.height * screen.scale),
            density: Float(screen.scale)
        )
    }

    func setOrientation(_ orientation: String) -> Bool {
        let device = XCUIDevice.shared

        switch orientation.lowercased() {
        case "portrait":
            device.orientation = .portrait
        case "landscape":
            device.orientation = .landscapeLeft
        default:
            return false
        }

        return true
    }

    // MARK: - Application Control

    func launchApp(_ bundleId: String) -> Bool {
        let app = XCUIApplication(bundleIdentifier: bundleId)
        app.launch()
        return true
    }

    func closeApp(_ bundleId: String) -> Bool {
        let app = XCUIApplication(bundleIdentifier: bundleId)
        app.terminate()
        return true
    }

    func isAppInstalled(_ bundleId: String) -> Bool {
        // XCTest doesn't provide a direct way to check installation
        // Try to launch and see if it works
        let app = XCUIApplication(bundleIdentifier: bundleId)
        return app.exists
    }

    func getCurrentApp() -> String? {
        // Return current app bundle ID
        return app.bundleID
    }

    // MARK: - Advanced Features

    func getPageSource() -> String {
        return app.debugDescription
    }

    func waitForElement(_ selector: ElementSelector, timeout: TimeInterval) -> UIElement? {
        let predicate: NSPredicate

        if let text = selector.byText {
            predicate = NSPredicate(format: "label CONTAINS[c] %@", text)
        } else if let id = selector.byId {
            predicate = NSPredicate(format: "identifier == %@", id)
        } else if let contentDesc = selector.byContentDesc {
            predicate = NSPredicate(format: "label == %@", contentDesc)
        } else {
            return nil
        }

        let element = app.descendants(matching: .any).containing(predicate).firstMatch

        if element.waitForExistence(timeout: timeout) {
            return wrapElement(element, text: selector.byText)
        }

        return nil
    }

    // MARK: - Helper Methods

    private func wrapElement(_ element: XCUIElement, text: String? = nil, id: String? = nil, type: String? = nil, contentDesc: String? = nil, index: Int? = nil) -> UIElement {
        let elementId = "elem_\(nextElementId)"
        nextElementId += 1

        // Cache element
        elementCache[elementId] = element

        let frame = element.frame
        let bounds = Bounds(
            left: Int(frame.minX),
            top: Int(frame.minY),
            right: Int(frame.maxX),
            bottom: Int(frame.maxY)
        )

        return UIElement(
            id: elementId,
            text: text ?? element.label,
            contentDescription: contentDesc ?? element.label,
            className: String(describing: element.elementType),
            resourceId: id ?? element.identifier,
            bounds: bounds,
            isClickable: element.isHittable,
            isEditable: element.elementType == .textField || element.elementType == .secureTextField,
            isScrollable: element.elementType == .scrollView || element.elementType == .table,
            isEnabled: element.isEnabled,
            isVisible: element.exists && element.isHittable
        )
    }

    private func mapElementType(_ type: String) -> XCUIElement.ElementType {
        switch type.lowercased() {
        case "button": return .button
        case "textfield", "edittext": return .textField
        case "textview": return .textView
        case "label", "textview": return .staticText
        case "image", "imageview": return .image
        case "switch": return .switch
        case "slider": return .slider
        case "scrollview": return .scrollView
        case "table", "list": return .table
        case "cell": return .cell
        default: return .any
        }
    }
}

// MARK: - Data Models

struct DeviceInfo {
    let platform: String
    let model: String
    let version: String
    let serial: String
    let manufacturer: String
    let screenWidth: Int
    let screenHeight: Int
    let dpi: Int
    let language: String
    let region: String
}

struct UIElement {
    let id: String
    let text: String?
    let contentDescription: String?
    let className: String?
    let resourceId: String?
    let bounds: Bounds
    let isClickable: Bool
    let isEditable: Bool
    let isScrollable: Bool
    let isEnabled: Bool
    let isVisible: Bool
}

struct Bounds {
    let left: Int
    let top: Int
    let right: Int
    let bottom: Int

    var width: Int { right - left }
    var height: Int { bottom - top }
    var centerX: Int { left + width / 2 }
    var centerY: Int { top + height / 2 }
}

struct ScreenSize {
    let width: Int
    let height: Int
    let density: Float
}

struct Screenshot {
    let data: Data
    let format: String
    let width: Int
    let height: Int
}

struct ElementSelector {
    let byText: String?
    let byId: String?
    let byType: String?
    let byContentDesc: String?

    init(byText: String? = nil, byId: String? = nil, byType: String? = nil, byContentDesc: String? = nil) {
        self.byText = byText
        self.byId = byId
        self.byType = byType
        self.byContentDesc = byContentDesc
    }
}
