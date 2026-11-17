package com.ultrahd.wallpapers.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ultrahd.wallpapers.data.Categories
import com.ultrahd.wallpapers.data.Wallpaper

private data class TopTab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TopTab("home", "Home", Icons.Default.Home),
    TopTab("categories", "Categories", Icons.Default.Menu),
    TopTab("search", "Search", Icons.Default.Search),
    TopTab("library", "Library", Icons.Default.Favorite),
)

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val open: (Wallpaper) -> Unit = { Selected.wallpaper = it; nav.navigate("detail") }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (tabs.any { it.route == route }) {
                NavigationBar {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = {
                                nav.navigate(t.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(t.icon, null) },
                            label = { Text(t.label) },
                        )
                    }
                }
            }
        },
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(pad),
            enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 12 } },
            exitTransition = { fadeOut(tween(150)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 12 } },
        ) {
            composable("home") { HomeScreen(open) { nav.navigate("category/${it.name}") } }
            composable("categories") { CategoriesScreen { nav.navigate("category/${it.name}") } }
            composable("category/{name}") { e ->
                val c = Categories.firstOrNull { it.name == e.arguments?.getString("name") }
                if (c == null) LaunchedEffect(Unit) { nav.popBackStack() }
                else CategoryScreen(c, onBack = { nav.popBackStack() }, onOpen = open)
            }
            composable("search") { SearchScreen(open) }
            composable("library") { LibraryScreen(open) }
            composable("detail") {
                val w = Selected.wallpaper
                if (w == null) LaunchedEffect(Unit) { nav.popBackStack() }
                else DetailScreen(w) { nav.popBackStack() }
            }
        }
    }
}
