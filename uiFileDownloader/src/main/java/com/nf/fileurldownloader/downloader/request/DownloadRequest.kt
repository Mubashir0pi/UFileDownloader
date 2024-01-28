package com.nf.fileurldownloader.downloader.request

import com.nf.fileurldownloader.downloader.config.DownloadStatus
import com.nf.fileurldownloader.downloader.utils.getUniqueId
import kotlinx.coroutines.Job

/**
 * Represents a download request with various configuration options.
 *
 * @property url The URL to download.
 * @property tag An optional tag to associate with the download request.
 * @property dirPath The directory path where the downloaded file will be stored.
 * @property downloadId A unique identifier for the download request.
 * @property fileName The name of the downloaded file.
 * @property readTimeOut The read timeout for the download request.
 * @property connectTimeOut The connect timeout for the download request.
 * @property status The status of the download request.
 * @property headers The headers to be included in the HTTP request.
 */
class DownloadRequest private constructor(
    internal var url: String,
    internal val tag: String?,
    internal val dirPath: String,
    internal val downloadId: Int,
    internal val fileName: String,
    internal var readTimeOut: Int,
    internal var connectTimeOut: Int,
    internal var status: DownloadStatus = DownloadStatus.UNKNOWN,
    internal val headers: HashMap<String, List<String>>?,
) {

    internal var totalBytes: Long = 0
    internal var downloadedBytes: Long = 0
    internal lateinit var job: Job
    internal lateinit var onStart: () -> Unit
    internal lateinit var onProgress: (value: Int) -> Unit
    internal lateinit var onPause: () -> Unit
    internal lateinit var onCompleted: () -> Unit
    internal lateinit var onError: (error: String) -> Unit

    /**
     * Builder pattern for creating DownloadRequest instances with optional parameters.
     *
     * @property url The URL to download.
     * @property dirPath The directory path where the downloaded file will be stored.
     * @property fileName The name of the downloaded file.
     */
    data class Builder(
        private val url: String,
        private val dirPath: String,
        private val fileName: String
    ) {

        private var tag: String? = null
        private var readTimeOut: Int = 0
        private var connectTimeOut: Int = 0
        private var headers: HashMap<String, List<String>>? = null

        /**
         * Sets a tag for the download request.
         *
         * @param tag The tag to associate with the download request.
         * @return The Builder instance for method chaining.
         */
        fun tag(tag: String) = apply {
            this.tag = tag
        }

        /**
         * Sets the read timeout for the download request.
         *
         * @param timeout The read timeout value in milliseconds.
         * @return The Builder instance for method chaining.
         */
        fun readTimeout(timeout: Int) = apply {
            this.readTimeOut = timeout
        }

        /**
         * Sets the connect timeout for the download request.
         *
         * @param timeout The connect timeout value in milliseconds.
         * @return The Builder instance for method chaining.
         */
        fun connectTimeout(timeout: Int) = apply {
            this.connectTimeOut = timeout
        }

        /**
         * Sets custom headers for the download request.
         *
         * @param headers The headers to be included in the HTTP request.
         * @return The Builder instance for method chaining.
         */
        fun headers(headers: HashMap<String, List<String>>) = apply {
            this.headers = headers
        }

        /**
         * Builds the DownloadRequest instance with the configured parameters.
         *
         * @return The created DownloadRequest instance.
         */
        fun build(): DownloadRequest {
            return DownloadRequest(
                url = url,
                tag = tag,
                dirPath = dirPath,
                downloadId = getUniqueId(url, dirPath, fileName),
                fileName = fileName,
                readTimeOut = readTimeOut,
                connectTimeOut = connectTimeOut,
                headers = headers,
            )
        }
    }
}
