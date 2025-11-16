import Foundation
import UIKit
import XCTest

/**
 * iOS Native Bridge
 * Provides built-in functions for AndroidScript on iOS
 * Bridges script runtime to iOS automation APIs
 */
class iOSNativeBridge {
    private let interpreter: Interpreter
    private let platformBridge: iOSPlatformBridge

    init(interpreter: Interpreter) {
        self.interpreter = interpreter
        self.platformBridge = iOSPlatformBridge()
    }

    /**
     * Register all built-in functions with the interpreter
     */
    func registerBuiltins() {
        let env = interpreter.getGlobalEnvironment()

        // UI Automation
        env.define(name: "Tap", value: .nativeFunction(tap))
        env.define(name: "Swipe", value: .nativeFunction(swipe))
        env.define(name: "InputText", value: .nativeFunction(inputText))
        env.define(name: "FindByText", value: .nativeFunction(findByText))
        env.define(name: "FindById", value: .nativeFunction(findById))
        env.define(name: "FindByContentDesc", value: .nativeFunction(findByContentDesc))
        env.define(name: "Click", value: .nativeFunction(click))
        env.define(name: "LongClick", value: .nativeFunction(longClick))
        env.define(name: "Scroll", value: .nativeFunction(scroll))

        // System Actions
        env.define(name: "PressHome", value: .nativeFunction(pressHome))
        env.define(name: "PressBack", value: .nativeFunction(pressBack))
        env.define(name: "PressRecents", value: .nativeFunction(pressRecents))
        env.define(name: "OpenNotifications", value: .nativeFunction(openNotifications))
        env.define(name: "OpenQuickSettings", value: .nativeFunction(openQuickSettings))

        // Device Info
        env.define(name: "GetDeviceInfo", value: .nativeFunction(getDeviceInfo))

        // Utility Functions
        env.define(name: "Print", value: .nativeFunction(print))
        env.define(name: "Sleep", value: .nativeFunction(sleep))
        env.define(name: "GetText", value: .nativeFunction(getText))
        env.define(name: "GetBounds", value: .nativeFunction(getBounds))
        env.define(name: "IsVisible", value: .nativeFunction(isVisible))
        env.define(name: "TakeScreenshot", value: .nativeFunction(takeScreenshot))
    }

    // MARK: - Helper Methods

    private func requireInt(_ value: Value, name: String) throws -> Int {
        guard case .int(let val) = value else {
            throw RuntimeError.custom("\(name) must be an integer")
        }
        return Int(val)
    }

    private func requireString(_ value: Value, name: String) throws -> String {
        guard case .string(let val) = value else {
            throw RuntimeError.custom("\(name) must be a string")
        }
        return val
    }

    private func requireBool(_ value: Value, name: String) throws -> Bool {
        guard case .bool(let val) = value else {
            throw RuntimeError.custom("\(name) must be a boolean")
        }
        return val
    }

    private func requireElementId(_ value: Value) throws -> String {
        guard case .object(let obj) = value,
              case .string(let elementId)? = obj["__elementId"] else {
            throw RuntimeError.custom("Expected element object")
        }
        return elementId
    }

    // MARK: - UI Automation Functions

    /**
     * Tap(x, y)
     * Performs a tap at the specified coordinates
     */
    private func tap(args: [Value]) -> Value {
        guard args.count == 2 else {
            return .bool(false)
        }

        do {
            let x = try requireInt(args[0], name: "x")
            let y = try requireInt(args[1], name: "y")

            let result = platformBridge.tap(x: x, y: y)
            print("Tap(\(x), \(y)) = \(result)")
            return .bool(result)
        } catch {
            print("Tap error: \(error.localizedDescription)")
            return .bool(false)
        }
    }

    /**
     * Swipe(x1, y1, x2, y2, duration)
     * Performs a swipe gesture
     */
    private func swipe(args: [Value]) -> Value {
        guard args.count >= 4 && args.count <= 5 else {
            return .bool(false)
        }

        do {
            let x1 = try requireInt(args[0], name: "x1")
            let y1 = try requireInt(args[1], name: "y1")
            let x2 = try requireInt(args[2], name: "x2")
            let y2 = try requireInt(args[3], name: "y2")
            let duration = args.count == 5 ? TimeInterval(try requireInt(args[4], name: "duration")) / 1000.0 : 0.3

            let result = platformBridge.swipe(x1: x1, y1: y1, x2: x2, y2: y2, duration: duration)
            print("Swipe(\(x1), \(y1), \(x2), \(y2), \(duration)) = \(result)")
            return .bool(result)
        } catch {
            print("Swipe error: \(error.localizedDescription)")
            return .bool(false)
        }
    }

    /**
     * InputText(text)
     * Types text (requires focused element)
     */
    private func inputText(args: [Value]) -> Value {
        guard args.count == 1 else {
            return .bool(false)
        }

        do {
            let text = try requireString(args[0], name: "text")
            // On iOS, we need an element to input text
            // This is a simplified version
            print("InputText(\"\(text)\")")
            return .bool(true)
        } catch {
            print("InputText error: \(error.localizedDescription)")
            return .bool(false)
        }
    }

    /**
     * FindByText(text)
     * Finds an element by text content
     */
    private func findByText(args: [Value]) -> Value {
        guard args.count == 1 else {
            return .nil
        }

        do {
            let text = try requireString(args[0], name: "text")

            if let element = platformBridge.findByText(text) {
                return wrapElement(element)
            }

            print("FindByText(\"\(text)\") = nil")
            return .nil
        } catch {
            print("FindByText error: \(error.localizedDescription)")
            return .nil
        }
    }

    /**
     * FindById(id)
     * Finds an element by accessibility ID
     */
    private func findById(args: [Value]) -> Value {
        guard args.count == 1 else {
            return .nil
        }

        do {
            let id = try requireString(args[0], name: "id")

            if let element = platformBridge.findById(id) {
                return wrapElement(element)
            }

            print("FindById(\"\(id)\") = nil")
            return .nil
        } catch {
            print("FindById error: \(error.localizedDescription)")
            return .nil
        }
    }

    /**
     * FindByContentDesc(description)
     * Finds an element by content description/accessibility label
     */
    private func findByContentDesc(args: [Value]) -> Value {
        guard args.count == 1 else {
            return .nil
        }

        do {
            let desc = try requireString(args[0], name: "description")

            if let element = platformBridge.findByContentDescription(desc) {
                return wrapElement(element)
            }

            print("FindByContentDesc(\"\(desc)\") = nil")
            return .nil
        } catch {
            print("FindByContentDesc error: \(error.localizedDescription)")
            return .nil
        }
    }

    /**
     * Click(element)
     * Clicks on an element
     */
    private func click(args: [Value]) -> Value {
        guard args.count == 1 else {
            return .bool(false)
        }

        do {
            let elementId = try requireElementId(args[0])
            let result = platformBridge.clickElement(elementId)
            print("Click(element) = \(result)")
            return .bool(result)
        } catch {
            print("Click error: \(error.localizedDescription)")
            return .bool(false)
        }
    }

    /**
     * LongClick(element)
     * Long clicks on an element
     */
    private func longClick(args: [Value]) -> Value {
        guard args.count == 1 else {
            return .bool(false)
        }

        do {
            let elementId = try requireElementId(args[0])
            let result = platformBridge.longClickElement(elementId)
            print("LongClick(element) = \(result)")
            return .bool(result)
        } catch {
            print("LongClick error: \(error.localizedDescription)")
            return .bool(false)
        }
    }

    /**
     * Scroll(element, direction)
     * Scrolls an element in a direction
     */
    private func scroll(args: [Value]) -> Value {
        guard args.count == 2 else {
            return .bool(false)
        }

        do {
            let elementId = try requireElementId(args[0])
            let direction = try requireString(args[1], name: "direction")

            let result = platformBridge.scroll(elementId, direction: direction)
            print("Scroll(element, \(direction)) = \(result)")
            return .bool(result)
        } catch {
            print("Scroll error: \(error.localizedDescription)")
            return .bool(false)
        }
    }

    // MARK: - System Actions

    private func pressHome(args: [Value]) -> Value {
        let result = platformBridge.pressHome()
        print("PressHome() = \(result)")
        return .bool(result)
    }

    private func pressBack(args: [Value]) -> Value {
        let result = platformBridge.pressBack()
        print("PressBack() = \(result)")
        return .bool(result)
    }

    private func pressRecents(args: [Value]) -> Value {
        let result = platformBridge.pressRecents()
        print("PressRecents() = \(result)")
        return .bool(result)
    }

    private func openNotifications(args: [Value]) -> Value {
        let result = platformBridge.openNotifications()
        print("OpenNotifications() = \(result)")
        return .bool(result)
    }

    private func openQuickSettings(args: [Value]) -> Value {
        let result = platformBridge.openQuickSettings()
        print("OpenQuickSettings() = \(result)")
        return .bool(result)
    }

    // MARK: - Device Info

    private func getDeviceInfo(args: [Value]) -> Value {
        let deviceInfo = platformBridge.getDeviceInfo()

        var obj: ValueMap = [:]
        obj["platform"] = .string(deviceInfo.platform)
        obj["model"] = .string(deviceInfo.model)
        obj["version"] = .string(deviceInfo.version)
        obj["serial"] = .string(deviceInfo.serial)
        obj["manufacturer"] = .string(deviceInfo.manufacturer)
        obj["screenWidth"] = .int(Int64(deviceInfo.screenWidth))
        obj["screenHeight"] = .int(Int64(deviceInfo.screenHeight))

        return .object(obj)
    }

    // MARK: - Utility Functions

    /**
     * Print(...values)
     * Prints values to the console
     */
    private func print(args: [Value]) -> Value {
        let message = args.map { $0.toString() }.joined(separator: " ")
        Swift.print("Script: \(message)")
        return .nil
    }

    /**
     * Sleep(milliseconds)
     * Sleeps for the specified duration
     */
    private func sleep(args: [Value]) -> Value {
        guard args.count == 1 else {
            return .nil
        }

        do {
            let ms = try requireInt(args[0], name: "milliseconds")
            Thread.sleep(forTimeInterval: TimeInterval(ms) / 1000.0)
            return .nil
        } catch {
            Swift.print("Sleep error: \(error.localizedDescription)")
            return .nil
        }
    }

    /**
     * GetText(element)
     * Gets the text of an element
     */
    private func getText(args: [Value]) -> Value {
        guard args.count == 1 else {
            return .nil
        }

        do {
            let elementId = try requireElementId(args[0])
            if let text = platformBridge.getText(elementId) {
                return .string(text)
            }
            return .string("")
        } catch {
            Swift.print("GetText error: \(error.localizedDescription)")
            return .string("")
        }
    }

    /**
     * GetBounds(element)
     * Gets the bounds of an element as an object
     */
    private func getBounds(args: [Value]) -> Value {
        guard args.count == 1 else {
            return .nil
        }

        do {
            let elementId = try requireElementId(args[0])
            if let bounds = platformBridge.getBounds(elementId) {
                var obj: ValueMap = [:]
                obj["left"] = .int(Int64(bounds.left))
                obj["top"] = .int(Int64(bounds.top))
                obj["right"] = .int(Int64(bounds.right))
                obj["bottom"] = .int(Int64(bounds.bottom))
                obj["width"] = .int(Int64(bounds.width))
                obj["height"] = .int(Int64(bounds.height))
                return .object(obj)
            }
            return .nil
        } catch {
            Swift.print("GetBounds error: \(error.localizedDescription)")
            return .nil
        }
    }

    /**
     * IsVisible(element)
     * Checks if an element is visible
     */
    private func isVisible(args: [Value]) -> Value {
        guard args.count == 1 else {
            return .bool(false)
        }

        do {
            let elementId = try requireElementId(args[0])
            let result = platformBridge.isVisible(elementId)
            return .bool(result)
        } catch {
            Swift.print("IsVisible error: \(error.localizedDescription)")
            return .bool(false)
        }
    }

    /**
     * TakeScreenshot()
     * Takes a screenshot
     */
    private func takeScreenshot(args: [Value]) -> Value {
        if let screenshot = platformBridge.takeScreenshot() {
            var obj: ValueMap = [:]
            obj["width"] = .int(Int64(screenshot.width))
            obj["height"] = .int(Int64(screenshot.height))
            obj["format"] = .string(screenshot.format)
            return .object(obj)
        }
        return .nil
    }

    // MARK: - Element Wrapping

    private func wrapElement(_ element: UIElement) -> Value {
        var obj: ValueMap = [:]
        obj["__elementId"] = .string(element.id)
        obj["text"] = .string(element.text ?? "")
        obj["contentDescription"] = .string(element.contentDescription ?? "")
        obj["className"] = .string(element.className ?? "")
        obj["resourceId"] = .string(element.resourceId ?? "")
        obj["isClickable"] = .bool(element.isClickable)
        obj["isEditable"] = .bool(element.isEditable)
        obj["isScrollable"] = .bool(element.isScrollable)

        return .object(obj)
    }
}
