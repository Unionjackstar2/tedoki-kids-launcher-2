package com.example.ui.settings

import android.net.Uri
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.LauncherSettings
import com.example.model.LockscreenClockStyle
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.util.LockscreenSecurityHelper
import java.io.File
import java.io.FileOutputStream

data class PresetLockscreenBackground(
    val id: String,
    val title: String,
    val previewGradient: Brush
)

/**
 * Dedicated Submenu for Lockscreen Settings in Parent Dashboard.
 * Parents can:
 * - Toggle Lockscreen On/Off (Gated if Android system lock screen is active)
 * - Clock Style: Conni-style Learning Clock vs. Digital Clock
 * - Background selection: Presets or Custom photo
 * - Optional: Math puzzle toggle before accessing home screen
 */
@Composable
fun LockscreenSettingsSubScreen(
    currentSettings: LauncherSettings,
    onSaveSettings: (LauncherSettings) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    BackHandler { onBack() }

    var isSystemLockscreenActive by remember {
        mutableStateOf(LockscreenSecurityHelper.isAndroidSystemLockscreenEnabled(context))
    }

    LaunchedEffect(Unit) {
        isSystemLockscreenActive = LockscreenSecurityHelper.isAndroidSystemLockscreenEnabled(context)
    }

    var isLockscreenEnabled by remember { mutableStateOf(currentSettings.enableLockscreen) }
    var selectedClockStyle by remember { mutableStateOf(currentSettings.lockscreenClockStyle) }
    var selectedPresetBg by remember { mutableStateOf(currentSettings.lockscreenPresetBackground) }
    var customBgUri by remember { mutableStateOf(currentSettings.lockscreenCustomBackgroundUri) }
    var requireMathProblem by remember { mutableStateOf(currentSettings.lockscreenRequireMathProblem) }

    val presetBackgrounds = remember {
        listOf(
            PresetLockscreenBackground(
                id = "soft_blue",
                title = "Himmelblau",
                previewGradient = Brush.verticalGradient(listOf(Color(0xFFE0F2FE), Color(0xFF7DD3FC)))
            ),
            PresetLockscreenBackground(
                id = "pastel_green",
                title = "Frühlingswiese",
                previewGradient = Brush.verticalGradient(listOf(Color(0xFFD1FAE5), Color(0xFF6EE7B7)))
            ),
            PresetLockscreenBackground(
                id = "sunny_yellow",
                title = "Sonnengelb",
                previewGradient = Brush.verticalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFBBF24)))
            ),
            PresetLockscreenBackground(
                id = "warm_orange",
                title = "Abendrot",
                previewGradient = Brush.verticalGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFB923C)))
            ),
            PresetLockscreenBackground(
                id = "gentle_purple",
                title = "Fliederzauber",
                previewGradient = Brush.verticalGradient(listOf(Color(0xFFF3E8FF), Color(0xFFC084FC)))
            ),
            PresetLockscreenBackground(
                id = "starry_night",
                title = "Sternennacht",
                previewGradient = Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF312E81)))
            )
        )
    }

    // Photo picker for custom lockscreen background
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val targetFile = File(context.filesDir, "lockscreen_wallpaper_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(targetFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                val localUriString = Uri.fromFile(targetFile).toString()
                customBgUri = localUriString
                onSaveSettings(
                    currentSettings.copy(
                        lockscreenCustomBackgroundUri = localUriString
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateSettings(
        enabled: Boolean = isLockscreenEnabled,
        clockStyle: LockscreenClockStyle = selectedClockStyle,
        presetBg: String = selectedPresetBg,
        customBg: String? = customBgUri,
        mathReq: Boolean = requireMathProblem
    ) {
        isLockscreenEnabled = enabled
        selectedClockStyle = clockStyle
        selectedPresetBg = presetBg
        customBgUri = customBg
        requireMathProblem = mathReq

        onSaveSettings(
            currentSettings.copy(
                enableLockscreen = enabled,
                lockscreenClockStyle = clockStyle,
                lockscreenPresetBackground = presetBg,
                lockscreenCustomBackgroundUri = customBg,
                lockscreenRequireMathProblem = mathReq
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
                .padding(20.dp)
        ) {
            // Header: Back button + Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AssistiveSurface)
                        .testTag("lockscreen_settings_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        tint = AssistiveTextPrimary
                    )
                }

                Column {
                    Text(
                        text = "Sperrbildschirm (Lockscreen)",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )
                    Text(
                        text = "Kindersperre mit Lernuhr & Zieh-Entsperrung",
                        fontSize = 13.sp,
                        color = AssistiveTextSecondary
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
                // Card 1: Main Toggle & Android Keyguard Check
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
                                        .background(if (isLockscreenEnabled) AssistiveGreen.copy(alpha = 0.15f) else AssistiveBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isLockscreenEnabled) AssistiveGreen else AssistiveBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Kinder-Sperrbildschirm aktivieren",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AssistiveTextPrimary
                                    )
                                    Text(
                                        text = if (isLockscreenEnabled) "Aktiviert bei Display-Aus" else "Deaktiviert",
                                        fontSize = 12.sp,
                                        color = if (isLockscreenEnabled) AssistiveGreen else AssistiveTextSecondary
                                    )
                                }
                            }

                            Switch(
                                checked = isLockscreenEnabled,
                                enabled = !isSystemLockscreenActive,
                                onCheckedChange = { checked ->
                                    if (isSystemLockscreenActive && checked) {
                                        // Gated: can only be activated if system lockscreen is disabled
                                        return@Switch
                                    }
                                    updateSettings(enabled = checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AssistiveGreen
                                ),
                                modifier = Modifier.testTag("toggle_lockscreen_enabled")
                            )
                        }

                        // Warning banner if Android system lockscreen is active
                        if (isSystemLockscreenActive) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFEF3C7))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Android-Bildschirmsperre noch aktiv",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    }

                                    Text(
                                        text = "Der Kinder-Lockscreen kann nur genutzt werden, wenn die Bildschirmsperre in den Android-Systemeinstellungen deaktiviert ist (auf 'Keine' oder 'Wischen' gesetzt).",
                                        fontSize = 12.sp,
                                        color = Color(0xFF78350F)
                                    )

                                    Button(
                                        onClick = { LockscreenSecurityHelper.openAndroidSecuritySettings(context) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Android-Sicherheitseinstellungen öffnen", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Card 2: Clock Style Selection (Conni Learning Clock vs Digital Clock)
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
                            text = "Uhren-Darstellung",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Learning Clock option
                            val isLearningClock = (selectedClockStyle == LockscreenClockStyle.ANALOG_CONNI)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isLearningClock) AssistiveBlue.copy(alpha = 0.08f) else AssistiveSurface
                                ),
                                border = BorderStroke(
                                    if (isLearningClock) 2.dp else 1.dp,
                                    if (isLearningClock) AssistiveBlue else AssistiveBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { updateSettings(clockStyle = LockscreenClockStyle.ANALOG_CONNI) }
                                    .testTag("clock_style_conni")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "⏰", fontSize = 32.sp)
                                    Text(
                                        text = "Lernuhr",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isLearningClock) AssistiveBlue else AssistiveTextPrimary
                                    )
                                    Text(
                                        text = "Analog mit 24h & Zeigern",
                                        fontSize = 11.sp,
                                        color = AssistiveTextSecondary
                                    )
                                }
                            }

                            // Digital Clock option
                            val isDigital = (selectedClockStyle == LockscreenClockStyle.DIGITAL)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDigital) AssistiveBlue.copy(alpha = 0.08f) else AssistiveSurface
                                ),
                                border = BorderStroke(
                                    if (isDigital) 2.dp else 1.dp,
                                    if (isDigital) AssistiveBlue else AssistiveBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { updateSettings(clockStyle = LockscreenClockStyle.DIGITAL) }
                                    .testTag("clock_style_digital")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = "12:00", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = AssistiveBlue)
                                    Text(
                                        text = "Digitale Uhr",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isDigital) AssistiveBlue else AssistiveTextPrimary
                                    )
                                    Text(
                                        text = "Große Ziffern & Datum",
                                        fontSize = 11.sp,
                                        color = AssistiveTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Card 3: Background Selection
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
                            text = "Hintergrundbild",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )

                        Text(
                            text = "Wähle aus vorgefertigten Farben oder lade ein eigenes Foto hoch:",
                            fontSize = 12.sp,
                            color = AssistiveTextSecondary
                        )

                        // Preset Backgrounds Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            presetBackgrounds.take(3).forEach { preset ->
                                val isSelected = (customBgUri == null && selectedPresetBg == preset.id)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(70.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(preset.previewGradient)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) AssistiveBlue else Color.White.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            updateSettings(presetBg = preset.id, customBg = null)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(AssistiveBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            presetBackgrounds.drop(3).take(3).forEach { preset ->
                                val isSelected = (customBgUri == null && selectedPresetBg == preset.id)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(70.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(preset.previewGradient)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) AssistiveBlue else Color.White.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            updateSettings(presetBg = preset.id, customBg = null)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(AssistiveBlue),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Custom Wallpaper Picker Button
                        Button(
                            onClick = {
                                wallpaperPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                                Text(
                                    text = if (customBgUri != null) "Eigenes Foto ändern" else "Eigenes Foto hochladen",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (customBgUri != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "✓ Eigenes Bild aktiv", fontSize = 12.sp, color = AssistiveGreen, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Entfernen",
                                    fontSize = 12.sp,
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        updateSettings(customBg = null)
                                    }
                                )
                            }
                        }
                    }
                }

                // Card 4: Optional Math Challenge upon Pulling Lock
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
                                text = "Kleine Plus-Aufgabe beim Entsperren",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                            Text(
                                text = "Nach dem Ziehen des Schloss-Buttons müssen Kinder eine sehr einfache Plus-Aufgabe lösen (z.B. 2 + 3), bevor der Startbildschirm erscheint.",
                                fontSize = 12.sp,
                                color = AssistiveTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = requireMathProblem,
                            onCheckedChange = { checked ->
                                updateSettings(mathReq = checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AssistiveGreen
                            ),
                            modifier = Modifier.testTag("toggle_math_challenge")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large full-width Back button at the bottom
            LargeBackButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lockscreen_settings_bottom_back")
            )
        }
    }
}
