package com.example.pitchapp.data.model

sealed class FeedItem {
    data class ReviewItem(
        val review: Review,
        val album: Album? = null
    ) : FeedItem()

    data class AlbumItem(
        val album: Album,
        val averageRating: Float = 0f,
        val popularity: Int
    ) : FeedItem()

    data class SectionHeader(val title: String) : FeedItem()
}

