package com.example.pitchapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Review(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val songId: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)