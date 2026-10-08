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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherSettings
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveRedLight
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.assistiveTextFieldColors

/**
 * Dedicated Submenu to completely reset the Launcher:
 * - Clear explanation of consequences
 * - Requires entering the parent PIN to confirm
 * - Resets all settings, pedagogical statuses, and redirects to initial onboarding
 */
@Composable
fun ResetLauncherSubScreen(
    currentSettings: LauncherSettings,
    onResetLauncher: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val isDark = LocalIsDarkTheme.current

    BackHandler { onBack() }

    var pinInput by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleConfirmReset() {
        errorMessage = null
        if (pinInput != currentSettings.pin) {
            errorMessage = "Falscher Eltern-PIN! Bitte überprüfe deine Eingabe."
            return
        }

        focusManager.clearFocus()
        keyboardController?.hide()
        Toast.makeText(context, "Launcher wurde zurückgesetzt. Willkommen zur Ersteinrichtung!", Toast.LENGTH_LONG).show()
        onResetLauncher()
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
                            .background(if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.4f) else AssistiveRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AssistiveRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Launcher zurücksetzen",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Vollständiger Reset & Ersteinrichtung",
                            fontSize = 13.sp,
                            color = AssistiveTextSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp)
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
                // Warning / Consequence Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF450A0A) else AssistiveRedLight
                    ),
                    border = BorderStroke(1.5.dp, AssistiveRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AssistiveRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Achtung: Vollständiger Reset",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Wenn du den Launcher zurücksetzt, werden alle Daten unwiderruflich gelöscht:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF7F1D1D)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "• Name, Alter und Avatar des Kindes\n• Alle Kurzwahl-Kontakte und Telefonnummern\n• Alle bisherigen Quiz-Ergebnisse & Freischaltungen\n• Individuelle Layout- und Sicherheits-Einstellungen",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFFFCA5A5).copy(alpha = 0.9f) else Color(0xFF991B1B),
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Der Launcher startet danach wieder von vorne mit der Schritt-für-Schritt-Ersteinrichtung.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF7F1D1D)
                        )
                    }
                }

                // PIN Confirmation Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AssistiveTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Mit Eltern-PIN bestätigen",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Bitte gib deinen 4-stelligen Eltern-PIN ein, um das Zurücksetzen zu autorisieren.",
                            fontSize = 13.sp,
                            color = AssistiveTextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                    pinInput = it
                                    errorMessage = null
                                }
                            },
                            placeholder = { Text("Eltern-PIN eingeben") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = assistiveTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reset_pin_input_field"),
                            trailingIcon = {
                                IconButton(onClick = { pinVisible = !pinVisible }) {
                                    Icon(
                                        imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "PIN anzeigen"
                                    )
                                }
                            }
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = AssistiveRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("reset_pin_error_text")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Button(
                    onClick = { handleConfirmReset() },
                    enabled = pinInput.length == 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AssistiveRed,
                        disabledContainerColor = AssistiveBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("confirm_reset_with_pin_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Launcher jetzt zurücksetzen",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AssistiveSurface
                    ),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("cancel_reset_button")
                ) {
                    Text(
                        text = "Abbrechen",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AssistiveTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
