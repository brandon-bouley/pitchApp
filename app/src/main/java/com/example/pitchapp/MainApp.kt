package com.example.pitchapp

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
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
import androidx.navigation.navigation
import com.example.pitchapp.data.local.PitchDatabase
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.remote.LastFmApi
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.data.repository.ReviewRepository
import com.example.pitchapp.ui.components.ThemeToggle
import com.example.pitchapp.ui.screens.profile.ProfileScreen
import com.example.pitchapp.ui.screens.search.AlbumDetailScreen
import com.example.pitchapp.ui.screens.profile.ProfileScreen
import com.example.pitchapp.viewmodel.AlbumDetailViewModel
import com.example.pitchapp.viewmodel.FeedViewModel
import com.example.pitchapp.viewmodel.ProfileViewModel
import com.example.pitchapp.viewmodel.ReviewViewModel
import com.example.pitchapp.viewmodel.SearchViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.pitchapp.ui.navigation.LogoHeader
import com.example.pitchapp.ui.theme.PitchAppTheme
import com.example.pitchapp.ui.screens.profile.LoginScreen
import com.example.pitchapp.ui.screens.profile.SignUpScreen
import com.example.pitchapp.viewmodel.AuthViewModel


@Composable
fun MainApp(
    feedViewModelFactory: FeedViewModelFactory,
    searchViewModelFactory: SearchViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory
) {
    val navController = rememberNavController()
    val systemDarkTheme = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDarkTheme) }
    val authViewModel = viewModel<AuthViewModel>()

    val reviewViewModelFactory = ReviewViewModelFactory(
            reviewRepository = ReviewRepository(),
            authViewModel = authViewModel
        )


    PitchAppTheme(darkTheme = darkTheme) {
        Scaffold(
            bottomBar = { BottomNavBar(navController) },
            topBar = { LogoHeader(darkTheme = darkTheme, onToggle = { darkTheme = it }) }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Feed.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Feed.route) {
                    FeedScreen(
                        navController = navController,
                        viewModel = viewModel(factory = feedViewModelFactory),
                        reviewViewModel = viewModel(factory = reviewViewModelFactory)
                    )
                }
                navigation(
                    startDestination = Screen.Search.route,
                    route = "search_root"
                ) {
                    composable(Screen.Search.route) {
                        SearchScreen(
                            navController = navController,
                            viewModel = viewModel(factory = searchViewModelFactory),
                            reviewViewModel = viewModel(factory = reviewViewModelFactory)
                        )
                    }

                    composable(Screen.Results.route) {
                        ResultScreen(
                            navController = navController,
                            viewModel = viewModel(factory = searchViewModelFactory)
                        )
                    }

                    composable(
                        route = Screen.AlbumDetail.route,
                        arguments = listOf(navArgument(Screen.AlbumDetail.ARG_ALBUM_ID) {
                            type = NavType.StringType
                        })
                    ) { backStackEntry ->
                        val albumId =
                            backStackEntry.arguments?.getString(Screen.AlbumDetail.ARG_ALBUM_ID)
                        AlbumDetailScreen(
                            albumId = albumId ?: "",
                            viewModel = viewModel(
                                factory = AlbumDetailViewModelFactory(
                                    MusicRepository(LastFmApi.service),
                                    ReviewRepository()
                                )
                            ),
                            reviewViewModel = viewModel(factory = reviewViewModelFactory),
                            navController = navController
                        )
                    }

                    composable(
                        route = Screen.AddReview.route,
                        arguments = listOf(navArgument(Screen.AddReview.ARG_ALBUM_ID) { type = NavType.StringType })
                    ) { backStackEntry ->
                        val albumId = backStackEntry.arguments?.getString(Screen.AddReview.ARG_ALBUM_ID)
                        AddReviewScreen(
                            albumId = albumId,
                            navController = navController,
                            reviewViewModel = viewModel(factory = reviewViewModelFactory),
                            searchViewModel = viewModel(factory = searchViewModelFactory),
                            authViewModel = authViewModel,
                            musicRepository = MusicRepository(LastFmApi.service)
                        )
                    }
                }

                composable("login") {
                    LoginScreen(navController, authViewModel)
                }

                composable("signup") {
                    SignUpScreen(navController, authViewModel)
                }



                    composable(
                        route = Screen.Profile.route,
                        arguments = listOf(
                            navArgument(Screen.Profile.ARG_USERNAME) {
                                type = NavType.StringType
                                defaultValue = Firebase.auth.currentUser?.uid ?: ""
                                nullable = true
                            }
                        )
                    ) { backStack ->
                        val userId = backStack.arguments?.getString(Screen.Profile.ARG_USERNAME)
                            ?: Firebase.auth.currentUser?.uid ?: ""

                        val profileVm = viewModel<ProfileViewModel>(
                            factory = profileViewModelFactory,
                            key = userId
                        )

                        ProfileScreen(
                            navController = navController,
                            viewModel = profileVm,
                            userId = userId,
                            authViewModel = authViewModel
                        )
                    }

                }
            }
        }
    }