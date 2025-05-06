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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.pitchapp.R
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.ui.components.ThemeToggle
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
    object AddTrackReview : Screen("add_track_review/{trackId}") {
        const val ARG_TRACK_ID = "trackId"
        fun createRoute(trackId: String) = "add_track_review/$trackId"
    }

    object AlbumDetail : Screen("album_detail/{albumId}") {
        const val ARG_ALBUM_ID = "albumId"
        fun createRoute(albumId: String) = "album_detail/$albumId"
    }

    object Profile : Screen("profile/{username}") {
        const val ARG_USERNAME = "username"
        fun createRoute(username: String?) = "profile/${username ?: ""}"
    }
}


    private data class NavItem(
        val screen: Screen,
        val icon: ImageVector,
        val label: String
    )

@Composable
fun BottomNavBar(
    navController: NavController,
    searchViewModel: SearchViewModel
) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val items = listOf(
            NavItem(Screen.Feed, Icons.Default.Home, "Home"),
            NavItem(Screen.Search, Icons.Default.Search, "Search"),
            NavItem(Screen.Profile, Icons.Default.Person, "Profile")
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.route == item.screen.route,
                onClick = {
                    if (currentDestination?.route != item.screen.route) {
                        searchViewModel.resetSearch()  // Use the passed ViewModel
                    }
                    navController.navigate(item.screen.route) {
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

