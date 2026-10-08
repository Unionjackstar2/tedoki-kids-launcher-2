package com.example.ui.calling

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.call.CallHistoryRepository
import com.example.model.LauncherSettings
import com.example.model.call.CallDirection
import com.example.model.call.CallHistoryEntry
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Submenu showing the history of incoming, outgoing, and missed calls.
 * Displays high-contrast pillow cards with caller name/number, direction badges,
 * timestamp, call duration, and a large back button at the bottom.
 * Allows redialing any call entry with one tap.
 */
@Composable
fun CallHistorySubScreen(
    settings: LauncherSettings,
    onCallNumber: (String, String?) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        CallHistoryRepository.init(context)
    }

    val callLogs by CallHistoryRepository.historyFlow.collectAsState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Verlauf leeren?",
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )
            },
            text = {
                Text(
                    text = "Möchtest du wirklich alle Einträge aus der Telefonhistorie löschen?",
                    color = AssistiveTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        CallHistoryRepository.clearAll(context)
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Löschen", color = AssistiveRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Abbrechen", color = AssistiveTextSecondary)
                }
            },
            containerColor = AssistiveSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AssistiveBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header: Title with History Icon + Clear Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)), // Blue accent for history
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Anrufverlauf",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )
                        Text(
                            text = "Ein- und ausgehende Anrufe",
                            fontSize = 14.sp,
                            color = AssistiveTextSecondary
                        )
                    }
                }

                if (callLogs.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AssistiveSurface)
                            .testTag("clear_call_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Verlauf leeren",
                            tint = AssistiveTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // List of Call History Items
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (callLogs.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = AssistiveSurface.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, AssistiveBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(AssistiveSurface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = AssistiveTextMuted,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(
                                    text = "Noch keine Anrufe vorhanden",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Ein- und ausgehende Telefonate werden hier übersichtlich angezeigt.",
                                    fontSize = 14.sp,
                                    color = AssistiveTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(callLogs, key = { it.id }) { call ->
                        CallHistoryCard(
                            call = call,
                            settings = settings,
                            onRedial = {
                                onCallNumber(call.phoneNumber, call.contactName)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large full-width Back Button at the bottom
            LargeBackButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("call_history_back_button")
            )
        }
    }
}

@Composable
private fun CallHistoryCard(
    call: CallHistoryEntry,
    settings: LauncherSettings,
    onRedial: () -> Unit
) {
    val matchedContact = remember(call.phoneNumber, settings.contacts) {
        settings.contacts.find {
            it.phoneNumber.replace("\\s+".toRegex(), "") ==
                call.phoneNumber.replace("\\s+".toRegex(), "")
        }
    }

    val displayName = call.contactName ?: matchedContact?.name ?: call.phoneNumber.ifBlank { "Unbekannt" }
    val displayPhoto = call.photoUri ?: matchedContact?.photoUri
    val avatarColorHex = matchedContact?.colorHex ?: 0xFF059669

    val formattedDate = remember(call.timestamp) {
        val sdf = SimpleDateFormat("dd.MM.yyyy • HH:mm", Locale.GERMANY)
        sdf.format(Date(call.timestamp))
    }

    val formattedDuration = remember(call.durationSeconds) {
        if (call.durationSeconds > 0) {
            val min = call.durationSeconds / 60
            val sec = call.durationSeconds % 60
            if (min > 0) "${min}m ${sec}s" else "${sec}s"
        } else {
            null
        }
    }

    val (directionIcon, directionColor, directionLabel) = when (call.direction) {
        CallDirection.OUTGOING -> Triple(
            Icons.AutoMirrored.Filled.CallMade,
            Color(0xFF0284C7), // Blue
            "Ausgehend"
        )
        CallDirection.INCOMING -> Triple(
            Icons.AutoMirrored.Filled.CallReceived,
            Color(0xFF059669), // Green
            "Eingehend"
        )
        CallDirection.MISSED -> Triple(
            Icons.AutoMirrored.Filled.CallMissed,
            AssistiveRed, // Red
            "Verpasst"
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, AssistiveBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onRedial() }
            .testTag("call_history_entry_${call.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(avatarColorHex)),
                contentAlignment = Alignment.Center
            ) {
                if (displayPhoto != null) {
                    AsyncImage(
                        model = displayPhoto,
                        contentDescription = displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Info Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = directionIcon,
                        contentDescription = null,
                        tint = directionColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = directionLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = directionColor
                    )
                    if (formattedDuration != null) {
                        Text(
                            text = "($formattedDuration)",
                            fontSize = 13.sp,
                            color = AssistiveTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "$formattedDate • ${call.phoneNumber}",
                    fontSize = 12.sp,
                    color = AssistiveTextMuted
                )
            }

            // Call Back Action Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF059669).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Anrufen",
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
