package com.androidscript.agent.ui

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidscript.agent.R
import com.androidscript.agent.models.Script
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

/**
 * Fragment for displaying and managing script files
 */
class ScriptListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private val scripts = mutableListOf<Script>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_script_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            createNewScript()
        }

        loadScripts()
    }

    private fun loadScripts() {
        scripts.clear()

        // Load scripts from app-specific storage
        val scriptsDir = File(requireContext().filesDir, "scripts")
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs()
        }

        // Also check external storage if available
        val externalDir = requireContext().getExternalFilesDir("scripts")
        externalDir?.let {
            if (it.exists()) {
                it.listFiles()?.filter { file -> file.extension == "as" }?.forEach { file ->
                    scripts.add(Script.fromFile(file))
                }
            }
        }

        // Load from internal storage
        scriptsDir.listFiles()?.filter { file -> file.extension == "as" }?.forEach { file ->
            scripts.add(Script.fromFile(file))
        }

        // Update adapter (we'll create this later)
        // recyclerView.adapter = ScriptAdapter(scripts)

        if (scripts.isEmpty()) {
            // Show empty state
            Toast.makeText(requireContext(), R.string.no_scripts, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNewScript() {
        // TODO: Show dialog to create new script
        Toast.makeText(requireContext(), "Create new script", Toast.LENGTH_SHORT).show()
    }
}
