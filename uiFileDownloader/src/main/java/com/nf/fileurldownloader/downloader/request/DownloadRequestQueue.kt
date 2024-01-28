package com.library.internal

import com.nf.fileurldownloader.downloader.dispatcher.DownloadDispatcher
import com.nf.fileurldownloader.downloader.request.DownloadRequest

/**
 * Manages a queue of download requests using a DownloadDispatcher.
 *
 * @param dispatcher The DownloadDispatcher responsible for handling download requests.
 */
class DownloadRequestQueue(private val dispatcher: DownloadDispatcher) {

    // Map to store download requests with their unique IDs
    private val idRequestMap: HashMap<Int, DownloadRequest> = hashMapOf()

    /**
     * Enqueues a download request and adds it to the internal map.
     *
     * @param request The download request to be enqueued.
     * @return The unique download ID associated with the request.
     */
    fun enqueue(request: DownloadRequest): Int {
        // Store the request in the map and enqueue it using the dispatcher
        idRequestMap[request.downloadId] = request
        return dispatcher.enqueue(request)
    }

    /**
     * Pauses a specific download request based on its ID.
     *
     * @param id The unique download ID of the request to be paused.
     */
    fun pause(id: Int) {
        // Cancel the download request and remove it from the map
        idRequestMap[id]?.let {
            dispatcher.cancel(it)
        }
    }

    /**
     * Resumes a specific download request based on its ID.
     *
     * @param id The unique download ID of the request to be resumed.
     */
    fun resume(id: Int) {
        // Enqueue the download request using the dispatcher
        idRequestMap[id]?.let {
            dispatcher.enqueue(it)
        }
    }

    /**
     * Cancels a specific download request based on its ID and removes it from the map.
     *
     * @param id The unique download ID of the request to be canceled.
     */
    fun cancel(id: Int) {
        // Cancel the download request, remove it from the map
        idRequestMap[id]?.let {
            dispatcher.cancel(it)
        }
        idRequestMap.remove(id)
    }

    /**
     * Cancels all download requests with a specific tag and removes them from the map.
     *
     * @param tag The tag associated with the download requests to be canceled.
     */
    fun cancel(tag: String) {
        // Find requests with the specified tag, cancel them, and remove from the map
        val requestsWithTag = idRequestMap.values.filter {
            it.tag == tag
        }
        for (req in requestsWithTag) {
            cancel(req.downloadId)
        }
    }

    /**
     * Cancels all download requests and clears the internal map.
     */
    fun cancelAll() {
        // Clear the map and cancel all ongoing download requests
        idRequestMap.clear()
        dispatcher.cancelAll()
    }
}
