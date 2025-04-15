package com.example.pitchapp.data.repository

import android.util.Log
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.FeedItem
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.local.ReviewDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedRepository(
    private val reviewDao: ReviewDao,
    private val musicRepo: MusicRepository
) {
    suspend fun getPopularAlbumsFromReviews(limit: Int = 10): List<FeedItem.AlbumItem> {
        return try {

            val allReviews = reviewDao.getAllReviews()
            val albumReviewMap = allReviews.groupBy { it.albumId }


            albumReviewMap.mapNotNull { (albumId, reviews) ->

                when (val result = musicRepo.getAlbumDetails(albumId)) {
                    is MusicRepository.Result.Success -> {
                        val album = result.data
                        val avgRating = reviews.map { it.rating }.average().toFloat()
                        FeedItem.AlbumItem(
                            album = album,
                            averageRating = avgRating,
                            popularity = album.popularity
                        )
                    }
                    is MusicRepository.Result.Error -> null
                }
            }.sortedByDescending { it.popularity }
                .take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getRecentReviewItems(limit: Int = 10): List<FeedItem.ReviewItem> {
        return reviewDao.getRecentReviews(limit).map { review ->
            val albumResult = musicRepo.getAlbumDetails(review.albumId)
            FeedItem.ReviewItem(
                review = review,
                album = (albumResult as? MusicRepository.Result.Success)?.data
            )
        }
    }
}