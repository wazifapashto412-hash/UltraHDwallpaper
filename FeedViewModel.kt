package com.ultrahd.wallpapers.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultrahd.wallpapers.data.PexelsApi
import com.ultrahd.wallpapers.data.Wallpaper
import kotlinx.coroutines.launch

/** Paginated feed. `query == null` loads the curated feed. */
class FeedViewModel(private val query: String?) : ViewModel() {
    var items by mutableStateOf<List<Wallpaper>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var endReached by mutableStateOf(false); private set

    private var page = 1
    private val seen = HashSet<Long>()

    init { loadMore() }

    fun loadMore() {
        if (loading || endReached) return
        loading = true
        error = null
        viewModelScope.launch {
            try {
                var fresh = emptyList<Wallpaper>()
                var tries = 0
                // Pages can be empty after the 4K filter; keep going a few pages.
                while (fresh.isEmpty() && !endReached && tries < 4) {
                    val r = PexelsApi.fetch(query, page++)
                    endReached = !r.hasMore
                    fresh = r.items.filter { seen.add(it.id) }
                    tries++
                }
                items = items + fresh
            } catch (e: Exception) {
                error = when (e) {
                    is java.net.UnknownHostException, is java.net.SocketTimeoutException ->
                        "No internet connection. Check your network and retry."
                    else -> e.message ?: "Something went wrong."
                }
            } finally {
                loading = false
            }
        }
    }
}
