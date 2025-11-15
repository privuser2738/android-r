package com.androidscript.agent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.androidscript.agent.ui.ExecutionFragment
import com.androidscript.agent.ui.ScriptListFragment
import com.androidscript.agent.ui.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Main Activity with tabbed interface for scripts, execution, and settings
 */
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 1001
        private const val REQUEST_OVERLAY_PERMISSION = 1002
        private const val REQUEST_ACCESSIBILITY_SETTINGS = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
        checkPermissions()
    }

    private fun setupUI() {
        // Setup ViewPager2 with fragments
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ViewPagerAdapter(this)

        // Setup BottomNavigationView
        bottomNav = findViewById(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_scripts -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_execution -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_settings -> {
                    viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }

        // Sync ViewPager with BottomNavigationView
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun checkPermissions() {
        // Check storage permission (for Android 10 and below)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    REQUEST_STORAGE_PERMISSION
                )
            }
        } else {
            // Android 11+ - check MANAGE_EXTERNAL_STORAGE if needed
            if (!Environment.isExternalStorageManager()) {
                // For now, just use app-specific storage
                // Can request MANAGE_EXTERNAL_STORAGE if needed
            }
        }

        // Check overlay permission (for floating controls)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // Can request overlay permission if needed
                // val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                //     Uri.parse("package:$packageName"))
                // startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                } else {
                    // Permission denied - show explanation
                }
            }
        }
    }

    /**
     * ViewPager adapter for fragments
     */
    private class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ScriptListFragment()
                1 -> ExecutionFragment()
                2 -> SettingsFragment()
                else -> ScriptListFragment()
            }
        }
    }
}
