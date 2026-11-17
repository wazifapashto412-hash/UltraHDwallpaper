package com.ultrahd.wallpapers.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ultrahd.wallpapers.data.Store
import com.ultrahd.wallpapers.data.Wallpaper
import com.ultrahd.wallpapers.data.WallpaperActions
import kotlinx.coroutines.launch

/** Holds the wallpaper being viewed (avoids encoding objects into navigation routes). */
object Selected { var wallpaper: Wallpaper? = null }

@Composable
fun DetailScreen(w: Wallpaper, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var busy by remember { mutableStateOf(false) }
    var showSet by remember { mutableStateOf(false) }
    val fav = Store.isFav(w.id)

    LaunchedEffect(w.id) { Store.addRecent(w) }

    fun run(okMessage: String, block: suspend () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            val msg = try {
                block(); okMessage
            } catch (e: java.net.UnknownHostException) {
                "No internet connection."
            } catch (e: Exception) {
                e.message ?: "Something went wrong."
            }
            busy = false
            snack.showSnackbar(msg)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(w.thumbUrl, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        AsyncImage(w.previewUrl, w.alt, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f), contentColor = Color.White),
            modifier = Modifier.statusBarsPadding().padding(12.dp),
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))))
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer16()
            Text(w.alt, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(
                "${w.quality} • ${w.width}×${w.height}",
                color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = { openUrl(ctx, w.pageUrl.ifBlank { w.photographerUrl }) }) {
                Text("Photo by ${w.photographer} on Pexels", color = Color.White)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { Store.toggleFav(w) }) {
                    Icon(
                        if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        if (fav) "Remove from favorites" else "Add to favorites",
                        tint = if (fav) Color(0xFFFF5C7A) else Color.White,
                    )
                }
                IconButton(onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${w.alt} – photo by ${w.photographer} on Pexels\n${w.pageUrl}")
                    }
                    ctx.startActivity(Intent.createChooser(send, "Share wallpaper"))
                }) { Icon(Icons.Default.Share, "Share", tint = Color.White) }
                OutlinedButton(
                    enabled = !busy, modifier = Modifier.weight(1f),
                    onClick = { run("Saved to Pictures/UltraHD Wallpapers") { WallpaperActions.download(ctx, w) } },
                ) { Text("Download", color = Color.White) }
                Button(enabled = !busy, modifier = Modifier.weight(1f), onClick = { showSet = true }) { Text("Set as…") }
            }
            if (busy) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.TopCenter).statusBarsPadding())
    }

    if (showSet) {
        AlertDialog(
            onDismissRequest = { showSet = false },
            title = { Text("Set wallpaper") },
            text = {
                Column {
                    listOf(
                        "Home screen" to WallpaperActions.HOME,
                        "Lock screen" to WallpaperActions.LOCK,
                        "Home & lock screen" to WallpaperActions.BOTH,
                    ).forEach { (label, flag) ->
                        TextButton(onClick = {
                            showSet = false
                            run("Wallpaper set: $label") { WallpaperActions.setWallpaper(ctx, w, flag) }
                        }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showSet = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Spacer16() = Box(Modifier.size(16.dp))

private fun openUrl(ctx: android.content.Context, url: String) {
    if (url.startsWith("https://")) {
        try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
    }
}
