package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.LauncherContact
import com.example.model.LauncherSettings
import com.example.model.LayoutStyle
import com.example.model.ParentAccount
import com.example.model.ThemeMode
import com.example.model.WebBookmark
import com.example.util.KeystoreHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class LauncherPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("assistive_launcher_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<LauncherSettings> = _settingsFlow.asStateFlow()

    fun getSettings(): LauncherSettings = _settingsFlow.value

    fun updateSettings(newSettings: LauncherSettings) {
        saveSettings(newSettings)
        _settingsFlow.value = newSettings
    }

    fun resetLauncherToInitialSetup() {
        prefs.edit().clear().apply()
        _settingsFlow.value = loadSettings()
    }

    private fun loadSettings(): LauncherSettings {
        val isSetupComplete = prefs.getBoolean(KEY_SETUP_COMPLETE, false)
        val childName = prefs.getString(KEY_CHILD_NAME, "") ?: ""
        val childAge = prefs.getInt(KEY_CHILD_AGE, 8)
        val childAvatar = prefs.getString(KEY_CHILD_AVATAR, "lion") ?: "lion"
        val parentEmail = prefs.getString(KEY_PARENT_EMAIL, "") ?: ""
        val parentName = prefs.getString(KEY_PARENT_NAME, "") ?: ""
        val parentPhoto = prefs.getString(KEY_PARENT_PHOTO, null)
        val isParentConnected = prefs.getBoolean(KEY_PARENT_CONNECTED, false)
        val rawPin = prefs.getString(KEY_PIN, "1234") ?: "1234"
        val pin = if (rawPin.length <= 6 && rawPin.all { it.isDigit() }) {
            // Legacy plaintext PIN: migrate to encrypted
            val encrypted = KeystoreHelper.encrypt(rawPin)
            prefs.edit().putString(KEY_PIN, encrypted).apply()
            rawPin
        } else {
            val decrypted = KeystoreHelper.decrypt(rawPin)
            if (decrypted.isEmpty()) "1234" else decrypted
        }
        val layoutStyleName = prefs.getString(KEY_LAYOUT_STYLE, LayoutStyle.GRID.name)
        val layoutStyle = try {
            LayoutStyle.valueOf(layoutStyleName ?: LayoutStyle.GRID.name)
        } catch (e: Exception) {
            LayoutStyle.GRID
        }
        val systemLang = java.util.Locale.getDefault().language
        val defaultLang = if (systemLang.startsWith("de")) "de" else "en"
        val language = prefs.getString(KEY_LANGUAGE, defaultLang) ?: defaultLang

        val buttonsJson = prefs.getString(KEY_BUTTONS_JSON, null)
        val buttons = if (buttonsJson != null) {
            parseButtons(buttonsJson)
        } else {
            LauncherSettings.defaultButtons()
        }

        val contactsJson = prefs.getString(KEY_CONTACTS_JSON, null)
        val contacts = if (contactsJson != null) {
            parseContacts(contactsJson)
        } else {
            LauncherSettings.defaultContacts()
        }

        val websitesJson = prefs.getString(KEY_WEBSITES_JSON, null)
        val allowedWebsites = if (websitesJson != null) {
            parseWebsites(websitesJson)
        } else {
            LauncherSettings.defaultWebsites()
        }

        val emergencyNumber = prefs.getString(KEY_EMERGENCY_NUMBER, "112") ?: "112"
        val emergencyContactName = prefs.getString(KEY_EMERGENCY_NAME, "Emergency / Notruf") ?: "Emergency / Notruf"
        val showSosButton = prefs.getBoolean(KEY_SHOW_SOS, true)
        val sendSosLocationSms = prefs.getBoolean(KEY_SEND_SOS_LOCATION_SMS, true)
        val enableClockClick = prefs.getBoolean(KEY_ENABLE_CLOCK_CLICK, true)
        val enableCalendarClick = prefs.getBoolean(KEY_ENABLE_CALENDAR_CLICK, true)
        val enableKeypadDialer = prefs.getBoolean(KEY_ENABLE_KEYPAD_DIALER, false)
        val enableAndroidContacts = prefs.getBoolean(KEY_ENABLE_ANDROID_CONTACTS, false)
        val allowedContactAccounts = stringToAccountList(prefs.getString(KEY_ALLOWED_CONTACT_ACCOUNTS, "[]") ?: "[]")
        val themeModeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (e: Exception) { ThemeMode.SYSTEM }
        val enableLockscreen = prefs.getBoolean(KEY_ENABLE_LOCKSCREEN, false)
        val lockscreenClockStyleStr = prefs.getString(KEY_LOCKSCREEN_CLOCK_STYLE, com.example.model.LockscreenClockStyle.ANALOG_CONNI.name) ?: com.example.model.LockscreenClockStyle.ANALOG_CONNI.name
        val lockscreenClockStyle = try { com.example.model.LockscreenClockStyle.valueOf(lockscreenClockStyleStr) } catch (e: Exception) { com.example.model.LockscreenClockStyle.ANALOG_CONNI }
        val lockscreenPresetBackground = prefs.getString(KEY_LOCKSCREEN_PRESET_BG, "soft_blue") ?: "soft_blue"
        val lockscreenCustomBackgroundUri = prefs.getString(KEY_LOCKSCREEN_CUSTOM_BG_URI, null)
        val lockscreenRequireMathProblem = prefs.getBoolean(KEY_LOCKSCREEN_REQUIRE_MATH, false)

        return LauncherSettings(
            isSetupComplete = isSetupComplete,
            childName = childName,
            childAge = childAge,
            childAvatar = childAvatar,
            parentAccount = ParentAccount(
                email = parentEmail,
                displayName = parentName,
                photoUrl = parentPhoto,
                isConnected = isParentConnected
            ),
            pin = pin,
            layoutStyle = layoutStyle,
            language = language,
            buttons = buttons,
            contacts = contacts,
            emergencyNumber = emergencyNumber,
            emergencyContactName = emergencyContactName,
            showSosButton = showSosButton,
            sendSosLocationSms = sendSosLocationSms,
            enableClockClick = enableClockClick,
            enableCalendarClick = enableCalendarClick,
            enableKeypadDialer = enableKeypadDialer,
            enableAndroidContacts = enableAndroidContacts,
            allowedContactAccounts = allowedContactAccounts,
            allowedWebsites = allowedWebsites,
            themeMode = themeMode,
            enableLockscreen = enableLockscreen,
            lockscreenClockStyle = lockscreenClockStyle,
            lockscreenPresetBackground = lockscreenPresetBackground,
            lockscreenCustomBackgroundUri = lockscreenCustomBackgroundUri,
            lockscreenRequireMathProblem = lockscreenRequireMathProblem
        )
    }

    private fun saveSettings(settings: LauncherSettings) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_SETUP_COMPLETE, settings.isSetupComplete)
        editor.putString(KEY_CHILD_NAME, settings.childName)
        editor.putInt(KEY_CHILD_AGE, settings.childAge)
        editor.putString(KEY_CHILD_AVATAR, settings.childAvatar)
        editor.putString(KEY_PARENT_EMAIL, settings.parentAccount.email)
        editor.putString(KEY_PARENT_NAME, settings.parentAccount.displayName)
        editor.putString(KEY_PARENT_PHOTO, settings.parentAccount.photoUrl)
        editor.putBoolean(KEY_PARENT_CONNECTED, settings.parentAccount.isConnected)
        editor.putString(KEY_PIN, KeystoreHelper.encrypt(settings.pin))
        editor.putString(KEY_LAYOUT_STYLE, settings.layoutStyle.name)
        editor.putString(KEY_LANGUAGE, settings.language)
        editor.putString(KEY_BUTTONS_JSON, buttonsToJson(settings.buttons))
        editor.putString(KEY_CONTACTS_JSON, contactsToJson(settings.contacts))
        editor.putString(KEY_WEBSITES_JSON, websitesToJson(settings.allowedWebsites))
        editor.putString(KEY_EMERGENCY_NUMBER, settings.emergencyNumber)
        editor.putString(KEY_EMERGENCY_NAME, settings.emergencyContactName)
        editor.putBoolean(KEY_SHOW_SOS, settings.showSosButton)
        editor.putBoolean(KEY_SEND_SOS_LOCATION_SMS, settings.sendSosLocationSms)
        editor.putBoolean(KEY_ENABLE_CLOCK_CLICK, settings.enableClockClick)
        editor.putBoolean(KEY_ENABLE_CALENDAR_CLICK, settings.enableCalendarClick)
        editor.putBoolean(KEY_ENABLE_KEYPAD_DIALER, settings.enableKeypadDialer)
        editor.putBoolean(KEY_ENABLE_ANDROID_CONTACTS, settings.enableAndroidContacts)
        editor.putString(KEY_ALLOWED_CONTACT_ACCOUNTS, accountListToString(settings.allowedContactAccounts))
        editor.putString(KEY_THEME_MODE, settings.themeMode.name)
        editor.putBoolean(KEY_ENABLE_LOCKSCREEN, settings.enableLockscreen)
        editor.putString(KEY_LOCKSCREEN_CLOCK_STYLE, settings.lockscreenClockStyle.name)
        editor.putString(KEY_LOCKSCREEN_PRESET_BG, settings.lockscreenPresetBackground)
        editor.putString(KEY_LOCKSCREEN_CUSTOM_BG_URI, settings.lockscreenCustomBackgroundUri)
        editor.putBoolean(KEY_LOCKSCREEN_REQUIRE_MATH, settings.lockscreenRequireMathProblem)
        editor.apply()
    }

    private fun buttonsToJson(buttons: List<LauncherButton>): String {
        val jsonArray = JSONArray()
        for (button in buttons) {
            val obj = JSONObject()
            obj.put("id", button.id)
            obj.put("type", button.type.name)
            obj.put("title", button.title)
            obj.put("phoneNumber", button.phoneNumber)
            obj.put("packageName", button.packageName)
            obj.put("photoUri", button.photoUri ?: "")
            obj.put("colorHex", button.colorHex)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun parseButtons(jsonStr: String): List<LauncherButton> {
        val list = mutableListOf<LauncherButton>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val typeName = obj.optString("type", ButtonType.CAMERA.name)
                val type = try { ButtonType.valueOf(typeName) } catch (e: Exception) { ButtonType.CAMERA }
                val photoUriStr = obj.optString("photoUri", "")
                list.add(
                    LauncherButton(
                        id = obj.optString("id", "btn_$i"),
                        type = type,
                        title = obj.optString("title", "Button"),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        packageName = obj.optString("packageName", ""),
                        photoUri = if (photoUriStr.isNotEmpty()) photoUriStr else null,
                        colorHex = obj.optLong("colorHex", 0xFF2563EBL)
                    )
                )
            }
        } catch (e: Exception) {
            return LauncherSettings.defaultButtons()
        }
        return if (list.isEmpty()) LauncherSettings.defaultButtons() else list
    }

    private fun websitesToJson(websites: List<WebBookmark>): String {
        val jsonArray = JSONArray()
        for (site in websites) {
            val obj = JSONObject()
            obj.put("id", site.id)
            obj.put("title", site.title)
            obj.put("url", site.url)
            obj.put("domain", site.domain)
            obj.put("iconEmoji", site.iconEmoji)
            obj.put("colorHex", site.colorHex)
            obj.put("isEnabled", site.isEnabled)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun parseWebsites(jsonStr: String): List<WebBookmark> {
        val list = mutableListOf<WebBookmark>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    WebBookmark(
                        id = obj.optString("id", "site_$i"),
                        title = obj.optString("title", "Website"),
                        url = obj.optString("url", "https://"),
                        domain = obj.optString("domain", ""),
                        iconEmoji = obj.optString("iconEmoji", "🌐"),
                        colorHex = obj.optLong("colorHex", 0xFF0284C7L),
                        isEnabled = obj.optBoolean("isEnabled", true)
                    )
                )
            }
        } catch (e: Exception) {
            return LauncherSettings.defaultWebsites()
        }
        return if (list.isEmpty()) LauncherSettings.defaultWebsites() else list
    }

    private fun contactsToJson(contacts: List<LauncherContact>): String {
        val jsonArray = JSONArray()
        for (contact in contacts) {
            val obj = JSONObject()
            obj.put("id", contact.id)
            obj.put("name", contact.name)
            obj.put("phoneNumber", contact.phoneNumber)
            obj.put("photoUri", contact.photoUri ?: "")
            obj.put("colorHex", contact.colorHex)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun parseContacts(jsonStr: String): List<LauncherContact> {
        val list = mutableListOf<LauncherContact>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val photoUriStr = obj.optString("photoUri", "")
                list.add(
                    LauncherContact(
                        id = obj.optString("id", "contact_$i"),
                        name = obj.optString("name", "Kontakt"),
                        phoneNumber = obj.optString("phoneNumber", ""),
                        photoUri = if (photoUriStr.isNotEmpty()) photoUriStr else null,
                        colorHex = obj.optLong("colorHex", 0xFF2563EBL)
                    )
                )
            }
        } catch (e: Exception) {
            return LauncherSettings.defaultContacts()
        }
        return if (list.isEmpty()) LauncherSettings.defaultContacts() else list
    }

    private fun accountListToString(accounts: List<String>): String {
        val jsonArray = JSONArray()
        for (acc in accounts) {
            jsonArray.put(acc)
        }
        return jsonArray.toString()
    }

    private fun stringToAccountList(jsonStr: String): List<String> {
        val list = mutableListOf<String>()
        if (jsonStr.isBlank()) return list
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val acc = jsonArray.optString(i)
                if (acc.isNotBlank()) {
                    list.add(acc)
                }
            }
        } catch (e: Exception) {
            // return empty list
        }
        return list
    }

    companion object {
        private const val KEY_SETUP_COMPLETE = "key_setup_complete"
        private const val KEY_CHILD_NAME = "key_child_name"
        private const val KEY_CHILD_AGE = "key_child_age"
        private const val KEY_CHILD_AVATAR = "key_child_avatar"
        private const val KEY_PARENT_EMAIL = "key_parent_email"
        private const val KEY_PARENT_NAME = "key_parent_name"
        private const val KEY_PARENT_PHOTO = "key_parent_photo"
        private const val KEY_PARENT_CONNECTED = "key_parent_connected"
        private const val KEY_PIN = "key_pin"
        private const val KEY_LAYOUT_STYLE = "key_layout_style"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_BUTTONS_JSON = "key_buttons_json"
        private const val KEY_CONTACTS_JSON = "key_contacts_json"
        private const val KEY_WEBSITES_JSON = "key_websites_json"
        private const val KEY_EMERGENCY_NUMBER = "key_emergency_number"
        private const val KEY_EMERGENCY_NAME = "key_emergency_name"
        private const val KEY_SHOW_SOS = "key_show_sos"
        private const val KEY_SEND_SOS_LOCATION_SMS = "key_send_sos_location_sms"
        private const val KEY_ENABLE_CLOCK_CLICK = "key_enable_clock_click"
        private const val KEY_ENABLE_CALENDAR_CLICK = "key_enable_calendar_click"
        private const val KEY_ENABLE_KEYPAD_DIALER = "key_enable_keypad_dialer"
        private const val KEY_ENABLE_ANDROID_CONTACTS = "key_enable_android_contacts"
        private const val KEY_ALLOWED_CONTACT_ACCOUNTS = "key_allowed_contact_accounts"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_ENABLE_LOCKSCREEN = "key_enable_lockscreen"
        private const val KEY_LOCKSCREEN_CLOCK_STYLE = "key_lockscreen_clock_style"
        private const val KEY_LOCKSCREEN_PRESET_BG = "key_lockscreen_preset_bg"
        private const val KEY_LOCKSCREEN_CUSTOM_BG_URI = "key_lockscreen_custom_bg_uri"
        private const val KEY_LOCKSCREEN_REQUIRE_MATH = "key_lockscreen_require_math"
    }
}
