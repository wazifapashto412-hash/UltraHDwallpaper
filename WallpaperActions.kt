package com.ultrahd.wallpapers.data

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object WallpaperActions {
    const val HOME = WallpaperManager.FLAG_SYSTEM
    const val LOCK = WallpaperManager.FLAG_LOCK
    const val BOTH = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
    private const val MAX_EDGE = 4096

    private fun open(url: String): HttpURLConnection {
        require(url.startsWith("https://")) { "Insecure URL blocked" }
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15_000
        c.readTimeout = 30_000
        if (c.responseCode != 200) throw IOException("Download failed (HTTP ${c.responseCode})")
        return c
    }

    /** Saves the original-resolution file to Pictures/UltraHD Wallpapers (no storage permission needed). */
    suspend fun download(context: Context, w: Wallpaper): Uri = withContext(Dispatchers.IO) {
        val ext = w.originalUrl.substringBefore('?').substringAfterLast('.').lowercase()
            .let { if (it in listOf("jpg", "jpeg", "png")) it else "jpg" }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "UltraHD_${w.id}_${w.width}x${w.height}.$ext")
            put(MediaStore.Images.Media.MIME_TYPE, if (ext == "png") "image/png" else "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/UltraHD Wallpapers")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Could not create file in Pictures.")
        val conn = try { open(w.originalUrl) } catch (e: Exception) { resolver.delete(uri, null, null); throw e }
        try {
            conn.inputStream.use { input ->
                (resolver.openOutputStream(uri) ?: throw IOException("Could not open file.")).use { input.copyTo(it) }
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        } finally {
            conn.disconnect()
        }
    }

    suspend fun setWallpaper(context: Context, w: Wallpaper, target: Int) = withContext(Dispatchers.IO) {
        val tmp = File(context.cacheDir, "wp_${w.id}.tmp")
        try {
            val conn = open(w.originalUrl)
            try { conn.inputStream.use { i -> tmp.outputStream().use { i.copyTo(it) } } } finally { conn.disconnect() }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(tmp.path, bounds)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_EDGE) sample *= 2

            var bmp: Bitmap? = null
            while (bmp == null) {
                try {
                    bmp = BitmapFactory.decodeFile(tmp.path, BitmapFactory.Options().apply { inSampleSize = sample })
                        ?: throw IOException("Could not decode image.")
                } catch (e: OutOfMemoryError) {
                    if (sample >= 16) throw IOException("Not enough memory to set this wallpaper.")
                    sample *= 2
                }
            }
            try {
                WallpaperManager.getInstance(context).setBitmap(bmp, null, true, target)
            } finally {
                bmp.recycle()
            }
        } finally {
            tmp.delete()
        }
    }
}
