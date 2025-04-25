package com.example.pitchapp.data.model
import com.google.gson.annotations.SerializedName


// Domain model for Artists
data class Artist(
    val name: String,
    val mbid: String,
    val imageUrl: String,
    val listeners: Int,
    val lastFmUrl: String
)
//endregion

// Artist Search Result Models
data class ArtistSearchResponse(
    @SerializedName("results")
    val results: ArtistResults
)

data class ArtistResults(
    @SerializedName("opensearch:Query")   val query: OpenSearchQuery,
    @SerializedName("opensearch:totalResults") val totalResults: String,
    @SerializedName("opensearch:startIndex")   val startIndex: String,
    @SerializedName("opensearch:itemsPerPage") val itemsPerPage: String,
    @SerializedName("artistmatches")       val artistMatches: ArtistMatches,
    @SerializedName("@attr")               val attr: Attr
)

data class ArtistMatches(
    @SerializedName("artist") val artists: List<ApiArtist>
)

data class ApiArtist(
    val name: String,
    val listeners: String,
    val mbid: String,
    val url: String,
    val streamable: String,
    val image: List<Image>
)
//endregion

// Artist Top Albums Models
data class ArtistTopAlbumsResponse(
    @SerializedName("topalbums")
    val topalbums: TopAlbumsWrapper
)

data class TopAlbumsWrapper(
    @SerializedName("album")
    val albums: List<ApiAlbumSimple>,
    @SerializedName("@attr")
    val attr: AttrTopAlbums
)

data class ApiAlbumSimple(
    val name: String,
    val playcount: Int,
    val mbid: String,
    val url: String,
    val artist: ArtistInfo,
    val image: List<Image>
)

data class ArtistInfo(
    val name: String,
    val mbid: String,
    val url: String
)

data class AttrTopAlbums(
    val artist: String,
    val page: String,
    val perPage: String,
    val totalPages: String,
    val total: String
)
//endregion

// Artist Detail Models
data class ArtistInfoResponse(
    val artist: ArtistDetail
)

data class ArtistDetail(
    val name: String,
    val mbid: String?,
    val url: String,
    val image: List<Image>,
    val stats: ArtistStats,
    val similar: SimilarArtists,
    val bio: Bio
)

data class ArtistStats(
    val listeners: String,
    val plays: String
)

data class SimilarArtists(
    @SerializedName("artist") val artists: List<SimpleArtist>
)

data class SimpleArtist(
    val name: String,
    val url: String,
    val image: List<Image>
)


data class Bio(
    val published: String,
    val summary: String,
    val content: String
)
//endregion