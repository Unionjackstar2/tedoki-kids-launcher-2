package com.example.ui.calling

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.LauncherSettings
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary

/**
 * ChildCallScreen displays the contact list configured by the parents in the parent settings.
 * It matches the beautiful "Pillow Design" of the SMS Contact selection screen.
 * Tapping a contact initiates the standard calling routine instantly.
 */
@Composable
fun ChildCallScreen(
    settings: LauncherSettings,
    onDialContact: (LauncherButton) -> Unit,
    onBack: () -> Unit
) {
    var showKeypadSheet by remember { mutableStateOf(false) }
    var showHistorySubScreen by remember { mutableStateOf(false) }
    var showAndroidContactsSubScreen by remember { mutableStateOf(false) }

    if (showHistorySubScreen) {
        CallHistorySubScreen(
            settings = settings,
            onCallNumber = { number, name ->
                showHistorySubScreen = false
                val dialedButton = LauncherButton(
                    id = "history_call_${System.currentTimeMillis()}",
                    type = ButtonType.CALL,
                    title = name ?: number,
                    phoneNumber = number,
                    colorHex = 0xFF059669
                )
                onDialContact(dialedButton)
            },
            onBack = { showHistorySubScreen = false }
        )
        return
    }

    if (showAndroidContactsSubScreen) {
        AndroidContactsSubScreen(
            settings = settings,
            onCallNumber = { number, name ->
                showAndroidContactsSubScreen = false
                val dialedButton = LauncherButton(
                    id = "android_contact_call_${System.currentTimeMillis()}",
                    type = ButtonType.CALL,
                    title = name.ifBlank { number },
                    phoneNumber = number,
                    colorHex = 0xFF0284C7
                )
                onDialContact(dialedButton)
            },
            onBack = { showAndroidContactsSubScreen = false }
        )
        return
    }

    BackHandler {
        if (showKeypadSheet) {
            showKeypadSheet = false
        } else {
            onBack()
        }
    }

    val contacts = settings.contacts

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
            // Header: Icon + Title matching SMS screen layout + Optional Contacts & History Submenu Buttons on the top right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF059669)), // Distinctive Green for Phone
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Telefon",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )
                        Text(
                            text = "Wen möchtest du anrufen?",
                            fontSize = 14.sp,
                            color = AssistiveTextSecondary
                        )
                    }
                }

                // Action buttons on the top right:
                // Links neben dem Button der Telefonhistorie: Kontakte Button (gleiche Größe 48.dp), falls freigegeben
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (settings.enableAndroidContacts) {
                        IconButton(
                            onClick = { showAndroidContactsSubScreen = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AssistiveSurface)
                                .testTag("open_android_contacts_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = "Kontakte",
                                tint = AssistiveTextPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Button top right next to "Telefon" to enter call history submenu
                    IconButton(
                        onClick = { showHistorySubScreen = true },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AssistiveSurface)
                            .testTag("open_call_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Anrufverlauf",
                            tint = AssistiveTextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Contact Pillow Cards
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (contacts.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = AssistiveSurface.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, AssistiveBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Keine Kontakte eingerichtet.\nBitte die Eltern im Dashboard fragen.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AssistiveTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(contacts, key = { it.id }) { contact ->
                        Card(
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, AssistiveBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(26.dp))
                                .clickable {
                                    // Create a temporary launcher button for calling routine
                                    val tempButton = LauncherButton(
                                        id = contact.id,
                                        type = ButtonType.CALL,
                                        title = contact.name,
                                        phoneNumber = contact.phoneNumber,
                                        photoUri = contact.photoUri,
                                        colorHex = contact.colorHex
                                    )
                                    onDialContact(tempButton)
                                }
                                .testTag("call_contact_${contact.phoneNumber}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Avatar Circle
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(contact.colorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (contact.photoUri != null) {
                                        AsyncImage(
                                            model = contact.photoUri,
                                            contentDescription = contact.name,
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
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = contact.name,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AssistiveTextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = contact.phoneNumber,
                                        fontSize = 14.sp,
                                        color = AssistiveTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Optional Keypad Dialer Button (if enabled in Parent Settings)
            if (settings.enableKeypadDialer) {
                Button(
                    onClick = { showKeypadSheet = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("open_keypad_dialer_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dialpad,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Tastenfeld öffnen",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Large full-width Back button at the very bottom
            LargeBackButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("call_screen_back_button")
            )
        }

        // Bottom Sheet Overlay for Keypad Dialing (anchored to the bottom)
        KeypadDialerSheet(
            visible = showKeypadSheet,
            modifier = Modifier.align(Alignment.BottomCenter),
            onDismiss = { showKeypadSheet = false },
            onCallNumber = { dialedNumber ->
                showKeypadSheet = false
                val customCallButton = LauncherButton(
                    id = "dialed_call_${System.currentTimeMillis()}",
                    type = ButtonType.CALL,
                    title = dialedNumber,
                    phoneNumber = dialedNumber,
                    colorHex = 0xFF059669
                )
                onDialContact(customCallButton)
            }
        )
    }
}
