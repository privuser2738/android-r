package com.androidscript.agent.download

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.androidscript.agent.service.AutomationAccessibilityService

/**
 * LinkExtractor extracts episode links from web pages using accessibility tree
 * Specifically designed for anime series pages in Firefox
 */
class LinkExtractor {

    companion object {
        private const val TAG = "LinkExtractor"
    }

    /**
     * Extract all episode links from current Firefox page
     * Returns list of (episodeName, url) pairs
     */
    fun extractEpisodeLinks(service: AutomationAccessibilityService): List<Pair<String, String>> {
        val rootNode = service.rootInActiveWindow ?: run {
            Log.e(TAG, "No root window available")
            return emptyList()
        }

        val episodes = mutableListOf<Pair<String, String>>()
        val foundUrls = mutableSetOf<String>()  // Prevent duplicates

        // Extract links from the page
        extractLinksRecursive(rootNode, episodes, foundUrls)

        Log.i(TAG, "Extracted ${episodes.size} episode links")
        return episodes.sortedBy { extractEpisodeNumber(it.first) }
    }

    /**
     * Extract series name from page title or first heading
     */
    fun extractSeriesName(service: AutomationAccessibilityService): String {
        val rootNode = service.rootInActiveWindow ?: return "Unknown Series"

        // Try to find page title or h1 heading
        val title = findSeriesTitle(rootNode)
        if (title.isNotBlank()) {
            return cleanSeriesName(title)
        }

        return "Unknown Series"
    }

    /**
     * Recursively extract links from accessibility tree
     */
    private fun extractLinksRecursive(
        node: AccessibilityNodeInfo,
        episodes: MutableList<Pair<String, String>>,
        foundUrls: MutableSet<String>,
        depth: Int = 0
    ) {
        if (depth > 30) return  // Prevent infinite recursion

        // Check if this node is a link/clickable element
        if (node.isClickable || node.className?.contains("Link") == true) {
            val text = node.text?.toString() ?: node.contentDescription?.toString() ?: ""
            val viewId = node.viewIdResourceName ?: ""

            // Look for episode patterns in text
            if (isEpisodeLink(text)) {
                // Try to extract URL from various sources
                val url = extractUrl(node, text)
                if (url != null && !foundUrls.contains(url)) {
                    val episodeName = extractEpisodeName(text)
                    episodes.add(episodeName to url)
                    foundUrls.add(url)
                    Log.d(TAG, "Found episode: $episodeName -> $url")
                }
            }
        }

        // Recurse through children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                extractLinksRecursive(child, episodes, foundUrls, depth + 1)
                child.recycle()
            }
        }
    }

    /**
     * Check if text indicates an episode link
     */
    private fun isEpisodeLink(text: String): Boolean {
        val lowerText = text.lowercase()

        // Common episode patterns
        val patterns = listOf(
            "episode \\d+".toRegex(),
            "ep\\s*\\d+".toRegex(),
            "e\\d+".toRegex(),
            "第\\d+話".toRegex(),  // Japanese
            "\\d+話".toRegex(),
            "chapter \\d+".toRegex(),
            "\\d+ - .*".toRegex(),  // "12 - Episode Title"
            ".*\\d{1,3}\\s*$".toRegex()  // Ends with episode number
        )

        return patterns.any { it.containsMatchIn(lowerText) }
    }

    /**
     * Extract episode name from text
     */
    private fun extractEpisodeName(text: String): String {
        // Clean up common prefixes/suffixes
        var name = text.trim()
            .replace("Watch ", "", ignoreCase = true)
            .replace("Download ", "", ignoreCase = true)
            .replace("Stream ", "", ignoreCase = true)
            .trim()

        // If it's too long, try to extract just the episode number and title
        if (name.length > 50) {
            val episodeMatch = "episode\\s+(\\d+)".toRegex(RegexOption.IGNORE_CASE).find(name)
            if (episodeMatch != null) {
                name = "Episode ${episodeMatch.groupValues[1]}"
            }
        }

        return name.take(100)  // Limit length
    }

    /**
     * Extract episode number for sorting
     */
    private fun extractEpisodeNumber(episodeName: String): Int {
        val patterns = listOf(
            "episode\\s+(\\d+)".toRegex(RegexOption.IGNORE_CASE),
            "ep\\s*(\\d+)".toRegex(RegexOption.IGNORE_CASE),
            "e(\\d+)".toRegex(RegexOption.IGNORE_CASE),
            "第(\\d+)話".toRegex(),
            "(\\d+)話".toRegex(),
            "chapter\\s+(\\d+)".toRegex(RegexOption.IGNORE_CASE),
            "^(\\d+)".toRegex(),  // Starts with number
            "(\\d+)\\s*$".toRegex()  // Ends with number
        )

        for (pattern in patterns) {
            val match = pattern.find(episodeName)
            if (match != null) {
                return match.groupValues[1].toIntOrNull() ?: 0
            }
        }

        return 0
    }

    /**
     * Extract URL from node
     * This is tricky as accessibility doesn't directly expose URLs
     * We'll use heuristics and content description
     */
    private fun extractUrl(node: AccessibilityNodeInfo, text: String): String? {
        // Method 1: Check content description (some sites put URLs there)
        val contentDesc = node.contentDescription?.toString()
        if (contentDesc != null && contentDesc.startsWith("http")) {
            return contentDesc
        }

        // Method 2: Check for data attributes or hints
        val hint = node.hintText?.toString()
        if (hint != null && hint.startsWith("http")) {
            return hint
        }

        // Method 3: Try to find associated URL in parent or siblings
        node.parent?.let { parent ->
            for (i in 0 until parent.childCount) {
                parent.getChild(i)?.let { sibling ->
                    val siblingText = sibling.text?.toString() ?: sibling.contentDescription?.toString()
                    if (siblingText != null && siblingText.startsWith("http")) {
                        sibling.recycle()
                        return siblingText
                    }
                    sibling.recycle()
                }
            }
        }

        // Method 4: Generate synthetic URL from pattern (fallback for testing)
        // This will need to be customized based on the actual anime site
        // For now, return null to avoid false positives
        return null
    }

    /**
     * Find series title from page
     */
    private fun findSeriesTitle(node: AccessibilityNodeInfo, depth: Int = 0): String {
        if (depth > 20) return ""

        // Look for title-like elements
        val text = node.text?.toString() ?: ""
        val className = node.className?.toString() ?: ""

        // Check if this looks like a heading or title
        if (className.contains("Heading", ignoreCase = true) ||
            className.contains("Title", ignoreCase = true) ||
            node.viewIdResourceName?.contains("title", ignoreCase = true) == true
        ) {
            if (text.isNotBlank() && text.length > 3 && text.length < 200) {
                return text
            }
        }

        // Recurse through children
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                val result = findSeriesTitle(child, depth + 1)
                child.recycle()
                if (result.isNotBlank()) {
                    return result
                }
            }
        }

        return ""
    }

    /**
     * Clean series name for use as folder name
     */
    private fun cleanSeriesName(name: String): String {
        return name
            .replace("[^a-zA-Z0-9\\s-]".toRegex(), "")  // Remove special chars
            .replace("\\s+".toRegex(), " ")  // Normalize spaces
            .trim()
            .take(100)  // Limit length
    }

    /**
     * Extract video URLs from current page using more aggressive methods
     * This is a fallback method that looks for video player elements
     */
    fun extractVideoUrls(service: AutomationAccessibilityService): List<String> {
        val rootNode = service.rootInActiveWindow ?: return emptyList()
        val urls = mutableListOf<String>()

        findVideoUrlsRecursive(rootNode, urls)

        return urls.distinct()
    }

    private fun findVideoUrlsRecursive(node: AccessibilityNodeInfo, urls: MutableList<String>, depth: Int = 0) {
        if (depth > 30) return

        // Look for video player elements
        val className = node.className?.toString() ?: ""
        val viewId = node.viewIdResourceName ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""

        if (className.contains("video", ignoreCase = true) ||
            viewId.contains("video", ignoreCase = true) ||
            viewId.contains("player", ignoreCase = true)
        ) {
            // Try to extract URL from various attributes
            listOf(contentDesc, node.text?.toString(), node.hintText?.toString()).forEach { text ->
                if (text != null && (text.startsWith("http") || text.contains(".mp4") || text.contains(".m3u8"))) {
                    urls.add(text)
                }
            }
        }

        // Recurse
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                findVideoUrlsRecursive(child, urls, depth + 1)
                child.recycle()
            }
        }
    }
}
