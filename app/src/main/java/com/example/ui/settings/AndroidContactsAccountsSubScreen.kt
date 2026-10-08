package com.example.ui.settings

import android.Manifest
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.contact.AndroidContactsRepository
import com.example.data.contact.DeviceContactAccount
import com.example.model.LauncherSettings
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary

/**
 * Submenu in Parent Dashboard to configure access to Android system contacts
 * and select which accounts (Google, SIM, Local, etc.) are allowed for the child.
 */
@Composable
fun AndroidContactsAccountsSubScreen(
    currentSettings: LauncherSettings,
    onSaveSettings: (LauncherSettings) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    BackHandler { onBack() }

    var isEnabled by remember { mutableStateOf(currentSettings.enableAndroidContacts) }
    var selectedAccounts by remember { mutableStateOf(currentSettings.allowedContactAccounts.toSet()) }
    var availableAccounts by remember { mutableStateOf<List<DeviceContactAccount>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(AndroidContactsRepository.hasContactsPermission(context)) }

    fun refreshAccounts() {
        hasPermission = AndroidContactsRepository.hasContactsPermission(context)
        if (hasPermission) {
            val fetched = AndroidContactsRepository.getAvailableContactAccounts(context)
            availableAccounts = fetched
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            refreshAccounts()
            Toast.makeText(context, "Zugriff auf Android-Kontakte gestattet", Toast.LENGTH_SHORT).show()
        } else {
            isEnabled = false
            onSaveSettings(currentSettings.copy(enableAndroidContacts = false))
            Toast.makeText(context, "Berechtigung für Kontakte erforderlich", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (hasPermission) {
            refreshAccounts()
        }
    }

    fun updateSettings(newEnabled: Boolean, newAccounts: Set<String>) {
        isEnabled = newEnabled
        selectedAccounts = newAccounts
        onSaveSettings(
            currentSettings.copy(
                enableAndroidContacts = newEnabled,
                allowedContactAccounts = newAccounts.toList()
            )
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
                .padding(horizontal = 20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AssistiveSurface)
                        .border(1.dp, AssistiveBorder, CircleShape)
                        .testTag("accounts_sub_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = AssistiveBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Android-Kontakte & Konten",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )
            }

            // Scrollable Settings
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Main Toggle Card
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
                                text = "Zugriff auf Android-Kontakte",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ermöglicht dem Kind, Telefonbuch-Kontakte im Telefonmenü aufzurufen",
                                fontSize = 12.sp,
                                color = AssistiveTextSecondary
                            )
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                if (checked && !hasPermission) {
                                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                } else {
                                    updateSettings(checked, selectedAccounts)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AssistiveGreen,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("toggle_android_contacts")
                        )
                    }
                }

                // If permission missing but enabled clicked
                if (!hasPermission) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                        border = BorderStroke(1.dp, AssistiveBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Berechtigung erforderlich",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                            Text(
                                text = "Um Kontakte aus dem Telefonspeicher oder Google-Konto freizugeben, wird die Lese-Berechtigung benötigt.",
                                fontSize = 13.sp,
                                color = AssistiveTextSecondary
                            )
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Berechtigung erteilen", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Account Selection Section
                if (isEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Freigegebene Konten auswählen",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )

                        IconButton(
                            onClick = { refreshAccounts() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Aktualisieren",
                                tint = AssistiveBlue
                            )
                        }
                    }

                    Text(
                        text = "Wähle aus, aus welchen Konten Kontakte für das Kind sichtbar sein dürfen (z.B. nur Familie oder Gerätekontakte). Ist kein Konto ausgewählt, sind alle Kontakte sichtbar.",
                        fontSize = 13.sp,
                        color = AssistiveTextSecondary,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )

                    if (availableAccounts.isEmpty()) {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = AssistiveSurface.copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, AssistiveBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Keine Konten gefunden oder Kontakte werden geladen...",
                                    fontSize = 13.sp,
                                    color = AssistiveTextMuted
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            availableAccounts.forEach { account ->
                                val isChecked = selectedAccounts.contains(account.accountName)
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isChecked) AssistiveBlue.copy(alpha = 0.08f) else AssistiveSurface
                                    ),
                                    border = BorderStroke(
                                        width = if (isChecked) 1.5.dp else 1.dp,
                                        color = if (isChecked) AssistiveBlue else AssistiveBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val newSet = selectedAccounts.toMutableSet()
                                            if (isChecked) {
                                                newSet.remove(account.accountName)
                                            } else {
                                                newSet.add(account.accountName)
                                            }
                                            updateSettings(isEnabled, newSet)
                                        }
                                        .testTag("account_item_${account.accountName}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE2E8F0)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountBox,
                                                    contentDescription = null,
                                                    tint = AssistiveBlue,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = account.displayTitle,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AssistiveTextPrimary
                                                )
                                                Text(
                                                    text = "Typ: ${account.accountType}",
                                                    fontSize = 12.sp,
                                                    color = AssistiveTextSecondary
                                                )
                                            }
                                        }

                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                val newSet = selectedAccounts.toMutableSet()
                                                if (checked) {
                                                    newSet.add(account.accountName)
                                                } else {
                                                    newSet.remove(account.accountName)
                                                }
                                                updateSettings(isEnabled, newSet)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = AssistiveBlue,
                                                uncheckedColor = Color(0xFF94A3B8)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Standard bottom back button
            LargeBackButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("android_contacts_settings_back")
            )
        }
    }
}
