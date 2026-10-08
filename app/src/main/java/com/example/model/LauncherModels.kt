package com.example.model

enum class LayoutStyle {
    GRID,
    ROWS
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ButtonType {
    CAMERA,
    PHOTOS,
    CALL,
    APP,
    BROWSER,
    SMS
}

enum class LockscreenClockStyle {
    ANALOG_CONNI,
    DIGITAL
}

data class WebBookmark(
    val id: String,
    val title: String,
    val url: String,
    val domain: String,
    val iconEmoji: String = "🌐",
    val colorHex: Long = 0xFF0284C7,
    val isEnabled: Boolean = true
)

data class LauncherContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val colorHex: Long = 0xFF2563EB
)

data class LauncherButton(
    val id: String,
    val type: ButtonType,
    val title: String,
    val phoneNumber: String = "",
    val packageName: String = "",
    val photoUri: String? = null,
    val colorHex: Long = 0xFF2563EB
)

data class ParentAccount(
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val isConnected: Boolean = false
)

data class LauncherSettings(
    val isSetupComplete: Boolean = false,
    val childName: String = "",
    val childAge: Int = 8,
    val childAvatar: String = "lion",
    val parentAccount: ParentAccount = ParentAccount(),
    val pin: String = "1234",
    val layoutStyle: LayoutStyle = LayoutStyle.GRID,
    val language: String = if (java.util.Locale.getDefault().language.startsWith("de")) "de" else "en",
    val buttons: List<LauncherButton> = defaultButtons(),
    val contacts: List<LauncherContact> = defaultContacts(),
    val emergencyNumber: String = "112",
    val emergencyContactName: String = "Notruf / Emergency",
    val showSosButton: Boolean = true,
    val sendSosLocationSms: Boolean = true,
    val enableClockClick: Boolean = true,
    val enableCalendarClick: Boolean = true,
    val enableKeypadDialer: Boolean = false,
    val enableAndroidContacts: Boolean = false,
    val allowedContactAccounts: List<String> = emptyList(), // Account names selected by parents
    val allowedWebsites: List<WebBookmark> = defaultWebsites(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val enableLockscreen: Boolean = false,
    val lockscreenClockStyle: LockscreenClockStyle = LockscreenClockStyle.ANALOG_CONNI,
    val lockscreenPresetBackground: String = "soft_blue", // soft_blue, pastel_green, starry_night, sunny_yellow, warm_orange, gentle_purple
    val lockscreenCustomBackgroundUri: String? = null,
    val lockscreenRequireMathProblem: Boolean = false
) {
    companion object {
        fun defaultContacts(): List<LauncherContact> = listOf(
            LauncherContact(
                id = "contact_mom",
                name = "Mama",
                phoneNumber = "555-0101",
                colorHex = 0xFF059669
            ),
            LauncherContact(
                id = "contact_dad",
                name = "Papa",
                phoneNumber = "555-0102",
                colorHex = 0xFF2563EB
            )
        )

        fun defaultWebsites(): List<WebBookmark> = listOf(
            WebBookmark(
                id = "site_maus",
                title = "Die Maus",
                url = "https://www.wdrmaus.de/",
                domain = "wdrmaus.de",
                iconEmoji = "🐘",
                colorHex = 0xFFEA580C
            ),
            WebBookmark(
                id = "site_fragfinn",
                title = "FragFINN",
                url = "https://www.fragfinn.de/",
                domain = "fragfinn.de",
                iconEmoji = "🔍",
                colorHex = 0xFF2563EB
            ),
            WebBookmark(
                id = "site_kika",
                title = "KiKA",
                url = "https://www.kika.de/",
                domain = "kika.de",
                iconEmoji = "📺",
                colorHex = 0xFF059669
            ),
            WebBookmark(
                id = "site_blindekuh",
                title = "Blinde Kuh",
                url = "https://www.blinde-kuh.de/",
                domain = "blinde-kuh.de",
                iconEmoji = "🐮",
                colorHex = 0xFF7C3AED
            )
        )

        fun defaultButtons(): List<LauncherButton> = listOf(
            LauncherButton(
                id = "camera_default",
                type = ButtonType.CAMERA,
                title = "Kamera",
                colorHex = 0xFF2563EB
            ),
            LauncherButton(
                id = "sms_default",
                type = ButtonType.SMS,
                title = "Nachrichten",
                colorHex = 0xFF6366F1
            ),
            LauncherButton(
                id = "call_default",
                type = ButtonType.CALL,
                title = "Telefon",
                colorHex = 0xFF059669
            ),
            LauncherButton(
                id = "browser_default",
                type = ButtonType.BROWSER,
                title = "Surfen",
                colorHex = 0xFF0284C7
            ),
            LauncherButton(
                id = "photos_default",
                type = ButtonType.PHOTOS,
                title = "Fotos",
                colorHex = 0xFFDB2777
            )
        )
    }
}
