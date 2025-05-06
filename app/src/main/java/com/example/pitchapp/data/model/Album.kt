package com.example.pitchapp.data.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

// Domain model for Albums
data class Album(
    val id: String = "",
    val mbid: String? = null,
    val title: String = "",
    val artist: String = "",
    val artworkUrl: String = "https://www.freeiconspng.com/uploads/music-note-icon-33.png",
    val lastFmUrl: String = "",
    val releaseDate: String = "Unknown",
    val listeners: Int = 0,
    val playCount: Int = 0,
    val tracks: List<Track> = emptyList(),
    val summary: String? = "No summary available on Last.fm",
    val reviewCount: Int = 0,
    val averageRating: Float = 0f
) {
    data class Track(
        val title: String = "Unkown Track",
        val duration: Int = 0,
        val position: Int = 0
    )
}
//endregion

// Album Search Result Models
data class AlbumSearchResponse(
    @SerializedName("results")
    val results: AlbumResults
)
data class RandomSearchResponse(
    @SerializedName("tracks")
    val tracks: List<Album>
)

data class AlbumResults(
    @SerializedName("opensearch:Query")
    val query: OpenSearchQuery,
    @SerializedName("opensearch:totalResults")
    val totalResults: String,
    @SerializedName("opensearch:startIndex")
    val startIndex: String,
    @SerializedName("opensearch:itemsPerPage")
    val itemsPerPage: String,
    @SerializedName("albummatches")
    val albumMatches: AlbumMatches,
    @SerializedName("@attr")
    val attr: Attr
)

data class AlbumMatches(
    @SerializedName("album")
    val albums: List<ApiAlbum>
)

data class ApiAlbum(
    @SerializedName("name")
    val name: String,
    @SerializedName("artist")
    val artist: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("streamable")
    val streamable: String,
    @SerializedName("mbid")
    val mbid: String,
    @SerializedName("image")
    val image: List<Image>
)
data class AlbumInfoResponse(
    @SerializedName("album")
    val album: AlbumDetail = AlbumDetail()
)
//endregion



// Album Detail Models
data class AlbumDetail(
    @SerializedName("artist") val artist: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("image") val image: List<Image> = emptyList(),
    @SerializedName("tracks") val tracks: TrackWrapper? = null,
    @SerializedName("mbid") val mbid: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("listeners") val listeners: String? = null,
    @SerializedName("playcount") val playcount: String? = null,
    @SerializedName("wiki") val wiki: Wiki? = null
)


data class TrackWrapper(
    @SerializedName("track")
    val track: Any? // Can be List<DetailTrack> or DetailTrack
)


data class DetailTrack(
    @SerializedName("name") val name: String = "",
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("@attr") val attr: RankAttr? = null
)

data class RankAttr(
    @SerializedName("rank") val rank: String = "0"
)

data class Wiki(
    @SerializedName("published") val published: String,
    @SerializedName("summary")   val summary: String,
    @SerializedName("content")   val content: String
)
//endregion


