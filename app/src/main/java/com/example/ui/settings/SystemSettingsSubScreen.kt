package com.example.ui.settings

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.LauncherSettings
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveGreenLight
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.LocalIsDarkTheme

/**
 * Clean, structured sub-menu for System, Clock/Calendar interactions, and Telephony default apps.
 */
@Composable
fun SystemSettingsSubScreen(
    currentSettings: LauncherSettings,
    onSaveSettings: (LauncherSettings) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val scrollState = rememberScrollState()

    var enableClockClick by remember { mutableStateOf(currentSettings.enableClockClick) }
    var enableCalendarClick by remember { mutableStateOf(currentSettings.enableCalendarClick) }
    var enableKeypadDialer by remember { mutableStateOf(currentSettings.enableKeypadDialer) }

    var isDefaultDialer by remember {
        mutableStateOf(com.example.telecom.CallManager.isDefaultDialer(context))
    }
    val dialerRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultDialer = com.example.telecom.CallManager.isDefaultDialer(context)
    }

    var isDefaultSms by remember {
        mutableStateOf(com.example.sms.SmsRepository.isDefaultSmsApp(context))
    }
    val smsRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isDefaultSms = com.example.sms.SmsRepository.isDefaultSmsApp(context)
    }

    LaunchedEffect(Unit) {
        isDefaultDialer = com.example.telecom.CallManager.isDefaultDialer(context)
        isDefaultSms = com.example.sms.SmsRepository.isDefaultSmsApp(context)
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
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = AssistiveTextPrimary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "System & Telefonie",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AssistiveTextPrimary
                    )
                    Text(
                        text = "Standard-Apps & Interaktionen verwalten",
                        fontSize = 13.sp,
                        color = AssistiveTextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category: Homescreen-Interaktionen
                Text(
                    text = "HOMESCREEN-INTERAKTIONEN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = AssistiveTextMuted,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Clock click toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_clock_click_title),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.settings_clock_click_desc),
                                    fontSize = 12.sp,
                                    color = AssistiveTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = enableClockClick,
                                onCheckedChange = { checked ->
                                    enableClockClick = checked
                                    onSaveSettings(currentSettings.copy(enableClockClick = checked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AssistiveBlue
                                ),
                                modifier = Modifier.testTag("toggle_clock_click")
                            )
                        }

                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(AssistiveBorder))

                        // Calendar click toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_calendar_click_title),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.settings_calendar_click_desc),
                                    fontSize = 12.sp,
                                    color = AssistiveTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = enableCalendarClick,
                                onCheckedChange = { checked ->
                                    enableCalendarClick = checked
                                    onSaveSettings(currentSettings.copy(enableCalendarClick = checked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AssistiveBlue
                                ),
                                modifier = Modifier.testTag("toggle_calendar_click")
                            )
                        }
                    }
                }

                // Category: Telefonie & SMS
                Text(
                    text = "TELEFONIE & NACHRICHTEN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = AssistiveTextMuted,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Keypad Dialer Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ziffern-Tastenfeld in Telefonie",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = "Ermöglicht dem Kind die manuelle Eingabe beliebiger Rufnummern.",
                                    fontSize = 12.sp,
                                    color = AssistiveTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Switch(
                                checked = enableKeypadDialer,
                                onCheckedChange = { checked ->
                                    enableKeypadDialer = checked
                                    onSaveSettings(currentSettings.copy(enableKeypadDialer = checked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AssistiveGreen
                                ),
                                modifier = Modifier.testTag("toggle_keypad_dialer")
                            )
                        }

                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(AssistiveBorder))

                        // Default Dialer App
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Standard-Telefon-App",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = if (isDefaultDialer) {
                                        "Aktiv: Vollbild-Anrufe direkt in TEDOKI Kids"
                                    } else {
                                        "Inaktiv: Tippen zum Festlegen als Standard-Telefon"
                                    },
                                    fontSize = 12.sp,
                                    color = if (isDefaultDialer) AssistiveGreen else AssistiveTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            if (!isDefaultDialer) {
                                Button(
                                    onClick = {
                                        val intent = com.example.telecom.CallManager.createDefaultDialerIntent(context)
                                        if (intent != null) {
                                            dialerRoleLauncher.launch(intent)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "Festlegen", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AssistiveGreenLight)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(text = "✓ Aktiv", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AssistiveGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(AssistiveBorder))

                        // Default SMS App
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Standard-SMS-App",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = if (isDefaultSms) {
                                        "Aktiv: Kindgerechte SMS-Kommunikation"
                                    } else {
                                        "Inaktiv: Tippen zum Festlegen als Standard-SMS"
                                    },
                                    fontSize = 12.sp,
                                    color = if (isDefaultSms) AssistiveGreen else AssistiveTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            if (!isDefaultSms) {
                                Button(
                                    onClick = {
                                        val intent = com.example.sms.SmsRepository.createDefaultSmsIntent(context)
                                        if (intent != null) {
                                            smsRoleLauncher.launch(intent)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "Festlegen", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AssistiveGreenLight)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(text = "✓ Aktiv", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AssistiveGreen)
                                }
                            }
                        }
                    }
                }

                // Category: Android Launcher Einstellungen
                Text(
                    text = "ANDROID-SYSTEM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = AssistiveTextMuted,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            try {
                                val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (ignored: Exception) {}
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = AssistiveBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_default_launcher),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AssistiveTextPrimary
                            )
                            Text(
                                text = stringResource(R.string.settings_default_launcher_desc),
                                fontSize = 12.sp,
                                color = AssistiveTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
