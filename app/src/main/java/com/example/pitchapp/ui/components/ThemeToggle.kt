package com.example.pitchapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon

@Composable
fun ThemeToggle(
    isDarkTheme: Boolean,
    onToggle: (Boolean) -> Unit,

) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onToggle(!isDarkTheme) },
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.Brightness2 else Icons.Default.WbSunny,
            contentDescription = if (isDarkTheme) "Dark Mode" else "Light Mode",
            modifier = Modifier.size(32.dp)
        )
    }
}
