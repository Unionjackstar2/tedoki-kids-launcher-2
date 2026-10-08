package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.sos.SosLogRepository
import com.example.model.sos.SosCallStatus
import com.example.model.sos.SosLogEntry
import com.example.model.sos.SosOverallOutcome
import com.example.model.sos.SosSmsDeliveryStatus
import com.example.model.sos.SosSmsSendStatus
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SosLogSubScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        SosLogRepository.init(context)
    }

    val logs by SosLogRepository.logsFlow.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text(
                    text = "SOS-Protokoll löschen?",
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )
            },
            text = {
                Text(
                    text = "Möchtest du alle Einträge im SOS-Protokoll unwiderruflich löschen?",
                    color = AssistiveTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        SosLogRepository.clearAllLogs(context)
                        showDeleteAllDialog = false
                    },
                    modifier = Modifier.testTag("confirm_delete_all_sos_logs")
                ) {
                    Text("Alle löschen", color = AssistiveRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Abbrechen", color = AssistiveTextPrimary)
                }
            },
            containerColor = AssistiveSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AssistiveBackground)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().coerceAtLeast(64.dp))
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = AssistiveRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SOS-Protokoll",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Verlauf der Notrufe & SMS-Zustellungen",
                            fontSize = 13.sp,
                            color = AssistiveTextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (logs.isNotEmpty()) {
                        IconButton(
                            onClick = { showDeleteAllDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("delete_all_sos_logs_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Alle löschen",
                                tint = AssistiveRed
                            )
                        }
                    }

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("sos_log_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück",
                            tint = AssistiveTextPrimary
                        )
                    }
                }
            }

            // Content
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECFDF5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AssistiveGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Text(
                            text = "Keine SOS-Notrufe vorhanden",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )
                        Text(
                            text = "Ausgelöste Notrufe und SMS-Zustellberichte werden hier automatisch lokal für 90 Tage dokumentiert.",
                            fontSize = 13.sp,
                            color = AssistiveTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 12.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
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
                                    text = "Ereignisse werden ohne Standortdaten gespeichert und nach 90 Tagen automatisch bereinigt.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    items(logs, key = { it.id }) { entry ->
                        SosLogCard(
                            entry = entry,
                            onDelete = {
                                SosLogRepository.deleteEntry(context, entry.id)
                            }
                        )
                    }
                }
            }

            // Bottom Return Button
            LargeBackButton(
                onClick = onBack,
                text = stringResource(R.string.nav_back),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun SosLogCard(
    entry: SosLogEntry,
    onDelete: () -> Unit
) {
    val dateStr = remember(entry.timestamp) {
        val format = SimpleDateFormat("dd.MM.yyyy · HH:mm 'Uhr'", Locale.GERMANY)
        format.format(Date(entry.timestamp))
    }

    val outcome = entry.overallOutcome
    val outcomeTitle = when (outcome) {
        SosOverallOutcome.SUCCESS -> "SOS erfolgreich"
        SosOverallOutcome.PARTIAL -> "SOS teilweise erfolgreich"
        SosOverallOutcome.FAILED -> "SOS fehlgeschlagen"
    }

    val outcomeColor = when (outcome) {
        SosOverallOutcome.SUCCESS -> Color(0xFF059669) // Grün
        SosOverallOutcome.PARTIAL -> Color(0xFFD97706) // Orange
        SosOverallOutcome.FAILED -> Color(0xFFDC2626)  // Rot
    }

    val outcomeEmoji = when (outcome) {
        SosOverallOutcome.SUCCESS -> "🟢"
        SosOverallOutcome.PARTIAL -> "🟠"
        SosOverallOutcome.FAILED -> "🔴"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
        border = BorderStroke(1.dp, AssistiveBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sos_log_entry_${entry.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Date & Time + Single Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateStr,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = AssistiveTextMuted
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_log_btn_${entry.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eintrag löschen",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Outcome Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$outcomeEmoji $outcomeTitle",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = outcomeColor
                )
            }

            // Bullet points
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(start = 6.dp)
            ) {
                // Call line
                val callLine = when (entry.callStatus) {
                    SosCallStatus.INITIATED -> {
                        val recipient = if (entry.emergencyNumber.isNotBlank()) " (${entry.emergencyNumber})" else ""
                        "• Notruf getätigt$recipient"
                    }
                    SosCallStatus.FAILED -> "• Notruf fehlgeschlagen"
                    SosCallStatus.NOT_ATTEMPTED -> "• Kein Notruf hinterlegt"
                }
                val callLineColor = when (entry.callStatus) {
                    SosCallStatus.INITIATED -> AssistiveTextPrimary
                    SosCallStatus.FAILED -> AssistiveRed
                    SosCallStatus.NOT_ATTEMPTED -> AssistiveTextSecondary
                }
                Text(
                    text = callLine,
                    fontSize = 14.sp,
                    color = callLineColor,
                    fontWeight = FontWeight.Medium
                )

                // SMS Send line
                if (entry.smsSendStatus != SosSmsSendStatus.NOT_REQUESTED) {
                    val sendLine = when (entry.smsSendStatus) {
                        SosSmsSendStatus.SENT -> "• SMS versendet"
                        SosSmsSendStatus.SENDING -> "• SMS wird versendet..."
                        SosSmsSendStatus.FAILED -> "• SMS fehlgeschlagen"
                        SosSmsSendStatus.NOT_REQUESTED -> ""
                    }
                    val sendColor = when (entry.smsSendStatus) {
                        SosSmsSendStatus.SENT -> AssistiveTextPrimary
                        SosSmsSendStatus.SENDING -> Color(0xFFD97706)
                        SosSmsSendStatus.FAILED -> AssistiveRed
                        SosSmsSendStatus.NOT_REQUESTED -> AssistiveTextPrimary
                    }
                    Text(
                        text = sendLine,
                        fontSize = 14.sp,
                        color = sendColor,
                        fontWeight = FontWeight.Medium
                    )

                    // SMS Delivery line
                    val deliveryLine = when (entry.smsDeliveryStatus) {
                        SosSmsDeliveryStatus.DELIVERED -> "• SMS zugestellt"
                        SosSmsDeliveryStatus.PENDING -> "• SMS-Zustellung ausstehend"
                        SosSmsDeliveryStatus.NOT_DELIVERED -> "• SMS nicht zugestellt"
                        SosSmsDeliveryStatus.NOT_REQUESTED -> ""
                    }
                    val deliveryColor = when (entry.smsDeliveryStatus) {
                        SosSmsDeliveryStatus.DELIVERED -> AssistiveTextPrimary
                        SosSmsDeliveryStatus.PENDING -> Color(0xFFD97706)
                        SosSmsDeliveryStatus.NOT_DELIVERED -> AssistiveRed
                        SosSmsDeliveryStatus.NOT_REQUESTED -> AssistiveTextPrimary
                    }
                    Text(
                        text = deliveryLine,
                        fontSize = 14.sp,
                        color = deliveryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
