package com.example.pitchapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthViewModel : ViewModel() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId

    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username

    private val _bio = MutableStateFlow<String>("")
    val bio: StateFlow<String> = _bio

    private val _themePreference = MutableStateFlow("light")
    val themePreference: StateFlow<String> = _themePreference

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun setThemePreference(pref: String) {
        _themePreference.value = pref

        // If user is logged in, save preference to Firebase
        _userId.value?.let { uid ->
            viewModelScope.launch {
                try {
                    db.collection("users").document(uid)
                        .update("themePreference", pref)
                        .await()
                    Log.d("AuthViewModel", "Theme preference updated successfully")
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Failed to update theme preference", e)
                }
            }
        }
    }

    suspend fun login(username: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _isLoading.value = true
        try {
            val querySnapshot = db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()

            val userDoc = querySnapshot.documents.firstOrNull()

            if (userDoc == null) {
                onFailure("Username not found")
                _isLoading.value = false
                return
            }

            val storedPassword = userDoc.getString("password") ?: ""
            if (storedPassword == password) {
                _userId.value = userDoc.id
                fetchUserData(userDoc.id)
                Log.d("AuthViewModel", "Login successful for user: $username")
                Log.d("AuthViewModel", "User ID: ${_userId.value}")
                onSuccess()
            } else {
                onFailure("Incorrect password")
            }
        } catch (e: Exception) {
            onFailure(e.message ?: "Login failed")
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createAccount(username: String, password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _isLoading.value = true
        try {
            val existing = db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!existing.isEmpty) {
                onFailure("Username already taken")
                _isLoading.value = false
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
            _username.value = username
            _bio.value = ""
            _themePreference.value = "light"

            Log.d("AuthViewModel", "Account created successfully for user: $username")
            onSuccess()
        } catch (e: Exception) {
            println("Signup failed inside ViewModel: ${e.message}")
            onFailure(e.message ?: "Signup failed")
        } finally {
            _isLoading.value = false
        }
    }

    fun logout() {
        _userId.value = null
        _username.value = null
        _bio.value = ""
        _themePreference.value = "light"
        Log.d("AuthViewModel", "User logged out")
    }

    private suspend fun fetchUserData(userId: String) {
        try {
            val doc = db.collection("users")
                .document(userId)
                .get()
                .await()

            _username.value = doc.getString("username")
            _bio.value = doc.getString("bio") ?: ""
            _themePreference.value = doc.getString("themePreference") ?: "light"

            Log.d("AuthViewModel", "Fetched user data: username=${_username.value}, bio=${_bio.value}, theme=${_themePreference.value}")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error fetching user data", e)
        }
    }

    fun updateBio(newBio: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _userId.value?.let { uid ->
            viewModelScope.launch {
                try {
                    _isLoading.value = true
                    db.collection("users").document(uid)
                        .update("bio", newBio, "updatedAt", com.google.firebase.Timestamp.now())
                        .await()

                    _bio.value = newBio
                    Log.d("AuthViewModel", "Bio updated successfully")
                    onSuccess()
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Failed to update bio", e)
                    onFailure(e.message ?: "Failed to update bio")
                } finally {
                    _isLoading.value = false
                }
            }
        } ?: onFailure("User not logged in")
    }

    fun refreshUserData() {
        _userId.value?.let { uid ->
            viewModelScope.launch {
                try {
                    fetchUserData(uid)
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Failed to refresh user data", e)
                }
            }
        }
    }
}