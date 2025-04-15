package com.example.pitchapp.ui.navigation

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
import androidx.compose.ui.graphics.vector.ImageVector


sealed class Screen(val route: String) {
    data object Feed : Screen("feed")
    data object Search : Screen("search")
    data object Results : Screen("results")

    data object AddReview  : Screen("add_review") {
        const val ARG = "albumId"
        const val WITH_ARG = "add_review/{$ARG}"
        fun createRoute(albumId: String) = "add_review/$albumId"
    }

    object Profile    : Screen("profile?username={username}") {
        const val ARG       = "username"
        const val BASE_ROUTE = "profile"
        fun createRoute(username: String) = "$BASE_ROUTE?username=$username"
    }

    data object AlbumDetail : Screen("album/{id}") {
        fun createRoute(id: String) = "album/$id"
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val items = listOf(
            NavItem(Screen.Feed, Icons.Default.Home, "Home"),
            NavItem(Screen.Search, Icons.Default.Search, "Search"),
            NavItem(Screen.AddReview, Icons.Default.Add, "Review"),
            NavItem(Screen.Profile, Icons.Default.Person, "Profile")
        )

        items.forEach { item ->
            val navigateRoute = when(item.screen) {
                Screen.Profile -> Screen.Profile.createRoute("current_user")
                else            -> item.screen.route
            }

            val selected = when(item.screen) {
                Screen.Profile -> currentRoute?.startsWith("profile") == true
                else            -> currentRoute == item.screen.route
            }

            NavigationBarItem(
                icon =    { Icon(item.icon, contentDescription = item.label) },
                label =   { Text(item.label) },
                selected = selected,
                onClick = {
                    navController.navigate(navigateRoute) {
                        popUpTo(navController.graph.startDestinationId) {
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


private data class NavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)