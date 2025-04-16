package com.example.vidplay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.vidplay.models.Media
import com.example.vidplay.ui.theme.VidPlayTheme
import com.example.vidplay.utils.MediaUtils
import com.example.vidplay.utils.PermissionUtils
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            // Handle permission results
        }

        setContent {
            VidPlayTheme {
                var videos by remember { mutableStateOf<List<Media>>(emptyList()) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    PermissionUtils.requestMediaPermissions(requestPermissions)
                }

                // Fetch videos after permissions are granted
                LaunchedEffect(Unit) {
                    scope.launch {
                        videos = MediaUtils.getVideos(contentResolver)
                    }
                }

                // Display videos in your UI
            }
        }
    }
}