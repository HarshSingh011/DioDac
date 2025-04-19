package com.example.vidplay.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vidplay.MainScreen
import com.example.vidplay.Page1Screen
import com.example.vidplay.ui.VideoPlayerScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun MyAppNavHost() {
    val navController = rememberNavController()

    // Ensure the NavController is passed to the NavHost
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController = navController)
        }
        composable("page1") {
            Page1Screen(navController = navController)
        }
        composable(
            route = "videoPlayer/{videoUri}",
            arguments = listOf(
                navArgument("videoUri") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("videoUri") ?: ""
            val decodedUri = URLDecoder.decode(encodedUri, StandardCharsets.UTF_8.toString())
            VideoPlayerScreen(navController = navController, videoUri = decodedUri)
        }
    }
}
