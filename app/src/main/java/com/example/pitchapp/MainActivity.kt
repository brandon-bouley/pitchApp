package com.example.pitchapp


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import kotlinx.coroutines.flow.first
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.pitchapp.data.Review
import com.example.pitchapp.data.GeniusApiService
import com.example.pitchapp.data.PitchDatabase
import com.example.pitchapp.data.ReviewDao
import com.example.pitchapp.ui.theme.PitchAppTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.text.SimpleDateFormat
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize API client and database
        val geniusApi = createGeniusApi()
        val database = PitchDatabase.getDatabase(applicationContext)
        val reviewDao = database.reviewDao()

        setContent {
            PitchAppTheme {
                // Navigation controller manages screen transitions
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "search"
                ) {
                    // Search screen declaration
                    composable("search") {
                        SearchScreen(
                            geniusApi = geniusApi,
                            onItemClick = { song ->
                                // Navigate to detail screen with song ID parameter
                                navController.navigate("detail/${song.id}")
                            }
                        )
                    }

                    /**
                     //Detail screen with required songId parameter.
                     * Uses NavType.StringType for type safety.
                     */
                    composable(
                        "detail/{songId}",
                        arguments = listOf(navArgument("songId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val songId = backStackEntry.arguments?.getString("songId") ?: ""
                        DetailScreen(
                            songId = songId,
                            geniusApi = geniusApi,
                            reviewDao = reviewDao,
                            onReviewClick = { navController.navigate("review/$songId") },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    /**
                     * Review creation screen with songId parameter
                     */
                    composable(
                        "review/{songId}",
                        arguments = listOf(navArgument("songId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val songId = backStackEntry.arguments?.getString("songId") ?: ""
                        ReviewScreen(
                            songId = songId,
                            reviewDao = reviewDao,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    /**
     * Initializes Retrofit instance for Genius API communication.
     * Configures:
     * - Base API URL
     * - GSON converter for JSON parsing
     * - Authentication header via access token
     */
    private fun createGeniusApi(): GeniusApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.genius.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }
}

/**
 * Search screen component handling:
 * - User input for search queries
 * - API request management
 * - Result display and error states
 */
@Composable
fun SearchScreen(
    geniusApi: GeniusApiService,
    onItemClick: (GeniusApiService.Song) -> Unit
) {
    // State management for search functionality
    var query by remember { mutableStateOf("") }
    val searchResults = remember { mutableStateListOf<GeniusApiService.Song>() }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    /**
     * LaunchedEffect triggers API call when query changes.
     */
    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            isLoading = true
            errorMessage = null
            try {
                val response = geniusApi.search(query)
                val results = response.body()
                    ?.response  // Access response wrapper
                    ?.hits      // Get list of search hits
                    ?.map { hit: GeniusApiService.Hit -> hit.result }  // Extract song data
                    ?: emptyList()

                searchResults.clear()
                searchResults.addAll(results)
            } catch (e: Exception) {
                errorMessage = "Search failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        } else {
            searchResults.clear()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Search input field with rounded corners
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp),
                placeholder = { Text("Search songs...") },
                shape = RoundedCornerShape(16.dp)
            )

            // Results list using lazy loading for performance
            if (searchResults.isNotEmpty()) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(searchResults) { song ->
                        SongItem(song = song, onItemClick = onItemClick)
                    }
                }
            }
        }

        // State overlay components
        when {
            isLoading -> CircularProgressIndicator()
            errorMessage != null -> Text(
                errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
            searchResults.isEmpty() && query.isNotEmpty() -> Text(
                "No results found",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * Detail screen showing:
 * - Album/song metadata from Genius API
 * - User reviews from local database
 * - Navigation to review creation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    songId: String,
    geniusApi: GeniusApiService,
    reviewDao: ReviewDao,
    onReviewClick: () -> Unit,
    onBack: () -> Unit
) {
    // State management for detail view
    var songDetails by remember { mutableStateOf<GeniusApiService.Song?>(null) }
    val reviews = remember { mutableStateListOf<Review>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    /**
     * Combined data loading effect:
     * 1. Fetches song details from Genius API
     * 2. Loads reviews from local database
     * Runs once when screen is opened
     */
    LaunchedEffect(songId) {
        try {
            // API call for detailed song information
            val detailsResponse = geniusApi.getSongDetails(songId)
            songDetails = detailsResponse.body()?.response?.song

            // Database query for persisted reviews
            val dbReviews = reviewDao.getReviewsForSong(songId).first()
            reviews.clear()
            reviews.addAll(dbReviews)
        } catch (e: Exception) {
            errorMessage = "Failed to load details: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Album Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(
                    errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
                songDetails != null -> {
                    // Album art display with Coil image loading
                    AsyncImage(
                        model = songDetails!!.thumbnail,
                        contentDescription = "Album art"
                    )
                    Text(songDetails!!.title, style = MaterialTheme.typography.headlineMedium)
                    Text(songDetails!!.artist, style = MaterialTheme.typography.titleMedium)

                    // Reviews list with automatic updates from database
                    LazyColumn {
                        items(reviews) { review ->
                            ReviewItem(review = review)
                        }
                    }

                    // Review creation trigger
                    Button(onClick = onReviewClick) {
                        Text("Write a Review")
                    }
                }
            }
        }
    }
}

/**
 * Review creation screen handling:
 * - Rating input via slider
 * - Text review input
 * - Database persistence
 */
@Composable
fun ReviewScreen(
    songId: String,
    reviewDao: ReviewDao,
    onBack: () -> Unit
) {
    // Form state management
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Centered form layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Rating input section
            Text(
                "Rate this song",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Slider(
                value = rating.toFloat(),
                onValueChange = { rating = it.toInt() },
                valueRange = 1f..10f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Review text input
            TextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 24.dp),
                placeholder = { Text("Write your review...") }
            )

            // Submission button with coroutine handling
            Button(
                onClick = {
                    coroutineScope.launch {
                        reviewDao.insert(
                            Review(
                                songId = songId,
                                rating = rating,
                                comment = comment
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier.width(200.dp)
            ) {
                Text("Submit Review")
            }
        }
    }
}


// Song Item Component
@Composable
fun SongItem(song: GeniusApiService.Song, onItemClick: (GeniusApiService.Song) -> Unit) {
    Card(
        onClick = { onItemClick(song) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = song.thumbnail,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(song.title, style = MaterialTheme.typography.bodyLarge)
                Text(song.artist, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// Review Item Component
@Composable
fun ReviewItem(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "Rating:",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "${review.rating}/10",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Comment
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Timestamp
            Text(
                text = formatTimestamp(review.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
