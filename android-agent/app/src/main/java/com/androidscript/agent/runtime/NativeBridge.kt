package com.androidscript.agent.runtime

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.androidscript.agent.service.AutomationAccessibilityService

/**
 * NativeBridge provides built-in functions for AndroidScript
 * Bridges script runtime to Android AccessibilityService APIs
 */
class NativeBridge(private val interpreter: Interpreter) {

    companion object {
        private const val TAG = "NativeBridge"
    }

    /**
     * Register all built-in functions with the interpreter
     */
    fun registerBuiltins() {
        val env = interpreter.getGlobalEnvironment()

        // UI Automation
        env.define("Tap", NativeFunctionValue(::tap))
        env.define("Swipe", NativeFunctionValue(::swipe))
        env.define("InputText", NativeFunctionValue(::inputText))
        env.define("FindByText", NativeFunctionValue(::findByText))
        env.define("FindByResourceId", NativeFunctionValue(::findByResourceId))
        env.define("FindByContentDesc", NativeFunctionValue(::findByContentDesc))
        env.define("Click", NativeFunctionValue(::click))
        env.define("LongClick", NativeFunctionValue(::longClick))
        env.define("Scroll", NativeFunctionValue(::scroll))

        // Global Actions
        env.define("PressBack", NativeFunctionValue(::pressBack))
        env.define("PressHome", NativeFunctionValue(::pressHome))
        env.define("PressRecents", NativeFunctionValue(::pressRecents))
        env.define("OpenNotifications", NativeFunctionValue(::openNotifications))
        env.define("OpenQuickSettings", NativeFunctionValue(::openQuickSettings))

        // Utility Functions
        env.define("Print", NativeFunctionValue(::print))
        env.define("Sleep", NativeFunctionValue(::sleep))
        env.define("GetText", NativeFunctionValue(::getText))
        env.define("GetBounds", NativeFunctionValue(::getBounds))
        env.define("IsVisible", NativeFunctionValue(::isVisible))
    }

    // ========================================================================
    // Helper Functions
    // ========================================================================

    private fun getService(): AutomationAccessibilityService {
        return AutomationAccessibilityService.getInstance()
            ?: throw RuntimeException("AccessibilityService not running")
    }

    private fun requireInt(value: Value, name: String): Long {
        if (!value.isInt()) {
            throw RuntimeException("$name must be an integer")
        }
        return value.asInt()
    }

    private fun requireString(value: Value, name: String): String {
        if (!value.isString()) {
            throw RuntimeException("$name must be a string")
        }
        return value.asString()
    }

    private fun requireBool(value: Value, name: String): Boolean {
        if (!value.isBool()) {
            throw RuntimeException("$name must be a boolean")
        }
        return value.asBool()
    }

    // ========================================================================
    // UI Automation Functions
    // ========================================================================

    /**
     * Tap(x, y)
     * Performs a tap at the specified coordinates
     */
    private fun tap(args: List<Value>): Value {
        if (args.size != 2) {
            throw RuntimeException("Tap requires 2 arguments: x, y")
        }

        val x = requireInt(args[0], "x").toInt()
        val y = requireInt(args[1], "y").toInt()

        val service = getService()
        val result = service.tap(x, y)

        Log.d(TAG, "Tap($x, $y) = $result")
        return BoolValue(result)
    }

    /**
     * Swipe(x1, y1, x2, y2, duration)
     * Performs a swipe gesture
     */
    private fun swipe(args: List<Value>): Value {
        if (args.size < 4 || args.size > 5) {
            throw RuntimeException("Swipe requires 4-5 arguments: x1, y1, x2, y2, [duration]")
        }

        val x1 = requireInt(args[0], "x1").toInt()
        val y1 = requireInt(args[1], "y1").toInt()
        val x2 = requireInt(args[2], "x2").toInt()
        val y2 = requireInt(args[3], "y2").toInt()
        val duration = if (args.size == 5) requireInt(args[4], "duration") else 300L

        val service = getService()
        val result = service.swipe(x1, y1, x2, y2, duration)

        Log.d(TAG, "Swipe($x1, $y1, $x2, $y2, $duration) = $result")
        return BoolValue(result)
    }

    /**
     * InputText(text)
     * Inputs text into the focused field
     */
    private fun inputText(args: List<Value>): Value {
        if (args.size != 1) {
            throw RuntimeException("InputText requires 1 argument: text")
        }

        val text = requireString(args[0], "text")

        val service = getService()
        val result = service.inputText(text)

        Log.d(TAG, "InputText(\"$text\") = $result")
        return BoolValue(result)
    }

    /**
     * FindByText(text)
     * Finds an element by text content
     */
    private fun findByText(args: List<Value>): Value {
        if (args.size != 1) {
            throw RuntimeException("FindByText requires 1 argument: text")
        }

        val text = requireString(args[0], "text")

        val service = getService()
        val node = service.findByText(text)

        Log.d(TAG, "FindByText(\"$text\") = ${node != null}")
        return if (node != null) {
            wrapNode(node)
        } else {
            NilValue
        }
    }

    /**
     * FindByResourceId(resourceId)
     * Finds an element by resource ID
     */
    private fun findByResourceId(args: List<Value>): Value {
        if (args.size != 1) {
            throw RuntimeException("FindByResourceId requires 1 argument: resourceId")
        }

        val resourceId = requireString(args[0], "resourceId")

        val service = getService()
        val node = service.findByResourceId(resourceId)

        Log.d(TAG, "FindByResourceId(\"$resourceId\") = ${node != null}")
        return if (node != null) {
            wrapNode(node)
        } else {
            NilValue
        }
    }

    /**
     * FindByContentDesc(description)
     * Finds an element by content description
     */
    private fun findByContentDesc(args: List<Value>): Value {
        if (args.size != 1) {
            throw RuntimeException("FindByContentDesc requires 1 argument: description")
        }

        val description = requireString(args[0], "description")

        val service = getService()
        val node = service.findByContentDescription(description)

        Log.d(TAG, "FindByContentDesc(\"$description\") = ${node != null}")
        return if (node != null) {
            wrapNode(node)
        } else {
            NilValue
        }
    }

    /**
     * Click(element)
     * Clicks on an element
     */
    private fun click(args: List<Value>): Value {
        if (args.size != 1) {
            throw RuntimeException("Click requires 1 argument: element")
        }

        val node = unwrapNode(args[0])
        val service = getService()
        val result = service.clickNode(node)

        Log.d(TAG, "Click(element) = $result")
        return BoolValue(result)
    }

    /**
     * LongClick(element)
     * Long clicks on an element
     */
    private fun longClick(args: List<Value>): Value {
        if (args.size != 1) {
            throw RuntimeException("LongClick requires 1 argument: element")
        }

        val node = unwrapNode(args[0])
        val service = getService()
        val result = service.longClickNode(node)

        Log.d(TAG, "LongClick(element) = $result")
        return BoolValue(result)
    }

    /**
     * Scroll(element, forward)
     * Scrolls an element forward or backward
     */
    private fun scroll(args: List<Value>): Value {
        if (args.size != 2) {
            throw RuntimeException("Scroll requires 2 arguments: element, forward")
        }

        val node = unwrapNode(args[0])
        val forward = requireBool(args[1], "forward")

        val service = getService()
        val result = service.scrollNode(node, forward)

        Log.d(TAG, "Scroll(element, $forward) = $result")
        return BoolValue(result)
    }

    // ========================================================================
    // Global Actions
    // ========================================================================

    private fun pressBack(args: List<Value>): Value {
        if (args.isNotEmpty()) {
            throw RuntimeException("PressBack requires 0 arguments")
        }

        val service = getService()
        val result = service.pressBack()

        Log.d(TAG, "PressBack() = $result")
        return BoolValue(result)
    }

    private fun pressHome(args: List<Value>): Value {
        if (args.isNotEmpty()) {
            throw RuntimeException("PressHome requires 0 arguments")
        }

        val service = getService()
        val result = service.pressHome()

        Log.d(TAG, "PressHome() = $result")
        return BoolValue(result)
    }

    private fun pressRecents(args: List<Value>): Value {
        if (args.isNotEmpty()) {
            throw RuntimeException("PressRecents requires 0 arguments")
        }

        val service = getService()
        val result = service.pressRecents()

        Log.d(TAG, "PressRecents() = $result")
        return BoolValue(result)
    }

    private fun openNotifications(args: List<Value>): Value {
        if (args.isNotEmpty()) {
            throw RuntimeException("OpenNotifications requires 0 arguments")
        }

        val service = getService()
        val result = service.openNotifications()

        Log.d(TAG, "OpenNotifications() = $result")
        return BoolValue(result)
    }

    private fun openQuickSettings(args: List<Value>): Value {
        if (args.isNotEmpty()) {
            throw RuntimeException("OpenQuickSettings requires 0 arguments")
        }

        val service = getService()
        val result = service.openQuickSettings()

        Log.d(TAG, "OpenQuickSettings() = $result")
        return BoolValue(result)
    }

    // ========================================================================
    // Utility Functions
    // ========================================================================

    /**
     * Print(...values)
     * Prints values to the log
     */
    private fun print(args: List<Value>): Value {
        val message = args.joinToString(" ") { it.toString() }
        Log.i(TAG, "Script: $message")
        return NilValue
    }

    /**
     * Sleep(milliseconds)
     * Sleeps for the specified duration
     */
    private fun sleep(args: List<Value>): Value {
        if (args.size != 1) {
            throw RuntimeException("Sleep requires 1 argument: milliseconds")
        }

        val ms = requireInt(args[0], "milliseconds")

        try {
            Thread.sleep(ms)
        } catch (e: InterruptedException) {
            throw RuntimeException("Sleep interrupted")
        }

        return NilValue
    }

    /**
     * GetText(element)
     * Gets the text of an element
     */
    private fun getText(args: List<Value>): Value {
        if (args.size != 1) {
            throw RuntimeException("GetText requires 1 argument: element")
        }

        val node = unwrapNode(args[0])
        val text = node.text?.toString() ?: ""

        return StringValue(text)
    }

    /**
     * GetBounds(element)
     * Gets the bounds of an element as an object
     */
    private fun getBounds(args: List<Value>): Value {
        if (args.size != 1) {
            throw RuntimeException("GetBounds requires 1 argument: element")
        }

        val node = unwrapNode(args[0])
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)

        val obj = mutableMapOf<String, Value>()
        obj["left"] = IntValue(bounds.left.toLong())
        obj["top"] = IntValue(bounds.top.toLong())
        obj["right"] = IntValue(bounds.right.toLong())
        obj["bottom"] = IntValue(bounds.bottom.toLong())
        obj["width"] = IntValue(bounds.width().toLong())
        obj["height"] = IntValue(bounds.height().toLong())

        return ObjectValue(obj)
    }

    /**
     * IsVisible(element)
     * Checks if an element is visible
     */
    private fun isVisible(args: List<Value>): Value {
        if (args.size != 1) {
            throw RuntimeException("IsVisible requires 1 argument: element")
        }

        val node = unwrapNode(args[0])
        return BoolValue(node.isVisibleToUser)
    }

    // ========================================================================
    // Node Wrapping/Unwrapping
    // ========================================================================

    /**
     * Wrap an AccessibilityNodeInfo in a Value object
     * Store node reference in a map with unique ID
     */
    private val nodeMap = mutableMapOf<Long, AccessibilityNodeInfo>()
    private var nextNodeId = 1L

    private fun wrapNode(node: AccessibilityNodeInfo): Value {
        val id = nextNodeId++
        nodeMap[id] = node

        val obj = mutableMapOf<String, Value>()
        obj["__nodeId"] = IntValue(id)
        obj["text"] = StringValue(node.text?.toString() ?: "")
        obj["contentDescription"] = StringValue(node.contentDescription?.toString() ?: "")
        obj["className"] = StringValue(node.className?.toString() ?: "")
        obj["resourceId"] = StringValue(node.viewIdResourceName ?: "")
        obj["clickable"] = BoolValue(node.isClickable)
        obj["editable"] = BoolValue(node.isEditable)
        obj["scrollable"] = BoolValue(node.isScrollable)

        return ObjectValue(obj)
    }

    private fun unwrapNode(value: Value): AccessibilityNodeInfo {
        if (value !is ObjectValue) {
            throw RuntimeException("Expected element object")
        }

        val nodeIdValue = value["__nodeId"]
        if (!nodeIdValue.isInt()) {
            throw RuntimeException("Invalid element object")
        }

        val nodeId = nodeIdValue.asInt()
        return nodeMap[nodeId] ?: throw RuntimeException("Element no longer valid")
    }
}
