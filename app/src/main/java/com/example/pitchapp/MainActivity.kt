package com.example.pitchapp
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
import com.example.pitchapp.viewmodel.MusicViewModel
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
import com.example.pitchapp.data.local.PitchDatabase
import com.example.pitchapp.data.model.UserPreference


class MusicViewModelFactory(
    private val repository: MusicRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            return MusicViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val api = BuildApi.api
        val musicRepository = MusicRepository(api)
        val factory = MusicViewModelFactory(musicRepository)
            //test script starts
        val db = PitchDatabase.getDatabase(applicationContext)
        val userDao = db.userPreferenceDao()

        lifecycleScope.launch {
            // Insert sample data
            val testUser = UserPreference(
                username = "jane_doe",
                name = "Jane Doe",
                age = 30,
                bio = "Music Enthusiast",
                email = "jane@example.com",
                darkMode = true,
                ratedItems = listOf("album:001", "artist:xyz")
            )

            userDao.insertOrUpdatePreference(testUser)

            // Retrieve and log the data
            val loadedUser = userDao.getPreference("jane_doe")
            Log.d("DB_TEST", "Loaded user: $loadedUser")
        }
        //test script ends
        setContent {
                val navController = rememberNavController()
                NavGraph(navController = navController,factory=factory)

        }
    }

}


@Composable
fun NavGraph(navController: NavHostController, factory: MusicViewModelFactory) {
    val viewModel: MusicViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchScreen(navController, viewModel)
        }
        composable("results") {
            ResultScreen(navController, viewModel)
        }
    }
}


@Composable
fun SearchScreen(navController: NavController, viewModel: MusicViewModel) {
    var query by remember { mutableStateOf("") }
    var searchType by remember { mutableStateOf("artist") } // "artist" or "album"
    val searchResults by viewModel.searchResults.observeAsState(emptyList())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Select Search Type", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { searchType = "artist" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (searchType == "artist") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Search Artists")
            }

            Button(
                onClick = { searchType = "album" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (searchType == "album") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Search Albums")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Enter $searchType name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (searchType == "artist") {
                    viewModel.searchArtists(query)
                } else {
                    viewModel.searchAlbums(query)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search $searchType")
        }
        LaunchedEffect(searchResults) {
            if (searchResults.isNotEmpty()) {
                println("navigating to results")
                navController.navigate("results")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(navController: NavController, viewModel: MusicViewModel = viewModel()) {
    val results by viewModel.searchResults.observeAsState(emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Search Results", style = MaterialTheme.typography.titleLarge)
                }
            )
        }
    ) { padding ->
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("No results found.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(padding)
            ) {
                items(results.size) { index ->
                    when (val item = results[index]) {
                        is Artist -> TrackCard(track = item)
                        is Album -> AlbumCard(album = item)
                        else -> Text("Unknown item type")
                    }
                }
            }
        }
    }
}

@Composable
fun TrackCard(track: Artist) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Artist: ${track.name}", style = MaterialTheme.typography.titleMedium)
            Text("Profile: ${track.href}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AlbumCard(album: Album) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Album: ${album.name}", style = MaterialTheme.typography.titleMedium)
            Text(
                "Artist(s): ${album.artists.joinToString { it.name }}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
