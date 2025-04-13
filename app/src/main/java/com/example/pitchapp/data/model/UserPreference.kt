package com.example.pitchapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "user_preferences")
@TypeConverters(UserConverters::class)
data class UserPreference(
    @PrimaryKey val username: String,
    val name: String,
    val age: Int,
    val bio: String,
    val email: String,
    val darkMode: Boolean,
    val ratedItems: List<String> // Format: itemType:itemId (e.g., "album:123", "artist:abc")
)
