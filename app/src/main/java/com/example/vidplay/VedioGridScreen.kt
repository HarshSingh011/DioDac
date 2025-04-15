package com.example.vidplay

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoGridScreen(
    context: Context,
    modifier: Modifier = Modifier
) {
    var videoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
        if (isGranted) {
            videoUris = getLocalVideos(context)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_VIDEO)
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Video Grid") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (!permissionGranted) {
            Text(
                text = "Permission not granted",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else if (videoUris.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(videoUris) { videoUri ->
                    VideoThumbnail(videoUri = videoUri, modifier = Modifier.size(150.dp))
                }
            }
        } else {
            Text(
                text = "No videos found",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
fun VideoThumbnail(videoUri: Uri, modifier: Modifier = Modifier) {
    AsyncImage(
        model = videoUri,
        contentDescription = "Thumbnail of the video",
        modifier = modifier
    )
}

fun getLocalVideos(context: Context): List<Uri> {
    val videoList = mutableListOf<Uri>()
    val contentResolver: ContentResolver = context.contentResolver
    val projection = arrayOf(
        MediaStore.Video.Media._ID
    )
    val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    val cursor = contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${MediaStore.Video.Media.DATE_ADDED} DESC"
    )

    cursor?.use {
        val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        while (it.moveToNext()) {
            val id = it.getLong(idColumn)
            val contentUri = Uri.withAppendedPath(uri, id.toString())
            videoList.add(contentUri)
            Log.d("VideoGridScreen", "Video URI: $contentUri") // Debug log
        }
    }
    return videoList
}