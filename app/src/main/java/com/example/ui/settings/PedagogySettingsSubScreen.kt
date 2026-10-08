package com.example.ui.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.quiz.PedagogyPreferences
import com.example.model.ButtonType
import com.example.model.LauncherSettings
import com.example.model.quiz.ReviewDueState
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.LocalIsDarkTheme

/**
 * Dedicated clean sub-menu for Medienpädagogik & App-Freischaltungen (30 / 90 Tage Prüfzyklen).
 */
@Composable
fun PedagogySettingsSubScreen(
    currentSettings: LauncherSettings,
    pedagogyPreferences: PedagogyPreferences,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val scrollState = rememberScrollState()

    val pedStatuses by pedagogyPreferences.statusMapFlow.collectAsState()

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
                        text = "Medienpädagogik",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AssistiveTextPrimary
                    )
                    Text(
                        text = "App-Freischaltungen & Prüfzyklen verwalten",
                        fontSize = 13.sp,
                        color = AssistiveTextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Info Banner Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF0FDF4)
                    ),
                    border = BorderStroke(1.dp, if (isDark) AssistiveBorder else Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF064E3B) else Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = AssistiveGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lern- & Sicherheitskonzept",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AssistiveTextPrimary
                            )
                            Text(
                                text = "Apps werden nach erfolgreichem Quiz freigeschaltet. Nach 30 bzw. 90 Tagen wird das Wissen spielerisch aufgefrischt.",
                                fontSize = 12.sp,
                                color = AssistiveTextSecondary
                            )
                        }
                    }
                }

                Text(
                    text = "STATUS DER HOMESCREEN-APPS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = AssistiveTextMuted,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                val distinctButtons = currentSettings.buttons.distinctBy {
                    PedagogyPreferences.resolveAppKey(it)
                }

                var quizAppCount = 0

                distinctButtons.forEach { btn ->
                    val appKey = PedagogyPreferences.resolveAppKey(btn)
                    val cat = PedagogyPreferences.resolveQuizCategory(btn)
                    if (cat != null) {
                        quizAppCount++
                        val status = pedStatuses[appKey]
                        val isUnlocked = status?.isFullyUnlocked() == true
                        val reviewDue = status?.isReviewDue() ?: ReviewDueState.NONE

                        val displayTitle = if (btn.type == ButtonType.CALL) {
                            stringResource(R.string.btn_call_custom) + " (Alle Kontakte)"
                        } else {
                            com.example.ui.components.getLocalizedButtonTitle(btn)
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                            border = BorderStroke(1.dp, AssistiveBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = displayTitle,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AssistiveTextPrimary
                                    )
                                    val statusText = when {
                                        reviewDue == ReviewDueState.DUE_30_DAYS -> "30-Tage-Wiederholung fällig"
                                        reviewDue == ReviewDueState.DUE_90_DAYS -> "90-Tage-Wiederholung fällig"
                                        isUnlocked -> "Freigeschaltet ✓"
                                        else -> "Gesperrt (Quiz erforderlich)"
                                    }
                                    Text(
                                        text = statusText,
                                        fontSize = 12.sp,
                                        color = if (isUnlocked && reviewDue == ReviewDueState.NONE) AssistiveGreen else AssistiveRed
                                    )
                                }

                                Button(
                                    onClick = {
                                        pedagogyPreferences.resetAppPedagogy(appKey, displayTitle, cat)
                                        Toast.makeText(
                                            context,
                                            "$displayTitle gesperrt. Quiz muss erneut absolviert werden.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("reset_quiz_${btn.id}")
                                ) {
                                    Text(
                                        text = "Zurücksetzen",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }
                }

                if (quizAppCount == 0) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                        border = BorderStroke(1.dp, AssistiveBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Derzeit sind keine apps mit Quiz-Freischaltung auf dem Homescreen platziert.",
                            fontSize = 13.sp,
                            color = AssistiveTextSecondary,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
