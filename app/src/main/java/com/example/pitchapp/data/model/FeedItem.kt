package com.example.pitchapp.data.model

sealed class FeedItem {
    data class ReviewItem(
        val review: Review,
        val album: Album? = null,
        val track: RandoTrack? = null
    ) : FeedItem()

    data class AlbumItem(
        val album: Album,
        val averageRating: Float = 0f,
    ) : FeedItem()

    data class SectionHeader(val title: String) : FeedItem()
    data class TrackItem(val track: RandomTrack) : FeedItem()
}

