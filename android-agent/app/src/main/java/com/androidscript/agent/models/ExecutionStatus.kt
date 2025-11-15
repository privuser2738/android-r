package com.androidscript.agent.models

/**
 * Represents the current execution status of a script
 */
enum class ExecutionStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    ERROR,
    STOPPED
}

/**
 * Execution state with details
 */
data class ExecutionState(
    val status: ExecutionStatus = ExecutionStatus.IDLE,
    val currentLine: Int = 0,
    val totalLines: Int = 0,
    val output: String = "",
    val error: String? = null,
    val startTime: Long = 0,
    val endTime: Long = 0
) {
    val isRunning: Boolean
        get() = status == ExecutionStatus.RUNNING

    val isPaused: Boolean
        get() = status == ExecutionStatus.PAUSED

    val isCompleted: Boolean
        get() = status == ExecutionStatus.COMPLETED || status == ExecutionStatus.STOPPED

    val hasError: Boolean
        get() = status == ExecutionStatus.ERROR

    val duration: Long
        get() = if (endTime > 0) endTime - startTime else System.currentTimeMillis() - startTime

    val progress: Float
        get() = if (totalLines > 0) currentLine.toFloat() / totalLines.toFloat() else 0f
}
