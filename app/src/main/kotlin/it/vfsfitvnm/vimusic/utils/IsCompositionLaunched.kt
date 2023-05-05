package it.vfsfitvnm.vimusic.utils

import androidx.compose.runtime.*

@Composable
fun isCompositionLaunched(): Boolean {
    var isLaunched by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isLaunched = true
    }
    return isLaunched
}