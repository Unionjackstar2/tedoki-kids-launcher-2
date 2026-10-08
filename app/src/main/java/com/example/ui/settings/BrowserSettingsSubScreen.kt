package com.example.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
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
import com.example.model.LauncherSettings
import com.example.model.WebBookmark
import com.example.ui.components.LargeBackButton
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveGreenLight
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.assistiveTextFieldColors
import java.util.UUID

/**
 * Recommended kid-friendly German websites for 1-click addition.
 */
data class RecommendedWebsite(
    val title: String,
    val description: String,
    val url: String,
    val domain: String,
    val emoji: String,
    val colorHex: Long
)

val RECOMMENDED_KIDS_SITES = listOf(
    RecommendedWebsite(
        title = "Die Sendung mit der Maus",
        description = "Lach- und Sachgeschichten, Spiele & Videos",
        url = "https://www.wdrmaus.de/",
        domain = "wdrmaus.de",
        emoji = "🐘",
        colorHex = 0xFFEA580C
    ),
    RecommendedWebsite(
        title = "FragFINN",
        description = "Sichere Kindersuchmaschine für Internet-Neulinge",
        url = "https://www.fragfinn.de/",
        domain = "fragfinn.de",
        emoji = "🔍",
        colorHex = 0xFF2563EB
    ),
    RecommendedWebsite(
        title = "KiKA – Kinderkanal",
        description = "Sendungen, Hörspiele und Basteltipps von ARD & ZDF",
        url = "https://www.kika.de/",
        domain = "kika.de",
        emoji = "📺",
        colorHex = 0xFF059669
    ),
    RecommendedWebsite(
        title = "Blinde Kuh",
        description = "Deutschlands erste Suchmaschine speziell für Kinder",
        url = "https://www.blinde-kuh.de/",
        domain = "blinde-kuh.de",
        emoji = "🐮",
        colorHex = 0xFF7C3AED
    ),
    RecommendedWebsite(
        title = "Antolin",
        description = "Leseförderung und Buch-Quizze für Grundschüler",
        url = "https://antolin.westermann.de/",
        domain = "westermann.de",
        emoji = "📖",
        colorHex = 0xFFE11D48
    ),
    RecommendedWebsite(
        title = "Seitenstark",
        description = "Arbeitsgemeinschaft vernetzter Kinderseiten",
        url = "https://seitenstark.de/",
        domain = "seitenstark.de",
        emoji = "🐧",
        colorHex = 0xFF0D9488
    ),
    RecommendedWebsite(
        title = "logo! – Kindernachrichten",
        description = "Nachrichten für Kinder verständlich erklärt",
        url = "https://www.zdf.de/kinder/logo",
        domain = "zdf.de",
        emoji = "📰",
        colorHex = 0xFF4F46E5
    )
)

@Composable
fun BrowserSettingsSubScreen(
    currentSettings: LauncherSettings,
    onSaveSettings: (LauncherSettings) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    BackHandler { onBack() }

    var allowedWebsites by remember { mutableStateOf(currentSettings.allowedWebsites) }
    var customSiteName by remember { mutableStateOf("") }
    var customSiteUrl by remember { mutableStateOf("") }

    val hasBrowserButton = currentSettings.buttons.any { it.type == ButtonType.BROWSER }

    fun cleanDomain(rawUrl: String): String {
        var trimmed = rawUrl.trim().lowercase()
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "https://$trimmed"
        }
        return try {
            val uri = Uri.parse(trimmed)
            val host = uri.host ?: trimmed
            host.removePrefix("www.")
        } catch (e: Exception) {
            trimmed
        }
    }

    fun cleanUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        return if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            "https://$trimmed"
        } else trimmed
    }

    fun saveWebsites(newList: List<WebBookmark>) {
        allowedWebsites = newList
        onSaveSettings(currentSettings.copy(allowedWebsites = newList))
    }

    fun toggleHomescreenButton(enable: Boolean) {
        val updatedButtons = currentSettings.buttons.toMutableList()
        if (enable && !hasBrowserButton) {
            updatedButtons.add(
                LauncherButton(
                    id = "browser_default",
                    type = ButtonType.BROWSER,
                    title = "Surfen",
                    colorHex = 0xFF0284C7
                )
            )
            onSaveSettings(currentSettings.copy(buttons = updatedButtons))
            Toast.makeText(context, "„Surfen“ wurde zum Homescreen hinzugefügt", Toast.LENGTH_SHORT).show()
        } else if (!enable && hasBrowserButton) {
            updatedButtons.removeAll { it.type == ButtonType.BROWSER }
            onSaveSettings(currentSettings.copy(buttons = updatedButtons))
            Toast.makeText(context, "„Surfen“ vom Homescreen entfernt", Toast.LENGTH_SHORT).show()
        }
    }

    fun addCustomWebsite() {
        val name = customSiteName.trim()
        val rawUrl = customSiteUrl.trim()
        if (name.isBlank() || rawUrl.isBlank()) {
            Toast.makeText(context, "Bitte Name und Web-Adresse eingeben", Toast.LENGTH_SHORT).show()
            return
        }

        val domain = cleanDomain(rawUrl)
        val finalUrl = cleanUrl(rawUrl)

        val newBookmark = WebBookmark(
            id = "custom_${UUID.randomUUID()}",
            title = name,
            url = finalUrl,
            domain = domain,
            iconEmoji = "🌐",
            colorHex = 0xFF0284C7
        )

        saveWebsites(allowedWebsites + newBookmark)
        customSiteName = ""
        customSiteUrl = ""
        focusManager.clearFocus()
        keyboardController?.hide()
        Toast.makeText(context, "Webseite freigeschaltet!", Toast.LENGTH_SHORT).show()
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
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = AssistiveBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.browser_settings_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(R.string.browser_settings_desc),
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
                        .testTag("browser_settings_back_btn")
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
                // Info Card explaining the Walled Garden principle
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "100 % geschützter Kinder-Browser",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Das Kind hat keinen freien Zugriff auf das offene Internet. Es können ausschließlich die hier freigeschalteten Webseiten aufgerufen werden. Externe Links werden sofort blockiert.",
                                fontSize = 14.sp,
                                color = AssistiveTextSecondary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                // Switch: Show on Home Screen
                Card(
                    shape = RoundedCornerShape(20.dp),
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
                                text = stringResource(R.string.browser_settings_toggle_homescreen),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                            Text(
                                text = stringResource(R.string.browser_settings_toggle_homescreen_desc),
                                fontSize = 13.sp,
                                color = AssistiveTextSecondary
                            )
                        }
                        Switch(
                            checked = hasBrowserButton,
                            onCheckedChange = { toggleHomescreenButton(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AssistiveBlue
                            ),
                            modifier = Modifier.testTag("toggle_browser_homescreen")
                        )
                    }
                }

                // Section 1: Currently Approved Websites
                Text(
                    text = "${stringResource(R.string.browser_settings_allowed_list)} (${allowedWebsites.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )

                if (allowedWebsites.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                        border = BorderStroke(1.dp, AssistiveBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.browser_settings_no_sites),
                            fontSize = 14.sp,
                            color = AssistiveTextMuted,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                        border = BorderStroke(1.dp, AssistiveBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            allowedWebsites.forEachIndexed { index, site ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
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
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(Color(site.colorHex).copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = site.iconEmoji, fontSize = 20.sp)
                                        }
                                        Column {
                                            Text(
                                                text = site.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AssistiveTextPrimary
                                            )
                                            Text(
                                                text = site.domain,
                                                fontSize = 13.sp,
                                                color = AssistiveTextSecondary
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            val updated = allowedWebsites.filter { it.id != site.id }
                                            saveWebsites(updated)
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Entfernen",
                                            tint = AssistiveRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (index < allowedWebsites.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFF1F5F9))
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: 1-Click Kid-Safe Recommendations
                Text(
                    text = stringResource(R.string.browser_settings_recommendations),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        RECOMMENDED_KIDS_SITES.forEachIndexed { index, rec ->
                            val alreadyAdded = allowedWebsites.any { cleanDomain(it.domain) == cleanDomain(rec.domain) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
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
                                            .background(Color(rec.colorHex).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = rec.emoji, fontSize = 22.sp)
                                    }
                                    Column {
                                        Text(
                                            text = rec.title,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AssistiveTextPrimary
                                        )
                                        Text(
                                            text = rec.description,
                                            fontSize = 12.sp,
                                            color = AssistiveTextSecondary
                                        )
                                    }
                                }

                                if (alreadyAdded) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(AssistiveGreenLight)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = AssistiveGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Aktiv",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AssistiveGreen
                                            )
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val newSite = WebBookmark(
                                                id = "rec_${UUID.randomUUID()}",
                                                title = rec.title,
                                                url = rec.url,
                                                domain = rec.domain,
                                                iconEmoji = rec.emoji,
                                                colorHex = rec.colorHex
                                            )
                                            saveWebsites(allowedWebsites + newSite)
                                            Toast.makeText(context, "${rec.title} freigeschaltet!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AssistiveBlue,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        modifier = Modifier.height(38.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Hinzufügen", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (index < RECOMMENDED_KIDS_SITES.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFF1F5F9))
                                )
                            }
                        }
                    }
                }

                // Section 3: Add Custom Website
                Text(
                    text = stringResource(R.string.browser_settings_add_custom),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = customSiteName,
                            onValueChange = { customSiteName = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = AssistiveTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            ),
                            label = { Text(stringResource(R.string.browser_settings_site_name)) },
                            placeholder = { Text("z. B. Grundschule", color = AssistiveTextMuted) },
                            colors = assistiveTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_custom_site_name"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = customSiteUrl,
                            onValueChange = { customSiteUrl = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = AssistiveTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            ),
                            label = { Text(stringResource(R.string.browser_settings_site_url)) },
                            placeholder = { Text("z. B. meine-schule.de", color = AssistiveTextMuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            colors = assistiveTextFieldColors(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_custom_site_url"),
                            singleLine = true
                        )

                        Button(
                            onClick = { addCustomWebsite() },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AssistiveBlue,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_add_custom_site")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.browser_settings_add_btn),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                LargeBackButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}
