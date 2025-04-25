package com.example.pitchapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pitchapp.data.model.UserConverters
import com.example.pitchapp.data.model.UserPreference


@Database(
    entities = [UserPreference::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(UserConverters::class)
abstract class PitchDatabase : RoomDatabase() {
    abstract fun userPreferenceDao(): UserPreferenceDao
    companion object {
        @Volatile
        private var INSTANCE: PitchDatabase? = null
        fun getDatabase(context: Context): PitchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PitchDatabase::class.java,
                    "pitch_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}