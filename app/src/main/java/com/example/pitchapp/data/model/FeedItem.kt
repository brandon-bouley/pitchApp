package com.example.pitchapp.data.model

sealed class FeedItem {
    data class ReviewItem(
        val review: Review?=null,
        val trackReview: TrackReview? = null,
        val album: Album? = null,
        val track: RandomTrack? = null
    ) : FeedItem()

    data class AlbumItem(
        val album: Album,
        val averageRating: Float = 0f,
    ) : FeedItem()

    data class SectionHeader(val title: String) : FeedItem()
    data class TrackItem(val track: RandomTrack) : FeedItem()
}

