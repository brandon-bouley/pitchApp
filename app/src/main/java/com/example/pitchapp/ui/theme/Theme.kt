package com.example.pitchapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ColorScheme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


val AppTypography = Typography(
    titleLarge = TextStyle(

        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

val LightColors: ColorScheme = lightColorScheme(
    primary = DeepGreen,       // Primary color for Light Mode
    onPrimary = OffWhite,       // Text/Icon on primary
    secondary = MutedTeal,      // Accent color
    onSecondary = DeepGreen,    // Text on accent
    background = OffWhite,      // Background of screens
    onBackground = DeepGreen,   // Text on background
    surface = PaleMint,         // Cards/dialogs
    onSurface = DeepGreen
)

val DarkColors: ColorScheme = darkColorScheme(
    primary = BrightTeal,        // Primary color for Dark Mode
    onPrimary = TrueBlack,     // Text/Icon on primary
    secondary = DarkGrey,       // Accent
    onSecondary = DeeperTeal,   // Text on accent
    background = TrueBlack,     // Background of screens
    onBackground = BrightTeal,  // Text on background
    surface = DarkGrey,         // Cards/dialogs
    onSurface = BrightTeal

)

@Composable
fun PitchAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
){

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}