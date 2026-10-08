package com.example.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.model.LauncherSettings
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.assistiveTextFieldColors
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary

@Composable
fun EmergencySettingsSubScreen(
    currentSettings: LauncherSettings,
    onSaveSettings: (LauncherSettings) -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val safeBack = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onBack()
    }

    BackHandler { safeBack() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showSosButton by remember { mutableStateOf(currentSettings.showSosButton) }
    var emergencyNumber by remember { mutableStateOf(currentSettings.emergencyNumber) }
    var emergencyContactName by remember { mutableStateOf(currentSettings.emergencyContactName) }
    var sendSosLocationSms by remember { mutableStateOf(currentSettings.sendSosLocationSms) }
    var showSosLogSubScreen by remember { mutableStateOf(false) }

    if (showSosLogSubScreen) {
        SosLogSubScreen(onBack = { showSosLogSubScreen = false })
        return
    }

    fun checkPermissions(): Boolean {
        val hasLoc = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        return hasLoc && hasSms
    }

    var hasPermissions by remember { mutableStateOf(checkPermissions()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        hasPermissions = checkPermissions()
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
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = AssistiveRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.emergency_submenu_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Notrufnummer & Standort-SMS verwalten",
                            fontSize = 13.sp,
                            color = AssistiveTextSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("emergency_submenu_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = AssistiveTextPrimary
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SOS-Protokoll Entry Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSosLogSubScreen = true }
                        .testTag("open_sos_log_entry")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
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

                            Column {
                                Text(
                                    text = "SOS-Protokoll",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = "Ausgelöste Notrufe & SMS-Zustellungen einsehen",
                                    fontSize = 13.sp,
                                    color = AssistiveTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 1. Toggle: Show/Hide SOS Button on Home Screen
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_sos_toggle_title),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                            Text(
                                text = stringResource(R.string.settings_sos_toggle_desc),
                                fontSize = 13.sp,
                                color = AssistiveTextSecondary
                            )
                        }
                        Switch(
                            checked = showSosButton,
                            onCheckedChange = { checked ->
                                showSosButton = checked
                                onSaveSettings(currentSettings.copy(showSosButton = checked))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFDC2626)),
                            modifier = Modifier.testTag("toggle_sos_button_visibility")
                        )
                    }
                }

                // 2. Emergency Contact Number & Name
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = AssistiveBlue, modifier = Modifier.size(20.dp))
                            Text(
                                text = stringResource(R.string.settings_emergency_contact),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                        }

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetNumberChip(
                                label = "112 (Notruf)",
                                isSelected = emergencyNumber == "112",
                                onClick = {
                                    emergencyNumber = "112"
                                    emergencyContactName = "Notruf 112"
                                    onSaveSettings(currentSettings.copy(emergencyNumber = "112", emergencyContactName = "Notruf 112"))
                                }
                            )
                            PresetNumberChip(
                                label = "110 (Polizei)",
                                isSelected = emergencyNumber == "110",
                                onClick = {
                                    emergencyNumber = "110"
                                    emergencyContactName = "Polizei 110"
                                    onSaveSettings(currentSettings.copy(emergencyNumber = "110", emergencyContactName = "Polizei 110"))
                                }
                            )
                            PresetNumberChip(
                                label = "911",
                                isSelected = emergencyNumber == "911",
                                onClick = {
                                    emergencyNumber = "911"
                                    emergencyContactName = "Emergency 911"
                                    onSaveSettings(currentSettings.copy(emergencyNumber = "911", emergencyContactName = "Emergency 911"))
                                }
                            )
                        }

                        OutlinedTextField(
                            value = emergencyNumber,
                            onValueChange = {
                                emergencyNumber = it
                                onSaveSettings(currentSettings.copy(emergencyNumber = it))
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = AssistiveTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            ),
                            label = { Text(stringResource(R.string.settings_emergency_number)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = assistiveTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sub_emergency_number_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = emergencyContactName,
                            onValueChange = {
                                emergencyContactName = it
                                onSaveSettings(currentSettings.copy(emergencyContactName = it))
                            },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = AssistiveTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            ),
                            label = { Text(stringResource(R.string.contact_name)) },
                            colors = assistiveTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sub_emergency_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                // 3. Location via SMS Setting
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.send_location_sms_title),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AssistiveTextPrimary
                                    )
                                    Text(
                                        text = stringResource(R.string.send_location_sms_desc),
                                        fontSize = 13.sp,
                                        color = AssistiveTextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = sendSosLocationSms,
                                onCheckedChange = { checked ->
                                    sendSosLocationSms = checked
                                    onSaveSettings(currentSettings.copy(sendSosLocationSms = checked))
                                    if (checked && !hasPermissions) {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.SEND_SMS
                                            )
                                        )
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AssistiveBlue),
                                modifier = Modifier.testTag("toggle_send_location_sms")
                            )
                        }

                        // Permission status
                        if (sendSosLocationSms) {
                            if (hasPermissions) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFECFDF5))
                                        .padding(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AssistiveGreen, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = stringResource(R.string.location_permission_granted),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AssistiveGreen
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.SEND_SMS
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEFF6FF),
                                        contentColor = AssistiveBlue
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.request_permissions_btn),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. SMS Preview Mockup
                if (sendSosLocationSms) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                                Text(
                                    text = stringResource(R.string.emergency_sms_preview_title),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                            }
                            Text(
                                text = stringResource(R.string.emergency_sms_preview_sample),
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Return Button
            LargeBackButton(
                onClick = safeBack,
                text = stringResource(R.string.nav_back),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun PresetNumberChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AssistiveBlue else Color(0xFFF1F5F9))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else AssistiveTextPrimary
        )
    }
}
