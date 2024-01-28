package com.nf.fileurldownloader.downloader.dispatcher

import com.nf.fileurldownloader.downloader.client.HttpClient
import com.nf.fileurldownloader.downloader.config.DownloadStatus
import com.nf.fileurldownloader.downloader.request.DownloadRequest
import com.nf.fileurldownloader.downloader.request.DownloadTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * DownloadDispatcher handles the queuing and execution of download requests.
 *
 * @param httpClient The HTTP client used for making download requests.
 */
class DownloadDispatcher(private val httpClient: HttpClient) {

    // Coroutine scope for managing the download tasks
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Enqueues a download request for execution.
     *
     * @param req The download request to be enqueued.
     * @return The unique download ID associated with the request.
     */
    fun enqueue(req: DownloadRequest): Int {
        // Launch a coroutine for executing the download task
        val job = scope.launch {
            execute(req)
        }
        req.job = job
        return req.downloadId
    }

    /**
     * Executes the download task asynchronously.
     *
     * @param request The download request to be executed.
     */
    private suspend fun execute(request: DownloadRequest) {
        // Run the download task with callbacks for various states
        DownloadTask(request, httpClient).run(
            onStart = {
                executeOnMainThread { request.onStart() }
            },
            onProgress = {
                executeOnMainThread { request.onProgress(it) }
            },
            onPause = {
                executeOnMainThread { request.onPause() }
            },
            onCompleted = {
                executeOnMainThread { request.onCompleted() }
            },
            onError = {
                executeOnMainThread { request.onError(it) }
            }
        )
    }

    /**
     * Executes a block of code on the main thread using a coroutine.
     *
     * @param block The code block to be executed on the main thread.
     */
    private fun executeOnMainThread(block: () -> Unit) {
        scope.launch {
            block()
        }
    }

    /**
     * Cancels a specific download request.
     *
     * @param req The download request to be canceled.
     */
    fun cancel(req: DownloadRequest) {
        // Update the status and cancel the associated job
        req.status = DownloadStatus.CANCELLED
        req.job.cancel()
    }

    /**
     * Cancels all ongoing download requests.
     */
    fun cancelAll() {
        // Cancel the entire coroutine scope, terminating all download tasks
        scope.cancel()
    }
}
