package com.example.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.LauncherSettings
import com.example.model.LayoutStyle
import com.example.model.ThemeMode
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveGreenLight
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveRedLight
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.assistiveTextFieldColors
import com.example.util.InstalledAppIcon

@Composable
fun ParentSettingsScreen(
    currentSettings: LauncherSettings,
    pedagogyPreferences: com.example.data.quiz.PedagogyPreferences? = null,
    onSaveSettings: (LauncherSettings) -> Unit,
    onReturnToHome: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onResetLauncher: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val isDark = LocalIsDarkTheme.current

    val pedPrefs = remember(pedagogyPreferences) {
        pedagogyPreferences ?: com.example.data.quiz.PedagogyPreferences(context)
    }

    val safeReturnToHome = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onReturnToHome()
    }

    BackHandler { safeReturnToHome() }

    var layoutStyle by remember { mutableStateOf(currentSettings.layoutStyle) }
    var buttons by remember { mutableStateOf(currentSettings.buttons.toMutableList()) }
    var emergencyNumber by remember { mutableStateOf(currentSettings.emergencyNumber) }
    var emergencyContactName by remember { mutableStateOf(currentSettings.emergencyContactName) }
    var pin by remember { mutableStateOf(currentSettings.pin) }
    var currentLanguage by remember { mutableStateOf(currentSettings.language) }
    var themeMode by remember(currentSettings.themeMode) { mutableStateOf(currentSettings.themeMode) }

    var showAppsSubScreen by remember { mutableStateOf(false) }
    var showEmergencySubScreen by remember { mutableStateOf(false) }
    var showBrowserSubScreen by remember { mutableStateOf(false) }
    var showContactsSubScreen by remember { mutableStateOf(false) }
    var showPinSubScreen by remember { mutableStateOf(false) }
    var showLockscreenSubScreen by remember { mutableStateOf(false) }
    var showResetSubScreen by remember { mutableStateOf(false) }
    var showSystemSubScreen by remember { mutableStateOf(false) }
    var showPedagogySubScreen by remember { mutableStateOf(false) }

    var childName by remember(currentSettings.childName) { mutableStateOf(currentSettings.childName) }
    var childAvatar by remember(currentSettings.childAvatar) { mutableStateOf(currentSettings.childAvatar) }
    var showAvatarModal by remember { mutableStateOf(false) }

    val childAvatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val permanentUri = com.example.util.ImageStorageUtils.saveImageToInternalStorage(context, uri, "child_avatar") ?: uri.toString()
            childAvatar = permanentUri
            onSaveSettings(currentSettings.copy(childAvatar = permanentUri))
        }
    }

    if (showPinSubScreen) {
        PinSettingsSubScreen(
            currentSettings = currentSettings,
            onSaveSettings = { updated ->
                pin = updated.pin
                onSaveSettings(updated)
            },
            onBack = { showPinSubScreen = false }
        )
        return
    }

    if (showAppsSubScreen) {
        AppsSettingsSubScreen(
            currentSettings = currentSettings,
            onSaveSettings = { updated ->
                buttons = updated.buttons.toMutableList()
                onSaveSettings(updated)
            },
            onBack = { showAppsSubScreen = false }
        )
        return
    }

    if (showEmergencySubScreen) {
        EmergencySettingsSubScreen(
            currentSettings = currentSettings,
            onSaveSettings = onSaveSettings,
            onBack = { showEmergencySubScreen = false }
        )
        return
    }

    if (showBrowserSubScreen) {
        BrowserSettingsSubScreen(
            currentSettings = currentSettings,
            onSaveSettings = onSaveSettings,
            onBack = { showBrowserSubScreen = false }
        )
        return
    }

    if (showContactsSubScreen) {
        ContactsSettingsSubScreen(
            currentSettings = currentSettings,
            onSaveSettings = onSaveSettings,
            onBack = { showContactsSubScreen = false }
        )
        return
    }

    if (showLockscreenSubScreen) {
        LockscreenSettingsSubScreen(
            currentSettings = currentSettings,
            onSaveSettings = onSaveSettings,
            onBack = { showLockscreenSubScreen = false }
        )
        return
    }

    if (showResetSubScreen) {
        ResetLauncherSubScreen(
            currentSettings = currentSettings,
            onResetLauncher = {
                showResetSubScreen = false
                if (onResetLauncher != null) {
                    onResetLauncher()
                } else {
                    pedPrefs.resetAllPedagogy()
                    onSaveSettings(LauncherSettings(isSetupComplete = false))
                    onReturnToHome()
                }
            },
            onBack = { showResetSubScreen = false }
        )
        return
    }

    if (showSystemSubScreen) {
        SystemSettingsSubScreen(
            currentSettings = currentSettings,
            onSaveSettings = onSaveSettings,
            onBack = { showSystemSubScreen = false }
        )
        return
    }

    if (showPedagogySubScreen) {
        PedagogySettingsSubScreen(
            currentSettings = currentSettings,
            pedagogyPreferences = pedPrefs,
            onBack = { showPedagogySubScreen = false }
        )
        return
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
            // Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AssistiveSurface)
                            .border(1.dp, AssistiveBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = AssistiveBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Compact Pill Language Selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AssistiveSurface)
                        .border(1.dp, AssistiveBorder, RoundedCornerShape(20.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (currentLanguage == "de") AssistiveBlue else Color.Transparent)
                            .clickable {
                                currentLanguage = "de"
                                onLanguageChange("de")
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("lang_toggle_de"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentLanguage == "de") Color.White else AssistiveTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (currentLanguage == "en") AssistiveBlue else Color.Transparent)
                            .clickable {
                                currentLanguage = "en"
                                onLanguageChange("en")
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("lang_toggle_en"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "EN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentLanguage == "en") Color.White else AssistiveTextSecondary
                        )
                    }
                }
            }

            // Scrollable Settings Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Child Profile
                SettingsSectionLabel(title = "Profil des Kindes")

                val avatarPreset = com.example.ui.setup.PRESET_AVATARS.find { it.id == childAvatar }
                val isCustomPhoto = childAvatar.startsWith("content://") || childAvatar.startsWith("file://") || childAvatar.startsWith("/")

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth().testTag("child_profile_settings_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCustomPhoto) Color.Transparent
                                        else Color(avatarPreset?.bgHex ?: 0xFF3B82F6)
                                    )
                                    .border(2.dp, AssistiveBlue, CircleShape)
                                    .clickable {
                                        showAvatarModal = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCustomPhoto) {
                                    AsyncImage(
                                        model = childAvatar,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                } else {
                                    Text(text = avatarPreset?.emoji ?: "🦁", fontSize = 30.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = childName.ifBlank { "Kind" },
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = if (isCustomPhoto) "Eigenes Profilfoto hinterlegt ✓" else (avatarPreset?.name ?: "Avatar"),
                                    fontSize = 13.sp,
                                    color = AssistiveTextSecondary
                                )
                            }

                            Button(
                                onClick = {
                                    showAvatarModal = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF),
                                    contentColor = AssistiveBlue
                                ),
                                border = BorderStroke(1.dp, AssistiveBlue),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Foto ändern", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (showAvatarModal) {
                    Dialog(onDismissRequest = { showAvatarModal = false }) {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                            border = BorderStroke(1.dp, AssistiveBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Profilbild auswählen",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )

                                // Presets Grid
                                val columns = 4
                                val presets = com.example.ui.setup.PRESET_AVATARS
                                val rows = (presets.size + columns - 1) / columns

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    for (r in 0 until rows) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            for (c in 0 until columns) {
                                                val index = r * columns + c
                                                if (index < presets.size) {
                                                    val preset = presets[index]
                                                    val isSelected = childAvatar == preset.id
                                                    Box(
                                                        modifier = Modifier
                                                            .size(60.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(preset.bgHex))
                                                            .border(
                                                                width = if (isSelected) 3.dp else 1.dp,
                                                                color = if (isSelected) AssistiveBlue else Color.Transparent,
                                                                shape = CircleShape
                                                            )
                                                            .clickable {
                                                                childAvatar = preset.id
                                                                onSaveSettings(currentSettings.copy(childAvatar = preset.id))
                                                                showAvatarModal = false
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = preset.emoji, fontSize = 32.sp)
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.size(60.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(AssistiveBorder)
                                )

                                // Upload own photo option
                                Button(
                                    onClick = {
                                        showAvatarModal = false
                                        childAvatarPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AssistiveBlue,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Foto hochladen"
                                        )
                                        Text("Eigenes Foto hochladen", fontWeight = FontWeight.Bold)
                                    }
                                }

                                TextButton(
                                    onClick = { showAvatarModal = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Abbrechen", color = AssistiveTextSecondary, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Section: Homescreen & Layout
                SettingsSectionLabel(title = "Startbildschirm & Tasten")

                // Appearance & Theme Mode Selector Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth().testTag("theme_mode_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AssistiveBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = AssistiveBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.settings_appearance_title),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = stringResource(R.string.settings_appearance_desc),
                                    fontSize = 13.sp,
                                    color = AssistiveTextMuted
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeTogglePill(
                                title = stringResource(R.string.theme_mode_system),
                                isSelected = themeMode == ThemeMode.SYSTEM,
                                icon = Icons.Default.BrightnessAuto,
                                modifier = Modifier.weight(1f).testTag("theme_btn_system"),
                                onClick = {
                                    themeMode = ThemeMode.SYSTEM
                                    onSaveSettings(currentSettings.copy(themeMode = ThemeMode.SYSTEM))
                                }
                            )

                            ThemeTogglePill(
                                title = stringResource(R.string.theme_mode_light),
                                isSelected = themeMode == ThemeMode.LIGHT,
                                icon = Icons.Default.LightMode,
                                modifier = Modifier.weight(1f).testTag("theme_btn_light"),
                                onClick = {
                                    themeMode = ThemeMode.LIGHT
                                    onSaveSettings(currentSettings.copy(themeMode = ThemeMode.LIGHT))
                                }
                            )

                            ThemeTogglePill(
                                title = stringResource(R.string.theme_mode_dark),
                                isSelected = themeMode == ThemeMode.DARK,
                                icon = Icons.Default.DarkMode,
                                modifier = Modifier.weight(1f).testTag("theme_btn_dark"),
                                onClick = {
                                    themeMode = ThemeMode.DARK
                                    onSaveSettings(currentSettings.copy(themeMode = ThemeMode.DARK))
                                }
                            )
                        }
                    }
                }

                // Layout Style Switcher
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
                        Text(
                            text = stringResource(R.string.layout_style_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LayoutTogglePill(
                                title = stringResource(R.string.layout_grid),
                                isSelected = layoutStyle == LayoutStyle.GRID,
                                icon = Icons.Default.GridView,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    layoutStyle = LayoutStyle.GRID
                                    onSaveSettings(currentSettings.copy(layoutStyle = LayoutStyle.GRID))
                                }
                            )

                            LayoutTogglePill(
                                title = stringResource(R.string.layout_rows),
                                isSelected = layoutStyle == LayoutStyle.ROWS,
                                icon = Icons.Default.TableRows,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    layoutStyle = LayoutStyle.ROWS
                                    onSaveSettings(currentSettings.copy(layoutStyle = LayoutStyle.ROWS))
                                }
                            )
                        }
                    }
                }

                // Dedicated Apps & Tasten Management Submenu Entry Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAppsSubScreen = true }
                        .testTag("open_apps_submenu")
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

                            Column {
                                Text(
                                    text = "Apps & Tasten auswählen",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = "${buttons.size} von 8 Tasten auf dem Homescreen aktiv",
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

                // Section: Communication & Emergency
                SettingsSectionLabel(title = "Kommunikation & Notfall")

                // Dedicated Contacts Management Submenu Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showContactsSubScreen = true }
                        .testTag("open_contacts_submenu")
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
                                    .background(if (isDark) Color(0xFF0469A1).copy(alpha = 0.35f) else Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = AssistiveBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Kontakte verwalten",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = "${currentSettings.contacts.size} Kontakte hinterlegt (unabhängig von Tastenanzahl)",
                                    fontSize = 13.sp,
                                    color = AssistiveTextSecondary
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

                // Dedicated SOS Emergency Submenu Entry Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEmergencySubScreen = true }
                        .testTag("open_emergency_submenu")
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
                                    .background(
                                        if (currentSettings.showSosButton) {
                                            if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.4f) else Color(0xFFFEE2E2)
                                        } else {
                                            if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (currentSettings.showSosButton) Color(0xFFDC2626) else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = stringResource(R.string.emergency_submenu_title),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = if (currentSettings.showSosButton) {
                                        "${currentSettings.emergencyNumber} • ${currentSettings.emergencyContactName}"
                                    } else {
                                        stringResource(R.string.settings_sos_toggle_desc)
                                    },
                                    fontSize = 13.sp,
                                    color = AssistiveTextSecondary
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

                // Dedicated Kid Browser Submenu Entry Card ("Surfen")
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showBrowserSubScreen = true }
                        .testTag("open_browser_submenu")
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
                                    .background(if (isDark) Color(0xFF0369A1).copy(alpha = 0.35f) else Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🌐", fontSize = 22.sp)
                            }

                            Column {
                                Text(
                                    text = stringResource(R.string.browser_settings_title),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = "${currentSettings.allowedWebsites.size} freigegebene Webseiten • Walled Garden",
                                    fontSize = 13.sp,
                                    color = AssistiveTextSecondary
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

                // Section: Security & Child Protection
                SettingsSectionLabel(title = "Sicherheit & Kinderschutz")

                // Dedicated Parent PIN & Security Submenu Entry Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPinSubScreen = true }
                        .testTag("open_pin_submenu")
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
                                    .background(if (isDark) Color(0xFF78350F).copy(alpha = 0.4f) else Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = stringResource(R.string.settings_pin_submenu_title),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = "PIN ändern • Verschlüsselt mit Auge • Schutz vor Änderungen",
                                    fontSize = 13.sp,
                                    color = AssistiveTextSecondary
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

                // Dedicated Kinder-Sperrbildschirm (Lockscreen) Submenu Entry Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLockscreenSubScreen = true }
                        .testTag("open_lockscreen_submenu")
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
                                    .background(
                                        if (currentSettings.enableLockscreen) {
                                            if (isDark) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFFD1FAE5)
                                        } else {
                                            if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (currentSettings.enableLockscreen) AssistiveGreen else AssistiveTextSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Sperrbildschirm (Lockscreen)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = if (currentSettings.enableLockscreen) {
                                        val clockType = if (currentSettings.lockscreenClockStyle == com.example.model.LockscreenClockStyle.ANALOG_CONNI) "Lernuhr" else "Digitale Uhr"
                                        val mathInfo = if (currentSettings.lockscreenRequireMathProblem) " • Mit Rechenaufgabe" else ""
                                        "Aktiviert • $clockType$mathInfo"
                                    } else {
                                        "Deaktiviert • Tippen zum Konfigurieren"
                                    },
                                    fontSize = 13.sp,
                                    color = if (currentSettings.enableLockscreen) AssistiveGreen else AssistiveTextSecondary
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

                // Dedicated Pedagogy & App Unlocks Submenu Entry Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPedagogySubScreen = true }
                        .testTag("open_pedagogy_submenu")
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
                                    .background(if (isDark) Color(0xFF3B0764).copy(alpha = 0.4f) else Color(0xFFF3E8FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFF9333EA),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Medienpädagogik & App-Freischaltungen",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = "Prüfzyklen (30 / 90 Tage) • Freigaben verwalten • Quiz zurücksetzen",
                                    fontSize = 13.sp,
                                    color = AssistiveTextSecondary
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

                // Section: System & Device Administration
                SettingsSectionLabel(title = "System & Geräteverwaltung")

                // Dedicated System & Default Apps Submenu Entry Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSystemSubScreen = true }
                        .testTag("open_system_submenu")
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
                                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = AssistiveBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "System & Standard-Apps",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = "Standard-Launcher • Anrufe & SMS • Uhr/Kalender • Tastenfeld",
                                    fontSize = 13.sp,
                                    color = AssistiveTextSecondary
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

                // Dedicated Reset Submenu Entry Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showResetSubScreen = true }
                        .testTag("open_reset_submenu")
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
                                    .background(if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.4f) else AssistiveRedLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AssistiveRed,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Launcher zurücksetzen",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AssistiveTextPrimary
                                )
                                Text(
                                    text = "Vollständiger Reset mit Eltern-PIN • Neustart Ersteinrichtung",
                                    fontSize = 13.sp,
                                    color = AssistiveTextSecondary
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

                Spacer(modifier = Modifier.height(14.dp))

                // Return to Simplified Mode button (inside scrollable content at the bottom)
                LargeBackButton(
                    onClick = safeReturnToHome,
                    text = stringResource(R.string.settings_exit_to_home),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ThemeTogglePill(
    title: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                2.dp,
                if (isSelected) AssistiveBlue else AssistiveBorder,
                RoundedCornerShape(16.dp)
            )
            .background(
                if (isSelected) {
                    if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFFEFF6FF)
                } else {
                    if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) AssistiveBlue else AssistiveTextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AssistiveBlue else AssistiveTextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LayoutTogglePill(
    title: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                2.dp,
                if (isSelected) AssistiveBlue else AssistiveBorder,
                RoundedCornerShape(18.dp)
            )
            .background(
                if (isSelected) {
                    if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.5f) else Color(0xFFEFF6FF)
                } else {
                    if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) AssistiveBlue else AssistiveTextSecondary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AssistiveBlue else AssistiveTextPrimary
            )
        }
    }
}

@Composable
private fun SettingsSectionLabel(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = Color(0xFF64748B),
        modifier = modifier.padding(start = 4.dp, top = 6.dp)
    )
}
