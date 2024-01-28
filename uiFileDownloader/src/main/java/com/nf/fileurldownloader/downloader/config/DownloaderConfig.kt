package com.nf.fileurldownloader.downloader.config

import com.nf.fileurldownloader.downloader.client.HttpClient
import com.nf.fileurldownloader.downloader.client.DefaultHttpClient
import com.nf.fileurldownloader.downloader.utils.Constants

data class DownloaderConfig(
    val httpClient: HttpClient = DefaultHttpClient(),
    val connectTimeOut: Int = Constants.DEFAULT_CONNECT_TIMEOUT_MILLS,
    val readTimeOut: Int = Constants.DEFAULT_READ_TIMEOUT_MILLS
)