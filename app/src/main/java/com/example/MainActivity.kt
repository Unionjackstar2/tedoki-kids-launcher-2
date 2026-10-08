package com.example

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.data.LauncherPreferences
import com.example.data.quiz.PedagogyPreferences
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.quiz.QuizCategory
import com.example.model.quiz.ReviewDueState
import com.example.ui.browser.ChildBrowserScreen
import com.example.ui.camera.SimplifiedCameraScreen
import com.example.ui.calling.SimplifiedCallScreen
import com.example.ui.calling.ChildCallScreen
import com.example.ui.calling.SosInCallScreen
import com.example.ui.components.CustomLauncherStatusBar
import com.example.ui.home.SimplifiedHomeScreen
import com.example.ui.lockscreen.ChildLockScreen
import com.example.ui.photos.SimplifiedPhotosScreen
import com.example.ui.quiz.QuizFlowScreen
import com.example.ui.settings.ParentSettingsScreen
import com.example.ui.setup.ParentSetupScreen
import com.example.ui.sos.SimplifiedSosScreen
import com.example.ui.theme.AssistiveLauncherTheme
import com.example.util.DeviceStatusManager
import java.util.Locale

enum class LauncherScreen {
    SETUP,
    HOME,
    LOCKSCREEN,
    CAMERA,
    CALL,
    CALL_CONTACTS,
    PHOTOS,
    PARENT_SETTINGS,
    SOS,
    SOS_CALL,
    QUIZ,
    BROWSER,
    SMS
}

data class ActiveQuizTarget(
    val button: LauncherButton,
    val appKey: String,
    val appName: String,
    val category: QuizCategory,
    val reviewState: ReviewDueState
)

class MainActivity : ComponentActivity() {
    private lateinit var preferences: LauncherPreferences
    private lateinit var pedagogyPreferences: PedagogyPreferences

    fun setWakelockFlags(turnOn: Boolean) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(turnOn)
            setTurnScreenOn(turnOn)
        } else {
            @Suppress("DEPRECATION")
            if (turnOn) {
                window.addFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                )
            } else {
                window.clearFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                )
            }
        }
    }

    fun hideSystemBars() {
        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(
            androidx.core.view.WindowInsetsCompat.Type.statusBars() or
            androidx.core.view.WindowInsetsCompat.Type.navigationBars()
        )
        insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val activity = this
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide system status and navigation bars so launcher stays clean and immersive
        hideSystemBars()

        DeviceStatusManager.init(this)
        preferences = LauncherPreferences(this)
        pedagogyPreferences = PedagogyPreferences(this)

        setContent {
            val settings by preferences.settingsFlow.collectAsState()

            // Dynamic locale support for instant EN/DE switching without rebuilding context on rotation
            val localizedContext = remember(settings.language) {
                val locale = Locale(settings.language)
                Locale.setDefault(locale)

                class LocalizedActivityContext(
                    val base: MainActivity,
                    val lang: String
                ) : ContextWrapper(base), ActivityResultRegistryOwner by base {
                    override fun getResources(): Resources {
                        val conf = Configuration(base.resources.configuration)
                        val loc = Locale(lang)
                        conf.setLocale(loc)
                        conf.setLayoutDirection(loc)
                        return base.createConfigurationContext(conf).resources
                    }
                    override fun getAssets(): AssetManager = base.assets
                }

                LocalizedActivityContext(this@MainActivity, settings.language)
            }

            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDarkTheme = when (settings.themeMode) {
                com.example.model.ThemeMode.LIGHT -> false
                com.example.model.ThemeMode.DARK -> true
                com.example.model.ThemeMode.SYSTEM -> isSystemDark
            }

            CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides localizedContext,
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                AssistiveLauncherTheme(darkTheme = isDarkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AssistiveLauncherApp(
                            preferences = preferences,
                            pedagogyPreferences = pedagogyPreferences
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }
}

@Composable
fun AssistiveLauncherApp(
    preferences: LauncherPreferences,
    pedagogyPreferences: PedagogyPreferences
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is MainActivity) break
            ctx = ctx.baseContext
        }
        ctx as? MainActivity
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val settings by preferences.settingsFlow.collectAsState()
    val pedagogyStatuses by pedagogyPreferences.statusMapFlow.collectAsState()

    var currentScreen by rememberSaveable(settings.isSetupComplete) {
        mutableStateOf(
            if (!settings.isSetupComplete) {
                LauncherScreen.SETUP
            } else if (settings.enableLockscreen) {
                LauncherScreen.LOCKSCREEN
            } else {
                LauncherScreen.HOME
            }
        )
    }

    // Screen-Off BroadcastReceiver to automatically activate LockScreen:
    // Only activates when:
    // 1. Lockscreen is enabled in settings
    // 2. Setup is complete
    // 3. Not currently in an active phone call (Proximity sensor turning display off at ear)
    // 4. Not currently on SOS Call screen
    androidx.compose.runtime.DisposableEffect(context, settings.enableLockscreen) {
        val screenOffReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: android.content.Intent?) {
                if (intent?.action == android.content.Intent.ACTION_SCREEN_OFF) {
                    val isCallActive = com.example.util.LockscreenSecurityHelper.isCallActive()
                    if (settings.enableLockscreen && settings.isSetupComplete && !isCallActive) {
                        if (currentScreen != LauncherScreen.SOS_CALL && currentScreen != LauncherScreen.LOCKSCREEN) {
                            currentScreen = LauncherScreen.LOCKSCREEN
                        }
                    }
                }
            }
        }
        val filter = android.content.IntentFilter(android.content.Intent.ACTION_SCREEN_OFF)
        context.registerReceiver(screenOffReceiver, filter)

        onDispose {
            try {
                context.unregisterReceiver(screenOffReceiver)
            } catch (ignored: Exception) {}
        }
    }

    LaunchedEffect(currentScreen) {
        focusManager.clearFocus()
    }

    LaunchedEffect(activity?.intent) {
        val intent = activity?.intent
        if (intent != null && intent.getBooleanExtra("EXTRA_NAVIGATE_TO_SMS", false)) {
            currentScreen = LauncherScreen.SMS
            intent.removeExtra("EXTRA_NAVIGATE_TO_SMS")
        }
    }

    var activeCallButton by remember { mutableStateOf<LauncherButton?>(null) }
    var activeQuizTarget by remember { mutableStateOf<ActiveQuizTarget?>(null) }
    var celebrationMessage by remember { mutableStateOf<String?>(null) }
    var isBrowserFullScreenVideo by remember { mutableStateOf(false) }

    val activeTelecomCall by com.example.telecom.CallManager.currentCall.collectAsState()
    val telecomCallState by com.example.telecom.CallManager.callState.collectAsState()
    val isSosCall by com.example.telecom.CallManager.isSosCall.collectAsState()

    // Automatic screen navigation when InCallService reports an active Telecom call
    LaunchedEffect(activeTelecomCall, telecomCallState, isSosCall) {
        if (activeTelecomCall != null &&
            telecomCallState != android.telecom.Call.STATE_DISCONNECTED
        ) {
            // Wake up display and allow lock screen overlay for incoming/active calls
            activity?.setWakelockFlags(true)

            if (isSosCall) {
                if (currentScreen != LauncherScreen.SOS_CALL) {
                    currentScreen = LauncherScreen.SOS_CALL
                }
            } else if (currentScreen != LauncherScreen.CALL) {
                val phoneNum = com.example.telecom.CallManager.phoneNumber.value ?: ""
                val matchingButton = settings.buttons.find { it.type == ButtonType.CALL && it.phoneNumber == phoneNum }
                    ?: LauncherButton(
                        id = "telecom_call_${System.currentTimeMillis()}",
                        type = ButtonType.CALL,
                        title = if (phoneNum.isNotBlank()) phoneNum else "Anruf",
                        phoneNumber = phoneNum,
                        colorHex = 0xFF059669
                    )
                activeCallButton = matchingButton
                currentScreen = LauncherScreen.CALL
            }
        } else {
            // Restore secure lock screen settings when call ends
            activity?.setWakelockFlags(false)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
        when (screen) {
            LauncherScreen.LOCKSCREEN -> {
                ChildLockScreen(
                    settings = settings,
                    onUnlockToHome = {
                        currentScreen = LauncherScreen.HOME
                    }
                )
            }

            LauncherScreen.SETUP -> {
                ParentSetupScreen(
                    currentSettings = settings,
                    onCompleteSetup = { newSettings ->
                        preferences.updateSettings(newSettings)
                        currentScreen = LauncherScreen.HOME
                    },
                    onLanguageChange = { newLang ->
                        preferences.updateSettings(settings.copy(language = newLang))
                    }
                )
            }

            LauncherScreen.HOME -> {
                SimplifiedHomeScreen(
                    settings = settings,
                    pedagogyStatuses = pedagogyStatuses,
                    celebrationMessage = celebrationMessage,
                    onDismissCelebration = { celebrationMessage = null },
                    onButtonClick = { button ->
                        when (button.type) {
                            ButtonType.CAMERA -> currentScreen = LauncherScreen.CAMERA
                            ButtonType.PHOTOS -> currentScreen = LauncherScreen.PHOTOS
                            ButtonType.CALL -> {
                                currentScreen = LauncherScreen.CALL_CONTACTS
                            }
                            ButtonType.APP -> {
                                val launched = com.example.util.AppUtils.launchApp(context, button.packageName)
                                if (!launched) {
                                    android.widget.Toast.makeText(context, R.string.app_not_installed, android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            ButtonType.BROWSER -> {
                                currentScreen = LauncherScreen.BROWSER
                            }
                            ButtonType.SMS -> {
                                currentScreen = LauncherScreen.SMS
                            }
                        }
                    },
                    onTriggerQuiz = { button, appKey, category, reviewDue ->
                        celebrationMessage = null
                        val displayName = if (button.type == ButtonType.CALL) {
                            context.getString(R.string.btn_call_custom)
                        } else {
                            com.example.ui.components.getLocalizedButtonTitle(context, button)
                        }
                        activeQuizTarget = ActiveQuizTarget(
                            button = button,
                            appKey = appKey,
                            appName = displayName,
                            category = category,
                            reviewState = reviewDue
                        )
                        currentScreen = LauncherScreen.QUIZ
                    },
                    onOpenParentSettings = {
                        currentScreen = LauncherScreen.PARENT_SETTINGS
                    },
                    onSosTriggered = {
                        currentScreen = LauncherScreen.SOS
                    },
                    onReorderButtons = { newButtons ->
                        preferences.updateSettings(preferences.getSettings().copy(buttons = newButtons))
                    }
                )
            }

            LauncherScreen.QUIZ -> {
                val target = activeQuizTarget
                if (target != null) {
                    QuizFlowScreen(
                        appName = target.appName,
                        category = target.category,
                        reviewState = target.reviewState,
                        parentPin = settings.pin,
                        onCompleteUnlocked = {
                            pedagogyPreferences.unlockAppWithPin(target.appKey, target.appName, target.category)
                            celebrationMessage = "${target.appName} wurde freigeschaltet. Viel Spaß und denke an das, was du gelernt hast!"
                            currentScreen = LauncherScreen.HOME
                        },
                        onCancel = {
                            currentScreen = LauncherScreen.HOME
                        }
                    )
                } else {
                    currentScreen = LauncherScreen.HOME
                }
            }

            LauncherScreen.CAMERA -> {
                SimplifiedCameraScreen(
                    onBack = { currentScreen = LauncherScreen.HOME }
                )
            }

            LauncherScreen.CALL_CONTACTS -> {
                ChildCallScreen(
                    settings = settings,
                    onDialContact = { button ->
                        activeCallButton = button
                        currentScreen = LauncherScreen.CALL
                    },
                    onBack = { currentScreen = LauncherScreen.HOME }
                )
            }

            LauncherScreen.CALL -> {
                val callButton = activeCallButton ?: LauncherButton(
                    id = "call_default",
                    type = ButtonType.CALL,
                    title = "Phone",
                    colorHex = 0xFF059669
                )
                SimplifiedCallScreen(
                    button = callButton,
                    onBack = { currentScreen = LauncherScreen.CALL_CONTACTS }
                )
            }

            LauncherScreen.PHOTOS -> {
                SimplifiedPhotosScreen(
                    onBack = { currentScreen = LauncherScreen.HOME }
                )
            }

            LauncherScreen.BROWSER -> {
                ChildBrowserScreen(
                    currentSettings = settings,
                    onClose = {
                        isBrowserFullScreenVideo = false
                        currentScreen = LauncherScreen.HOME
                    },
                    onFullScreenVideoChange = { isFullscreen ->
                        isBrowserFullScreenVideo = isFullscreen
                    }
                )
            }

            LauncherScreen.PARENT_SETTINGS -> {
                ParentSettingsScreen(
                    currentSettings = settings,
                    pedagogyPreferences = pedagogyPreferences,
                    onSaveSettings = { updated ->
                        preferences.updateSettings(updated)
                    },
                    onReturnToHome = {
                        currentScreen = LauncherScreen.HOME
                    },
                    onLanguageChange = { newLang ->
                        preferences.updateSettings(settings.copy(language = newLang))
                    },
                    onResetLauncher = {
                        pedagogyPreferences.resetAllPedagogy()
                        preferences.resetLauncherToInitialSetup()
                        celebrationMessage = null
                        activeQuizTarget = null
                        activeCallButton = null
                        currentScreen = LauncherScreen.HOME
                    }
                )
            }

            LauncherScreen.SOS -> {
                SimplifiedSosScreen(
                    emergencyNumber = settings.emergencyNumber,
                    emergencyContactName = settings.emergencyContactName,
                    sendSosLocationSms = settings.sendSosLocationSms,
                    contacts = settings.contacts,
                    onBack = { currentScreen = LauncherScreen.HOME }
                )
            }

            LauncherScreen.SOS_CALL -> {
                SosInCallScreen(
                    settings = settings,
                    onEndCall = { currentScreen = LauncherScreen.HOME }
                )
            }

            LauncherScreen.SMS -> {
                com.example.ui.sms.ChildSmsScreen(
                    settings = settings,
                    onBack = { currentScreen = LauncherScreen.HOME }
                )
            }
        }
    }
}

        // Custom In-App Status Bar placed as an overlay on top of screen content
        // Transparent background so lockscreen wallpaper or homescreen gradient flows edge-to-edge
        if (!isBrowserFullScreenVideo) {
            CustomLauncherStatusBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(androidx.compose.ui.Alignment.TopCenter)
            )
        }
    }
}
