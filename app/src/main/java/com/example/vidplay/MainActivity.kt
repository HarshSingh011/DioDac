package com.example.vidplay

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.vidplay.Navigation.MyAppNavHost
import com.example.vidplay.ui.theme.VidPlayTheme
import com.example.vidplay.utils.PermissionUtils

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            results.forEach { (permission, isGranted) ->
                Log.d(TAG, "Permission $permission: ${if (isGranted) "Granted" else "Denied"}")
            }
        }

        setContent {
            VidPlayTheme {
                MyAppNavHost()

                LaunchedEffect(Unit) {
                    Log.d(TAG, "Requesting media permissions")
                    PermissionUtils.requestMediaPermissions(requestPermissions)
                }
            }
        }
    }
}
