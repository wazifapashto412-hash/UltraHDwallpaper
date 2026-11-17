package com.ultrahd.wallpapers.data

import com.ultrahd.wallpapers.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Wallpaper source: Pexels API (https://www.pexels.com/api/). Photos are free to use under the
 * Pexels License; attribution to the photographer is shown in the app.
 */
object PexelsApi {
    const val PER_PAGE = 30
    private const val MIN_LONG_EDGE = 3840 // only 4K+ originals are listed

    class Page(val items: List<Wallpaper>, val hasMore: Boolean)

    suspend fun fetch(query: String?, page: Int): Page = withContext(Dispatchers.IO) {
        val key = BuildConfig.PEXELS_API_KEY
        if (key.isBlank()) throw IOException("Pexels API key is not configured. See README.md (Setup).")
        val url = if (query.isNullOrBlank()) {
            "https://api.pexels.com/v1/curated?per_page=$PER_PAGE&page=$page"
        } else {
            "https://api.pexels.com/v1/search?query=${URLEncoder.encode(query, "UTF-8")}" +
                "&orientation=portrait&per_page=$PER_PAGE&page=$page"
        }
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout = 15_000
            c.readTimeout = 20_000
            c.setRequestProperty("Authorization", key)
            when (val code = c.responseCode) {
                200 -> parse(c.inputStream.bufferedReader().use { it.readText() })
                401, 403 -> throw IOException("The Pexels API key was rejected.")
                429 -> throw IOException("Too many requests. Please try again in a few minutes.")
                else -> throw IOException("Server error ($code). Please try again.")
            }
        } finally {
            c.disconnect()
        }
    }

    private fun parse(body: String): Page {
        val photos = JSONObject(body).getJSONArray("photos")
        val list = ArrayList<Wallpaper>()
        for (i in 0 until photos.length()) {
            val p = photos.getJSONObject(i)
            val src = p.getJSONObject("src")
            val w = Wallpaper(
                id = p.getLong("id"),
                width = p.getInt("width"),
                height = p.getInt("height"),
                photographer = p.optString("photographer"),
                photographerUrl = p.optString("photographer_url"),
                pageUrl = p.optString("url"),
                alt = p.optString("alt").ifBlank { "Wallpaper" },
                thumbUrl = src.getString("large"),
                previewUrl = src.getString("large2x"),
                originalUrl = src.getString("original"),
            )
            if (maxOf(w.width, w.height) >= MIN_LONG_EDGE) list.add(w)
        }
        return Page(list, hasMore = photos.length() >= PER_PAGE)
    }
}
