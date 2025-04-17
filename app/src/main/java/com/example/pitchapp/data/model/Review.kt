package com.example.pitchapp.data.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = UserPreference::class,
            parentColumns = ["username"],
            childColumns = ["author"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val albumId: String,
    val albumTitle: String,
    val author: String,
    val content: String,
    val rating: Int,
    val timestamp: Long = System.currentTimeMillis()
)