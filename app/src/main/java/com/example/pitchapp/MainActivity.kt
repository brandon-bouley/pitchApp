package com.example.pitchapp
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras

import com.example.pitchapp.data.remote.LastFmApi
import com.example.pitchapp.data.repository.FeedRepository
import com.example.pitchapp.data.repository.MusicRepository
import com.example.pitchapp.data.repository.ProfileRepository
import com.example.pitchapp.data.repository.ReviewRepository
import android.hardware.SensorManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlin.random.Random
import android.hardware.Sensor
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.ui.components.ShakeDetector




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

//class RandomTrackViewModelFactory(
//    private val repository: TrackReviewRepository,
//    private val authViewModel: AuthViewModel
//):  ViewModelProvider.Factory {
//    @Suppress("UNCHECKED_CAST")
//    override fun <T : ViewModel> create(
//        modelClass: Class<T>,
//        extras: CreationExtras
//    ): T {
//        return TrackReviewViewModel(
//            repository = repository,
//            authViewModel = authViewModel
//
//        ) as T
//    }
//}

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
    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector

    private lateinit var onShake: () -> Unit

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
//        val trackDetailFactory = TrackDetailViewModelFactory(musicRepository, trackRepository)

        val feedViewModelFactory = FeedViewModelFactory(feedRepository)
        val searchViewModelFactory = SearchViewModelFactory(musicRepository, reviewRepository)
        val profileViewModelFactory = ProfileViewModelFactory(profileRepository)
        onShake = {}
        shakeDetector = ShakeDetector { onShake() }
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager




        setContent {
            var showDialog by remember { mutableStateOf(false) }
            var selectedTrack by remember { mutableStateOf<Album?>(null)}
            var shouldReviewTrack by remember { mutableStateOf(false) }



            onShake = {
                CoroutineScope(Dispatchers.Main).launch {
                    val tracks = musicRepository.getTopTracks()
                    if (tracks.isNotEmpty()) {
                        val randomTrack = tracks[Random.nextInt(tracks.size)]
                       selectedTrack = randomTrack
                        showDialog = true
                    }
                }

            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Random Track 🎵") },
                    text = {
                        Column {
                            Text(
                                text = "🎵 ${selectedTrack?.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "👤 Artist: ${selectedTrack?.artist}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "🔊 Playcount: ${selectedTrack?.playCount}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "📥 Listeners: ${selectedTrack?.listeners}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "🌐 Tap to open track URL",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Blue
                            )
                        }
                    },
                    confirmButton = {
                        Row {
                            TextButton(onClick = {showDialog = false }) {
                                Text("Close")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            TextButton(onClick = {
                                shouldReviewTrack = true
                                showDialog = false

                            }) {
                                Text("Review this song")
                            }
                        }
                    }
                )
            }


            PitchAppTheme {
                FirebaseAuthHandler(
                    musicRepository = musicRepository,
                    reviewRepository = reviewRepository,
                    feedViewModelFactory = feedViewModelFactory,
                    searchViewModelFactory = searchViewModelFactory,
                    profileViewModelFactory = profileViewModelFactory,

                    selectedTrack = selectedTrack,
                    shouldReviewTrack = shouldReviewTrack,
                    onReviewComplete = { shouldReviewTrack = false }
                )
            }

        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            shakeDetector,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeDetector)
    }

}

@Composable
private fun FirebaseAuthHandler(
    musicRepository: MusicRepository,
    reviewRepository: ReviewRepository,
    feedViewModelFactory: FeedViewModelFactory,
    searchViewModelFactory: SearchViewModelFactory,
    profileViewModelFactory: ProfileViewModelFactory,
    selectedTrack: Album?,
    shouldReviewTrack: Boolean,
    onReviewComplete: () -> Unit


) {
    val authViewModel: AuthViewModel = viewModel()

    val reviewViewModelFactory = ReviewViewModelFactory(
        reviewRepository = reviewRepository,
        authViewModel = authViewModel
    )
//    val trackReviewRepository = TrackReviewRepository()
//    val trackViewModelFactory = RandomTrackViewModelFactory(trackReviewRepository,authViewModel )
    MainApp(
        feedViewModelFactory = feedViewModelFactory,
        searchViewModelFactory = searchViewModelFactory,
        profileViewModelFactory = profileViewModelFactory,
        reviewViewModelFactory = reviewViewModelFactory,
        selectedTrack = selectedTrack,
        shouldReviewTrack = shouldReviewTrack,
        onReviewComplete = onReviewComplete,
        authViewModel = authViewModel,
        musicRepository = musicRepository,
        reviewRepository = reviewRepository,
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




