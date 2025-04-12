package com.example.pitchapp.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: String, // Song or Album ID
    val itemType: String,
    val userName: String,
    val reviewText: String,
    val timestamp: Long = System.currentTimeMillis()
)