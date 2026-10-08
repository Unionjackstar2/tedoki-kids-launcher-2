package com.example.ui.calling

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.contact.AndroidContactsRepository
import com.example.data.contact.AndroidDeviceContact
import com.example.model.LauncherSettings
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.assistiveTextFieldColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Submenu in Phone Screen for browsing, searching, and calling Android contacts.
 * Features:
 * - Search bar with real-time name & phone number filter
 * - Safe background loading (Dispatchers.IO) with progress indicator
 * - Contact cards with photo/avatar fallback, account badge, and one-tap call
 * - Bottom full-width large back button
 */
@Composable
fun AndroidContactsSubScreen(
    settings: LauncherSettings,
    onCallNumber: (String, String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(AndroidContactsRepository.hasContactsPermission(context)) }
    var allContacts by remember { mutableStateOf<List<AndroidDeviceContact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun loadContactsAsync() {
        if (!AndroidContactsRepository.hasContactsPermission(context)) {
            hasPermission = false
            isLoading = false
            return
        }
        hasPermission = true
        isLoading = true
        coroutineScope.launch {
            val loaded = try {
                withContext(Dispatchers.IO) {
                    AndroidContactsRepository.loadContacts(
                        context = context,
                        allowedAccountNames = settings.allowedContactAccounts
                    )
                }
            } catch (e: Throwable) {
                emptyList()
            }
            allContacts = loaded
            isLoading = false
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            loadContactsAsync()
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        if (hasPermission) {
            loadContactsAsync()
        } else {
            isLoading = false
        }
    }

    val filteredContacts = remember(allContacts, searchQuery) {
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            allContacts
        } else {
            allContacts.filter { contact ->
                contact.name.lowercase().contains(query) ||
                contact.phoneNumber.replace("\\s|-".toRegex(), "").contains(query)
            }
        }
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
            // Header: Icon + Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AssistiveBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = "Kontakte",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )
                    Text(
                        text = "Telefonbuch durchsuchen & anrufen",
                        fontSize = 14.sp,
                        color = AssistiveTextSecondary
                    )
                }
            }

            // Search Bar with clear button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .testTag("contacts_search_input"),
                placeholder = { Text("Kontakt suchen...", color = AssistiveTextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Suche",
                        tint = AssistiveTextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Löschen",
                                tint = AssistiveTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = assistiveTextFieldColors()
            )

            // Content Area: Contacts list or Permission request or Empty state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (!hasPermission) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AssistiveBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = null,
                                tint = AssistiveBlue,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Zugriff auf Kontakte erforderlich",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Bitte erlaube den Zugriff auf deine Telefonbuch-Kontakte, um hier Personen auswählen und anrufen zu können.",
                                fontSize = 14.sp,
                                color = AssistiveTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Berechtigung erteilen", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                } else if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = AssistiveBlue,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Kontakte werden geladen...",
                                color = AssistiveTextMuted,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else if (filteredContacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Kein Kontakt für „$searchQuery“ gefunden" else "Keine Kontakte im Telefonbuch verfügbar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = AssistiveTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("android_contacts_list"),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredContacts, key = { it.id + "_" + it.phoneNumber }) { contact ->
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, AssistiveBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onCallNumber(contact.phoneNumber, contact.name) }
                                    .testTag("android_contact_item_${contact.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Avatar / Photo
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(AssistiveBlue.copy(alpha = 0.15f)),
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
                                                Text(
                                                    text = contact.name.take(1).uppercase(),
                                                    color = AssistiveBlue,
                                                    fontSize = 22.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = contact.name,
                                                fontSize = 18.sp,
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

                                    // Green Call Icon Button
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF059669)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "Anrufen",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large full-width Back button at the bottom
            LargeBackButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("android_contacts_sub_back_button")
            )
        }
    }
}
