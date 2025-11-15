package com.androidscript.agent.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Gesture controller for performing touch gestures
 */
class GestureController(private val service: AccessibilityService) {

    companion object {
        private const val TAG = "GestureController"
        private const val DEFAULT_GESTURE_DURATION = 100L
        private const val LONG_PRESS_DURATION = 1000L
        private const val SWIPE_DURATION = 300L
        private const val GESTURE_TIMEOUT = 5000L
    }

    /**
     * Perform a tap gesture at coordinates
     */
    fun tap(x: Float, y: Float, duration: Long = DEFAULT_GESTURE_DURATION): Boolean {
        Log.d(TAG, "Tap at ($x, $y)")

        val path = Path()
        path.moveTo(x, y)

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))

        return dispatchGesture(gestureBuilder.build())
    }

    /**
     * Perform a long press gesture at coordinates
     */
    fun longPress(x: Float, y: Float): Boolean {
        Log.d(TAG, "Long press at ($x, $y)")
        return tap(x, y, LONG_PRESS_DURATION)
    }

    /**
     * Perform a swipe gesture from (x1, y1) to (x2, y2)
     */
    fun swipe(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        duration: Long = SWIPE_DURATION
    ): Boolean {
        Log.d(TAG, "Swipe from ($x1, $y1) to ($x2, $y2)")

        val path = Path()
        path.moveTo(x1, y1)
        path.lineTo(x2, y2)

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))

        return dispatchGesture(gestureBuilder.build())
    }

    /**
     * Perform a swipe up gesture
     */
    fun swipeUp(startX: Float, startY: Float, distance: Float, duration: Long = SWIPE_DURATION): Boolean {
        return swipe(startX, startY, startX, startY - distance, duration)
    }

    /**
     * Perform a swipe down gesture
     */
    fun swipeDown(startX: Float, startY: Float, distance: Float, duration: Long = SWIPE_DURATION): Boolean {
        return swipe(startX, startY, startX, startY + distance, duration)
    }

    /**
     * Perform a swipe left gesture
     */
    fun swipeLeft(startX: Float, startY: Float, distance: Float, duration: Long = SWIPE_DURATION): Boolean {
        return swipe(startX, startY, startX - distance, startY, duration)
    }

    /**
     * Perform a swipe right gesture
     */
    fun swipeRight(startX: Float, startY: Float, distance: Float, duration: Long = SWIPE_DURATION): Boolean {
        return swipe(startX, startY, startX + distance, startY, duration)
    }

    /**
     * Perform a pinch gesture (zoom in/out)
     */
    fun pinch(
        centerX: Float,
        centerY: Float,
        startSpacing: Float,
        endSpacing: Float,
        duration: Long = SWIPE_DURATION
    ): Boolean {
        Log.d(TAG, "Pinch at ($centerX, $centerY) from $startSpacing to $endSpacing")

        // Calculate finger positions
        val startOffset = startSpacing / 2
        val endOffset = endSpacing / 2

        // First finger path (top to bottom or vice versa)
        val path1 = Path()
        path1.moveTo(centerX, centerY - startOffset)
        path1.lineTo(centerX, centerY - endOffset)

        // Second finger path (bottom to top or vice versa)
        val path2 = Path()
        path2.moveTo(centerX, centerY + startOffset)
        path2.lineTo(centerX, centerY + endOffset)

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path1, 0, duration))
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path2, 0, duration))

        return dispatchGesture(gestureBuilder.build())
    }

    /**
     * Perform a zoom in gesture (pinch outward)
     */
    fun zoomIn(
        centerX: Float,
        centerY: Float,
        spacing: Float = 200f,
        duration: Long = SWIPE_DURATION
    ): Boolean {
        return pinch(centerX, centerY, spacing / 2, spacing, duration)
    }

    /**
     * Perform a zoom out gesture (pinch inward)
     */
    fun zoomOut(
        centerX: Float,
        centerY: Float,
        spacing: Float = 200f,
        duration: Long = SWIPE_DURATION
    ): Boolean {
        return pinch(centerX, centerY, spacing, spacing / 2, duration)
    }

    /**
     * Perform a drag gesture from (x1, y1) to (x2, y2)
     * Similar to swipe but with longer duration for drag effect
     */
    fun drag(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        duration: Long = 1000L
    ): Boolean {
        Log.d(TAG, "Drag from ($x1, $y1) to ($x2, $y2)")
        return swipe(x1, y1, x2, y2, duration)
    }

    /**
     * Perform a custom multi-touch gesture
     */
    fun multiTouch(paths: List<Pair<Path, Long>>, startTime: Long = 0): Boolean {
        Log.d(TAG, "Multi-touch gesture with ${paths.size} strokes")

        val gestureBuilder = GestureDescription.Builder()

        paths.forEach { (path, duration) ->
            gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, startTime, duration))
        }

        return dispatchGesture(gestureBuilder.build())
    }

    /**
     * Perform a double tap gesture
     */
    fun doubleTap(x: Float, y: Float): Boolean {
        Log.d(TAG, "Double tap at ($x, $y)")

        // First tap
        if (!tap(x, y)) {
            return false
        }

        // Small delay between taps
        Thread.sleep(100)

        // Second tap
        return tap(x, y)
    }

    /**
     * Draw a custom path gesture
     */
    fun drawPath(path: Path, duration: Long = SWIPE_DURATION): Boolean {
        Log.d(TAG, "Drawing custom path")

        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))

        return dispatchGesture(gestureBuilder.build())
    }

    /**
     * Dispatch a gesture and wait for completion
     */
    private fun dispatchGesture(gesture: GestureDescription): Boolean {
        val latch = CountDownLatch(1)
        var success = false

        val callback = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Gesture completed successfully")
                success = true
                latch.countDown()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.w(TAG, "Gesture was cancelled")
                success = false
                latch.countDown()
            }
        }

        val dispatched = service.dispatchGesture(gesture, callback, null)
        if (!dispatched) {
            Log.e(TAG, "Failed to dispatch gesture")
            return false
        }

        // Wait for gesture to complete
        try {
            if (!latch.await(GESTURE_TIMEOUT, TimeUnit.MILLISECONDS)) {
                Log.e(TAG, "Gesture timed out")
                return false
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "Gesture interrupted", e)
            return false
        }

        return success
    }
}
