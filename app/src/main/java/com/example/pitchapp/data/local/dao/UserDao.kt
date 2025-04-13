package com.example.pitchapp.data.local.dao

import androidx.room.*
import com.example.pitchapp.data.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUser(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: User)

    @Delete
    suspend fun deleteUser(user: User)
}
