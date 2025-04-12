package com.example.pitchapp.data.repository
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.pitchapp.data.local.dao.AlbumDao
import com.example.pitchapp.data.local.dao.TrackDao
import com.example.pitchapp.data.remote.ReccoBeatsApiService
import com.example.pitchapp.data.local.dao.ReviewDao
import com.example.pitchapp.data.model.Review

class MusicRepository(
    private val apiService: ReccoBeatsApiService,
    private val trackDao: TrackDao,
    private val albumDao: AlbumDao
) {
    fun searchMusic(query: String): LiveData<List<Any>> = liveData {
        val localtracks = trackDao.searchTracks("%$query%")
        val localAlbums = albumDao.searchAlbums("%$query%")
        if (localtracks.isNotEmpty() || localAlbums.isNotEmpty()) {
            emit(localtracks + localAlbums)
        } else {
            val response = apiService.search(query)
            trackDao.insertAll(response.tracks)
            albumDao.insertAll(response.albums)
            emit(response.tracks + response.albums)
        }
    }
}
class ReviewRepository(private val reviewDao: ReviewDao) {

    suspend fun getReviewsForItem(itemId: String, itemType: String): List<Review> {
        return reviewDao.getReviews(itemId, itemType)
    }

    suspend fun addReview(review: Review) {
        reviewDao.insertReview(review)
    }

    suspend fun deleteReview(review: Review) {
        reviewDao.deleteReview(review)
    }
}