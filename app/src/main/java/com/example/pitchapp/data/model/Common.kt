package com.example.pitchapp.data.model

import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("#text") val url: String,
    val size: String
)

data class OpenSearchQuery(
    @SerializedName("#text")     val text: String,
    val role: String,
    val searchTerms: String,
    val startPage: String
)

data class Attr(
    @SerializedName("for") val forTerm: String
)

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}
