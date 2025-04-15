package com.example.vidplay

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun Page1Screen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    VideoGridScreen(context = context, modifier = modifier)
}