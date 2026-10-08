package com.example.ui.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.LauncherContact
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
import com.example.ui.theme.assistiveTextFieldColors
import java.util.UUID

@Composable
fun ContactsSettingsSubScreen(
    currentSettings: LauncherSettings,
    onSaveSettings: (LauncherSettings) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    var showAccountsSubScreen by remember { mutableStateOf(false) }

    BackHandler { 
        if (showAccountsSubScreen) {
            showAccountsSubScreen = false
        } else {
            onBack()
        }
    }

    var contactsList by remember { mutableStateOf(currentSettings.contacts) }
    var newContactName by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }

    if (showAccountsSubScreen) {
        AndroidContactsAccountsSubScreen(
            currentSettings = currentSettings,
            onSaveSettings = onSaveSettings,
            onBack = { showAccountsSubScreen = false }
        )
        return
    }

    val hasPhoneButton = currentSettings.buttons.any { it.type == ButtonType.CALL }

    fun saveContacts(newList: List<LauncherContact>) {
        contactsList = newList
        onSaveSettings(currentSettings.copy(contacts = newList))
    }

    fun toggleHomescreenPhoneButton(enable: Boolean) {
        val updatedButtons = currentSettings.buttons.toMutableList()
        if (enable && !hasPhoneButton) {
            updatedButtons.add(
                LauncherButton(
                    id = "call_default",
                    type = ButtonType.CALL,
                    title = "Telefon",
                    colorHex = 0xFF059669
                )
            )
            onSaveSettings(currentSettings.copy(buttons = updatedButtons))
            Toast.makeText(context, "„Telefon“ wurde zum Homescreen hinzugefügt", Toast.LENGTH_SHORT).show()
        } else if (!enable && hasPhoneButton) {
            updatedButtons.removeAll { it.type == ButtonType.CALL }
            onSaveSettings(currentSettings.copy(buttons = updatedButtons))
            Toast.makeText(context, "„Telefon“ vom Homescreen entfernt", Toast.LENGTH_SHORT).show()
        }
    }

    fun addNewContact() {
        val name = newContactName.trim()
        val phone = newContactPhone.trim()

        if (name.isBlank() || phone.isBlank()) {
            Toast.makeText(context, "Bitte Name und Telefonnummer eingeben", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate a random pleasant color for the contact avatar
        val colors = listOf(0xFF2563EB, 0xFF059669, 0xFF7C3AED, 0xFFDB2777, 0xFFEA580C, 0xFF0284C7)
        val randomColor = colors.random()

        val newContact = LauncherContact(
            id = "contact_${UUID.randomUUID()}",
            name = name,
            phoneNumber = phone,
            colorHex = randomColor
        )

        saveContacts(contactsList + newContact)
        newContactName = ""
        newContactPhone = ""
        focusManager.clearFocus()
        keyboardController?.hide()
        Toast.makeText(context, "Kontakt „$name“ wurde gespeichert", Toast.LENGTH_SHORT).show()
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
                        .testTag("contacts_sub_back_button")
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
                    text = "Kontakte verwalten",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Switch: Show Phone Button on Homescreen
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
                                text = "Telefon-App auf dem Homescreen",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                            Text(
                                text = "Ermöglicht dem Kind, die Telefonfunktion aufzurufen",
                                fontSize = 12.sp,
                                color = AssistiveTextSecondary
                            )
                        }

                        Switch(
                            checked = hasPhoneButton,
                            onCheckedChange = { toggleHomescreenPhoneButton(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AssistiveGreen,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.Transparent
                            ),
                            modifier = Modifier.testTag("toggle_homescreen_phone")
                        )
                    }
                }

                // Submenu Entry: Android Contacts & Account Permissions
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccountsSubScreen = true }
                        .testTag("open_android_contacts_accounts_sub")
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AssistiveBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = null,
                                    tint = AssistiveBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Android-Kontakte & Konten",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = if (currentSettings.enableAndroidContacts) {
                                        if (currentSettings.allowedContactAccounts.isEmpty()) "Aktiviert (Alle Konten)"
                                        else "Aktiviert (${currentSettings.allowedContactAccounts.size} Konten)"
                                    } else "Deaktiviert",
                                    fontSize = 12.sp,
                                    color = if (currentSettings.enableAndroidContacts) AssistiveGreen else AssistiveTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Card: Add New Contact
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Neuen Kontakt hinzufügen",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )

                        OutlinedTextField(
                            value = newContactName,
                            onValueChange = { newContactName = it },
                            label = { Text("Name (z.B. Mama)") },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = AssistiveTextPrimary),
                            colors = assistiveTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_contact_name"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = newContactPhone,
                            onValueChange = { newContactPhone = it },
                            label = { Text("Telefonnummer (z.B. 555-0101)") },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, color = AssistiveTextPrimary),
                            colors = assistiveTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_contact_phone"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = { addNewContact() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AssistiveBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_contact_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kontakt speichern", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Header: Existing Contacts
                Text(
                    text = "Eingerichtete Kontakte (${contactsList.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // List of Existing Contacts
                if (contactsList.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = AssistiveSurface.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, AssistiveBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Keine Kontakte eingerichtet.\nFüge oben einen neuen Kontakt hinzu.",
                                fontSize = 14.sp,
                                color = AssistiveTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        contactsList.forEach { contact ->
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                                border = BorderStroke(1.5.dp, AssistiveBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Avatar Circle
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(Color(contact.colorHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = contact.name.take(1).uppercase(),
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = contact.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AssistiveTextPrimary
                                            )
                                            Text(
                                                text = contact.phoneNumber,
                                                fontSize = 13.sp,
                                                color = AssistiveTextSecondary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            val updated = contactsList.filter { it.id != contact.id }
                                            saveContacts(updated)
                                            Toast.makeText(context, "Kontakt gelöscht", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFECEF)),
                                        colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                                            contentColor = Color(0xFFEF4444)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Kontakt löschen",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Back Button at the very bottom
            LargeBackButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("contacts_settings_back")
            )
        }
    }
}
