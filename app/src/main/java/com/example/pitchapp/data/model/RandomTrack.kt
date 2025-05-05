package com.example.pitchapp.data.model
import com.example.pitchapp.data.model.Album.Track
import com.google.gson.annotations.SerializedName

data class Track(
    val id: String = "",
    val mbid: String? = null,
    val name: String = "",
    val artist: String = "",
    val lastFmUrl: String = "",
    val playcount: Int = 0,
    val reviewCount: Int = 0,
    val averageRating: Float = 0f
)
data class RandomTrack(
    @SerializedName("name")
    val name: String,

    @SerializedName("playcount")
    val playcount: Int,

    @SerializedName("listeners")
    val listeners: Int,

    @SerializedName("mbid")
    val mbid: String?,

    @SerializedName("url")
    val url: String,

    @SerializedName("streamable")
    val streamable: Streamable,

    @SerializedName("artist")
    val artist: Artist
) {
    data class Streamable(
        @SerializedName("fulltrack")
        val fulltrack: Int,

        @SerializedName("#text")
        val value: Int
    )

    data class Artist(
        @SerializedName("name")
        val name: String,

        @SerializedName("mbid")
        val mbid: String?,

        @SerializedName("url")
        val url: String
    )
}
data class TopTracksResponse(
    @SerializedName("tracks") val tracks: Tracks
)

data class Tracks(
    @SerializedName("track") val track: List<RandomTrack>
)

data class TrackSearchResponse(
    @SerializedName("track_results")
    val results: TrackResults
)
data class TrackResults(
    @SerializedName("opensearch:Query")
    val query: OpenSearchQuery,
    @SerializedName("opensearch:totalResults")
    val totalResults: String,
    @SerializedName("opensearch:startIndex")
    val startIndex: String,
    @SerializedName("opensearch:itemsPerPage")
    val itemsPerPage: String,
    @SerializedName("trackmatches")
    val trackMatches: TrackMatches,
    @SerializedName("@attr")
    val attr: Attr
)

data class TrackMatches(
    @SerializedName("album")
    val albums: List<ApiTrack>
)
data class ApiTrack(
    @SerializedName("name")
    val name: String,
    @SerializedName("artist")
    val artist: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("mbid")
    val mbid: String,

)
data class TrackInfoResponse(
    @SerializedName("track")
    val track: TrackDetail = TrackDetail()
)
//endregion



// Track Detail Models
data class TrackDetail(
    @SerializedName("artist") val artist: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("mbid") val mbid: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("playcount") val playcount: String? = null,
)