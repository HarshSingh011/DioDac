package com.example.vidplay.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable
object Main
@Serializable
object Page1

@Composable
fun MyAppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    ){
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "main",
    ){

    }
}