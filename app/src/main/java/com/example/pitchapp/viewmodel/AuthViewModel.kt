package com.example.pitchapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthViewModel : ViewModel() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    private val _themePreference = MutableStateFlow("light")
    val themePreference: StateFlow<String> = _themePreference

    fun setThemePreference(pref: String) {
        _themePreference.value = pref
    }



    suspend fun login(username: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()

            val userDoc = querySnapshot.documents.firstOrNull()

            if (userDoc == null) {
                onFailure("Username not found")
                return
            }

            val storedPassword = userDoc.getString("password") ?: ""
            if (storedPassword == password) {
                _userId.value = userDoc.id
                fetchUsername(userDoc.id)
                Log.d("AuthViewModel", "Login successful for user: $username")
                onSuccess()
            } else {
                onFailure("Incorrect password")
            }
        } catch (e: Exception) {
            onFailure(e.message ?: "Login failed")
        }
    }

    suspend fun createAccount(username: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        try {

            val existing = db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!existing.isEmpty) {
                onFailure("Username already taken")
                return
            }

            val newUserId = UUID.randomUUID().toString()
            val userData = mapOf(
                "username" to username,
                "password" to password,
                "bio" to "",
                "profilePictureUrl" to "",
                "followers" to listOf<String>(),
                "following" to listOf<String>(),
                "themePreference" to "light",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            db.collection("users").document(newUserId).set(userData).await()

            _userId.value = newUserId
            fetchUsername(newUserId)
            Log.d("AuthViewModel", "Account created successfully for user: $username")
            onSuccess()
        } catch (e: Exception) {
            println("Signup failed inside ViewModel: ${e.message}")
            onFailure(e.message ?: "Signup failed")
        }
    }

    fun logout() {
        _userId.value = null
    }

    private suspend fun fetchUsername(userId: String) {
        val doc = FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .get()
            .await()
        _username.value = doc.getString("username")
    }
}

