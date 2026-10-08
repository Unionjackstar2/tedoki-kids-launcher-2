package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
fun SosHoldButton(
    emergencyNumber: String,
    onSosTriggered: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    // Pulsing aura when holding
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isHolding) 1.05f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isHolding) 400 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_pulse"
    )

    // Handle 3 second hold timer
    LaunchedEffect(isHolding) {
        if (isHolding) {
            val totalDurationMs = 3000L
            val startTime = System.currentTimeMillis()
            vibrateDevice(context, 50)

            while (isHolding) {
                val elapsed = System.currentTimeMillis() - startTime
                holdProgress = (elapsed.toFloat() / totalDurationMs).coerceIn(0f, 1f)

                if (holdProgress >= 1f) {
                    vibrateDevice(context, 500)
                    onSosTriggered()
                    isHolding = false
                    break
                }
                delay(16) // Smooth 60fps tracking
            }
        } else {
            holdProgress = 0f
        }
    }

    val remainingSeconds = ceil(3.0 * (1.0 - holdProgress)).toInt().coerceIn(1, 3)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp)
            .scale(pulseScale)
            .shadow(if (isHolding) 12.dp else 6.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isHolding = true
                    waitForUpOrCancellation()
                    isHolding = false
                }
            }
            .testTag("sos_emergency_button"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHolding) Color(0xFF991B1B) else Color(0xFFDC2626)
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.5.dp,
            if (isHolding) Color(0xFFFCA5A5) else Color(0xFFFECACA)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Live Progress fill layer (left to right)
            if (holdProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(holdProgress)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFB91C1C), Color(0xFFEF4444), Color(0xFFF87171))
                            )
                        )
                )
            }

            // Button content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Alert Icon Badge
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isHolding) "NOTRUF WIRD AUSGELÖST!" else stringResource(R.string.sos_button_title),
                            fontSize = if (isHolding) 17.sp else 19.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isHolding) "Gedrückt halten..." else "3 Sekunden gedrückt halten",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isHolding) Color(0xFFFEF08A) else Color(0xFFFEE2E2),
                            maxLines = 1
                        )
                    }
                }

                // Countdown badge / Location indicator (Enlarged 56.dp, prominent bold countdown)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isHolding) Color.White else Color(0x33FFFFFF))
                        .border(
                            width = if (isHolding) 2.dp else 0.dp,
                            color = if (isHolding) Color(0xFFFCA5A5) else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isHolding) {
                        Text(
                            text = "$remainingSeconds",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFDC2626)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun vibrateDevice(context: Context, durationMs: Long) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(durationMs)
        }
    } catch (ignored: Exception) {}
}

@Composable
fun SosModalDialog(
    emergencyNumber: String,
    emergencyContactName: String = "",
    onSosTriggered: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = com.example.ui.theme.LocalIsDarkTheme.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.AssistiveSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.AssistiveBorder),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header with icon and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.sos_button_title),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.AssistiveTextPrimary
                            )
                            if (emergencyContactName.isNotEmpty()) {
                                Text(
                                    text = "Notfallkontakt: $emergencyContactName ($emergencyNumber)",
                                    fontSize = 12.sp,
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("close_sos_modal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = com.example.ui.theme.AssistiveTextSecondary
                        )
                    }
                }

                Text(
                    text = "Halte die rote Taste unten 3 Sekunden gedrückt, um den Notruf auszulösen.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = com.example.ui.theme.AssistiveTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 21.sp
                )

                // 3-second hold SOS button
                SosHoldButton(
                    emergencyNumber = emergencyNumber,
                    onSosTriggered = {
                        onDismiss()
                        onSosTriggered()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Cancel button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9),
                        contentColor = com.example.ui.theme.AssistiveTextSecondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Abbrechen", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
