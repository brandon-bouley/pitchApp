package com.example.pitchapp.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pitchapp.viewmodel.AuthViewModel
import com.example.pitchapp.ui.navigation.Screen
import kotlinx.coroutines.launch


@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }


    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                } else {
                    coroutineScope.launch {
                        authViewModel.createAccount(username, password,
                            onSuccess = {
                                isLoading = false
                                println("Signup successful! Navigating to Feed")
                                navController.navigate("main_app") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            },
                            onFailure = { error ->
                                println("Signup failed: $error")
                                isLoading = false
                                errorMessage = error
                            }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Create Account")
            }
        }
        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}