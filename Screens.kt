@file:OptIn(ExperimentalMaterial3Api::class)

package com.ultrahd.wallpapers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ultrahd.wallpapers.data.Categories
import com.ultrahd.wallpapers.data.Category
import com.ultrahd.wallpapers.data.Store
import com.ultrahd.wallpapers.data.Wallpaper

@Composable
private fun feed(query: String?): FeedViewModel =
    viewModel(key = "feed_$query", factory = viewModelFactory { initializer { FeedViewModel(query) } })

@Composable
fun HomeScreen(onOpen: (Wallpaper) -> Unit, onCategory: (Category) -> Unit) {
    val vm = feed(null)
    Column(Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(title = { Text("Ultra HD 4K & 8K", fontWeight = FontWeight.Bold) })
        WallpaperGrid(
            list = vm.items, loading = vm.loading, error = vm.error, endReached = vm.endReached,
            emptyText = "No wallpapers found.", onMore = vm::loadMore, onRetry = vm::loadMore, onOpen = onOpen,
            header = {
                Column {
                    Text("Categories", style = MaterialTheme.typography.titleMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 8.dp),
                    ) {
                        items(Categories) { c ->
                            AssistChip(onClick = { onCategory(c) }, label = { Text("${c.emoji} ${c.name}") })
                        }
                    }
                    Text("Featured", style = MaterialTheme.typography.titleMedium)
                }
            },
        )
    }
}

@Composable
fun CategoriesScreen(onCategory: (Category) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(title = { Text("Categories", fontWeight = FontWeight.Bold) })
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(Categories, key = { it.name }) { c ->
                Box(
                    Modifier
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(c.colors.map { Color(it) }))
                        .clickable { onCategory(c) }
                        .padding(14.dp),
                ) {
                    Text(c.emoji, fontSize = 34.sp, modifier = Modifier.align(Alignment.TopStart))
                    Text(
                        c.name, color = Color.White, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.BottomStart),
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryScreen(category: Category, onBack: () -> Unit, onOpen: (Wallpaper) -> Unit) {
    val vm = feed(category.query)
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(category.name) },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            },
        )
        WallpaperGrid(
            list = vm.items, loading = vm.loading, error = vm.error, endReached = vm.endReached,
            emptyText = "No wallpapers found.", onMore = vm::loadMore, onRetry = vm::loadMore, onOpen = onOpen,
        )
    }
}

@Composable
fun SearchScreen(onOpen: (Wallpaper) -> Unit) {
    var text by remember { mutableStateOf("") }
    var committed by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf<Category?>(null) }
    val effective = listOfNotNull(committed.trim().takeIf { it.isNotEmpty() }, cat?.query).joinToString(" ")

    Column(Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(title = { Text("Search", fontWeight = FontWeight.Bold) })
        OutlinedTextField(
            value = text, onValueChange = { text = it },
            singleLine = true, placeholder = { Text("Search wallpapers") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (text.isNotEmpty()) IconButton(onClick = { text = ""; committed = "" }) {
                    Icon(Icons.Default.Close, "Clear")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { committed = text }),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(12.dp),
        ) {
            items(Categories) { c ->
                FilterChip(
                    selected = cat == c,
                    onClick = { cat = if (cat == c) null else c },
                    label = { Text(c.name) },
                )
            }
        }
        if (effective.isBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Type a name or pick a category", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val vm = feed(effective)
            WallpaperGrid(
                list = vm.items, loading = vm.loading, error = vm.error, endReached = vm.endReached,
                emptyText = "No results for \"$effective\".", onMore = vm::loadMore, onRetry = vm::loadMore, onOpen = onOpen,
            )
        }
    }
}

@Composable
fun LibraryScreen(onOpen: (Wallpaper) -> Unit) {
    var tab by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(title = { Text("Library", fontWeight = FontWeight.Bold) })
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Favorites") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Recently viewed") })
        }
        val list = if (tab == 0) Store.favorites else Store.recents
        WallpaperGrid(
            list = list.toList(), loading = false, error = null, endReached = true,
            emptyText = if (tab == 0) "No favorites yet.\nTap the heart on a wallpaper." else "Nothing viewed yet.",
            onMore = {}, onRetry = {}, onOpen = onOpen,
            header = {
                if (tab == 1 && Store.recents.isNotEmpty()) {
                    TextButton(onClick = { Store.clearRecents() }) { Text("Clear history") }
                }
            },
        )
    }
}
