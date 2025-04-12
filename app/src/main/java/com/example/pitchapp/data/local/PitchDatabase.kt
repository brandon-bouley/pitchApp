// data/PitchDatabase.kt
package com.example.pitchapp.data.local
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.pitchapp.data.local.dao.AlbumDao
import com.example.pitchapp.data.local.dao.TrackDao
import com.example.pitchapp.data.model.Album
import com.example.pitchapp.data.model.Track
import com.example.pitchapp.data.model.Review
import com.example.pitchapp.data.local.dao.ReviewDao


@Database(entities = [Track::class, Album::class, Review::class], version = 1)
abstract class PitchDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile private var INSTANCE: PitchDatabase? = null

        fun getDatabase(context: Context): PitchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PitchDatabase::class.java,
                    "pitchapp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }

}