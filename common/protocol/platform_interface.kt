package com.androidscript.common.protocol

/**
 * Platform-agnostic automation interface
 * Implemented by Android, iOS, and other platform agents
 */
interface PlatformBridge {

    // ========================================================================
    // Device Information
    // ========================================================================

    /**
     * Get device information
     */
    fun getDeviceInfo(): DeviceInfo

    /**
     * Get platform name
     */
    fun getPlatform(): Platform

    // ========================================================================
    // UI Automation - Core Actions
    // ========================================================================

    /**
     * Tap at screen coordinates
     * @param x X coordinate
     * @param y Y coordinate
     * @return Success status
     */
    fun tap(x: Int, y: Int): Boolean

    /**
     * Long press at coordinates
     * @param x X coordinate
     * @param y Y coordinate
     * @param duration Duration in milliseconds
     * @return Success status
     */
    fun longPress(x: Int, y: Int, duration: Long = 1000): Boolean

    /**
     * Swipe gesture
     * @param x1 Start X
     * @param y1 Start Y
     * @param x2 End X
     * @param y2 End Y
     * @param duration Duration in milliseconds
     * @return Success status
     */
    fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Long = 300): Boolean

    /**
     * Pinch gesture (zoom in/out)
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     * @param scale Scale factor (>1 = zoom in, <1 = zoom out)
     * @return Success status
     */
    fun pinch(centerX: Int, centerY: Int, scale: Float): Boolean

    /**
     * Drag from one element to another
     * @param fromElement Source element ID
     * @param toElement Target element ID
     * @param duration Duration in milliseconds
     * @return Success status
     */
    fun drag(fromElement: String, toElement: String, duration: Long = 500): Boolean

    // ========================================================================
    // UI Automation - Element Finding
    // ========================================================================

    /**
     * Find element by text content
     * @param text Text to search for
     * @param exact Exact match or contains
     * @return Element ID or null
     */
    fun findByText(text: String, exact: Boolean = false): UIElement?

    /**
     * Find element by accessibility ID/resource ID
     * @param id Accessibility identifier
     * @return Element ID or null
     */
    fun findById(id: String): UIElement?

    /**
     * Find element by type/class
     * @param type Element type (Button, TextField, etc.)
     * @return List of matching elements
     */
    fun findByType(type: String): List<UIElement>

    /**
     * Find element by content description/accessibility label
     * @param description Content description
     * @return Element ID or null
     */
    fun findByContentDescription(description: String): UIElement?

    /**
     * Find element by XPath (if supported)
     * @param xpath XPath expression
     * @return List of matching elements
     */
    fun findByXPath(xpath: String): List<UIElement>

    /**
     * Get all elements on screen
     * @return List of all visible elements
     */
    fun getAllElements(): List<UIElement>

    // ========================================================================
    // UI Automation - Element Interaction
    // ========================================================================

    /**
     * Click on an element
     * @param element Element to click
     * @return Success status
     */
    fun clickElement(element: UIElement): Boolean

    /**
     * Long click on an element
     * @param element Element to long click
     * @param duration Duration in milliseconds
     * @return Success status
     */
    fun longClickElement(element: UIElement, duration: Long = 1000): Boolean

    /**
     * Input text into an element
     * @param element Target element (must be editable)
     * @param text Text to input
     * @return Success status
     */
    fun inputText(element: UIElement, text: String): Boolean

    /**
     * Clear text from an element
     * @param element Target element
     * @return Success status
     */
    fun clearText(element: UIElement): Boolean

    /**
     * Get text from an element
     * @param element Source element
     * @return Element text
     */
    fun getText(element: UIElement): String?

    /**
     * Get element bounds/position
     * @param element Target element
     * @return Bounding rectangle
     */
    fun getBounds(element: UIElement): Bounds

    /**
     * Check if element is visible
     * @param element Target element
     * @return Visibility status
     */
    fun isVisible(element: UIElement): Boolean

    /**
     * Scroll element into view
     * @param element Target element
     * @return Success status
     */
    fun scrollIntoView(element: UIElement): Boolean

    /**
     * Scroll element up/down/left/right
     * @param element Scrollable element
     * @param direction Scroll direction
     * @return Success status
     */
    fun scroll(element: UIElement, direction: ScrollDirection): Boolean

    // ========================================================================
    // System Actions
    // ========================================================================

    /**
     * Press home button
     */
    fun pressHome(): Boolean

    /**
     * Press back button (Android) / swipe back (iOS)
     */
    fun pressBack(): Boolean

    /**
     * Open app switcher/recents
     */
    fun pressRecents(): Boolean

    /**
     * Open notifications
     */
    fun openNotifications(): Boolean

    /**
     * Open quick settings
     */
    fun openQuickSettings(): Boolean

    /**
     * Lock screen
     */
    fun lockScreen(): Boolean

    /**
     * Unlock screen
     */
    fun unlockScreen(): Boolean

    /**
     * Take screenshot
     * @return Screenshot data (base64 or file path)
     */
    fun takeScreenshot(): Screenshot

    /**
     * Get screen dimensions
     */
    fun getScreenSize(): ScreenSize

    /**
     * Set screen orientation
     * @param orientation Portrait or Landscape
     */
    fun setOrientation(orientation: Orientation): Boolean

    // ========================================================================
    // Application Control
    // ========================================================================

    /**
     * Launch app by package/bundle ID
     * @param packageId App identifier
     * @return Success status
     */
    fun launchApp(packageId: String): Boolean

    /**
     * Close/kill app
     * @param packageId App identifier
     * @return Success status
     */
    fun closeApp(packageId: String): Boolean

    /**
     * Check if app is installed
     * @param packageId App identifier
     * @return Installation status
     */
    fun isAppInstalled(packageId: String): Boolean

    /**
     * Get list of installed apps
     * @return List of app info
     */
    fun getInstalledApps(): List<AppInfo>

    /**
     * Get current app package/bundle ID
     * @return Current app identifier
     */
    fun getCurrentApp(): String?

    // ========================================================================
    // Advanced Features
    // ========================================================================

    /**
     * Execute JavaScript (for WebViews)
     * @param script JavaScript code
     * @return Execution result
     */
    fun executeJavaScript(script: String): String?

    /**
     * Get page source / UI hierarchy
     * @return XML/JSON representation of UI
     */
    fun getPageSource(): String

    /**
     * Wait for element with timeout
     * @param selector Element selector
     * @param timeout Timeout in milliseconds
     * @return Found element or null
     */
    fun waitForElement(selector: ElementSelector, timeout: Long): UIElement?

    /**
     * Wait for condition
     * @param condition Condition function
     * @param timeout Timeout in milliseconds
     * @return Success status
     */
    fun waitForCondition(condition: () -> Boolean, timeout: Long): Boolean
}

// ========================================================================
// Data Models
// ========================================================================

enum class Platform {
    ANDROID,
    IOS,
    HARMONYOS,
    WEB,
    UNKNOWN
}

data class DeviceInfo(
    val platform: Platform,
    val model: String,
    val version: String,
    val serial: String,
    val manufacturer: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val dpi: Int,
    val language: String,
    val region: String
)

data class UIElement(
    val id: String,
    val text: String?,
    val contentDescription: String?,
    val className: String?,
    val resourceId: String?,
    val bounds: Bounds,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isScrollable: Boolean,
    val isEnabled: Boolean,
    val isVisible: Boolean,
    val attributes: Map<String, String> = emptyMap()
)

data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val centerX: Int get() = left + width / 2
    val centerY: Int get() = top + height / 2
}

data class ScreenSize(
    val width: Int,
    val height: Int,
    val density: Float
)

data class Screenshot(
    val data: ByteArray,
    val format: ImageFormat,
    val width: Int,
    val height: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Screenshot
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int = data.contentHashCode()
}

enum class ImageFormat {
    PNG, JPEG, BMP
}

data class AppInfo(
    val packageId: String,
    val name: String,
    val version: String,
    val icon: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AppInfo
        if (packageId != other.packageId) return false
        if (name != other.name) return false
        if (version != other.version) return false
        if (icon != null) {
            if (other.icon == null) return false
            if (!icon.contentEquals(other.icon)) return false
        } else if (other.icon != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = packageId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + (icon?.contentHashCode() ?: 0)
        return result
    }
}

enum class ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}

enum class Orientation {
    PORTRAIT, LANDSCAPE
}

data class ElementSelector(
    val byText: String? = null,
    val byId: String? = null,
    val byType: String? = null,
    val byContentDesc: String? = null,
    val byXPath: String? = null
)
