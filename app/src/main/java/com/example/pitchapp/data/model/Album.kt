package com.example.pitchapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey val id: String,
    val name: String,
    val artist: String
)