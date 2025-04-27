package com.example.pitchapp.data.local

import androidx.room.*
import com.example.pitchapp.data.model.UserPreference

@Dao
interface UserPreferenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreference(userPreference: UserPreference)

    @Query("SELECT * FROM user_preferences WHERE username = :username")
    suspend fun getPreference(username: String): UserPreference?

    @Query("DELETE FROM user_preferences WHERE username = :username")
    suspend fun deletePreference(username: String)

    @Query("SELECT * FROM user_preferences")
    suspend fun getAllPreferences(): List<UserPreference>

    @Query("SELECT COUNT(*) FROM user_preferences")
    suspend fun countUsers(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pref: UserPreference)
}

