package com.androidscript.agent.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * SeriesDownloadManager handles concurrent episode downloads
 * Features:
 * - Max 5 concurrent downloads
 * - Auto-retry on failure
 * - Auto-resume on network issues
 * - Download progress tracking
 */
class SeriesDownloadManager(private val context: Context) {

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Track active downloads
    private val activeDownloads = ConcurrentHashMap<Long, EpisodeDownload>()
    private val downloadQueue = ArrayDeque<EpisodeDownload>()
    private val activeCount = AtomicInteger(0)
    private val completedEpisodes = mutableSetOf<String>()

    // Configuration
    private val maxConcurrentDownloads = 5
    private val retryDelayMs = 3000L  // 3 seconds
    private val maxRetries = 5
    private val hangTimeoutMs = 120000L  // 2 minutes

    // Callbacks
    var onProgress: ((current: Int, total: Int, episode: String) -> Unit)? = null
    var onComplete: ((successful: Int, failed: Int) -> Unit)? = null
    var onEpisodeComplete: ((episode: String, success: Boolean) -> Unit)? = null

    private var isRunning = false
    private var isCancelled = false

    companion object {
        private const val TAG = "SeriesDownloadManager"
    }

    data class EpisodeDownload(
        val episodeName: String,
        val url: String,
        val fileName: String,
        var downloadId: Long? = null,
        var retryCount: Int = 0,
        var lastAttemptTime: Long = 0,
        var status: DownloadStatus = DownloadStatus.QUEUED
    )

    enum class DownloadStatus {
        QUEUED,
        DOWNLOADING,
        COMPLETED,
        FAILED,
        CANCELLED,
        RETRYING
    }

    // Broadcast receiver for download completion
    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == -1L) return

            val download = activeDownloads[downloadId] ?: return

            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)

            if (cursor.moveToFirst()) {
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(statusIndex)

                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        handleDownloadSuccess(download)
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
                        val reason = cursor.getInt(reasonIndex)
                        handleDownloadFailure(download, reason)
                    }
                }
            }
            cursor.close()
        }
    }

    init {
        // Register download completion receiver
        context.registerReceiver(
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }

    /**
     * Start downloading a series of episodes
     * @param episodes List of episode URLs with names
     * @param seriesName Name of the series for folder organization
     */
    fun downloadSeries(episodes: List<Pair<String, String>>, seriesName: String) {
        if (isRunning) {
            Log.w(TAG, "Download already in progress")
            return
        }

        isRunning = true
        isCancelled = false
        downloadQueue.clear()
        activeDownloads.clear()
        completedEpisodes.clear()
        activeCount.set(0)

        // Create download tasks
        episodes.forEachIndexed { index, (episodeName, url) ->
            val fileName = "$seriesName - $episodeName.mp4"
            downloadQueue.add(EpisodeDownload(episodeName, url, fileName))
        }

        Log.i(TAG, "Starting series download: $seriesName (${episodes.size} episodes)")

        // Start download workers
        scope.launch {
            processDownloadQueue()
        }

        // Start progress monitor
        scope.launch {
            monitorDownloads()
        }
    }

    /**
     * Process the download queue maintaining max concurrent downloads
     */
    private suspend fun processDownloadQueue() {
        while (!isCancelled && (downloadQueue.isNotEmpty() || activeCount.get() > 0)) {
            // Start new downloads if under limit
            while (activeCount.get() < maxConcurrentDownloads && downloadQueue.isNotEmpty()) {
                val download = downloadQueue.removeFirst()
                startDownload(download)
            }

            delay(500)  // Check every 500ms
        }

        // All downloads complete
        if (!isCancelled) {
            val successful = completedEpisodes.size
            val failed = activeDownloads.values.count { it.status == DownloadStatus.FAILED }
            onComplete?.invoke(successful, failed)
            Log.i(TAG, "Series download complete: $successful successful, $failed failed")
        }

        isRunning = false
    }

    /**
     * Start downloading an episode
     */
    private fun startDownload(download: EpisodeDownload) {
        try {
            val request = DownloadManager.Request(Uri.parse(download.url))
                .setTitle(download.episodeName)
                .setDescription("Downloading episode...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "AndroidScript/${download.fileName}"
                )
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadId = downloadManager.enqueue(request)
            download.downloadId = downloadId
            download.status = DownloadStatus.DOWNLOADING
            download.lastAttemptTime = System.currentTimeMillis()

            activeDownloads[downloadId] = download
            activeCount.incrementAndGet()

            Log.d(TAG, "Started download: ${download.episodeName} (ID: $downloadId)")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start download: ${download.episodeName}", e)
            handleDownloadFailure(download, -1)
        }
    }

    /**
     * Handle successful download
     */
    private fun handleDownloadSuccess(download: EpisodeDownload) {
        download.status = DownloadStatus.COMPLETED
        completedEpisodes.add(download.episodeName)

        download.downloadId?.let { activeDownloads.remove(it) }
        activeCount.decrementAndGet()

        onEpisodeComplete?.invoke(download.episodeName, true)
        Log.i(TAG, "Download complete: ${download.episodeName}")
    }

    /**
     * Handle failed download with retry logic
     */
    private fun handleDownloadFailure(download: EpisodeDownload, reason: Int) {
        download.downloadId?.let { activeDownloads.remove(it) }
        activeCount.decrementAndGet()

        if (download.retryCount < maxRetries && !isCancelled) {
            download.retryCount++
            download.status = DownloadStatus.RETRYING

            Log.w(TAG, "Download failed: ${download.episodeName} (reason: $reason), retry ${download.retryCount}/$maxRetries")

            // Schedule retry
            scope.launch {
                delay(retryDelayMs)
                if (!isCancelled) {
                    downloadQueue.addFirst(download)  // Priority for retries
                }
            }
        } else {
            download.status = DownloadStatus.FAILED
            onEpisodeComplete?.invoke(download.episodeName, false)
            Log.e(TAG, "Download permanently failed: ${download.episodeName}")
        }
    }

    /**
     * Monitor downloads for hangs and progress
     */
    private suspend fun monitorDownloads() {
        while (!isCancelled && isRunning) {
            val now = System.currentTimeMillis()

            activeDownloads.values.forEach { download ->
                val downloadId = download.downloadId ?: return@forEach

                // Check for hung downloads
                if (now - download.lastAttemptTime > hangTimeoutMs) {
                    Log.w(TAG, "Download hung: ${download.episodeName}, cancelling...")
                    downloadManager.remove(downloadId)
                    handleDownloadFailure(download, DownloadManager.ERROR_UNKNOWN)
                }

                // Update progress
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                    val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                    val bytesTotal = cursor.getLong(bytesTotalIndex)

                    if (bytesDownloaded > 0) {
                        // Download is progressing, update timestamp
                        download.lastAttemptTime = now
                    }
                }
                cursor.close()
            }

            // Report overall progress
            val total = completedEpisodes.size + activeDownloads.size + downloadQueue.size
            val current = completedEpisodes.size
            val currentEpisode = activeDownloads.values.firstOrNull()?.episodeName ?: ""

            onProgress?.invoke(current, total, currentEpisode)

            delay(2000)  // Check every 2 seconds
        }
    }

    /**
     * Cancel all downloads
     */
    fun cancelAll() {
        isCancelled = true

        activeDownloads.keys.forEach { downloadId ->
            downloadManager.remove(downloadId)
        }

        activeDownloads.clear()
        downloadQueue.clear()
        activeCount.set(0)
        isRunning = false

        Log.i(TAG, "All downloads cancelled")
    }

    /**
     * Get download statistics
     */
    fun getStats(): DownloadStats {
        return DownloadStats(
            total = completedEpisodes.size + activeDownloads.size + downloadQueue.size,
            completed = completedEpisodes.size,
            active = activeCount.get(),
            queued = downloadQueue.size,
            failed = activeDownloads.values.count { it.status == DownloadStatus.FAILED }
        )
    }

    data class DownloadStats(
        val total: Int,
        val completed: Int,
        val active: Int,
        val queued: Int,
        val failed: Int
    )

    /**
     * Cleanup resources
     */
    fun cleanup() {
        try {
            context.unregisterReceiver(downloadReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        scope.cancel()
    }
}
