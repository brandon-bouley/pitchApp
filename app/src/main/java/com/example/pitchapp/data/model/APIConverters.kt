package com.example.pitchapp.data.model

import com.example.pitchapp.data.repository.MusicRepository.Companion.generateAlbumId
import com.example.pitchapp.data.repository.MusicRepository.Companion.generateTrackId

// --- Artist mapping ---
fun ApiArtist.toDomainArtist(): Artist {
    val imageUrl = image.firstOrNull { it.size == "medium" }?.url
        ?: image.firstOrNull()?.url
        ?: ""
    val listenersInt = listeners.toIntOrNull() ?: 0

    return Artist(
        name      = name,
        mbid      = mbid,
        imageUrl  = imageUrl,
        listeners = listenersInt,
        lastFmUrl = url
    )
}

// --- Album mapping for top‐albums endpoint ---
fun ApiAlbumSimple.toDomainAlbum(): Album {
    return Album(
        id = generateAlbumId(artist.name, name),
        title = name,
        artist = artist.name,
        artworkUrl = image.lastOrNull { it.size == "extralarge" }?.url ?: "",
        lastFmUrl = url
    )
}


// --- Album mapping for album.search endpoint ---
fun ApiAlbum.toDomainAlbum(): Album {
    return Album(
        id = generateAlbumId(this.artist, this.name),
        mbid = this.mbid.ifEmpty { null },
        title = this.name,
        artist = this.artist,
        artworkUrl = this.image.getLargestImageUrl(),
        lastFmUrl = this.url,
        releaseDate = "",
        listeners = 0,
        playCount = 0,
        tracks = emptyList()
    )
}

// --- Album mapping for album.getinfo endpoint ---
fun AlbumDetail.toDomainAlbum(): Album {
    val primaryImage = image.lastOrNull { it.size == "extralarge" }?.url
        ?: image.firstOrNull()?.url
        ?: ""

    return Album(
        id = generateAlbumId(artist, name),
        title = name,
        artist = artist,
        artworkUrl = primaryImage,
        lastFmUrl = url ?: "",
        releaseDate = wiki?.published?.substringBefore(",") ?: "Unknown",
        listeners = listeners?.toIntOrNull() ?: 0,
        playCount = playcount?.toIntOrNull() ?: 0,
        tracks = parseTracks(tracks),
        summary = wiki?.summary?.sanitizeWikiText() ?: "No description available"
    )
}
fun TrackDetail.toDomainTrack(): Track {
    return Track(
        id = generateTrackId(artist, name),
        name = name,
        artist = artist,
        lastFmUrl = url ?: "",
        playcount = playcount?.toIntOrNull() ?: 0,

    )
}

// Helper extensions
private fun List<Image>.getLargestImageUrl(): String {
    return this.find { it.size == "extralarge" }?.url ?: this.lastOrNull()?.url ?: ""
}

private fun DetailTrack.toDomainTrack(): Album.Track? {
    return if (name.isNotBlank()) {
        Album.Track(
            title = name,
            duration = duration,
            position = attr?.rank.safeToInt()
        )
    } else null
}

private fun String.sanitizeWikiText(): String {
    return this.replace("""<a[^>]*>(.*?)</a>""".toRegex(), "$1")
        .replace("""\n""", " ")
        .trim()
}
private fun parseTracks(wrapper: TrackWrapper?): List<Album.Track> {
    return when (val rawTracks = wrapper?.track) {
        is List<*> -> {
            (rawTracks as? List<DetailTrack>) // Safe cast
                ?.mapNotNull { it.toDomainTrack() }
                ?: emptyList()
        }
        is DetailTrack -> {
            listOfNotNull(rawTracks.toDomainTrack()) // Ensure non-null list
        }
        else -> emptyList()
    }
}

fun String?.safeToInt() = this?.toIntOrNull() ?: 0
