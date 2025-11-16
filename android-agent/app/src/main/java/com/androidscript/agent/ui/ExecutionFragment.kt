package com.androidscript.agent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.androidscript.agent.R
import com.androidscript.agent.download.LinkExtractor
import com.androidscript.agent.download.SeriesDownloadManager
import com.androidscript.agent.models.ExecutionStatus
import com.androidscript.agent.models.ExecutionState
import com.androidscript.agent.service.AutomationAccessibilityService
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Fragment for script execution controls and status
 */
class ExecutionFragment : Fragment() {

    private lateinit var statusCard: MaterialCardView
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var outputText: TextView
    private lateinit var runButton: Button
    private lateinit var stopButton: Button
    private lateinit var pauseButton: Button
    private lateinit var downloadSeriesButton: Button

    private var executionState = ExecutionState()
    private var downloadManager: SeriesDownloadManager? = null
    private val linkExtractor = LinkExtractor()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_execution, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusCard = view.findViewById(R.id.statusCard)
        statusText = view.findViewById(R.id.statusText)
        progressBar = view.findViewById(R.id.progressBar)
        outputText = view.findViewById(R.id.outputText)
        runButton = view.findViewById(R.id.runButton)
        stopButton = view.findViewById(R.id.stopButton)
        pauseButton = view.findViewById(R.id.pauseButton)
        downloadSeriesButton = view.findViewById(R.id.downloadSeriesButton)

        setupButtons()
        updateUI()
    }

    private fun setupButtons() {
        runButton.setOnClickListener {
            // TODO: Start script execution
            updateExecutionState(executionState.copy(status = ExecutionStatus.RUNNING))
        }

        stopButton.setOnClickListener {
            // TODO: Stop script execution
            updateExecutionState(executionState.copy(status = ExecutionStatus.STOPPED))
        }

        pauseButton.setOnClickListener {
            // TODO: Pause/Resume script execution
            if (executionState.isPaused) {
                updateExecutionState(executionState.copy(status = ExecutionStatus.RUNNING))
            } else {
                updateExecutionState(executionState.copy(status = ExecutionStatus.PAUSED))
            }
        }

        downloadSeriesButton.setOnClickListener {
            startSeriesDownload()
        }
    }

    /**
     * Start downloading anime series from current Firefox page
     */
    private fun startSeriesDownload() {
        // Get accessibility service
        val service = AutomationAccessibilityService.getInstance()
        if (service == null) {
            Toast.makeText(
                requireContext(),
                "Accessibility service not enabled",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Extract series name
        val seriesName = linkExtractor.extractSeriesName(service)
        appendOutput("Extracting episodes from: $seriesName\n")

        // Extract episode links
        val episodes = linkExtractor.extractEpisodeLinks(service)

        if (episodes.isEmpty()) {
            appendOutput("No episodes found. Make sure you're on an anime series page.\n")
            Toast.makeText(
                requireContext(),
                "No episodes found on this page",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        appendOutput("Found ${episodes.size} episodes\n")

        // Show confirmation dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Download Series")
            .setMessage("Download $seriesName?\n\n${episodes.size} episodes found\n\nEpisodes will be downloaded 5 at a time with auto-retry.")
            .setPositiveButton("Download") { _, _ ->
                beginDownload(episodes, seriesName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Begin the download process
     */
    private fun beginDownload(episodes: List<Pair<String, String>>, seriesName: String) {
        // Initialize download manager if needed
        if (downloadManager == null) {
            downloadManager = SeriesDownloadManager(requireContext())
        }

        // Setup callbacks
        downloadManager?.apply {
            onProgress = { current, total, episode ->
                requireActivity().runOnUiThread {
                    val progress = if (total > 0) current.toFloat() / total else 0f
                    progressBar.isIndeterminate = false
                    progressBar.progress = (progress * 100).toInt()
                    statusText.text = "Downloading: $episode ($current/$total)"
                }
            }

            onEpisodeComplete = { episode, success ->
                requireActivity().runOnUiThread {
                    if (success) {
                        appendOutput("✓ Downloaded: $episode\n")
                    } else {
                        appendOutput("✗ Failed: $episode\n")
                    }
                }
            }

            onComplete = { successful, failed ->
                requireActivity().runOnUiThread {
                    appendOutput("\n=== Download Complete ===\n")
                    appendOutput("Successful: $successful\n")
                    appendOutput("Failed: $failed\n")

                    progressBar.progress = 0
                    statusText.text = "Download complete"
                    downloadSeriesButton.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        "Download complete: $successful successful, $failed failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Start download
        downloadSeriesButton.isEnabled = false
        statusText.text = "Starting download..."
        appendOutput("\n=== Starting Download ===\n")
        appendOutput("Series: $seriesName\n")
        appendOutput("Episodes: ${episodes.size}\n")
        appendOutput("Concurrent downloads: 5\n\n")

        downloadManager?.downloadSeries(episodes, seriesName)
    }

    /**
     * Append text to output console
     */
    private fun appendOutput(text: String) {
        outputText.append(text)
        // Auto-scroll to bottom
        val scrollView = outputText.parent as? android.widget.ScrollView
        scrollView?.post {
            scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel downloads if still running
        downloadManager?.cancelAll()
        downloadManager?.cleanup()
        downloadManager = null
    }

    private fun updateExecutionState(newState: ExecutionState) {
        executionState = newState
        updateUI()
    }

    private fun updateUI() {
        // Update status text
        statusText.text = when (executionState.status) {
            ExecutionStatus.IDLE -> getString(R.string.script_stopped)
            ExecutionStatus.RUNNING -> getString(R.string.script_running)
            ExecutionStatus.PAUSED -> getString(R.string.script_paused)
            ExecutionStatus.COMPLETED -> getString(R.string.script_completed)
            ExecutionStatus.ERROR -> getString(R.string.script_error)
            ExecutionStatus.STOPPED -> getString(R.string.script_stopped)
        }

        // Update status card color
        val statusColor = when (executionState.status) {
            ExecutionStatus.RUNNING -> R.color.status_running
            ExecutionStatus.PAUSED -> R.color.status_paused
            ExecutionStatus.COMPLETED -> R.color.status_completed
            ExecutionStatus.ERROR -> R.color.status_error
            else -> R.color.status_stopped
        }
        statusCard.setCardBackgroundColor(requireContext().getColor(statusColor))

        // Update progress bar
        if (executionState.isRunning) {
            progressBar.isIndeterminate = false
            progressBar.progress = (executionState.progress * 100).toInt()
        } else {
            progressBar.isIndeterminate = false
            progressBar.progress = 0
        }

        // Update buttons
        runButton.isEnabled = !executionState.isRunning && !executionState.isPaused
        stopButton.isEnabled = executionState.isRunning || executionState.isPaused
        pauseButton.isEnabled = executionState.isRunning || executionState.isPaused
        pauseButton.text = if (executionState.isPaused) {
            getString(R.string.resume_script)
        } else {
            getString(R.string.pause_script)
        }

        // Update output
        outputText.text = executionState.output
        if (executionState.hasError) {
            outputText.append("\n\nError: ${executionState.error}")
        }
    }
}
