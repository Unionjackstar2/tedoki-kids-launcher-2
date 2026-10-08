package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Light Palette
val LightAssistiveBackground = Color(0xFFF2F4F7)
val LightAssistiveBackgroundWarm = Color(0xFFF8F9FA)
val LightAssistiveSurface = Color(0xFFFFFFFF)
val LightAssistiveSurfaceElevated = Color(0xFFFFFFFF)
val LightAssistiveBorder = Color(0xFFE2E8F0)

val LightAssistiveTextPrimary = Color(0xFF0F172A)
val LightAssistiveTextSecondary = Color(0xFF475569)
val LightAssistiveTextMuted = Color(0xFF64748B)

// High-contrast semantic colors
val AssistiveBlue = Color(0xFF2563EB)
val AssistiveBlueLight = Color(0xFFDBEAFE)
val AssistiveGreen = Color(0xFF059669)
val AssistiveGreenLight = Color(0xFFD1FAE5)
val AssistiveRed = Color(0xFFDC2626)
val AssistiveRedLight = Color(0xFFFEE2E2)
val AssistiveAmber = Color(0xFFD97706)
val AssistiveAmberLight = Color(0xFFFEF3C7)
val AssistivePurple = Color(0xFF7C3AED)
val AssistivePurpleLight = Color(0xFFEDE9FE)
val AssistivePink = Color(0xFFDB2777)
val AssistivePinkLight = Color(0xFFFCE7F3)

// Dark mode adaptations (calm deep slate)
val AssistiveDarkBackground = Color(0xFF0F172A)
val AssistiveDarkBackgroundWarm = Color(0xFF1E293B)
val AssistiveDarkSurface = Color(0xFF1E293B)
val AssistiveDarkSurfaceElevated = Color(0xFF283548)
val AssistiveDarkBorder = Color(0xFF334155)
val AssistiveDarkTextPrimary = Color(0xFFF8FAFC)
val AssistiveDarkTextSecondary = Color(0xFF94A3B8)
val AssistiveDarkTextMuted = Color(0xFF94A3B8)

// Dynamic properties that automatically adjust based on LocalIsDarkTheme
val AssistiveBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) AssistiveDarkBackground else LightAssistiveBackground

val AssistiveBackgroundWarm: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) AssistiveDarkBackgroundWarm else LightAssistiveBackgroundWarm

val AssistiveSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) AssistiveDarkSurface else LightAssistiveSurface

val AssistiveSurfaceElevated: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) AssistiveDarkSurfaceElevated else LightAssistiveSurfaceElevated

val AssistiveBorder: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) AssistiveDarkBorder else LightAssistiveBorder

val AssistiveTextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) AssistiveDarkTextPrimary else LightAssistiveTextPrimary

val AssistiveTextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) AssistiveDarkTextSecondary else LightAssistiveTextSecondary

val AssistiveTextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalIsDarkTheme.current) AssistiveDarkTextMuted else LightAssistiveTextMuted
