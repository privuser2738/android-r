package com.androidscript.agent.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.androidscript.agent.R
import com.androidscript.agent.utils.AccessibilityUtils

/**
 * Fragment for app settings and configuration
 */
class SettingsFragment : Fragment() {

    private lateinit var accessibilityStatusText: TextView
    private lateinit var accessibilityButton: Button
    private lateinit var networkSwitch: Switch

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accessibilityStatusText = view.findViewById(R.id.accessibilityStatusText)
        accessibilityButton = view.findViewById(R.id.accessibilityButton)
        networkSwitch = view.findViewById(R.id.networkSwitch)

        accessibilityButton.setOnClickListener {
            openAccessibilitySettings()
        }

        networkSwitch.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Start/stop network server
        }

        updateAccessibilityStatus()
    }

    override fun onResume() {
        super.onResume()
        updateAccessibilityStatus()
    }

    private fun updateAccessibilityStatus() {
        val isEnabled = AccessibilityUtils.isAccessibilityServiceEnabled(requireContext())

        accessibilityStatusText.text = if (isEnabled) {
            getString(R.string.accessibility_enabled)
        } else {
            getString(R.string.accessibility_disabled)
        }

        accessibilityStatusText.setTextColor(
            requireContext().getColor(
                if (isEnabled) R.color.status_running else R.color.status_error
            )
        )
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}
