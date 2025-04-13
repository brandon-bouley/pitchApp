package com.example.pitchapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.pitchapp.data.util.Converters

@Entity(tableName = "users")
@TypeConverters(Converters::class)
data class User(
    @PrimaryKey val username: String,
    val name: String,
    val age: Int,
    val bio: String,
    val email: String,
    val darkModeEnabled: Boolean,
    val ratedSongs: List<String>,
    val ratedAlbums: List<String>,
    val ratedArtists: List<String>
)
