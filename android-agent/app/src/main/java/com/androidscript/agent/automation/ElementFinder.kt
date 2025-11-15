package com.androidscript.agent.automation

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Element finder for locating UI elements in the accessibility tree
 */
class ElementFinder(private val service: AccessibilityService) {

    companion object {
        private const val TAG = "ElementFinder"
    }

    /**
     * Find the first node with matching text
     */
    fun findByText(root: AccessibilityNodeInfo, text: String, ignoreCase: Boolean = true): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding node with text: $text")
        return findNodeRecursive(root) { node ->
            val nodeText = node.text?.toString() ?: return@findNodeRecursive false
            if (ignoreCase) {
                nodeText.equals(text, ignoreCase = true)
            } else {
                nodeText == text
            }
        }
    }

    /**
     * Find the first node containing text
     */
    fun findByTextContains(root: AccessibilityNodeInfo, text: String, ignoreCase: Boolean = true): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding node containing text: $text")
        return findNodeRecursive(root) { node ->
            val nodeText = node.text?.toString() ?: return@findNodeRecursive false
            if (ignoreCase) {
                nodeText.contains(text, ignoreCase = true)
            } else {
                nodeText.contains(text)
            }
        }
    }

    /**
     * Find all nodes with matching text
     */
    fun findAllByText(root: AccessibilityNodeInfo, text: String, ignoreCase: Boolean = true): List<AccessibilityNodeInfo> {
        Log.d(TAG, "Finding all nodes with text: $text")
        return findAllNodesRecursive(root) { node ->
            val nodeText = node.text?.toString() ?: return@findAllNodesRecursive false
            if (ignoreCase) {
                nodeText.equals(text, ignoreCase = true)
            } else {
                nodeText == text
            }
        }
    }

    /**
     * Find the first node with matching resource ID
     */
    fun findByResourceId(root: AccessibilityNodeInfo, resourceId: String): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding node with resource ID: $resourceId")
        return findNodeRecursive(root) { node ->
            node.viewIdResourceName == resourceId
        }
    }

    /**
     * Find all nodes with matching resource ID
     */
    fun findAllByResourceId(root: AccessibilityNodeInfo, resourceId: String): List<AccessibilityNodeInfo> {
        Log.d(TAG, "Finding all nodes with resource ID: $resourceId")
        return findAllNodesRecursive(root) { node ->
            node.viewIdResourceName == resourceId
        }
    }

    /**
     * Find the first node with matching content description
     */
    fun findByContentDescription(root: AccessibilityNodeInfo, description: String, ignoreCase: Boolean = true): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding node with content description: $description")
        return findNodeRecursive(root) { node ->
            val nodeDesc = node.contentDescription?.toString() ?: return@findNodeRecursive false
            if (ignoreCase) {
                nodeDesc.equals(description, ignoreCase = true)
            } else {
                nodeDesc == description
            }
        }
    }

    /**
     * Find all nodes with matching content description
     */
    fun findAllByContentDescription(root: AccessibilityNodeInfo, description: String, ignoreCase: Boolean = true): List<AccessibilityNodeInfo> {
        Log.d(TAG, "Finding all nodes with content description: $description")
        return findAllNodesRecursive(root) { node ->
            val nodeDesc = node.contentDescription?.toString() ?: return@findAllNodesRecursive false
            if (ignoreCase) {
                nodeDesc.equals(description, ignoreCase = true)
            } else {
                nodeDesc == description
            }
        }
    }

    /**
     * Find the first node with matching class name
     */
    fun findByClassName(root: AccessibilityNodeInfo, className: String): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding node with class name: $className")
        return findNodeRecursive(root) { node ->
            node.className?.toString() == className
        }
    }

    /**
     * Find all nodes with matching class name
     */
    fun findAllByClassName(root: AccessibilityNodeInfo, className: String): List<AccessibilityNodeInfo> {
        Log.d(TAG, "Finding all nodes with class name: $className")
        return findAllNodesRecursive(root) { node ->
            node.className?.toString() == className
        }
    }

    /**
     * Find the first clickable node
     */
    fun findClickable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding first clickable node")
        return findNodeRecursive(root) { node ->
            node.isClickable
        }
    }

    /**
     * Find all clickable nodes
     */
    fun findAllClickable(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        Log.d(TAG, "Finding all clickable nodes")
        return findAllNodesRecursive(root) { node ->
            node.isClickable
        }
    }

    /**
     * Find the first editable node
     */
    fun findEditable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding first editable node")
        return findNodeRecursive(root) { node ->
            node.isEditable
        }
    }

    /**
     * Find all editable nodes
     */
    fun findAllEditable(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        Log.d(TAG, "Finding all editable nodes")
        return findAllNodesRecursive(root) { node ->
            node.isEditable
        }
    }

    /**
     * Find the first scrollable node
     */
    fun findScrollable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding first scrollable node")
        return findNodeRecursive(root) { node ->
            node.isScrollable
        }
    }

    /**
     * Find all scrollable nodes
     */
    fun findAllScrollable(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        Log.d(TAG, "Finding all scrollable nodes")
        return findAllNodesRecursive(root) { node ->
            node.isScrollable
        }
    }

    /**
     * Find the currently focused node
     */
    fun findFocusedNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding focused node")
        return findNodeRecursive(root) { node ->
            node.isFocused
        }
    }

    /**
     * Find nodes matching a custom predicate
     */
    fun findByPredicate(root: AccessibilityNodeInfo, predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding node with custom predicate")
        return findNodeRecursive(root, predicate)
    }

    /**
     * Find all nodes matching a custom predicate
     */
    fun findAllByPredicate(root: AccessibilityNodeInfo, predicate: (AccessibilityNodeInfo) -> Boolean): List<AccessibilityNodeInfo> {
        Log.d(TAG, "Finding all nodes with custom predicate")
        return findAllNodesRecursive(root, predicate)
    }

    /**
     * Find a node by index (breadth-first traversal)
     */
    fun findByIndex(root: AccessibilityNodeInfo, index: Int): AccessibilityNodeInfo? {
        Log.d(TAG, "Finding node at index: $index")
        var currentIndex = 0

        return findNodeRecursive(root) { _ ->
            if (currentIndex == index) {
                true
            } else {
                currentIndex++
                false
            }
        }
    }

    /**
     * Get all nodes (entire tree)
     */
    fun getAllNodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        Log.d(TAG, "Getting all nodes")
        return findAllNodesRecursive(root) { true }
    }

    /**
     * Count nodes matching a predicate
     */
    fun countNodes(root: AccessibilityNodeInfo, predicate: (AccessibilityNodeInfo) -> Boolean): Int {
        return findAllNodesRecursive(root, predicate).size
    }

    /**
     * Recursively find the first node matching a predicate
     */
    private fun findNodeRecursive(
        node: AccessibilityNodeInfo?,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (node == null) {
            return null
        }

        // Check current node
        if (predicate(node)) {
            return node
        }

        // Check children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findNodeRecursive(child, predicate)
            if (result != null) {
                return result
            }
        }

        return null
    }

    /**
     * Recursively find all nodes matching a predicate
     */
    private fun findAllNodesRecursive(
        node: AccessibilityNodeInfo?,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): List<AccessibilityNodeInfo> {
        if (node == null) {
            return emptyList()
        }

        val results = mutableListOf<AccessibilityNodeInfo>()

        // Check current node
        if (predicate(node)) {
            results.add(node)
        }

        // Check children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            results.addAll(findAllNodesRecursive(child, predicate))
        }

        return results
    }

    /**
     * Print the entire node tree for debugging
     */
    fun dumpTree(root: AccessibilityNodeInfo?, indent: Int = 0): String {
        if (root == null) {
            return "null"
        }

        val sb = StringBuilder()
        val indentStr = "  ".repeat(indent)

        sb.appendLine("${indentStr}Node:")
        sb.appendLine("${indentStr}  Text: ${root.text}")
        sb.appendLine("${indentStr}  ContentDesc: ${root.contentDescription}")
        sb.appendLine("${indentStr}  ResourceID: ${root.viewIdResourceName}")
        sb.appendLine("${indentStr}  Class: ${root.className}")
        sb.appendLine("${indentStr}  Clickable: ${root.isClickable}")
        sb.appendLine("${indentStr}  Editable: ${root.isEditable}")
        sb.appendLine("${indentStr}  Scrollable: ${root.isScrollable}")
        sb.appendLine("${indentStr}  Children: ${root.childCount}")

        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            sb.append(dumpTree(child, indent + 1))
        }

        return sb.toString()
    }
}
