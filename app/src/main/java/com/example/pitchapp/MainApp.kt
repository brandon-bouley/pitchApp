package com.example.pitchapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.pitchapp.data.remote.LastFmApi
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.data.repository.ReviewRepository
import com.example.pitchapp.ui.screens.search.AlbumDetailScreen
import com.example.pitchapp.ui.screens.profile.ProfileScreen
import com.example.pitchapp.viewmodel.AlbumDetailViewModel
import com.example.pitchapp.viewmodel.FeedViewModel
import com.example.pitchapp.viewmodel.ProfileViewModel
import com.example.pitchapp.viewmodel.ReviewViewModel
import com.example.pitchapp.viewmodel.SearchViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MainApp(
    feedViewModelFactory: FeedViewModelFactory,
    reviewViewModelFactory: ReviewViewModelFactory,
    searchViewModelFactory: SearchViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory,
) {
    val navController = rememberNavController()

    Scaffold(bottomBar = { BottomNavBar(navController) }) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Feed.route) {
                FeedScreen(
                    navController = navController,
                    viewModel = viewModel<FeedViewModel>(factory = feedViewModelFactory),
                    reviewViewModel = viewModel<ReviewViewModel>(factory = reviewViewModelFactory)
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    navController = navController,
                    viewModel = viewModel<SearchViewModel>(factory = searchViewModelFactory),
                    reviewViewModel = viewModel<ReviewViewModel>(factory = reviewViewModelFactory)
                )
            }
            composable(Screen.Results.route) {
                ResultScreen(
                    navController = navController,
                    viewModel = viewModel<SearchViewModel>(factory = searchViewModelFactory)
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
                val albumId = backStackEntry.arguments?.getString(Screen.AlbumDetail.ARG_ALBUM_ID)
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



                AlbumDetailScreen(
                        albumId = albumId,
                        viewModel = viewModel,
                        reviewViewModel = viewModel<ReviewViewModel>(factory = reviewViewModelFactory),
                        navController = navController

                    )

            }
            composable(Screen.AddReview.route) {
                val reviewVm = viewModel<ReviewViewModel>(factory = reviewViewModelFactory)
                val searchVm = viewModel<SearchViewModel>(factory = searchViewModelFactory)
                AddReviewScreen(
                    navController = navController,
                    reviewViewModel = reviewVm,
                    searchViewModel = searchVm
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
                    userId = userId
                )
            }
        }
    }
}
