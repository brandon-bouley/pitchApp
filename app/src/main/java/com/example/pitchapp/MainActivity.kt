package com.example.pitchapp
import android.app.Activity
import androidx.compose.runtime.livedata.observeAsState
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyColumn
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Artist
import com.example.pitchapp.data.remote.BuildApi
import com.example.pitchapp.data.repository.MusicRepository
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.pitchapp.data.local.PitchDatabase
import com.example.pitchapp.data.local.UserPreferenceDao
import com.example.pitchapp.data.model.UserPreference
import com.example.pitchapp.data.repository.FeedRepository
import com.example.pitchapp.data.repository.ReviewRepository
import com.example.pitchapp.viewmodel.AlbumDetailViewModel
import com.example.pitchapp.viewmodel.FeedViewModel
import com.example.pitchapp.viewmodel.ProfileViewModel
import com.example.pitchapp.viewmodel.ReviewViewModel
import com.example.pitchapp.viewmodel.SearchViewModel
import com.example.pitchapp.ui.theme.PitchAppTheme

class ProfileViewModelFactory(
    private val userPrefDao: UserPreferenceDao,
    private val reviewRepo: ReviewRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userPrefDao, reviewRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


class SearchViewModelFactory(
    private val musicRepository: MusicRepository,
    private val reviewRepository: ReviewRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(musicRepository, reviewRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


class FeedViewModelFactory(
    private val repository: FeedRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FeedViewModel(repository) as T
    }
}

class ReviewViewModelFactory(
    private val repository: ReviewRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReviewViewModel(repository) as T
    }
}

class AlbumDetailViewModelFactory(
    private val musicRepository: MusicRepository,
    private val reviewRepository: ReviewRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlbumDetailViewModel(musicRepository, reviewRepository) as T
    }
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = PitchDatabase.getDatabase(this).userPreferenceDao()
        lifecycleScope.launch {
            if (dao.countUsers() == 0) {
                dao.insert(UserPreference(username   = "current_user",
                    name       = "John Doe",
                    age        = 21,
                    bio        = "I am super obsessed with music! And Kotlin.",
                    email      = "dev@example.com",
                    darkMode   = false,
                    ratedItems = emptyList()
                ))
            }
        }




        // Dependency setup
        val api = BuildApi.api
        val musicRepository = MusicRepository(api)
        val db = PitchDatabase.getDatabase(applicationContext)

        // Repository initialization
        val reviewDao = db.reviewDao()
        val feedRepository = FeedRepository(reviewDao, musicRepository)
        val reviewRepository = ReviewRepository(reviewDao)

        // ViewModel factories
        val feedViewModelFactory = FeedViewModelFactory(feedRepository)
        val reviewViewModelFactory = ReviewViewModelFactory(reviewRepository)
        val searchViewModelFactory = SearchViewModelFactory(musicRepository, reviewRepository)
        val albumDetailViewModelFactory = AlbumDetailViewModelFactory(musicRepository, reviewRepository)



        setContent {
                MainApp(
                    feedViewModelFactory = feedViewModelFactory,
                    reviewViewModelFactory = reviewViewModelFactory,
                    searchViewModelFactory = searchViewModelFactory,
                    albumDetailViewModelFactory = albumDetailViewModelFactory
                )

        }
    }
}





