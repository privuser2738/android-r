package com.androidscript.agent.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.androidscript.agent.automation.UIAutomator
import com.androidscript.agent.automation.GestureController
import com.androidscript.agent.automation.ElementFinder

/**
 * Main Accessibility Service for AndroidScript automation
 * Provides UI automation capabilities: tap, swipe, find elements, gestures
 */
class AutomationAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AutomationService"

        // Singleton instance
        private var instance: AutomationAccessibilityService? = null

        /**
         * Get the active service instance
         */
        fun getInstance(): AutomationAccessibilityService? = instance

        /**
         * Check if service is running
         */
        fun isRunning(): Boolean = instance != null
    }

    // Automation components
    private lateinit var uiAutomator: UIAutomator
    private lateinit var gestureController: GestureController
    private lateinit var elementFinder: ElementFinder

    override fun onServiceConnected() {
        super.onServiceConnected()

        Log.i(TAG, "AutomationAccessibilityService connected")
        instance = this

        // Initialize automation components
        uiAutomator = UIAutomator(this)
        gestureController = GestureController(this)
        elementFinder = ElementFinder(this)

        Log.i(TAG, "Automation components initialized")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events if needed
        event?.let {
            when (it.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    Log.d(TAG, "Window changed: ${it.packageName}")
                }
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    Log.d(TAG, "View clicked: ${it.text}")
                }
                // Add more event handling as needed
                else -> {
                    // Other events
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "AutomationAccessibilityService destroyed")
        instance = null
    }

    // ========================================================================
    // Public API for UI Automation
    // ========================================================================

    /**
     * Get the UI automator instance
     */
    fun getUIAutomator(): UIAutomator = uiAutomator

    /**
     * Get the gesture controller instance
     */
    fun getGestureController(): GestureController = gestureController

    /**
     * Get the element finder instance
     */
    fun getElementFinder(): ElementFinder = elementFinder

    /**
     * Get the root accessibility node
     */
    fun getRootNode(): AccessibilityNodeInfo? {
        return rootInActiveWindow
    }

    /**
     * Perform a tap at coordinates
     */
    fun tap(x: Int, y: Int): Boolean {
        return gestureController.tap(x.toFloat(), y.toFloat())
    }

    /**
     * Perform a swipe gesture
     */
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Long): Boolean {
        return gestureController.swipe(
            x1.toFloat(), y1.toFloat(),
            x2.toFloat(), y2.toFloat(),
            duration
        )
    }

    /**
     * Input text (focus must already be on text field)
     */
    fun inputText(text: String): Boolean {
        val node = getRootNode()
        node?.let {
            val focusedNode = elementFinder.findFocusedNode(it)
            if (focusedNode != null) {
                return uiAutomator.setText(focusedNode, text)
            }
        }
        return false
    }

    /**
     * Execute a global action (back, home, recents, etc.)
     */
    fun executeGlobalAction(action: Int): Boolean {
        return performGlobalAction(action)
    }

    /**
     * Find element by text
     */
    fun findByText(text: String): AccessibilityNodeInfo? {
        val root = getRootNode() ?: return null
        return elementFinder.findByText(root, text)
    }

    /**
     * Find element by resource ID
     */
    fun findByResourceId(resourceId: String): AccessibilityNodeInfo? {
        val root = getRootNode() ?: return null
        return elementFinder.findByResourceId(root, resourceId)
    }

    /**
     * Find element by content description
     */
    fun findByContentDescription(description: String): AccessibilityNodeInfo? {
        val root = getRootNode() ?: return null
        return elementFinder.findByContentDescription(root, description)
    }

    /**
     * Click on a node
     */
    fun clickNode(node: AccessibilityNodeInfo): Boolean {
        return uiAutomator.click(node)
    }

    /**
     * Long click on a node
     */
    fun longClickNode(node: AccessibilityNodeInfo): Boolean {
        return uiAutomator.longClick(node)
    }

    /**
     * Scroll a node
     */
    fun scrollNode(node: AccessibilityNodeInfo, forward: Boolean): Boolean {
        return uiAutomator.scroll(node, forward)
    }

    /**
     * Execute a custom gesture
     */
    fun executeGesture(gesture: GestureDescription, callback: GestureResultCallback?): Boolean {
        return dispatchGesture(gesture, callback, null)
    }

    // ========================================================================
    // Global Actions
    // ========================================================================

    /**
     * Press back button
     */
    fun pressBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    /**
     * Press home button
     */
    fun pressHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    /**
     * Open recents
     */
    fun pressRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    /**
     * Open notifications
     */
    fun openNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

    /**
     * Open quick settings
     */
    fun openQuickSettings(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    }
}
