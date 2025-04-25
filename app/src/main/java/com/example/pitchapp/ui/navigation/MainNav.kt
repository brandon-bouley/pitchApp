package com.example.pitchapp.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pitchapp.R
import com.example.pitchapp.ui.components.ThemeToggle


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
    NavigationBar (
        tonalElevation = 8.dp
    ){
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
@Composable
fun LogoHeader(
    darkTheme: Boolean,
    onToggle: (Boolean) -> Unit

) {

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Fit

            )

            ThemeToggle(
                isDarkTheme = darkTheme,
                onToggle = onToggle,
            )
        }
    }
}