package com.example.pitchapp

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pitchapp.ui.navigation.BottomNavBar
import com.example.pitchapp.ui.navigation.Screen
import com.example.pitchapp.ui.screens.feed.FeedScreen
import com.example.pitchapp.ui.screens.review.AddReviewScreen
import com.example.pitchapp.ui.screens.search.ResultScreen
import com.example.pitchapp.ui.screens.search.SearchScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.pitchapp.data.local.PitchDatabase
import com.example.pitchapp.data.repository.ReviewRepository
import com.example.pitchapp.ui.components.ThemeToggle
import com.example.pitchapp.ui.screens.profile.ProfileScreen
import com.example.pitchapp.ui.screens.search.AlbumDetailScreen
import com.example.pitchapp.viewmodel.FeedViewModel
import com.example.pitchapp.viewmodel.ProfileViewModel
import com.example.pitchapp.viewmodel.ReviewViewModel
import com.example.pitchapp.viewmodel.SearchViewModel
import com.example.pitchapp.ui.navigation.LogoHeader
import com.example.pitchapp.ui.theme.PitchAppTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainApp(
    feedViewModelFactory: FeedViewModelFactory,
    reviewViewModelFactory: ReviewViewModelFactory,
    searchViewModelFactory: SearchViewModelFactory,
    albumDetailViewModelFactory: AlbumDetailViewModelFactory
) {

    val navController = rememberNavController()
    val systemDarkTheme = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDarkTheme) }


    PitchAppTheme(darkTheme=darkTheme) {
    Scaffold(bottomBar = { BottomNavBar(navController) }) { padding ->

        Column {
            LogoHeader(darkTheme = darkTheme,
                onToggle = { darkTheme = it })


            NavHost(
                navController = navController,
                startDestination = Screen.Feed.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Feed.route) {
                    FeedScreen(
                        navController = navController,
                        viewModel = viewModel<FeedViewModel>(factory = feedViewModelFactory)
                    )
                }
                composable(Screen.Search.route) {
                    SearchScreen(
                        navController = navController,
                        viewModel = viewModel<SearchViewModel>(factory = searchViewModelFactory)
                    )
                }
                composable(Screen.Results.route) {
                    ResultScreen(
                        navController = navController,
                        viewModel = viewModel<SearchViewModel>(factory = searchViewModelFactory)
                    )
                }

                composable(Screen.AlbumDetail.route) { backStackEntry ->
                    val albumId = backStackEntry.arguments?.getString("id") ?: ""
                    AlbumDetailScreen(
                        albumId = albumId,
                        navController = navController,
                        viewModel = viewModel(factory = albumDetailViewModelFactory)
                    )
                }

                composable(Screen.AddReview.route) {
                    val reviewVm = viewModel<ReviewViewModel>(factory = reviewViewModelFactory)
                    val searchVm = viewModel<SearchViewModel>(factory = searchViewModelFactory)
                    AddReviewScreen(
                        navController = navController,
                        reviewViewModel = reviewVm,
                        searchViewModel = searchVm,
                        albumId = null
                    )
                }

                composable(
                    route = Screen.AddReview.WITH_ARG,
                    arguments = listOf(navArgument(Screen.AddReview.ARG) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments!!.getString(Screen.AddReview.ARG)!!
                    val reviewVm = viewModel<ReviewViewModel>(factory = reviewViewModelFactory)
                    val searchVm = viewModel<SearchViewModel>(factory = searchViewModelFactory)
                    AddReviewScreen(
                        navController = navController,
                        reviewViewModel = reviewVm,
                        searchViewModel = searchVm,
                        albumId = id
                    )
                }

                composable(
                    route = Screen.Profile.route,
                    arguments = listOf(
                        navArgument(Screen.Profile.ARG) {
                            type = NavType.StringType
                            defaultValue = "current_user"
                            nullable = true
                        }
                    )
                ) { backStack ->
                    val username =
                        backStack.arguments?.getString(Screen.Profile.ARG) ?: "current_user"
                    val profileVm = viewModel<ProfileViewModel>(
                        factory = ProfileViewModelFactory(
                            reviewRepo = ReviewRepository(
                                PitchDatabase.getDatabase(LocalContext.current).reviewDao()
                            ),
                            userPrefDao = PitchDatabase.getDatabase(LocalContext.current)
                                .userPreferenceDao()
                        )
                    )
                    ProfileScreen(
                        navController = navController,
                        viewModel = profileVm,
                        username = username
                    )
                }

            }
        }
    }
}
}