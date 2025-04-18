package com.example.vidplay

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class VideoItem(val uri: Uri, val name: String, val thumbnail: Bitmap?)

class VideoGridScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoGrid()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoGrid() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Video Gallery") }
            )
        }
    ) { padding ->
        VideoGridContent(padding)
    }
}

@Composable
fun VideoGridContent(padding: PaddingValues) {
    val context = LocalContext.current
    val videoList = remember { mutableStateListOf<VideoItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var permissionDenied by remember { mutableStateOf(false) }
    val TAG = "VideoGridContent"

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Permission granted, loading videos")
            isLoading = true
            permissionDenied = false
        } else {
            Log.d(TAG, "Permission denied")
            isLoading = false
            permissionDenied = true
        }
    }

    LaunchedEffect(key1 = true) {
        val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when (ContextCompat.checkSelfPermission(context, readPermission)) {
            PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Permission already granted, loading videos")
                try {
                    loadVideos(context, videoList)
                    Log.d(TAG, "Videos loaded: ${videoList.size}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading videos", e)
                }
                isLoading = false
            }
            else -> {
                Log.d(TAG, "Requesting permission")
                permissionLauncher.launch(readPermission)
            }
        }
    }

    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        permissionDenied -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Permission to access videos was denied")
                    Text("Please grant permission in app settings",
                        modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        videoList.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No videos found on device")
            }
        }
        else -> {
            VideoGridView(videoList = videoList, padding = padding)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoGridView(videoList: List<VideoItem>, padding: PaddingValues) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = padding,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(videoList) { video ->
            VideoCard(video = video)
        }
    }
}

@Composable
fun VideoCard(video: VideoItem) {
    val TAG = "VideoCard"

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable {
                Log.d(TAG, "Video clicked: ${video.name}")
            }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (video.thumbnail != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(video.thumbnail)
                        .build(),
                    contentDescription = video.name,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("No Thumbnail", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

suspend fun loadVideos(context: Context, videoList: MutableList<VideoItem>) = withContext(Dispatchers.IO) {
    videoList.clear()
    val TAG = "loadVideos"
    Log.d(TAG, "Starting to load videos")

    val collection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            )
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME
    )

    try {
        val query = context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            MediaStore.Video.Media.DATE_ADDED + " DESC"
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            Log.d(TAG, "Found ${cursor.count} videos")

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val thumbnail = try {
                    MediaStore.Video.Thumbnails.getThumbnail(
                        context.contentResolver,
                        id,
                        MediaStore.Video.Thumbnails.MINI_KIND,
                        null
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error generating thumbnail for video: $name", e)
                    null
                }

                videoList.add(VideoItem(contentUri, name, thumbnail))
                Log.d(TAG, "Added video: $name")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error querying videos", e)
    }

    Log.d(TAG, "Finished loading videos, count: ${videoList.size}")
}