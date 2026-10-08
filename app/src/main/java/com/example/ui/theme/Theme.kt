package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

val LocalIsDarkTheme = staticCompositionLocalOf { false }

private val AssistiveLightColorScheme = lightColorScheme(
    primary = AssistiveBlue,
    onPrimary = Color.White,
    primaryContainer = AssistiveBlueLight,
    onPrimaryContainer = LightAssistiveTextPrimary,
    secondary = AssistiveGreen,
    onSecondary = Color.White,
    secondaryContainer = AssistiveGreenLight,
    onSecondaryContainer = LightAssistiveTextPrimary,
    background = LightAssistiveBackground,
    onBackground = LightAssistiveTextPrimary,
    surface = LightAssistiveSurface,
    onSurface = LightAssistiveTextPrimary,
    surfaceVariant = LightAssistiveBackgroundWarm,
    onSurfaceVariant = LightAssistiveTextSecondary,
    outline = LightAssistiveBorder,
    error = AssistiveRed,
    onError = Color.White,
    errorContainer = AssistiveRedLight,
    onErrorContainer = AssistiveRed
)

private val AssistiveDarkColorScheme = darkColorScheme(
    primary = AssistiveBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = AssistiveGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFD1FAE5),
    background = AssistiveDarkBackground,
    onBackground = AssistiveDarkTextPrimary,
    surface = AssistiveDarkSurface,
    onSurface = AssistiveDarkTextPrimary,
    surfaceVariant = AssistiveDarkSurfaceElevated,
    onSurfaceVariant = AssistiveDarkTextSecondary,
    outline = AssistiveDarkBorder,
    error = AssistiveRed,
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

val AssistiveInputTextStyle: androidx.compose.ui.text.TextStyle
    @Composable
    get() = androidx.compose.ui.text.TextStyle(
        color = AssistiveTextPrimary,
        fontSize = 16.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
    )

/**
 * Standard accessible styling for text input fields across the app.
 * Dynamically provides high contrast text and appropriate container colors in both Light and Dark modes.
 */
@Composable
fun assistiveTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AssistiveTextPrimary,
    unfocusedTextColor = AssistiveTextPrimary,
    disabledTextColor = AssistiveTextSecondary,
    focusedContainerColor = if (LocalIsDarkTheme.current) Color(0xFF1E293B) else Color(0xFFF8FAFC),
    unfocusedContainerColor = if (LocalIsDarkTheme.current) Color(0xFF1E293B) else Color(0xFFF8FAFC),
    disabledContainerColor = if (LocalIsDarkTheme.current) Color(0xFF0F172A) else Color(0xFFF1F5F9),
    cursorColor = AssistiveBlue,
    focusedBorderColor = AssistiveBlue,
    unfocusedBorderColor = if (LocalIsDarkTheme.current) Color(0xFF475569) else Color(0xFFCBD5E1),
    focusedLabelColor = AssistiveBlue,
    unfocusedLabelColor = AssistiveTextSecondary,
    focusedPlaceholderColor = AssistiveTextMuted,
    unfocusedPlaceholderColor = AssistiveTextMuted,
    focusedSupportingTextColor = AssistiveTextSecondary,
    unfocusedSupportingTextColor = AssistiveTextSecondary,
    errorTextColor = AssistiveRed,
    errorBorderColor = AssistiveRed
)

@Composable
fun AssistiveLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AssistiveDarkColorScheme else AssistiveLightColorScheme
    val primaryText = if (darkTheme) AssistiveDarkTextPrimary else LightAssistiveTextPrimary

    CompositionLocalProvider(
        LocalIsDarkTheme provides darkTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography
        ) {
            CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides primaryText,
                androidx.compose.material3.LocalTextStyle provides Typography.bodyLarge.copy(color = primaryText),
                content = content
            )
        }
    }
}

