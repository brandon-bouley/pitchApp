package com.example.pitchapp

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.RandomTrack
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
import com.example.pitchapp.ui.screens.review.AddTrackReviewScreen
import com.example.pitchapp.viewmodel.AuthViewModel
import com.example.pitchapp.viewmodel.TrackReviewViewModel



@Composable
fun MainApp(
    feedViewModelFactory: FeedViewModelFactory,
    reviewViewModelFactory: ReviewViewModelFactory,
    searchViewModelFactory: SearchViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory,
    authViewModel: AuthViewModel,
    musicRepository: MusicRepository,
    reviewRepository: ReviewRepository
    profileViewModelFactory: ProfileViewModelFactory,
    trackReviewViewModelFactory: RandomTrackViewModelFactory,
    selectedTrack: RandomTrack?,
    shouldReviewTrack: Boolean,
    onReviewComplete: ()->Unit

) {

    Log.d("Selected Track","$selectedTrack")
    val navController = rememberNavController()
    val systemDarkTheme = isSystemInDarkTheme()
    val trackReviewViewModel: TrackReviewViewModel = viewModel(factory = trackReviewViewModelFactory)


    var darkTheme by remember { mutableStateOf(systemDarkTheme) }
    val authViewModel = viewModel<AuthViewModel>()
    LaunchedEffect(shouldReviewTrack, selectedTrack) {
        if (shouldReviewTrack && selectedTrack != null) {
            trackReviewViewModel.setSelectedTrack(selectedTrack)
            navController.navigate("add_track_review")
            onReviewComplete()
        }
    }


    PitchAppTheme(darkTheme = darkTheme) {
        Scaffold(bottomBar = { BottomNavBar(navController) }) { padding ->

            Column {
                LogoHeader(
                    darkTheme = darkTheme,
                    onToggle = { darkTheme = it }
                )

                NavHost(
                    navController = navController,
                    startDestination = Screen.Feed.route,
                    modifier = Modifier.padding(padding)
                ) {
                    composable(Screen.Feed.route) {
                        val feedVm = viewModel<FeedViewModel>(factory = feedViewModelFactory)
                        val reviewVm = viewModel<ReviewViewModel>(factory = reviewViewModelFactory)

                        FeedScreen(
                            navController = navController,
                            viewModel = feedVm,
                            reviewViewModel = reviewVm
                        )
                    }

                    composable(Screen.Search.route) {
                        val searchVm = viewModel<SearchViewModel>(factory = searchViewModelFactory)
                        val reviewVm = viewModel<ReviewViewModel>(factory = reviewViewModelFactory)

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

                    composable("login") {
                        LoginScreen(
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }


                    composable("signup") {
                        SignUpScreen(
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }

                    composable(
                        route = Screen.AlbumDetail.route,
                        arguments = listOf(
                            navArgument(Screen.AlbumDetail.ARG_ALBUM_ID) {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val albumId =
                            backStackEntry.arguments?.getString(Screen.AlbumDetail.ARG_ALBUM_ID)
                        val viewModel: AlbumDetailViewModel = viewModel(
                            factory = AlbumDetailViewModelFactory(
                                musicRepository = MusicRepository(LastFmApi.service),
                                reviewRepository = ReviewRepository()
                            )
                        )

                        if (albumId == null) {
                            Text("Error: Missing album ID")
                            return@composable
                        }

                        val albumDetailViewModel = viewModel<AlbumDetailViewModel>(
                            factory = AlbumDetailViewModelFactory(
                                musicRepository = musicRepository,
                                reviewRepository = reviewRepository
                            )
                        )

                        val reviewViewModel = viewModel<ReviewViewModel>(
                            factory = reviewViewModelFactory
                        )

                        AlbumDetailScreen(
                            albumId = albumId,
                            viewModel = albumDetailViewModel,
                            reviewViewModel = reviewViewModel,
                            navController = navController
                        )
                    }


                    composable(
                        route = Screen.AddReview.route,
                        arguments = listOf(
                            navArgument(Screen.AddReview.ARG_ALBUM_ID) { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val albumId = backStackEntry.arguments?.getString(Screen.AddReview.ARG_ALBUM_ID)
                            ?: return@composable

                        val reviewVm = viewModel<ReviewViewModel>(factory = reviewViewModelFactory)
                        val searchVm = viewModel<SearchViewModel>(factory = searchViewModelFactory)

                        AddReviewScreen(
                            navController = navController,
                            reviewViewModel = reviewVm,
                            searchViewModel = searchVm,
                            authViewModel = authViewModel,
                            musicRepository = musicRepository,
                            albumId = albumId
                        )


                        )
                    }

                    composable("add_track_review") {
                         AddTrackReviewScreen(
                             navController = navController,
                             trackReviewViewModel = trackReviewViewModel
                         )
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
                            profileUserId = userId,
                            authViewModel = authViewModel
                        )
                    }

                }
            }
        }
    }
}
