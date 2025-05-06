package com.example.pitchapp.data.model
import com.example.pitchapp.data.model.Album.Track
import com.google.gson.annotations.SerializedName

// Serialize data models for Random Track
data class RandoTrack(
    @SerializedName("id")
    var id: String = "",
    @SerializedName("mbid")
    val mbid: String? = null,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("artist")
    val artist: String = "",
)

data class RandomTrack(
    @SerializedName("id")
    val id: String,
    @SerializedName("mbid")
    val mbid: String?,

    @SerializedName("name")
    val name: String,
    @SerializedName("artist")
    val artist: Artist,

    @SerializedName("playCount")
    val playCount: Int,

    @SerializedName("listeners")
    val listeners: Int,



    @SerializedName("url")
    val url: String,

    @SerializedName("streamable")
    val streamable: Streamable,


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


data class TracksResponse(
    @SerializedName("track") val track: RandomTrack
)
data class TopTracksResponse(
    @SerializedName("tracks") val tracks: Tracks
)

data class Tracks(
    @SerializedName("track") val track: List<RandomTrack>
)
