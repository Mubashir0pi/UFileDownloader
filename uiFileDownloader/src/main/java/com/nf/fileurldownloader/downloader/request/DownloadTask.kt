package com.nf.fileurldownloader.downloader.request

import com.nf.fileurldownloader.downloader.client.HttpClient
import com.nf.fileurldownloader.downloader.config.DownloadStatus
import com.nf.fileurldownloader.downloader.config.FileDownloadOutputStream
import com.nf.fileurldownloader.downloader.config.FileDownloadRandomAccessFile
import com.nf.fileurldownloader.downloader.utils.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * DownloadTask handles the actual download process for a given DownloadRequest.
 *
 * @param req The DownloadRequest to be processed.
 * @param httpClient The HttpClient used for making the download request.
 */
class DownloadTask(private val req: DownloadRequest, private val httpClient: HttpClient) {

    private var responseCode = 0
    private var totalBytes: Long = 0
    private var inputStream: InputStream? = null
    private lateinit var outputStream: FileDownloadOutputStream

    private var tempPath: String = ""
    private var isResumeSupported = true

    companion object {
        private const val TIME_GAP_FOR_SYNC: Long = 2000
        private const val MIN_BYTES_FOR_SYNC: Long = 65536
        private const val BUFFER_SIZE = 1024 * 4
    }

    /**
     * Runs the download task asynchronously.
     *
     * @param onStart Callback when the download starts.
     * @param onProgress Callback to track download progress.
     * @param onPause Callback when the download is paused.
     * @param onCompleted Callback when the download is completed.
     * @param onError Callback when an error occurs during the download.
     */
    suspend fun run(
        onStart: () -> Unit = {},
        onProgress: (value: Int) -> Unit = { _ -> },
        onPause: () -> Unit = {},
        onCompleted: () -> Unit = {},
        onError: (error: String) -> Unit = { _ -> }
    ) {
        withContext(Dispatchers.IO) {
            try {
                tempPath = getTempPath(req.dirPath, req.fileName)
                var file = File(tempPath)

                req.status = DownloadStatus.IN_PROGRESS

                onStart()

                httpClient.connect(req)

                responseCode = httpClient.getResponseCode()

                totalBytes = req.totalBytes

                if (totalBytes == 0L) {
                    totalBytes = httpClient.getContentLength()
                    req.totalBytes = (totalBytes)
                }

                inputStream = httpClient.getInputStream()
                if (inputStream == null) {
                    return@withContext
                }

                val buff = ByteArray(BUFFER_SIZE)

                if (!file.exists()) {
                    val parentFile = file.parentFile
                    if (parentFile != null && !parentFile.exists()) {
                        if (parentFile.mkdirs()) {
                            file.createNewFile()
                        }
                    } else {
                        file.createNewFile()
                    }
                }

                this@DownloadTask.outputStream = FileDownloadRandomAccessFile.create(file)

                do {
                    val byteCount = inputStream!!.read(buff, 0, BUFFER_SIZE)
                    if (byteCount == -1) {
                        break
                    }
                    if (req.status === DownloadStatus.CANCELLED) {
                        deleteTempFile()
                        onError("Cancelled")
                        return@withContext
                    }
                    outputStream.write(buff, 0, byteCount)
                    req.downloadedBytes = req.downloadedBytes + byteCount

                    var progress = 0
                    if (totalBytes > 0) {
                        progress = ((req.downloadedBytes * 100) / totalBytes).toInt()
                    }
                    onProgress(progress)
                } while (true)

                val path = getPath(req.dirPath, req.fileName)
                renameFileName(tempPath, path)
                onCompleted()
                req.status = DownloadStatus.COMPLETED
                return@withContext
            } catch (e: CancellationException) {
                deleteTempFile()
                req.status = DownloadStatus.FAILED
                onError(e.toString())
                return@withContext
            } catch (e: Exception) {
                if (!isResumeSupported) {
                    deleteTempFile()
                }
                req.status = DownloadStatus.FAILED
                onError(e.toString())
                return@withContext
            } finally {
                closeAllSafely()
            }
        }
    }

    private fun deleteTempFile(): Boolean {
        val file = File(tempPath)
        if (file.exists()) {
            return file.delete()
        }
        return false
    }

    private suspend fun closeAllSafely() {
        try {
            httpClient.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            inputStream.let { it?.close() }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            if (::outputStream.isInitialized) {
                outputStream.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
