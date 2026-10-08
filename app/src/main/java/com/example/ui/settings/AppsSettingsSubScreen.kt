package com.example.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.LauncherSettings
import com.example.ui.components.AddButtonChoiceDialog
import com.example.ui.components.AppPickerDialog
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.assistiveTextFieldColors
import com.example.util.InstalledAppIcon

/**
 * Dedicated Subscreen for selecting and managing apps / function buttons on the Homescreen.
 */
@Composable
fun AppsSettingsSubScreen(
    currentSettings: LauncherSettings,
    onSaveSettings: (LauncherSettings) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isDark = LocalIsDarkTheme.current

    BackHandler { onBack() }

    var buttons by remember { mutableStateOf(currentSettings.buttons.toMutableList()) }

    var showAddChoiceDialog by remember { mutableStateOf(false) }
    var showAppPickerDialog by remember { mutableStateOf(false) }

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
                            .background(if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            tint = AssistiveBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Apps & Tasten verwalten",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${buttons.size} von 8 Tasten auf dem Homescreen aktiv",
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
                        .testTag("apps_settings_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = AssistiveTextPrimary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Button List Card
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Aktive Tasten",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = "Positionen können per Drag & Drop auf dem Homescreen sortiert werden",
                                    fontSize = 12.sp,
                                    color = AssistiveTextMuted
                                )
                            }

                            if (buttons.size < 8) {
                                Surface(
                                    onClick = { showAddChoiceDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFFE0F2FE),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF3B82F6) else Color(0xFFBAE6FD)),
                                    modifier = Modifier.testTag("add_button_top_action")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = AssistiveBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "+ Neu",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AssistiveBlue
                                        )
                                    }
                                }
                            }
                        }

                        // Buttons items
                        buttons.forEachIndexed { index, button ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(button.colorHex)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (button.photoUri != null) {
                                                    AsyncImage(
                                                        model = button.photoUri,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else if (button.type == ButtonType.APP) {
                                                    InstalledAppIcon(
                                                        packageName = button.packageName,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                } else {
                                                    val icon = when (button.type) {
                                                        ButtonType.CAMERA -> Icons.Default.PhotoCamera
                                                        ButtonType.PHOTOS -> Icons.Default.Image
                                                        ButtonType.CALL -> Icons.Default.Phone
                                                        ButtonType.APP -> Icons.Default.Phone
                                                        ButtonType.BROWSER -> Icons.Default.Language
                                                        ButtonType.SMS -> Icons.Default.Email
                                                    }
                                                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }

                                            Column {
                                                Text(
                                                    text = com.example.ui.components.getLocalizedButtonTitle(button),
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AssistiveTextPrimary
                                                )
                                                if (button.type == ButtonType.APP) {
                                                    Text(
                                                        text = button.packageName,
                                                        fontSize = 11.sp,
                                                        color = AssistiveTextMuted,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }

                                        if (buttons.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    val updated = buttons.toMutableList()
                                                    updated.removeAt(index)
                                                    buttons = updated
                                                    onSaveSettings(currentSettings.copy(buttons = updated))
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Entfernen",
                                                    tint = if (isDark) Color(0xFFEF4444) else Color(0xFF94A3B8),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = button.title,
                                        onValueChange = { newTitle ->
                                            val updated = buttons.toMutableList()
                                            updated[index] = button.copy(title = newTitle)
                                            buttons = updated
                                            onSaveSettings(currentSettings.copy(buttons = updated))
                                        },
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = AssistiveTextPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        label = { Text("Tasten-Beschriftung") },
                                        colors = assistiveTextFieldColors(),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }
                        }

                        // Compact Add Button below list
                        if (buttons.size < 8) {
                            Surface(
                                onClick = { showAddChoiceDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFBAE6FD)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_button_bottom_action")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = AssistiveBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Taste / App hinzufügen (${buttons.size}/8)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AssistiveBlue
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LargeBackButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        }

        // Add Button Choice Dialog
        if (showAddChoiceDialog) {
            AddButtonChoiceDialog(
                onAddAppChoice = {
                    showAddChoiceDialog = false
                    showAppPickerDialog = true
                },
                onAddContactChoice = {
                    showAddChoiceDialog = false
                    if (buttons.size < 8) {
                        val newBtn = LauncherButton(
                            id = "btn_${System.currentTimeMillis()}",
                            type = ButtonType.CALL,
                            title = context.getString(R.string.btn_call_custom),
                            phoneNumber = "",
                            colorHex = 0xFF059669
                        )
                        val updated = (buttons + newBtn).toMutableList()
                        buttons = updated
                        onSaveSettings(currentSettings.copy(buttons = updated))
                    }
                },
                onAddBrowserChoice = {
                    showAddChoiceDialog = false
                    if (buttons.size < 8) {
                        val newBtn = LauncherButton(
                            id = "browser_${System.currentTimeMillis()}",
                            type = ButtonType.BROWSER,
                            title = "Surfen",
                            colorHex = 0xFF0284C7
                        )
                        val updated = (buttons + newBtn).toMutableList()
                        buttons = updated
                        onSaveSettings(currentSettings.copy(buttons = updated))
                    }
                },
                onAddSmsChoice = {
                    showAddChoiceDialog = false
                    if (buttons.size < 8) {
                        val newBtn = LauncherButton(
                            id = "sms_${System.currentTimeMillis()}",
                            type = ButtonType.SMS,
                            title = "Nachrichten",
                            colorHex = 0xFF6366F1
                        )
                        val updated = (buttons + newBtn).toMutableList()
                        buttons = updated
                        onSaveSettings(currentSettings.copy(buttons = updated))
                    }
                },
                onDismiss = { showAddChoiceDialog = false }
            )
        }

        // App Picker Dialog
        if (showAppPickerDialog) {
            AppPickerDialog(
                onAppSelected = { appInfo ->
                    showAppPickerDialog = false
                    if (buttons.size < 8) {
                        val newBtn = LauncherButton(
                            id = "app_${appInfo.packageName}_${System.currentTimeMillis()}",
                            type = ButtonType.APP,
                            title = appInfo.label,
                            packageName = appInfo.packageName,
                            colorHex = 0xFF4F46E5
                        )
                        val updated = (buttons + newBtn).toMutableList()
                        buttons = updated
                        onSaveSettings(currentSettings.copy(buttons = updated))
                    }
                },
                onDismiss = { showAppPickerDialog = false }
            )
        }
    }
}
