package com.example.vidplay.ui

import android.content.ContentUris
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vidplay.models.Media
import com.example.vidplay.utils.MediaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun VideoGrid(navController: NavController) {
    val context = LocalContext.current
    val videos = remember { mutableStateOf<List<Media>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        videos.value = withContext(Dispatchers.IO) {
            MediaUtils.getVideos(context.contentResolver)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Videos",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(videos.value) { video ->
                VideoItem(video = video) {
                    try {
                        Log.d("VideoGrid", "Original video URI: ${video.uri}")

                        val encodedUri = URLEncoder.encode(
                            video.uri.toString(),
                            StandardCharsets.UTF_8.toString()
                        )

                        Log.d("VideoGrid", "Encoded URI: $encodedUri")

                        navController.navigate("videoPlayer/$encodedUri")
                    } catch (e: Exception) {
                        Log.e("VideoGrid", "Navigation error: ${e.message}", e)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoItem(video: Media, onClick: () -> Unit) {
    val context = LocalContext.current
    var thumbnailBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(video.uri) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            context.contentResolver.loadThumbnail(
                                video.uri,
                                android.util.Size(320, 180),
                                null
                            )
                        } catch (e: Exception) {
                            Log.e("VideoItem", "Error loading thumbnail: ${e.message}", e)
                            null
                        }
                    } else {
                        try {
                            ThumbnailUtils.createVideoThumbnail(
                                video.uri.path ?: "",
                                MediaStore.Images.Thumbnails.MINI_KIND
                            )
                        } catch (e: Exception) {
                            Log.e("VideoItem", "Error loading thumbnail: ${e.message}", e)
                            null
                        }
                    }
                }
                thumbnailBitmap = bitmap
            } catch (e: Exception) {
                Log.e("VideoItem", "Error loading thumbnail: ${e.message}", e)
            }
        }
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            if (thumbnailBitmap != null) {
                Image(
                    bitmap = thumbnailBitmap!!.asImageBitmap(),
                    contentDescription = video.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "Loading...",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                Text(
                    text = video.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
