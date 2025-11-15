package com.androidscript.agent.utils

import android.content.Context
import android.provider.Settings
import android.text.TextUtils

/**
 * Utility functions for checking accessibility service status
 */
object AccessibilityUtils {

    /**
     * Check if AndroidScript accessibility service is enabled
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val serviceName = "${context.packageName}/com.androidscript.agent.service.AutomationAccessibilityService"

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        return if (!TextUtils.isEmpty(enabledServices)) {
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)

            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(serviceName, ignoreCase = true)) {
                    return true
                }
            }
            false
        } else {
            false
        }
    }
}
