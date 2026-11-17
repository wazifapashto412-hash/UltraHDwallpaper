package com.ultrahd.wallpapers.data

import org.json.JSONObject

data class Wallpaper(
    val id: Long,
    val width: Int,
    val height: Int,
    val photographer: String,
    val photographerUrl: String,
    val pageUrl: String,
    val alt: String,
    val thumbUrl: String,
    val previewUrl: String,
    val originalUrl: String,
) {
    /** Label derived from the real pixel size of the original file. */
    val quality: String
        get() {
            val m = maxOf(width, height)
            return when {
                m >= 7680 -> "8K"
                m >= 3840 -> "4K"
                m >= 2560 -> "2K"
                else -> "HD"
            }
        }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id).put("w", width).put("h", height)
        .put("by", photographer).put("byUrl", photographerUrl).put("page", pageUrl)
        .put("alt", alt).put("thumb", thumbUrl).put("preview", previewUrl).put("orig", originalUrl)

    companion object {
        fun fromJson(o: JSONObject) = Wallpaper(
            o.getLong("id"), o.getInt("w"), o.getInt("h"),
            o.optString("by"), o.optString("byUrl"), o.optString("page"), o.optString("alt"),
            o.optString("thumb"), o.optString("preview"), o.getString("orig"),
        )
    }
}

data class Category(val name: String, val query: String, val emoji: String, val colors: List<Long>)

val Categories = listOf(
    Category("Nature", "nature landscape", "🌿", listOf(0xFF1B8A5A, 0xFF74D680)),
    Category("Mountains", "mountains", "🏔️", listOf(0xFF3A6073, 0xFF16222A)),
    Category("Cars", "sports car", "🏎️", listOf(0xFFD31027, 0xFFEA384D)),
    Category("Islamic", "mosque", "🕌", listOf(0xFF0F9B8E, 0xFF1E3C72)),
    Category("Animals", "wildlife animals", "🦁", listOf(0xFFF7971E, 0xFFFFD200)),
    Category("Flowers", "flowers", "🌸", listOf(0xFFEC6EAD, 0xFF3494E6)),
    Category("Space", "galaxy night sky", "🚀", listOf(0xFF0F0C29, 0xFF302B63)),
    Category("Technology", "technology futuristic", "💻", listOf(0xFF0083B0, 0xFF00B4DB)),
    Category("Abstract", "abstract", "🎨", listOf(0xFF8E2DE2, 0xFF4A00E0)),
    Category("Architecture", "architecture", "🏛️", listOf(0xFF485563, 0xFF29323C)),
)
