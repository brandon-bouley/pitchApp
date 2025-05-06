package com.example.pitchapp.data.repository

import android.util.Log
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.data.model.Review
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import com.example.pitchapp.data.model.Result


class FeedRepository(
    private val reviewRepository: ReviewRepository,
    private val musicRepository: MusicRepository
) {

    suspend fun getPopularAlbumsFromReviews(limit: Int = 10): List<FeedItem.AlbumItem> {
        return try {
            when (val reviewsResult = reviewRepository.getAllReviews()) {
                is Result.Success -> processPopularAlbums(reviewsResult.data, limit)
                is Result.Error -> {
                    Log.e("FEED_REPO", "Failed to get reviews", reviewsResult.exception)
                    emptyList()
                }
                else -> throw IllegalStateException("Unexpected result type")
            }
        } catch (e: Exception) {
            Log.e("FEED_REPO", "Popular albums error", e)
            throw e
        }
    }

    private suspend fun processPopularAlbums(
        allReviews: List<Review>,
        limit: Int
    ): List<FeedItem.AlbumItem> {
        val albumMap = mutableMapOf<String, Pair<Album, List<Review>>>()

        // Group reviews by album ID
        val groupedReviews = allReviews.groupBy { it.albumId }

        // Fetch album details in parallel
        coroutineScope {
            groupedReviews.forEach { (albumId, reviews) ->
                launch {
                    when (val albumResult = musicRepository.getAlbumFromFirestore(albumId!!)) {
                        is Result.Success -> {
                            albumMap[albumId] = Pair(albumResult.data, reviews)
                        }
                        is Result.Error -> {
                            Log.w("FEED_REPO", "Failed to fetch album $albumId", albumResult.exception)
                        }
                        else -> throw IllegalStateException("Unexpected result type")
                    }
                }
            }
        }

        return albumMap.values
            .map { (album, reviews) ->
                FeedItem.AlbumItem(
                    album = album,
                    averageRating = reviews.map { it.rating }.average().toFloat()
                )
            }
            .sortedByDescending { it.averageRating }
            .take(limit)
    }

    suspend fun getRecentReviewItems(limit: Int = 10): List<Review> {
        return try {
            when (val result = reviewRepository.getRecentReviews(limit)) {
                is Result.Success -> result.data
                is Result.Error -> throw result.exception
                else -> throw IllegalStateException("Unexpected result type")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun processRecentReviews(
        reviews: List<Review>
    ): List<FeedItem.ReviewItem> {
        return reviews.map { review ->
            val album = when (val result = musicRepository.getAlbumFromFirestore(review.albumId!!)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    Log.w("FEED_REPO", "Missing album ${review.albumId}", result.exception)
                    null
                }
                else -> throw IllegalStateException("Unexpected result type")
            }

            FeedItem.ReviewItem(
                review = review,
                album = album
            )
        }
    }
}