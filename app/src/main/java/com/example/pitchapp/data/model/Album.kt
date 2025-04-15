package com.example.pitchapp.data.model
import com.example.pitchapp.data.model.Artist



data class Album(
    val id: String,
    val albumType: String,
    val artists: List<Artist>,
    val totalTracks: Int,
    val href: String,
    val name: String,
    val releaseDate: String?,
    val releaseDateFormat: String,
    val isrc: String?,
    val ean: String?,
    val upc: String?,
    val label: String,
    val popularity: Int
) {
    val formattedReleaseDate: String
        get() = when (releaseDateFormat) {
            "day" -> releaseDate?.take(10) ?: "Unknown"
            "month" -> releaseDate?.take(7) ?: "Unknown"
            "year" -> releaseDate?.take(4) ?: "Unknown"
            else -> "Unknown"
        }
}

