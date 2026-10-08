package com.example.ui.setup

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.LauncherSettings
import com.example.model.LayoutStyle
import kotlinx.coroutines.delay
import com.example.ui.theme.AssistiveAmber
import com.example.ui.theme.AssistiveAmberLight
import com.example.ui.theme.AssistiveBackground
import com.example.ui.theme.AssistiveBlue
import com.example.ui.theme.AssistiveBlueLight
import com.example.ui.theme.AssistiveBorder
import com.example.ui.theme.AssistiveGreen
import com.example.ui.theme.AssistiveGreenLight
import com.example.ui.theme.AssistivePurple
import com.example.ui.theme.AssistivePurpleLight
import com.example.ui.theme.AssistiveRed
import com.example.ui.theme.AssistiveRedLight
import com.example.ui.theme.AssistiveSurface
import com.example.ui.theme.AssistiveTextMuted
import com.example.ui.theme.AssistiveTextPrimary
import com.example.ui.theme.AssistiveTextSecondary
import com.example.ui.theme.assistiveTextFieldColors

/**
 * 10-step guided initial onboarding wizard for parents:
 * 1. Welcome to TEDOKI Kids Launcher
 * 2. Pedagogical Quiz Unlock Concept
 * 3. Child profile: Name & Age (free input)
 * 4. Avatar Selection: Horizontal scrolling preset avatars or custom photo
 * 5. Initial Homescreen Features: Camera, Photos, Calling & first 2 contacts
 * 6. Dashboard Preview: Explanation of later unlocking additional features/apps with icons
 * 7. Emergency SOS setup
 * 8. Layout Style: Grid vs. Rows
 * 9. Parent PIN setup & 2x verification
 * 10. Completion & celebration
 */
enum class SetupStep {
    WELCOME,
    PEDAGOGY_CONCEPT,
    CHILD_INFO,
    AVATAR_PICKER,
    FEATURE_SELECTION,
    INITIAL_CONTACTS,
    DASHBOARD_INFO,
    EMERGENCY_SETUP,
    LAYOUT_STYLE,
    PARENT_PIN,
    COMPLETION
}

data class ChildAvatarPreset(
    val id: String,
    val name: String,
    val emoji: String,
    val bgHex: Long
)

val PRESET_AVATARS = listOf(
    ChildAvatarPreset("lion", "Leo", "🦁", 0xFFF59E0B),
    ChildAvatarPreset("panda", "Bao", "🐼", 0xFF10B981),
    ChildAvatarPreset("astro", "Astro", "🚀", 0xFF3B82F6),
    ChildAvatarPreset("fox", "Felix", "🦊", 0xFFEA580C),
    ChildAvatarPreset("cat", "Mimi", "🐱", 0xFFEC4899),
    ChildAvatarPreset("robot", "Beep", "🤖", 0xFF6366F1),
    ChildAvatarPreset("dragon", "Fuchur", "🐲", 0xFF059669),
    ChildAvatarPreset("bunny", "Hops", "🐰", 0xFFF43F5E),
    ChildAvatarPreset("owl", "Luna", "🦉", 0xFF8B5CF6),
    ChildAvatarPreset("penguin", "Pico", "🐧", 0xFF0284C7),
    ChildAvatarPreset("unicorn", "Sternchen", "🦄", 0xFFD946EF),
    ChildAvatarPreset("dog", "Bello", "🐶", 0xFFD97706),
    ChildAvatarPreset("tiger", "Sammy", "🐯", 0xFFDC2626),
    ChildAvatarPreset("bear", "Bruno", "🐻", 0xFF92400E)
)

@Composable
fun ParentSetupScreen(
    currentSettings: LauncherSettings,
    onCompleteSetup: (LauncherSettings) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var currentStep by remember { mutableStateOf(SetupStep.WELCOME) }
    var currentLanguage by remember(currentSettings.language) { mutableStateOf(currentSettings.language) }
    val isEn = currentLanguage == "en"

    // Step 3 state
    var childName by remember { mutableStateOf(currentSettings.childName) }
    var childAgeText by remember { mutableStateOf(if (currentSettings.childAge > 0) currentSettings.childAge.toString() else "8") }

    // Step 4 state
    var selectedAvatarId by remember { mutableStateOf(currentSettings.childAvatar.ifEmpty { "lion" }) }
    var customPhotoUri by remember {
        mutableStateOf<String?>(
            if (currentSettings.childAvatar.startsWith("content://") ||
                currentSettings.childAvatar.startsWith("file://") ||
                currentSettings.childAvatar.startsWith("/")
            ) currentSettings.childAvatar else null
        )
    }

    // Step 5 state (Initial feature selection)
    var includeSms by remember { mutableStateOf(true) }
    var includeCalls by remember { mutableStateOf(true) }
    var includeCamera by remember { mutableStateOf(true) }
    var includePhotos by remember { mutableStateOf(true) }

    var contact1Name by remember { mutableStateOf(if (isEn) "Mom" else "Mama") }
    var contact1Number by remember { mutableStateOf("0170 1234567") }

    var contact2Name by remember { mutableStateOf(if (isEn) "Dad" else "Papa") }
    var contact2Number by remember { mutableStateOf("0171 7654321") }

    // Step 7 state (Emergency SOS)
    var sosEnabled by remember { mutableStateOf(currentSettings.showSosButton) }
    var emergencyContactName by remember {
        mutableStateOf(
            if (currentSettings.emergencyContactName == "Emergency / Notruf" || currentSettings.emergencyContactName == "Notruf / Emergency") {
                if (isEn) "Mom" else "Mama"
            } else currentSettings.emergencyContactName
        )
    }
    var emergencyNumber by remember { mutableStateOf(currentSettings.emergencyNumber.ifEmpty { "112" }) }
    var sendSosLocationSms by remember { mutableStateOf(currentSettings.sendSosLocationSms) }

    // Step 8 state (Layout)
    var layoutStyle by remember { mutableStateOf(currentSettings.layoutStyle) }

    // Step 9 state (PIN)
    var pin by remember { mutableStateOf("") }
    var pinConfirm by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }

    // Photo picker for custom avatar with permanent storage copy
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val permanentUri = com.example.util.ImageStorageUtils.saveImageToInternalStorage(context, uri, "child_avatar") ?: uri.toString()
            customPhotoUri = permanentUri
            selectedAvatarId = permanentUri
        }
    }

    // Back handling
    BackHandler(enabled = currentStep != SetupStep.WELCOME) {
        val steps = SetupStep.values()
        val currentIdx = steps.indexOf(currentStep)
        if (currentIdx > 0) {
            currentStep = steps[currentIdx - 1]
        }
    }

    val stepIndex = SetupStep.values().indexOf(currentStep)
    val totalSteps = SetupStep.values().size
    val progress = (stepIndex + 1).toFloat() / totalSteps.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AssistiveBackground)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar: Step progress, Title, and Language Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (stepIndex > 0) {
                        IconButton(
                            onClick = {
                                val steps = SetupStep.values()
                                currentStep = steps[stepIndex - 1]
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = if (isEn) "Back" else "Zurück",
                                tint = AssistiveTextPrimary
                            )
                        }
                    }
                    Text(
                        text = if (isEn) "Step ${stepIndex + 1} of $totalSteps" else "Schritt ${stepIndex + 1} von $totalSteps",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextMuted
                    )
                }

                // Language toggle pill (DE / EN)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AssistiveSurface,
                    border = BorderStroke(1.dp, AssistiveBorder),
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "DE",
                            fontSize = 12.sp,
                            fontWeight = if (!isEn) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isEn) AssistiveBlue else AssistiveTextMuted,
                            modifier = Modifier.clickable {
                                currentLanguage = "de"
                                onLanguageChange("de")
                            }
                        )
                        Text(text = "|", fontSize = 12.sp, color = AssistiveTextMuted)
                        Text(
                            text = "EN",
                            fontSize = 12.sp,
                            fontWeight = if (isEn) FontWeight.Bold else FontWeight.Normal,
                            color = if (isEn) AssistiveBlue else AssistiveTextMuted,
                            modifier = Modifier.clickable {
                                currentLanguage = "en"
                                onLanguageChange("en")
                            }
                        )
                    }
                }
            }

            // Progress Indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = AssistiveBlue,
                trackColor = AssistiveBorder
            )

            // Step Content Area (Animated)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "setup_step_transition"
                ) { step ->
                    when (step) {
                        SetupStep.WELCOME -> {
                            WelcomeStep(
                                isEn = isEn,
                                onNext = { currentStep = SetupStep.PEDAGOGY_CONCEPT }
                            )
                        }
                        SetupStep.PEDAGOGY_CONCEPT -> {
                            PedagogyConceptStep(
                                isEn = isEn,
                                onNext = { currentStep = SetupStep.CHILD_INFO }
                            )
                        }
                        SetupStep.CHILD_INFO -> {
                            ChildInfoStep(
                                isEn = isEn,
                                childName = childName,
                                onNameChange = { childName = it },
                                childAgeText = childAgeText,
                                onAgeChange = { childAgeText = it },
                                onNext = {
                                    focusManager.clearFocus()
                                    currentStep = SetupStep.AVATAR_PICKER
                                }
                            )
                        }
                        SetupStep.AVATAR_PICKER -> {
                            AvatarPickerStep(
                                isEn = isEn,
                                childName = childName.ifBlank { if (isEn) "your child" else "dein Kind" },
                                selectedAvatarId = selectedAvatarId,
                                customPhotoUri = customPhotoUri,
                                onSelectPreset = {
                                    selectedAvatarId = it.id
                                    customPhotoUri = null
                                },
                                onPickCustomPhoto = {
                                    avatarPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onNext = { currentStep = SetupStep.FEATURE_SELECTION }
                            )
                        }
                        SetupStep.FEATURE_SELECTION -> {
                            InitialFeatureSelectionStep(
                                isEn = isEn,
                                includeSms = includeSms,
                                onToggleSms = { includeSms = it },
                                includeCalls = includeCalls,
                                onToggleCalls = { includeCalls = it },
                                includeCamera = includeCamera,
                                onToggleCamera = { includeCamera = it },
                                includePhotos = includePhotos,
                                onTogglePhotos = { includePhotos = it },
                                onNext = { currentStep = SetupStep.INITIAL_CONTACTS }
                            )
                        }
                        SetupStep.INITIAL_CONTACTS -> {
                            InitialContactsSetupStep(
                                isEn = isEn,
                                contact1Name = contact1Name,
                                onContact1NameChange = { contact1Name = it },
                                contact1Number = contact1Number,
                                onContact1NumberChange = { contact1Number = it },
                                contact2Name = contact2Name,
                                onContact2NameChange = { contact2Name = it },
                                contact2Number = contact2Number,
                                onContact2NumberChange = { contact2Number = it },
                                onNext = { currentStep = SetupStep.DASHBOARD_INFO }
                            )
                        }
                        SetupStep.DASHBOARD_INFO -> {
                            DashboardInfoStep(
                                isEn = isEn,
                                onNext = { currentStep = SetupStep.EMERGENCY_SETUP }
                            )
                        }
                        SetupStep.EMERGENCY_SETUP -> {
                            EmergencySetupStep(
                                isEn = isEn,
                                sosEnabled = sosEnabled,
                                onToggleSos = { sosEnabled = it },
                                emergencyName = emergencyContactName,
                                onNameChange = { emergencyContactName = it },
                                emergencyNumber = emergencyNumber,
                                onNumberChange = { emergencyNumber = it },
                                sendSms = sendSosLocationSms,
                                onToggleSms = { sendSosLocationSms = it },
                                onNext = {
                                    focusManager.clearFocus()
                                    currentStep = SetupStep.LAYOUT_STYLE
                                }
                            )
                        }
                        SetupStep.LAYOUT_STYLE -> {
                            LayoutStyleStep(
                                isEn = isEn,
                                selectedStyle = layoutStyle,
                                onSelectStyle = { layoutStyle = it },
                                onNext = { currentStep = SetupStep.PARENT_PIN }
                            )
                        }
                        SetupStep.PARENT_PIN -> {
                            ParentPinStep(
                                isEn = isEn,
                                pin = pin,
                                onPinChange = { pin = it },
                                pinConfirm = pinConfirm,
                                onConfirmChange = { pinConfirm = it },
                                pinVisible = pinVisible,
                                onToggleVisibility = { pinVisible = !pinVisible },
                                onNext = {
                                    focusManager.clearFocus()
                                    currentStep = SetupStep.COMPLETION
                                }
                            )
                        }
                        SetupStep.COMPLETION -> {
                            CompletionStep(
                                isEn = isEn,
                                childName = childName.ifBlank { if (isEn) "Child" else "Kind" },
                                childAvatar = selectedAvatarId,
                                includeCamera = includeCamera,
                                includePhotos = includePhotos,
                                includeCalls = includeCalls,
                                sosEnabled = sosEnabled,
                                emergencyNumber = emergencyNumber,
                                layoutStyle = layoutStyle,
                                onFinish = {
                                    // Assemble initial buttons: Nachrichten, Telefonie, Kamera, Fotos
                                    val finalButtons = mutableListOf<LauncherButton>()
                                    if (includeSms) {
                                        finalButtons.add(
                                            LauncherButton(
                                                id = "sms_default",
                                                type = ButtonType.SMS,
                                                title = if (isEn) "Messages" else "Nachrichten",
                                                colorHex = 0xFF6366F1
                                            )
                                        )
                                    }
                                    if (includeCalls) {
                                        finalButtons.add(
                                            LauncherButton(
                                                id = "call_default",
                                                type = ButtonType.CALL,
                                                title = if (isEn) "Phone" else "Telefon",
                                                colorHex = 0xFF059669
                                            )
                                        )
                                    }
                                    if (includeCamera) {
                                        finalButtons.add(
                                            LauncherButton(
                                                id = "camera_default",
                                                type = ButtonType.CAMERA,
                                                title = if (isEn) "Camera" else "Kamera",
                                                colorHex = 0xFF2563EB
                                            )
                                        )
                                    }
                                    if (includePhotos) {
                                        finalButtons.add(
                                            LauncherButton(
                                                id = "photos_default",
                                                type = ButtonType.PHOTOS,
                                                title = if (isEn) "Photos" else "Fotos",
                                                colorHex = 0xFFDB2777
                                            )
                                        )
                                    }

                                    // Assemble initial contacts for Contacts submenu
                                    val finalContacts = mutableListOf<com.example.model.LauncherContact>()
                                    if (contact1Name.isNotBlank() && contact1Number.isNotBlank()) {
                                        finalContacts.add(
                                            com.example.model.LauncherContact(
                                                id = "contact_0",
                                                name = contact1Name.trim(),
                                                phoneNumber = contact1Number.trim(),
                                                colorHex = 0xFF059669
                                            )
                                        )
                                    }
                                    if (contact2Name.isNotBlank() && contact2Number.isNotBlank()) {
                                        finalContacts.add(
                                            com.example.model.LauncherContact(
                                                id = "contact_1",
                                                name = contact2Name.trim(),
                                                phoneNumber = contact2Number.trim(),
                                                colorHex = 0xFF2563EB
                                            )
                                        )
                                    }

                                    val parsedAge = childAgeText.filter { it.isDigit() }.toIntOrNull() ?: 8
                                    val finalSettings = currentSettings.copy(
                                        isSetupComplete = true,
                                        language = currentLanguage,
                                        childName = childName.trim(),
                                        childAge = parsedAge,
                                        childAvatar = selectedAvatarId,
                                        buttons = finalButtons.take(8),
                                        contacts = if (finalContacts.isNotEmpty()) finalContacts else LauncherSettings.defaultContacts(),
                                        layoutStyle = layoutStyle,
                                        pin = pin.ifBlank { "1234" },
                                        showSosButton = sosEnabled,
                                        emergencyContactName = emergencyContactName.ifBlank { if (isEn) "Emergency" else "Notruf / Emergency" },
                                        emergencyNumber = emergencyNumber.ifBlank { "112" },
                                        sendSosLocationSms = sendSosLocationSms
                                    )
                                    onCompleteSetup(finalSettings)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================================
// STEP 1: WELCOME
// =====================================================================================
@Composable
private fun WelcomeStep(
    isEn: Boolean,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Hero Brand Logo
            Image(
                painter = painterResource(id = R.drawable.img_tedoki_logo),
                contentDescription = "TEDOKI Kids Launcher Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isEn) "Welcome to TEDOKI Kids" else "Willkommen bei TEDOKI Kids",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isEn)
                    "A thoughtfully simplified launcher providing a safe, calm, and playful smartphone start for your child."
                else
                    "Ein bewusst reduzierter Launcher, mit dem Kinder ihre ersten Schritte mit dem Smartphone sicher, ruhig und spielerisch unternehmen können.",
                fontSize = 15.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3 Key highlights
            WelcomeFeatureCard(
                icon = "🛡️",
                title = if (isEn) "Protected Space" else "Geschützter Raum",
                description = if (isEn)
                    "No ad traps, no social media doomscrolling, and no uncontrolled access to the open web."
                else
                    "Keine Werbefallen, keine Social-Media-Sogwirkung und kein unkontrollierter Zugriff auf das offene Internet."
            )

            Spacer(modifier = Modifier.height(12.dp))

            WelcomeFeatureCard(
                icon = "🧘",
                title = if (isEn) "Thoughtfully Simplified" else "Bewusst reduziert",
                description = if (isEn)
                    "Large tiles, clear typography, and a calm, clean design for a stress-free experience."
                else
                    "Große Kacheln, klare Schriften und ein ruhiges, aufgeräumtes Design für beste Übersicht."
            )

            Spacer(modifier = Modifier.height(12.dp))

            WelcomeFeatureCard(
                icon = "🌱",
                title = if (isEn) "Educational Guidance" else "Pädagogisch begleitet",
                description = if (isEn)
                    "Features aren't silently banned; they are unlocked together through kid-friendly quizzes and reflections."
                else
                    "Funktionen werden nicht stumm verboten, sondern gemeinsam mit deinem Kind über kindgerechte Quizze freigeschaltet."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("welcome_next_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isEn) "Get Started" else "Jetzt einrichten",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun WelcomeFeatureCard(
    icon: String,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
        border = BorderStroke(1.dp, AssistiveBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = icon, fontSize = 28.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = AssistiveTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// =====================================================================================
// STEP 2: PEDAGOGY CONCEPT
// =====================================================================================
@Composable
private fun PedagogyConceptStep(
    isEn: Boolean,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(AssistiveGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    tint = AssistiveGreen,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isEn) "Unlock Together" else "Gemeinsam freischalten",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isEn) "Understanding instead of just banning" else "Verstehen statt bloßem Verbieten",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AssistiveGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.dp, AssistiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    ConceptStepRow(
                        badgeNumber = "1",
                        badgeColor = AssistiveBlue,
                        title = if (isEn) "Child completes a short quiz" else "Kind absolviert ein Kurz-Quiz",
                        text = if (isEn)
                            "Before your child uses a feature (e.g. Camera, Calls, Browser), they answer 2 short questions with practical tips (e.g. 'Ask for permission first')."
                        else
                            "Bevor dein Kind z. B. fotografiert oder telefoniert, beantwortet es 2 kurze Fragen mit praktischen Tipps (z. B. 'Vorher um Erlaubnis fragen')."
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ConceptStepRow(
                        badgeNumber = "2",
                        badgeColor = AssistiveAmber,
                        title = if (isEn) "Handover to parents & media tips" else "Übergabe an Eltern & Medientipps",
                        text = if (isEn)
                            "As a parent, you receive compact pedagogical insights and answer brief reflection questions."
                        else
                            "Du als Elternteil erhältst kompakte Hintergrundtipps und beantwortest kurze Reflexionsfragen zur Medienbegleitung."
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ConceptStepRow(
                        badgeNumber = "3",
                        badgeColor = AssistiveGreen,
                        title = if (isEn) "Unlock via Parent PIN" else "Freischaltung per Eltern-PIN",
                        text = if (isEn)
                            "The feature is activated with your 4-digit parent PIN. Quick refreshers are scheduled after 30 and 90 days."
                        else
                            "Mit deiner 4-stelligen Eltern-PIN wird die Funktion anschließend aktiviert. Nach 30 und 90 Tagen gibt es kurze Auffrischungsfragen."
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Yellow note
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveAmberLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AssistiveAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isEn)
                            "You maintain full control in the Parent Dashboard at all times and can reset quizzes whenever needed."
                        else
                            "Du behältst im Elterndashboard jederzeit die volle Kontrolle und kannst Quizze bei Bedarf zurücksetzen.",
                        fontSize = 13.sp,
                        color = Color(0xFF78350F),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("pedagogy_concept_next_button")
        ) {
            Text(
                text = if (isEn) "Next: Child Profile" else "Weiter: Angaben zum Kind",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun ConceptStepRow(
    badgeNumber: String,
    badgeColor: Color,
    title: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeNumber,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                fontSize = 13.sp,
                color = AssistiveTextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

// =====================================================================================
// STEP 3: CHILD INFO (Name & Age Free Input)
// =====================================================================================
@Composable
private fun ChildInfoStep(
    isEn: Boolean,
    childName: String,
    onNameChange: (String) -> Unit,
    childAgeText: String,
    onAgeChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AssistiveBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AssistiveBlue,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isEn) "Who will use this smartphone?" else "Wer benutzt dieses Smartphone?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isEn)
                    "These details help us customize the home screen and pedagogical quizzes."
                else
                    "Mit diesen Angaben gestalten wir die Oberfläche und Quizze passend.",
                fontSize = 14.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Child Name Field
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.dp, AssistiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEn) "Child's name or nickname" else "Name oder Spitzname des Kindes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = childName,
                        onValueChange = onNameChange,
                        placeholder = { Text(if (isEn) "e.g. Ben, Emma, Leo…" else "z. B. Ben, Emma, Leo…") },
                        singleLine = true,
                        colors = assistiveTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("child_name_input"),
                        trailingIcon = {
                            if (childName.isNotBlank()) {
                                IconButton(onClick = { onNameChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = if (isEn) "Clear" else "Löschen")
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Child Age Free Input Field
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.dp, AssistiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEn) "How old is your child? (Free input)" else "Wie alt ist dein Kind? (Freiangabe)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = childAgeText,
                        onValueChange = {
                            if (it.length <= 15) {
                                onAgeChange(it)
                            }
                        },
                        placeholder = { Text(if (isEn) "Enter age (e.g. 7 or 8 years)" else "Alter eingeben (z. B. 7 oder 8 Jahre)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = assistiveTextFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("child_age_input"),
                        trailingIcon = {
                            if (childAgeText.isNotBlank()) {
                                IconButton(onClick = { onAgeChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = if (isEn) "Clear" else "Löschen")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isEn) "Free age input, e.g. as a number." else "Freie Angabe des Alters, z. B. als Zahl.",
                        fontSize = 12.sp,
                        color = AssistiveTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        val isNameValid = childName.trim().isNotBlank()
        Button(
            onClick = onNext,
            enabled = isNameValid,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AssistiveBlue,
                disabledContainerColor = AssistiveBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("child_info_next_button")
        ) {
            Text(
                text = if (isNameValid) {
                    if (isEn) "Next: Choose Avatar" else "Weiter: Avatar wählen"
                } else {
                    if (isEn) "Please enter a name" else "Bitte Namen eingeben"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// =====================================================================================
// STEP 4: AVATAR PICKER
// =====================================================================================
@Composable
private fun AvatarPickerStep(
    isEn: Boolean,
    childName: String,
    selectedAvatarId: String,
    customPhotoUri: String?,
    onSelectPreset: (ChildAvatarPreset) -> Unit,
    onPickCustomPhoto: () -> Unit,
    onNext: () -> Unit
) {
    val selectedPreset = PRESET_AVATARS.find { it.id == selectedAvatarId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEn) "Choose an avatar for $childName" else "Wähle einen Avatar für $childName",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isEn)
                    "Scroll through the animal avatars or upload your own photo."
                else
                    "Scrolle durch die Tiere oder lade ein eigenes Bild hoch.",
                fontSize = 14.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Large selected avatar preview card
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (customPhotoUri != null) Color.Transparent
                        else Color(selectedPreset?.bgHex ?: 0xFF3B82F6)
                    )
                    .border(4.dp, AssistiveBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (customPhotoUri != null) {
                    AsyncImage(
                        model = customPhotoUri,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = selectedPreset?.emoji ?: "🦁",
                        fontSize = 64.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (customPhotoUri != null) {
                    if (isEn) "Custom photo for $childName" else "Eigenes Foto von $childName"
                } else (selectedPreset?.name ?: "Avatar"),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isEn) "Preset Avatars (scroll left to right):" else "Vorgefertigte Avatare (von links nach rechts scrollen):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextMuted,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal scrolling preset avatars
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("avatar_presets_row"),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
                items(PRESET_AVATARS, key = { it.id }) { preset ->
                    val isSelected = selectedAvatarId == preset.id && customPhotoUri == null
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectPreset(preset) }
                            .padding(4.dp)
                            .testTag("avatar_chip_${preset.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(preset.bgHex))
                                .border(
                                    width = if (isSelected) 3.5.dp else 1.dp,
                                    color = if (isSelected) AssistiveBlue else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = preset.emoji, fontSize = 38.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = preset.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AssistiveBlue else AssistiveTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Or upload own photo button
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.5.dp, if (customPhotoUri != null) AssistiveBlue else AssistiveBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onPickCustomPhoto() }
                    .testTag("upload_custom_avatar_button")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AssistiveBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = if (isEn) "Upload photo" else "Foto hochladen",
                            tint = AssistiveBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Upload custom photo" else "Eigenes Foto hochladen",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )
                        Text(
                            text = if (customPhotoUri != null) {
                                if (isEn) "Photo selected ✓" else "Foto ausgewählt ✓"
                            } else {
                                if (isEn) "Choose an image from your device gallery" else "Ein Bild aus der Galerie deines Smartphones auswählen"
                            },
                            fontSize = 12.sp,
                            color = if (customPhotoUri != null) AssistiveGreen else AssistiveTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("avatar_next_button")
        ) {
            Text(
                text = if (isEn) "Next: Select Features" else "Weiter: Funktionen wählen",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// =====================================================================================
// STEP 5: INITIAL FEATURE SELECTION (Camera, Photos, Calls & first 2 contacts)
// =====================================================================================
@Composable
private fun InitialFeatureSelectionStep(
    isEn: Boolean,
    includeSms: Boolean,
    onToggleSms: (Boolean) -> Unit,
    includeCalls: Boolean,
    onToggleCalls: (Boolean) -> Unit,
    includeCamera: Boolean,
    onToggleCamera: (Boolean) -> Unit,
    includePhotos: Boolean,
    onTogglePhotos: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEn) "Select Initial Apps" else "Erste Apps auswählen",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isEn)
                    "Choose which initial core apps should appear on the home screen."
                else
                    "Wähle aus, welche ersten Apps auf dem Startbildschirm erscheinen sollen.",
                fontSize = 14.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Important callout: Selection != already unlocked
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveAmberLight),
                border = BorderStroke(1.dp, AssistiveAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = AssistiveAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isEn)
                            "Important: Features start locked to protect your child. You will unlock them together later via quiz."
                        else
                            "Wichtig: Die Funktionen starten zum Schutz deines Kindes gesperrt. Du schaltest sie später gemeinsam per Quiz frei.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF78350F),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Nachrichten (SMS) Toggle
            FeatureToggleCard(
                icon = Icons.Default.Email,
                iconBg = 0xFF6366F1,
                title = if (isEn) "Messages" else "Nachrichten",
                desc = if (isEn) "Send & receive text messages safely" else "Sicher Textnachrichten senden & empfangen",
                checked = includeSms,
                onCheckedChange = onToggleSms,
                tag = "feature_toggle_sms"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Telefonie Toggle
            FeatureToggleCard(
                icon = Icons.Default.Phone,
                iconBg = 0xFF059669,
                title = if (isEn) "Phone" else "Telefonie",
                desc = if (isEn) "Call saved contacts with large buttons" else "Hinterlegte Kontakte einfach anrufen",
                checked = includeCalls,
                onCheckedChange = onToggleCalls,
                tag = "feature_toggle_calls"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Camera Toggle
            FeatureToggleCard(
                icon = Icons.Default.CameraAlt,
                iconBg = 0xFF2563EB,
                title = if (isEn) "Camera" else "Kamera",
                desc = if (isEn) "Take photos & videos" else "Fotos & Videos aufnehmen",
                checked = includeCamera,
                onCheckedChange = onToggleCamera,
                tag = "feature_toggle_camera"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Photos Toggle
            FeatureToggleCard(
                icon = Icons.Default.Image,
                iconBg = 0xFFDB2777,
                title = if (isEn) "Photos" else "Fotos",
                desc = if (isEn) "Protected gallery of captured photos" else "Geschützte Galerie der eigenen Aufnahmen",
                checked = includePhotos,
                onCheckedChange = onTogglePhotos,
                tag = "feature_toggle_photos"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        val hasAnyFeature = includeSms || includeCalls || includeCamera || includePhotos
        Button(
            onClick = onNext,
            enabled = hasAnyFeature,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AssistiveBlue,
                disabledContainerColor = AssistiveBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("features_next_button")
        ) {
            Text(
                text = if (hasAnyFeature) {
                    if (isEn) "Next: Set Up Initial Contacts" else "Weiter: Erste Kontakte anlegen"
                } else {
                    if (isEn) "Select at least 1 app" else "Mindestens 1 App wählen"
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun InitialContactsSetupStep(
    isEn: Boolean,
    contact1Name: String,
    onContact1NameChange: (String) -> Unit,
    contact1Number: String,
    onContact1NumberChange: (String) -> Unit,
    contact2Name: String,
    onContact2NameChange: (String) -> Unit,
    contact2Number: String,
    onContact2NumberChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEn) "Initial Contacts" else "Erste Kontakte anlegen",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isEn)
                    "Enter the first contacts (e.g. Mom & Dad). They will appear in the Contacts sub-menu and in the Phone & Messages apps."
                else
                    "Lege hier die ersten Kontakte an (z. B. Mama und Papa). Sie stehen deinem Kind in der Telefon- & Nachrichten-App zur Verfügung und werden im Untermenü des Elterndashboards gespeichert.",
                fontSize = 14.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.dp, AssistiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isEn) "1st Contact (e.g. Mom):" else "1. Kontakt (z. B. Mama):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = contact1Name,
                            onValueChange = onContact1NameChange,
                            placeholder = { Text(if (isEn) "Mom" else "Mama") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = assistiveTextFieldColors()
                        )
                        OutlinedTextField(
                            value = contact1Number,
                            onValueChange = onContact1NumberChange,
                            placeholder = { Text("0170 1234567") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = assistiveTextFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isEn) "2nd Contact (e.g. Dad or Grandma):" else "2. Kontakt (z. B. Papa oder Oma):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = contact2Name,
                            onValueChange = onContact2NameChange,
                            placeholder = { Text(if (isEn) "Dad" else "Papa") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = assistiveTextFieldColors()
                        )
                        OutlinedTextField(
                            value = contact2Number,
                            onValueChange = onContact2NumberChange,
                            placeholder = { Text("0171 7654321") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = assistiveTextFieldColors()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("contacts_next_button")
        ) {
            Text(
                text = if (isEn) "Next: Explore Parent Dashboard" else "Weiter: Elterndashboard entdecken",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// =====================================================================================
// STEP 6: DASHBOARD INFO (Explaining later additions of apps and features)
// =====================================================================================
@Composable
private fun DashboardInfoStep(
    isEn: Boolean,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AssistivePurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = AssistivePurple,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isEn) "Expandable Later" else "Später erweiterbar",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isEn)
                    "Add more apps & features anytime in the Parent Dashboard."
                else
                    "Weitere Apps & Funktionen jederzeit im Elterndashboard hinzufügen.",
                fontSize = 14.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.dp, AssistiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEn)
                            "For the start, the core features are enough. In the protected parent area, you can gradually unlock more apps:"
                        else
                            "Für den Start genügen die Grundfunktionen. Im geschützten Elternbereich kannst du später nach und nach weitere Apps freischalten:",
                        fontSize = 14.sp,
                        color = AssistiveTextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DashboardFeatureExampleRow(
                        icon = "🌐",
                        title = if (isEn) "Safe Internet" else "Sicheres Internet",
                        desc = if (isEn)
                            "Kid-friendly web browser with whitelist for PBS Kids, BBC Bitesize, Kiddle, etc."
                        else
                            "Kindgerechter Web-Browser mit Whitelist für Die Maus, KiKA, FragFINN etc."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DashboardFeatureExampleRow(
                        icon = "💬",
                        title = if (isEn) "Messenger & WhatsApp" else "Messenger & WhatsApp",
                        desc = if (isEn)
                            "Dedicated chat apps with educational quiz on privacy & messages."
                        else
                            "Eigene Chat-Apps mit pädagogischem Quiz zu Datenschutz & Nachrichten."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DashboardFeatureExampleRow(
                        icon = "🎧",
                        title = if (isEn) "Audiobooks & Music" else "Hörspiele & Musik",
                        desc = if (isEn)
                            "Kids streaming like Spotify Kids or audiobook apps."
                        else
                            "Kinder-Streaming wie Spotify Kids, Tigerbox oder Hörspiel-Apps."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DashboardFeatureExampleRow(
                        icon = "📚",
                        title = if (isEn) "Learning & School Apps" else "Lern- & Schul-Apps",
                        desc = if (isEn)
                            "Math apps, learning tools, or any apps installed on the device."
                        else
                            "Anton, Mathe-Apps oder andere auf dem Smartphone installierte Apps."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DashboardFeatureExampleRow(
                        icon = "👥",
                        title = if (isEn) "More Contacts" else "Weitere Kontakte",
                        desc = if (isEn)
                            "Grandparents, siblings, and friends as quick-dial tiles."
                        else
                            "Oma, Opa, Geschwister und Freunde als Schnellwahl-Kacheln."
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Blue Info Note
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveBlueLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AssistiveBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isEn)
                            "Every newly added app automatically goes through the educational unlock routine before first use."
                        else
                            "Jede neu hinzugefügte App durchläuft vor der ersten Nutzung automatisch die kindgerechte Freischalt-Routine.",
                        fontSize = 13.sp,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("dashboard_info_next_button")
        ) {
            Text(
                text = if (isEn) "Next: Setup Emergency SOS" else "Weiter: Notfallfunktion einrichten",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun DashboardFeatureExampleRow(
    icon: String,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = icon, fontSize = 24.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = AssistiveTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun FeatureToggleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Long,
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
        border = BorderStroke(1.dp, if (checked) Color(iconBg) else AssistiveBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(iconBg)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = AssistiveTextSecondary
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(iconBg)
                )
            )
        }
    }
}

// =====================================================================================
// STEP 7: EMERGENCY SETUP (SOS)
// =====================================================================================
@Composable
private fun EmergencySetupStep(
    isEn: Boolean,
    sosEnabled: Boolean,
    onToggleSos: (Boolean) -> Unit,
    emergencyName: String,
    onNameChange: (String) -> Unit,
    emergencyNumber: String,
    onNumberChange: (String) -> Unit,
    sendSms: Boolean,
    onToggleSms: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AssistiveRedLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = AssistiveRed,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isEn) "SOS Emergency Feature" else "SOS-Notfallfunktion",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isEn) "Quick help for your child in critical situations." else "Schnelle Hilfe für dein Kind im Ernstfall.",
                fontSize = 14.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Explanation Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.dp, AssistiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "🚨", fontSize = 24.sp)
                        Text(
                            text = if (isEn) "How does the SOS button work?" else "Wie funktioniert der SOS-Knopf?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isEn)
                            "A red SOS button is located at the bottom of the home screen. Pressing and holding it for 3 seconds immediately calls the configured emergency number or contact."
                        else
                            "Unten auf dem Homescreen befindet sich ein roter SOS-Knopf. Wird dieser für 3 Sekunden gedrückt gehalten, ruft das Smartphone sofort den hinterlegten Kontakt oder Notruf an.",
                        fontSize = 13.sp,
                        color = AssistiveTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Toggle SOS on/off
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.dp, if (sosEnabled) AssistiveRed else AssistiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Enable SOS Emergency Button" else "SOS-Notrufknopf aktivieren",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AssistiveTextPrimary
                        )
                        Text(
                            text = if (sosEnabled) {
                                if (isEn) "Active on home screen" else "Aktiv auf dem Homescreen"
                            } else {
                                if (isEn) "Disabled" else "Deaktiviert"
                            },
                            fontSize = 12.sp,
                            color = if (sosEnabled) AssistiveRed else AssistiveTextMuted
                        )
                    }

                    Switch(
                        checked = sosEnabled,
                        onCheckedChange = onToggleSos,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AssistiveRed
                        )
                    )
                }
            }

            AnimatedVisibility(visible = sosEnabled) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                        border = BorderStroke(1.dp, AssistiveBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isEn) "Contact Name" else "Name des Kontakts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = emergencyName,
                                onValueChange = onNameChange,
                                placeholder = { Text(if (isEn) "e.g. Mom, Dad or Emergency" else "z.B. Mama, Papa oder Notruf 112") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = assistiveTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (isEn) "Emergency Number" else "Notrufnummer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AssistiveTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = emergencyNumber,
                                onValueChange = onNumberChange,
                                placeholder = { Text(if (isEn) "112, 911 or mobile number" else "112 oder Handynummer") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                colors = assistiveTextFieldColors(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (emergencyNumber == "112") AssistiveRedLight else AssistiveBackground,
                                    border = BorderStroke(1.dp, if (emergencyNumber == "112") AssistiveRed else AssistiveBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onNumberChange("112") }
                                ) {
                                    Text(
                                        text = "112 (EU)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (emergencyNumber == "112") AssistiveRed else AssistiveTextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (emergencyNumber == "911") AssistiveRedLight else AssistiveBackground,
                                    border = BorderStroke(1.dp, if (emergencyNumber == "911") AssistiveRed else AssistiveBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onNumberChange("911") }
                                ) {
                                    Text(
                                        text = "911 (US)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (emergencyNumber == "911") AssistiveRed else AssistiveTextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (emergencyNumber == "110") AssistiveRedLight else AssistiveBackground,
                                    border = BorderStroke(1.dp, if (emergencyNumber == "110") AssistiveRed else AssistiveBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onNumberChange("110") }
                                ) {
                                    Text(
                                        text = if (isEn) "110 (Police)" else "110 (Polizei)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (emergencyNumber == "110") AssistiveRed else AssistiveTextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // SMS location toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onToggleSms(!sendSms) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isEn) "Send GPS Location via SMS" else "Standort per SMS senden",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AssistiveTextPrimary
                                    )
                                    Text(
                                        text = if (isEn)
                                            "Automatically sends a Google Maps link with the current GPS coordinates"
                                        else
                                            "Sendet automatisch einen Google Maps-Link mit aktuellem GPS-Standort",
                                        fontSize = 12.sp,
                                        color = AssistiveTextSecondary
                                    )
                                }
                                Switch(
                                    checked = sendSms,
                                    onCheckedChange = onToggleSms,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AssistiveGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("emergency_next_button")
        ) {
            Text(
                text = if (isEn) "Next: Choose Layout" else "Weiter: Layout wählen",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// =====================================================================================
// STEP 8: LAYOUT STYLE (Grid vs. Rows)
// =====================================================================================
@Composable
private fun LayoutStyleStep(
    isEn: Boolean,
    selectedStyle: LayoutStyle,
    onSelectStyle: (LayoutStyle) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isEn) "Choose Layout" else "Layout auswählen",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isEn)
                    "How should tiles be arranged on the home screen?"
                else
                    "Wie sollen die Kacheln auf dem Homescreen angeordnet sein?",
                fontSize = 14.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Grid Choice Card
            LayoutChoiceCard(
                icon = Icons.Default.GridView,
                title = if (isEn) "Grid (2 Columns)" else "Gitter (Grid)",
                desc = if (isEn)
                    "Large, square tiles in two columns. Ideal for tablets and visual recognition."
                else
                    "Große, quadratische Kacheln in zwei Spalten. Ideal für Tablets und visuelle Erkennung.",
                isSelected = selectedStyle == LayoutStyle.GRID,
                onClick = { onSelectStyle(LayoutStyle.GRID) },
                tag = "layout_choice_grid"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rows Choice Card
            LayoutChoiceCard(
                icon = Icons.Default.TableRows,
                title = if (isEn) "Rows (List)" else "Reihen (Rows)",
                desc = if (isEn)
                    "Full-width rows with large icon and title side-by-side. Highly readable."
                else
                    "Große Zeilen über die volle Breite mit Icon und Text nebeneinander. Besonders gut lesbar.",
                isSelected = selectedStyle == LayoutStyle.ROWS,
                onClick = { onSelectStyle(LayoutStyle.ROWS) },
                tag = "layout_choice_rows"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("layout_next_button")
        ) {
            Text(
                text = if (isEn) "Next: Set Parent PIN" else "Weiter: Eltern-PIN festlegen",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun LayoutChoiceCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
        border = BorderStroke(
            2.dp,
            if (isSelected) AssistiveBlue else AssistiveBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) AssistiveBlueLight else AssistiveBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) AssistiveBlue else AssistiveTextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AssistiveTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    fontSize = 13.sp,
                    color = AssistiveTextSecondary,
                    lineHeight = 18.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = AssistiveBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// =====================================================================================
// STEP 9: PARENT PIN (Enter 2x to verify)
// =====================================================================================
@Composable
private fun ParentPinStep(
    isEn: Boolean,
    pin: String,
    onPinChange: (String) -> Unit,
    pinConfirm: String,
    onConfirmChange: (String) -> Unit,
    pinVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onNext: () -> Unit
) {
    val is4Digits = pin.length == 4 && pin.all { it.isDigit() }
    val isConfirm4Digits = pinConfirm.length == 4 && pinConfirm.all { it.isDigit() }
    val isMatch = is4Digits && isConfirm4Digits && pin == pinConfirm

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AssistiveBlueLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AssistiveBlue,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isEn) "Set Parent PIN" else "Eltern-PIN festlegen",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isEn)
                    "Secure the parent area and unlock features for your child after the quiz."
                else
                    "Sichere den Elternbereich und schalte damit Funktionen nach dem Quiz für dein Kind frei.",
                fontSize = 14.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.dp, AssistiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isEn) "1. New 4-digit PIN:" else "1. Neue 4-stellige PIN:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                onPinChange(it)
                            }
                        },
                        placeholder = { Text(if (isEn) "4 digits (e.g. 2468)" else "4 Ziffern (z. B. 2468)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        colors = assistiveTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_pin_field"),
                        trailingIcon = {
                            IconButton(onClick = onToggleVisibility) {
                                Icon(
                                    imageVector = if (pinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isEn) "Show PIN" else "PIN anzeigen"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (isEn) "2. Repeat PIN to confirm:" else "2. PIN zur Bestätigung wiederholen:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pinConfirm,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                onConfirmChange(it)
                            }
                        },
                        placeholder = { Text(if (isEn) "Repeat PIN" else "PIN wiederholen") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        colors = assistiveTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_pin_confirm_field")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real-time verification feedback
                    if (pin.length == 4 && pinConfirm.length == 4) {
                        if (isMatch) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AssistiveGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isEn) "PINs match ✓" else "PINs stimmen überein ✓",
                                    color = AssistiveGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AssistiveRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isEn) "PINs do not match!" else "PINs stimmen nicht überein!",
                                    color = AssistiveRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (isEn) "Please enter exactly 4 digits." else "Bitte genau 4 Ziffern eingeben.",
                            fontSize = 12.sp,
                            color = AssistiveTextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onNext,
            enabled = isMatch,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AssistiveBlue,
                disabledContainerColor = AssistiveBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("pin_next_button")
        ) {
            Text(
                text = if (isMatch) {
                    if (isEn) "Save PIN & Continue" else "PIN speichern & Weiter"
                } else {
                    if (isEn) "Please enter a valid 4-digit PIN" else "Bitte gültige 4-stellige PIN eingeben"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// =====================================================================================
// STEP 10: COMPLETION & CELEBRATION
// =====================================================================================
@Composable
private fun CompletionStep(
    isEn: Boolean,
    childName: String,
    childAvatar: String,
    includeCamera: Boolean,
    includePhotos: Boolean,
    includeCalls: Boolean,
    sosEnabled: Boolean,
    emergencyNumber: String,
    layoutStyle: LayoutStyle,
    onFinish: () -> Unit
) {
    val preset = PRESET_AVATARS.find { it.id == childAvatar }
    val isCustomPhoto = childAvatar.startsWith("content://") || childAvatar.startsWith("file://")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Big celebratory hero badge
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFEC4899))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎉", fontSize = 52.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isEn) "All set for $childName!" else "Alles bereit für $childName!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AssistiveTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isEn)
                    "Initial setup is complete. We wish you and your child a joyful and safe journey exploring the smartphone world together!"
                else
                    "Die Ersteinrichtung ist erfolgreich abgeschlossen. Wir wünschen dir und deinem Kind viel Freude beim gemeinsamen Erkunden der Smartphone-Welt!",
                fontSize = 15.sp,
                color = AssistiveTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Summary Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AssistiveSurface),
                border = BorderStroke(1.dp, AssistiveBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isEn) "Summary of chosen settings:" else "Zusammenfassung der Einstellungen:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AssistiveTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(preset?.bgHex ?: 0xFF3B82F6)),
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
                                Text(text = preset?.emoji ?: "🦁", fontSize = 20.sp)
                            }
                        }

                        Text(
                            text = if (isEn) "Profile: $childName" else "Profil: $childName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AssistiveTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val activeFeatures = mutableListOf<String>()
                    if (includeCamera) activeFeatures.add(if (isEn) "Camera" else "Kamera")
                    if (includePhotos) activeFeatures.add(if (isEn) "Photos" else "Fotos")
                    if (includeCalls) activeFeatures.add(if (isEn) "Calls" else "Anrufen")

                    Text(
                        text = if (isEn)
                            "• Features: ${activeFeatures.joinToString(", ")} (initially protected)"
                        else
                            "• Funktionen: ${activeFeatures.joinToString(", ")} (anfangs geschützt)",
                        fontSize = 13.sp,
                        color = AssistiveTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (sosEnabled) {
                            if (isEn) "• SOS Emergency: Active ($emergencyNumber)" else "• SOS-Notruf: Aktiv ($emergencyNumber)"
                        } else {
                            if (isEn) "• SOS Emergency: Disabled" else "• SOS-Notruf: Deaktiviert"
                        },
                        fontSize = 13.sp,
                        color = AssistiveTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isEn) {
                            "• Layout: ${if (layoutStyle == LayoutStyle.GRID) "Grid (2 columns)" else "Rows"}"
                        } else {
                            "• Layout: ${if (layoutStyle == LayoutStyle.GRID) "Gitter (2 Spalten)" else "Reihen"}"
                        },
                        fontSize = 13.sp,
                        color = AssistiveTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onFinish,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AssistiveGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("setup_finish_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White
                )
                Text(
                    text = if (isEn) "Launch TEDOKI Kids Launcher" else "TEDOKI Kids Launcher starten",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
