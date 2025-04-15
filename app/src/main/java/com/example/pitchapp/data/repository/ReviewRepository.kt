package com.example.pitchapp.data.repository

import com.example.pitchapp.data.local.ReviewDao
import com.example.pitchapp.data.model.Review

class ReviewRepository(private val reviewDao: ReviewDao) {
    suspend fun insertReview(review: Review) = reviewDao.insert(review)

    suspend fun getReviewsByUser(username: String) = reviewDao.getReviewsByUser(username)

    suspend fun getReviewsForAlbum(albumId: String) = reviewDao.getReviewsForAlbum(albumId)

    suspend fun getRecentReviews(limit: Int = 50) = reviewDao.getRecentReviews(limit)

    suspend fun getAverageRating(albumId: String) = reviewDao.getAverageRating(albumId)

}