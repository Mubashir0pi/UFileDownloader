package com.mubashir.nfinity.ufiledownloader

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nf.fileurldownloader.downloader.config.Downloader

import com.mubashir.nfinity.ufiledownloader.ui.theme.UfileDownloaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UfileDownloaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FileDownloaderScreen()
                }
            }
        }
    }
}

@Composable
fun FileDownloaderScreen() {
    val context = LocalContext.current
    val downloader = Downloader.create()

    val dirPath = "${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        FileDownloaderItem(dirPath, downloader, "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_30mb.mp4")
        Spacer(modifier = Modifier.height(16.dp))
        FileDownloaderItem(dirPath, downloader, "https://media.giphy.com/media/Bk0CW5frw4qfS/giphy.gif")
    }
}

@Composable
fun FileDownloaderItem(dirPath: String, downloader: Downloader, url: String) {
    var isDownloading by remember { mutableStateOf(false) }

    Column {
        Text(
            text = "URL: $url",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isDownloading) {
            DownloadProgress(downloader, url)
        } else {
            DownloadButton(
                downloader = downloader,
                url = url,
                dirPath = dirPath,
                onStart = { isDownloading = true },
                onCompleted = { isDownloading = false }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DownloadButton(
    downloader: Downloader,
    url: String,
    dirPath: String,
    onStart: () -> Unit,
    onCompleted: () -> Unit
) {
    Button(
        onClick = {
            val request = downloader.newReqBuilder(url, dirPath, "downloaded_file").build()
            downloader.enqueue(
                request,
                onStart = {
                    onStart()
                },
                onCompleted = {
                    onCompleted()
                }
            )
        },
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(imageVector = Icons.Default.Done, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Download")
    }
}

@Composable
fun DownloadProgress(downloader: Downloader, url: String) {
    val progress = remember { mutableStateOf(0) }

    LaunchedEffect(key1 = url) {
        downloader.getProgress(url) {
            progress.value = it
        }
    }

    Column {
        LinearProgressIndicator(progress = progress.value.toFloat())
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Progress: ${progress.value}%")
    }
}

@Composable
fun GreetingPreview() {
    UfileDownloaderTheme {
        FileDownloaderScreen()
    }
}
