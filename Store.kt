package com.ultrahd.wallpapers.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray

/** Local-only storage of favourites and recently viewed items. Nothing leaves the device. */
object Store {
    private lateinit var prefs: SharedPreferences
    val favorites = mutableStateListOf<Wallpaper>()
    val recents = mutableStateListOf<Wallpaper>()

    fun init(context: Context) {
        prefs = context.getSharedPreferences("store", Context.MODE_PRIVATE)
        favorites.addAll(read("fav"))
        recents.addAll(read("recent"))
    }

    private fun read(key: String): List<Wallpaper> = try {
        val a = JSONArray(prefs.getString(key, "[]"))
        (0 until a.length()).map { Wallpaper.fromJson(a.getJSONObject(it)) }
    } catch (e: Exception) {
        emptyList()
    }

    private fun write(key: String, list: List<Wallpaper>) {
        prefs.edit().putString(key, JSONArray(list.map { it.toJson() }).toString()).apply()
    }

    fun isFav(id: Long) = favorites.any { it.id == id }

    fun toggleFav(w: Wallpaper) {
        val i = favorites.indexOfFirst { it.id == w.id }
        if (i >= 0) favorites.removeAt(i) else favorites.add(0, w)
        write("fav", favorites)
    }

    fun addRecent(w: Wallpaper) {
        recents.removeAll { it.id == w.id }
        recents.add(0, w)
        while (recents.size > 60) recents.removeAt(recents.lastIndex)
        write("recent", recents)
    }

    fun clearRecents() {
        recents.clear()
        write("recent", recents)
    }
}
