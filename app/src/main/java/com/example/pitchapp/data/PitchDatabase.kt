// data/PitchDatabase.kt
package com.example.pitchapp.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [Review::class],
    version = 1,
    exportSchema = false
)
abstract class PitchDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: PitchDatabase? = null

        fun getDatabase(context: Context): PitchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PitchDatabase::class.java,
                    "pitch_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}