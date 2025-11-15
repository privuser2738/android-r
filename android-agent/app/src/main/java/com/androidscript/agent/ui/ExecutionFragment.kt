package com.androidscript.agent.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.androidscript.agent.R
import com.androidscript.agent.models.ExecutionStatus
import com.androidscript.agent.models.ExecutionState
import com.google.android.material.card.MaterialCardView

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

    private var executionState = ExecutionState()

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
