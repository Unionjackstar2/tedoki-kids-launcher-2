package com.example.ui.lockscreen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.LauncherSettings
import com.example.model.LockscreenClockStyle
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-screen Lockscreen for kids:
 * - Upper third: Clock (Conni-style analog learning clock OR modern digital clock with date)
 * - Middle area: Calm breathing space or wallpaper
 * - Lower third: Large green directional pull lock button + subtle helper text
 * - Optional: Math puzzle overlay before reaching home screen
 */
@Composable
fun ChildLockScreen(
    settings: LauncherSettings,
    onUnlockToHome: () -> Unit
) {
    // Prevent back button from escaping lockscreen
    BackHandler { /* Do nothing - locked */ }

    var showMathProblem by remember { mutableStateOf(false) }

    // Live date and time for digital clock
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }
    val isDarkTheme = com.example.ui.theme.LocalIsDarkTheme.current

    val locale = if (settings.language == "de") Locale.GERMAN else Locale.ENGLISH

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", locale)
        val dateFormat = SimpleDateFormat("EEEE, d. MMMM", locale)
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000L)
        }
    }

    // Background selection
    val backgroundBrush = remember(settings.lockscreenPresetBackground) {
        when (settings.lockscreenPresetBackground) {
            "pastel_green" -> Brush.verticalGradient(listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0), Color(0xFF6EE7B7)))
            "starry_night" -> Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81)))
            "sunny_yellow" -> Brush.verticalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A), Color(0xFFFBBF24)))
            "warm_orange" -> Brush.verticalGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFED7AA), Color(0xFFFB923C)))
            "gentle_purple" -> Brush.verticalGradient(listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF), Color(0xFFC084FC)))
            else -> Brush.verticalGradient(listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFF7DD3FC))) // soft_blue
        }
    }

    val isDarkBackground = settings.lockscreenPresetBackground == "starry_night" || isDarkTheme
    val primaryTextColor = if (isDarkBackground) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val secondaryTextColor = if (isDarkBackground) Color(0xFFCBD5E1) else Color(0xFF475569)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("child_lock_screen")
    ) {
        // 1. Wallpaper (Custom Photo or Preset Gradient)
        if (!settings.lockscreenCustomBackgroundUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(settings.lockscreenCustomBackgroundUri),
                contentDescription = "Hintergrundbild",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Subtle scrim over custom photo, darker in dark mode
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isDarkTheme) Color.Black.copy(alpha = 0.55f)
                        else Color.Black.copy(alpha = 0.2f)
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
            )
            // Dark mode dimming: Strongly dim background without making it solid black
            if (isDarkTheme) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.50f))
                )
            }
        }

        // 2. Lockscreen Content Column (Wallpaper behind flows edge-to-edge under the status bar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 20.dp)
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper third: Clock display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                contentAlignment = Alignment.Center
            ) {
                if (settings.lockscreenClockStyle == LockscreenClockStyle.ANALOG_CONNI) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ConniLearningClock(
                            modifier = Modifier.size(240.dp)
                        )
                        if (currentDateString.isNotEmpty()) {
                            Text(
                                text = currentDateString,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryTextColor
                            )
                        }
                    }
                } else {
                    // Digital Clock
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = currentTimeString.ifEmpty { "12:00" },
                            fontSize = 68.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryTextColor
                        )
                        Text(
                            text = currentDateString,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = secondaryTextColor
                        )
                    }
                }
            }

            // Middle section: Spacing
            Spacer(modifier = Modifier.weight(0.3f))

            // Lower third: Large green directional pull lock button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                DirectionalPullUnlockButton(
                    resetTrigger = showMathProblem,
                    onUnlocked = {
                        if (settings.lockscreenRequireMathProblem) {
                            showMathProblem = true
                        } else {
                            onUnlockToHome()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Zum Entsperren in eine Richtung ziehen",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryTextColor.copy(alpha = 0.85f)
                )
            }
        }

        // 3. Optional Math Problem Dialog
        if (showMathProblem) {
            LockscreenMathDialog(
                onDismiss = { showMathProblem = false },
                onSolved = {
                    showMathProblem = false
                    onUnlockToHome()
                }
            )
        }
    }
}
