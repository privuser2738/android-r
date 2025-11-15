package com.androidscript.agent.automation

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

/**
 * UI Automation class for interacting with accessibility nodes
 */
class UIAutomator(private val service: AccessibilityService) {

    companion object {
        private const val TAG = "UIAutomator"
    }

    /**
     * Click on an accessibility node
     */
    fun click(node: AccessibilityNodeInfo): Boolean {
        if (!node.isClickable) {
            Log.w(TAG, "Node is not clickable")
            // Try to find clickable parent
            val clickableParent = findClickableParent(node)
            if (clickableParent != null) {
                return clickableParent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            return false
        }

        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    /**
     * Long click on an accessibility node
     */
    fun longClick(node: AccessibilityNodeInfo): Boolean {
        if (!node.isLongClickable) {
            Log.w(TAG, "Node is not long clickable")
            return false
        }

        return node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
    }

    /**
     * Set text in an accessibility node
     */
    fun setText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (!node.isEditable) {
            Log.w(TAG, "Node is not editable")
            return false
        }

        // Focus the node first
        if (!node.isFocused) {
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        }

        // Set the text
        val arguments = Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    /**
     * Clear text in an accessibility node
     */
    fun clearText(node: AccessibilityNodeInfo): Boolean {
        return setText(node, "")
    }

    /**
     * Scroll a node forward or backward
     */
    fun scroll(node: AccessibilityNodeInfo, forward: Boolean): Boolean {
        if (!node.isScrollable) {
            Log.w(TAG, "Node is not scrollable")
            return false
        }

        val action = if (forward) {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        } else {
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        }

        return node.performAction(action)
    }

    /**
     * Expand or collapse a node
     */
    fun expand(node: AccessibilityNodeInfo, expand: Boolean): Boolean {
        val action = if (expand) {
            AccessibilityNodeInfo.ACTION_EXPAND
        } else {
            AccessibilityNodeInfo.ACTION_COLLAPSE
        }

        return node.performAction(action)
    }

    /**
     * Copy text from a node
     */
    fun copy(node: AccessibilityNodeInfo): Boolean {
        // Then copy
        return node.performAction(AccessibilityNodeInfo.ACTION_COPY)
    }

    /**
     * Cut text from a node
     */
    fun cut(node: AccessibilityNodeInfo): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_CUT)
    }

    /**
     * Paste text to a node
     */
    fun paste(node: AccessibilityNodeInfo): Boolean {
        return node.performAction(AccessibilityNodeInfo.ACTION_PASTE)
    }

    /**
     * Get the bounds of a node
     */
    fun getBounds(node: AccessibilityNodeInfo): Rect {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)
        return bounds
    }

    /**
     * Get the center point of a node
     */
    fun getCenter(node: AccessibilityNodeInfo): Pair<Int, Int> {
        val bounds = getBounds(node)
        return Pair(
            bounds.centerX(),
            bounds.centerY()
        )
    }

    /**
     * Check if a node is visible on screen
     */
    fun isVisible(node: AccessibilityNodeInfo): Boolean {
        if (!node.isVisibleToUser) {
            return false
        }

        val bounds = getBounds(node)
        return bounds.width() > 0 && bounds.height() > 0
    }

    /**
     * Get the text of a node
     */
    fun getText(node: AccessibilityNodeInfo): String? {
        return node.text?.toString()
    }

    /**
     * Get the content description of a node
     */
    fun getContentDescription(node: AccessibilityNodeInfo): String? {
        return node.contentDescription?.toString()
    }

    /**
     * Get the resource ID of a node
     */
    fun getResourceId(node: AccessibilityNodeInfo): String? {
        return node.viewIdResourceName
    }

    /**
     * Get the class name of a node
     */
    fun getClassName(node: AccessibilityNodeInfo): String? {
        return node.className?.toString()
    }

    /**
     * Check if node matches any text
     */
    fun hasText(node: AccessibilityNodeInfo, text: String, ignoreCase: Boolean = true): Boolean {
        val nodeText = getText(node) ?: return false
        return if (ignoreCase) {
            nodeText.equals(text, ignoreCase = true)
        } else {
            nodeText == text
        }
    }

    /**
     * Check if node contains text
     */
    fun containsText(node: AccessibilityNodeInfo, text: String, ignoreCase: Boolean = true): Boolean {
        val nodeText = getText(node) ?: return false
        return if (ignoreCase) {
            nodeText.contains(text, ignoreCase = true)
        } else {
            nodeText.contains(text)
        }
    }

    /**
     * Find clickable parent of a node
     */
    private fun findClickableParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node.parent
        while (current != null) {
            if (current.isClickable) {
                return current
            }
            current = current.parent
        }
        return null
    }

    /**
     * Dump node information for debugging
     */
    fun dumpNode(node: AccessibilityNodeInfo): String {
        return buildString {
            appendLine("Node Info:")
            appendLine("  Text: ${getText(node)}")
            appendLine("  Content Desc: ${getContentDescription(node)}")
            appendLine("  Resource ID: ${getResourceId(node)}")
            appendLine("  Class: ${getClassName(node)}")
            appendLine("  Clickable: ${node.isClickable}")
            appendLine("  Editable: ${node.isEditable}")
            appendLine("  Scrollable: ${node.isScrollable}")
            appendLine("  Visible: ${isVisible(node)}")
            appendLine("  Bounds: ${getBounds(node)}")
        }
    }
}
