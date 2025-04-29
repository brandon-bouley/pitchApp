package com.example.pitchapp
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pitchapp.data.remote.LastFmApi
import com.example.pitchapp.data.repository.FeedRepository
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.data.repository.ProfileRepository
import com.example.pitchapp.data.repository.ReviewRepository
import com.example.pitchapp.viewmodel.AlbumDetailViewModel
import com.example.pitchapp.viewmodel.AuthViewModel
import com.example.pitchapp.viewmodel.FeedViewModel
import com.example.pitchapp.viewmodel.ProfileViewModel
import com.example.pitchapp.viewmodel.ReviewViewModel
import com.example.pitchapp.viewmodel.SearchViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.initialize


class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        return ProfileViewModel(
            repository = profileRepository,
        ) as T
    }
}

// FeedViewModelFactory
class FeedViewModelFactory(
    private val repository: FeedRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        return FeedViewModel(
            feedRepository = repository,
        ) as T
    }
}

// ReviewViewModelFactory
class ReviewViewModelFactory(
    private val reviewRepository: ReviewRepository,
    private val authViewModel: AuthViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            return ReviewViewModel(reviewRepository, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// SearchViewModelFactory
class SearchViewModelFactory(
    private val musicRepository: MusicRepository,
    private val reviewRepository: ReviewRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        return SearchViewModel(
            musicRepository = musicRepository,
            reviewRepository = reviewRepository,
        ) as T
    }
}

class AlbumDetailViewModelFactory(
    private val musicRepository: MusicRepository,
    private val reviewRepository: ReviewRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        return AlbumDetailViewModel(
            musicRepository = musicRepository,
            reviewRepository = reviewRepository,
            savedStateHandle = extras.createSavedStateHandle()
        ) as T
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dependency setup
        val api = LastFmApi.service
        val musicRepository = MusicRepository(api)

        // Repository initialization
        val reviewRepository = ReviewRepository()
        val feedRepository = FeedRepository(
            reviewRepository = reviewRepository,
            musicRepository = musicRepository,
        )
        val profileRepository = ProfileRepository()
        // ViewModel factories

        val feedViewModelFactory = FeedViewModelFactory(feedRepository)
        val searchViewModelFactory = SearchViewModelFactory(musicRepository, reviewRepository)
        val profileViewModelFactory = ProfileViewModelFactory(profileRepository)

        setContent {
            PitchAppTheme {
                FirebaseAuthHandler(
                    musicRepository = musicRepository,
                    reviewRepository = reviewRepository,
                    feedViewModelFactory = feedViewModelFactory,
                    searchViewModelFactory = searchViewModelFactory,
                    profileViewModelFactory = profileViewModelFactory
                )
            }
        }
    }
}

@Composable
private fun FirebaseAuthHandler(
    musicRepository: MusicRepository,
    reviewRepository: ReviewRepository,
    feedViewModelFactory: FeedViewModelFactory,
    searchViewModelFactory: SearchViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory,

) {


    MainApp(
        feedViewModelFactory = feedViewModelFactory,
        searchViewModelFactory = searchViewModelFactory,
        profileViewModelFactory = profileViewModelFactory
    )
    }


@Composable
fun PitchAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFFA3C1D3)
        ),
        content = content
    )
}




