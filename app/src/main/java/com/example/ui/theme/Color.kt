package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Instagram Brand Gradient Colors
val InstaPurple = Color(0xFF833AB4)
val InstaPink = Color(0xFFFD1D1D)
val InstaOrange = Color(0xFFFCB045)
val InstaYellow = Color(0xFFFFDC80)
val InstaBlue = Color(0xFF3897F0)
val InstaHeartRed = Color(0xFFED4956)

// Light Theme Colors
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFAFAFA)
val LightSurfaceVariant = Color(0xFFF0F2F5)
val LightOnBackground = Color(0xFF1C1E21)
val LightBorder = Color(0xFFE0E0E0)

// Dark Theme Colors
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF282828)
val DarkOnBackground = Color(0xFFF5F5F7)
val DarkBorder = Color(0xFF333333)

// Online Status Indicator
val OnlineGreen = Color(0xFF10B981)
val OfflineGrey = Color(0xFF9E9E9E)

// Gradient Brush for Stories & Reels
val InstaStoryGradient = Brush.linearGradient(
    colors = listOf(InstaPurple, InstaPink, InstaOrange, InstaYellow)
)
val InstaBadgeGradient = Brush.horizontalGradient(
    colors = listOf(InstaPurple, InstaPink, InstaOrange)
)
