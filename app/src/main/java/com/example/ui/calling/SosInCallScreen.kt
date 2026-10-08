package com.example.ui.calling

import android.telecom.Call
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sos.SosLogRepository
import com.example.model.LauncherSettings
import com.example.model.sos.SosLogEntry
import com.example.model.sos.SosSmsDeliveryStatus
import com.example.model.sos.SosSmsSendStatus
import com.example.telecom.CallManager
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.delay

/**
 * Dedicated, calm in-call screen exclusively for TEDOKI SOS Emergency Calls.
 * Reassuring, clear, spacious design without flashing alarms or panic visual noise.
 * Synchronizes live call state and SMS status directly with SosLogRepository.
 */
@Composable
fun SosInCallScreen(
    settings: LauncherSettings,
    onEndCall: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val callState by CallManager.callState.collectAsState()
    val durationSeconds by CallManager.durationSeconds.collectAsState()
    val rawPhoneNumber by CallManager.phoneNumber.collectAsState()
    val activeSosLogId by CallManager.activeSosLogId.collectAsState()

    val allSosLogs by SosLogRepository.logsFlow.collectAsState()
    val activeSosLog: SosLogEntry? = remember(allSosLogs, activeSosLogId) {
        if (!activeSosLogId.isNullOrBlank()) {
            allSosLogs.find { it.id == activeSosLogId }
        } else {
            allSosLogs.firstOrNull()
        }
    }

    val handleEndCall = {
        CallManager.hangUp()
        CallManager.resetSosState()
        onEndCall()
    }

    BackHandler { handleEndCall() }

    // Auto-return to home if call is disconnected after showing final state
    LaunchedEffect(callState) {
        if (callState == Call.STATE_DISCONNECTED) {
            delay(1800)
            CallManager.resetSosState()
            onEndCall()
        }
    }

    // Calm, very soft warm rose/red background
    val bgColor = if (isDark) Color(0xFF1C1316) else Color(0xFFFFF5F5)
    val cardBorderColor = if (isDark) Color(0xFF3F1D24) else Color(0xFFFEE2E2)

    val displayName = remember(settings, rawPhoneNumber) {
        val contactName = settings.emergencyContactName.trim()
        if (contactName.isNotBlank()) {
            contactName
        } else if (!rawPhoneNumber.isNullOrBlank()) {
            rawPhoneNumber!!
        } else {
            settings.emergencyNumber.ifBlank { "Notruf" }
        }
    }

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val formattedDuration = String.format("%02d:%02d Min.", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues())
            .testTag("sos_in_call_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. KOPFBEREICH (Top Header)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Subtle SOS Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFFECDD3))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDC2626))
                        )
                        Text(
                            text = "SOS-NOTRUF",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB91C1C),
                            letterSpacing = 0.8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Anruf an",
                    fontSize = 15.sp,
                    color = AssistiveTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = displayName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                if (settings.emergencyNumber.isNotBlank() && settings.emergencyNumber != displayName) {
                    Text(
                        text = settings.emergencyNumber,
                        fontSize = 14.sp,
                        color = AssistiveTextMuted
                    )
                }

                Text(
                    text = "TEDOKI ist für dich da. Bleib ganz ruhig.",
                    fontSize = 13.sp,
                    color = Color(0xFF991B1B),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 2. STATUSBEREICH (Central Status Card)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.5.dp, cardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // A) TELEFONSTATUS
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    when (callState) {
                                        Call.STATE_ACTIVE -> Color(0xFFECFDF5)
                                        Call.STATE_DISCONNECTED -> Color(0xFFF1F5F9)
                                        else -> Color(0xFFFEF3C7)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (callState) {
                                    Call.STATE_ACTIVE -> Icons.Default.PhoneInTalk
                                    Call.STATE_DISCONNECTED -> Icons.Default.Phone
                                    else -> Icons.Default.Phone
                                },
                                contentDescription = null,
                                tint = when (callState) {
                                    Call.STATE_ACTIVE -> AssistiveGreen
                                    Call.STATE_DISCONNECTED -> Color(0xFF64748B)
                                    else -> Color(0xFFD97706)
                                },
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when (callState) {
                                    Call.STATE_ACTIVE -> "Notruf verbunden"
                                    Call.STATE_DISCONNECTED -> "Notruf beendet"
                                    else -> "Notruf wird aufgebaut …"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (callState) {
                                    Call.STATE_ACTIVE -> Color(0xFF047857)
                                    Call.STATE_DISCONNECTED -> AssistiveTextMuted
                                    else -> Color(0xFFB45309)
                                }
                            )

                            if (callState == Call.STATE_ACTIVE) {
                                Text(
                                    text = formattedDuration,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AssistiveTextSecondary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = cardBorderColor.copy(alpha = 0.6f))

                    // B) SMS-STATUS (Aus zentraler SosLogRepository Logik)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Standort-SMS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextMuted,
                            letterSpacing = 0.5.sp
                        )

                        val smsSendStatus = activeSosLog?.smsSendStatus ?: SosSmsSendStatus.NOT_REQUESTED
                        val smsDeliveryStatus = activeSosLog?.smsDeliveryStatus ?: SosSmsDeliveryStatus.NOT_REQUESTED

                        when {
                            smsSendStatus == SosSmsSendStatus.NOT_REQUESTED -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Keine SMS erforderlich (Notrufzentrale)",
                                        fontSize = 14.sp,
                                        color = AssistiveTextSecondary
                                    )
                                }
                            }

                            smsSendStatus == SosSmsSendStatus.SENDING -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFFD97706)
                                    )
                                    Text(
                                        text = "○ SMS wird versendet …",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }

                            smsSendStatus == SosSmsSendStatus.FAILED -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = AssistiveRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "⚠ SMS konnte nicht versendet werden",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                            }

                            smsDeliveryStatus == SosSmsDeliveryStatus.DELIVERED -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AssistiveGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "✓✓ SMS zugestellt",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }

                            smsDeliveryStatus == SosSmsDeliveryStatus.NOT_DELIVERED -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = AssistiveRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "⚠ SMS konnte nicht zugestellt werden",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFB91C1C)
                                    )
                                }
                            }

                            smsSendStatus == SosSmsSendStatus.SENT -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = AssistiveGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "✓ SMS versendet",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = cardBorderColor.copy(alpha = 0.6f))

                    // C) LAUTSPRECHER-STATUS
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "🔊 Freisprechen aktiv",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                    }
                }
            }

            // 3. AUFLEGEN-BUTTON (Large child-friendly End Call Button)
            Button(
                onClick = handleEndCall,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .testTag("sos_end_call_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Notruf beenden",
                        modifier = Modifier.size(30.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "NOTRUF BEENDEN",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
