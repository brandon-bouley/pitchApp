package com.example.pitchapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.pitchapp.data.model.Album
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.google.firebase.firestore.FirebaseFirestoreSettings

@RunWith(AndroidJUnit4::class)
class FirebaseAlbumTest {

    private lateinit var firestore: FirebaseFirestore
    private val testAlbumId = "test_album_123"

    @Before
    fun setup() {
        // Initialize Firebase with your configuration
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val options = FirebaseOptions.Builder()
            .setProjectId("newpitchdb")
            .setApplicationId("1:12057886075:android:b6040fc6fe4722fc0291de")
            .setApiKey("AIzaSyDgvFcdn3sl2zzK9MZwXS5xDIZSwp7v6ns")
            .setStorageBucket("newpitchdb.firebasestorage.app")
            .build()

        try {
            FirebaseApp.initializeApp(context, options)
        } catch (e: IllegalStateException) {
            FirebaseApp.getInstance()
        }

        // Connect to Firestore emulator
        firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080) // Android emulator localhost
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .build()
    }

    @Test
    fun addAlbumToFirestore() = runBlocking {
        // Create test album
        val testAlbum = Album(
            id = testAlbumId,
            title = "Test Album",
            artist = "Test Artist",
            artworkUrl = "https://example.com/test.jpg",
            tracks = listOf(
                Album.Track(
                    title = "Test Track 1",
                    duration = 180,
                    position = 1
                )
            )
        )

        // Add to Firestore
        val documentRef = firestore.collection("albums").document(testAlbumId)
        documentRef.set(testAlbum).await()

        // Verify
        val snapshot = documentRef.get().await()
        assertTrue(snapshot.exists(), "Album should exist in Firestore")
        assertEquals("Test Album", snapshot.getString("title"), "Title should match")
        assertEquals("Test Artist", snapshot.getString("artist"), "Artist should match")
    }

    @After
    fun cleanup() {
        runBlocking {
            firestore.collection("albums").document(testAlbumId)
                .delete()
                .await()
        }
    }
}