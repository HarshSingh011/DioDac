package com.example.vidplay.ui.VideoSection

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

@Composable
fun Page1Screen(navController: NavController) {
    val context = LocalContext.current
    val TAG = "Page1Screen"

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Storage permission granted")
        } else {
            Log.d(TAG, "Storage permission denied")
        }
    }

    LaunchedEffect(key1 = true) {
        val readPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_VIDEO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when (ContextCompat.checkSelfPermission(context, readPermission)) {
            PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Permission already granted")
            }
            else -> {
                Log.d(TAG, "Requesting permission")
                permissionLauncher.launch(readPermission)
            }
        }
    }

    VideoGrid(navController = navController)
}
