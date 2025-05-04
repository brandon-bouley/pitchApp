package com.example.pitchapp.data.model
import com.google.gson.annotations.SerializedName


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
