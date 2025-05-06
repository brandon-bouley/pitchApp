package com.example.pitchapp.ui.navigation

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.pitchapp.R
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.ui.components.ThemeToggle
import com.example.pitchapp.viewmodel.AuthViewModel
import com.example.pitchapp.viewmodel.SearchViewModel
import com.google.gson.Gson


sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Search : Screen("search")
    object Results : Screen("results")
    object Login : Screen("login")
    object SignUp : Screen("signup")

    object AddReview : Screen("add_review/{albumId}") {
        const val ARG_ALBUM_ID = "albumId"
        fun createRoute(albumId: String) = "add_review/$albumId"
    }

    object AlbumDetail : Screen("album_detail/{albumId}") {
        const val ARG_ALBUM_ID = "albumId"
        fun createRoute(albumId: String) = "album_detail/$albumId"
    }

    object Profile : Screen("profile/{username}") {
        const val ARG_USERNAME = "username"
        fun createRoute(username: String?) = "profile/${username ?: ""}"
    }

    object UserSearch : Screen("user_search")
}


    private data class NavItem(
        val route: String,
        val title: String,
        val selectedIcon: ImageVector,
        val unselectedIcon: ImageVector
    )

@Composable
fun BottomNavBar(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val username by authViewModel.username.collectAsState()

    val navItems = listOf(
        NavItem(
            route = Screen.Feed.route,
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        NavItem(
            route = Screen.Search.route,
            title = "Search",
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search
        ),
        NavItem(
            route = username?.let { Screen.Profile.createRoute(it) } ?: "",
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {

        NavigationBar {
            navItems.forEach { item ->
                // Only show items with a non‑empty route
                if (item.route.isNotEmpty()) {

                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.route == item.route }

                    if (selected != null) {
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected == true) item.selectedIcon
                                    else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}