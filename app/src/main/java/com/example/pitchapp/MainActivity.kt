package com.example.pitchapp

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
import com.example.pitchapp.data.model.MusicViewModel
import com.example.pitchapp.data.model.ReviewViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Track
import androidx.compose.runtime.livedata.observeAsState
import com.example.pitchapp.data.GeniusApiService
import com.example.pitchapp.data.local.PitchDatabase
import com.example.pitchapp.data.remote.BuildApi
import com.example.pitchapp.data.repository.MusicRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val reccoApi = BuildApi
        val database = PitchDatabase.getDatabase(applicationContext)
        val reviewDao = database.reviewDao()


        setContent {
                val navController = rememberNavController()
                NavGraph(navController = navController)

        }
    }
    private fun createReccoApi(): GeniusApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.genius.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }
}


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchScreen(navController)
        }
        composable("results") {
            ResultScreen(navController)
        }
    }
}


@Composable
fun SearchScreen(navController: NavController, viewModel: MusicViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Search for tracks or Albums", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search Query") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.search(query)
                navController.navigate("results")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
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
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { item ->
                    when (item) {
                        is Track -> TrackCard(track = item)
                        is Album -> AlbumCard(album = item)
                    }
                }
            }
        }
    }
}

@Composable
fun TrackCard(track: Track) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("track: ${track.title}", style = MaterialTheme.typography.titleMedium)
            Text("Artist: ${track.artist}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AlbumCard(album: Album) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Album: ${album.name}", style = MaterialTheme.typography.titleMedium)
            Text("Artist: ${album.artist}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
