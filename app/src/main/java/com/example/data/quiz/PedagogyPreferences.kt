package com.example.data.quiz

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ButtonType
import com.example.model.LauncherButton
import com.example.model.quiz.AppPedagogyStatus
import com.example.model.quiz.QuizCategory
import com.example.model.quiz.ReviewDueState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class PedagogyPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("tedoki_pedagogy_prefs", Context.MODE_PRIVATE)

    private val prefChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        reloadAll()
    }

    private val _statusMapFlow = MutableStateFlow<Map<String, AppPedagogyStatus>>(emptyMap())
    val statusMapFlow: StateFlow<Map<String, AppPedagogyStatus>> = _statusMapFlow.asStateFlow()

    init {
        prefs.registerOnSharedPreferenceChangeListener(prefChangeListener)
        reloadAll()
    }

    private fun reloadAll() {
        val map = mutableMapOf<String, AppPedagogyStatus>()
        var migratedCallStatus: AppPedagogyStatus? = null
        for ((key, value) in prefs.all) {
            if (key.startsWith("app_status_") && value is String) {
                try {
                    val status = parseStatusJson(value)
                    map[status.appKey] = status
                    if (status.appKey.startsWith("tedoki_call_") && status.unlocked) {
                        if (migratedCallStatus == null || !migratedCallStatus.unlocked) {
                            migratedCallStatus = status.copy(
                                appKey = "tedoki_call",
                                appName = "Anrufen"
                            )
                        }
                    }
                } catch (ignored: Exception) {}
            }
        }
        if (migratedCallStatus != null && !map.containsKey("tedoki_call")) {
            map["tedoki_call"] = migratedCallStatus
            try {
                prefs.edit().putString("app_status_tedoki_call", statusToJson(migratedCallStatus)).apply()
            } catch (ignored: Exception) {}
        }
        _statusMapFlow.value = map
    }

    fun getAppStatus(appKey: String, appName: String, category: QuizCategory): AppPedagogyStatus {
        val existing = _statusMapFlow.value[appKey]
        if (existing != null) return existing

        val raw = prefs.getString("app_status_$appKey", null)
        if (raw != null) {
            try {
                val parsed = parseStatusJson(raw)
                return parsed
            } catch (ignored: Exception) {}
        }

        return AppPedagogyStatus(
            appKey = appKey,
            appName = appName,
            quizCategory = category
        )
    }

    fun saveAppStatus(status: AppPedagogyStatus) {
        val json = statusToJson(status)
        prefs.edit().putString("app_status_${status.appKey}", json).apply()
        val current = _statusMapFlow.value.toMutableMap()
        current[status.appKey] = status
        _statusMapFlow.value = current
    }

    fun recordFirstOpened(appKey: String, appName: String, category: QuizCategory): AppPedagogyStatus {
        val current = getAppStatus(appKey, appName, category)
        val updated = current.copy(firstOpened = true)
        saveAppStatus(updated)
        return updated
    }

    fun recordChildQuizPassed(appKey: String, appName: String, category: QuizCategory): AppPedagogyStatus {
        val current = getAppStatus(appKey, appName, category)
        val updated = current.copy(
            childQuizPassed = true,
            childQuizPassedAt = System.currentTimeMillis()
        )
        saveAppStatus(updated)
        return updated
    }

    fun recordParentQuizPassed(appKey: String, appName: String, category: QuizCategory): AppPedagogyStatus {
        val current = getAppStatus(appKey, appName, category)
        val updated = current.copy(
            parentQuizPassed = true,
            parentQuizPassedAt = System.currentTimeMillis()
        )
        saveAppStatus(updated)
        return updated
    }

    fun unlockAppWithPin(
        appKey: String,
        appName: String,
        category: QuizCategory,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): AppPedagogyStatus {
        val current = getAppStatus(appKey, appName, category)
        val is30Due = current.isReviewDue(currentTimeMillis) == ReviewDueState.DUE_30_DAYS
        val is90Due = current.isReviewDue(currentTimeMillis) == ReviewDueState.DUE_90_DAYS

        val updated = current.copy(
            pinVerified = true,
            unlocked = true,
            unlockedAt = current.unlockedAt ?: currentTimeMillis,
            review30Completed = if (is30Due) true else current.review30Completed,
            review30CompletedAt = if (is30Due) currentTimeMillis else current.review30CompletedAt,
            review90Completed = if (is90Due) true else current.review90Completed,
            review90CompletedAt = if (is90Due) currentTimeMillis else current.review90CompletedAt
        )
        saveAppStatus(updated)
        return updated
    }

    fun resetAppPedagogy(
        appKey: String,
        appName: String = "",
        category: QuizCategory? = null
    ) {
        val existing = _statusMapFlow.value[appKey]
        val cat = category ?: existing?.quizCategory ?: QuizCategory.FOTOS_MACHEN
        val name = if (appName.isNotBlank()) appName else (existing?.appName ?: appKey)

        val lockedStatus = AppPedagogyStatus(
            appKey = appKey,
            appName = name,
            quizCategory = cat,
            firstOpened = false,
            childQuizPassed = false,
            childQuizPassedAt = null,
            parentQuizPassed = false,
            parentQuizPassedAt = null,
            pinVerified = false,
            unlocked = false,
            unlockedAt = null,
            review30Completed = false,
            review30CompletedAt = null,
            review90Completed = false,
            review90CompletedAt = null
        )

        val editor = prefs.edit()
        editor.putString("app_status_$appKey", statusToJson(lockedStatus))
        if (appKey == "tedoki_call" || appKey.startsWith("tedoki_call_")) {
            for (key in prefs.all.keys) {
                if (key.startsWith("app_status_tedoki_call_")) {
                    editor.remove(key)
                }
            }
        }
        editor.apply()

        val current = _statusMapFlow.value.toMutableMap()
        current[appKey] = lockedStatus
        if (appKey == "tedoki_call") {
            current.keys.filter { it.startsWith("tedoki_call_") }.forEach { current.remove(it) }
        }
        _statusMapFlow.value = current
    }

    fun resetAllPedagogy() {
        prefs.edit().clear().apply()
        _statusMapFlow.value = emptyMap()
    }

    companion object {
        fun resolveAppKey(button: LauncherButton): String {
            return when (button.type) {
                ButtonType.CAMERA -> "tedoki_camera"
                ButtonType.PHOTOS -> "tedoki_photos"
                ButtonType.CALL -> "tedoki_call"
                ButtonType.BROWSER -> "tedoki_browser"
                ButtonType.SMS -> "tedoki_sms"
                ButtonType.APP -> {
                    if (button.packageName.isNotBlank()) button.packageName
                    else "app_${button.id}"
                }
            }
        }

        fun resolveQuizCategory(button: LauncherButton): QuizCategory? {
            val pkg = button.packageName.lowercase()
            val title = button.title.lowercase()

            return when {
                button.type == ButtonType.CAMERA || pkg.contains("camera") -> QuizCategory.FOTOS_MACHEN
                button.type == ButtonType.PHOTOS || pkg.contains("gallery") || pkg.contains("photos") -> QuizCategory.FOTOS_VON_ANDEREN
                button.type == ButtonType.CALL || pkg.contains("dialer") || pkg.contains("telecom") -> QuizCategory.ANRUFEN
                button.type == ButtonType.BROWSER || pkg.contains("browser") || pkg.contains("chrome") -> QuizCategory.INTERNET_SURFEN
                button.type == ButtonType.SMS || pkg.contains("mms") || pkg.contains("messaging") || pkg.contains("sms") -> QuizCategory.NACHRICHTEN
                pkg.contains("whatsapp") || title.contains("whatsapp") -> QuizCategory.WHATSAPP
                else -> null
            }
        }

        private fun statusToJson(status: AppPedagogyStatus): String {
            val json = JSONObject()
            json.put("appKey", status.appKey)
            json.put("appName", status.appName)
            json.put("quizCategory", status.quizCategory.name)
            json.put("firstOpened", status.firstOpened)
            json.put("childQuizPassed", status.childQuizPassed)
            json.put("childQuizPassedAt", status.childQuizPassedAt ?: -1L)
            json.put("parentQuizPassed", status.parentQuizPassed)
            json.put("parentQuizPassedAt", status.parentQuizPassedAt ?: -1L)
            json.put("pinVerified", status.pinVerified)
            json.put("unlocked", status.unlocked)
            json.put("unlockedAt", status.unlockedAt ?: -1L)
            json.put("review30Completed", status.review30Completed)
            json.put("review30CompletedAt", status.review30CompletedAt ?: -1L)
            json.put("review90Completed", status.review90Completed)
            json.put("review90CompletedAt", status.review90CompletedAt ?: -1L)
            return json.toString()
        }

        private fun parseStatusJson(raw: String): AppPedagogyStatus {
            val json = JSONObject(raw)
            val appKey = json.getString("appKey")
            val appName = json.optString("appName", appKey)
            val catName = json.optString("quizCategory", QuizCategory.FOTOS_MACHEN.name)
            val cat = try {
                QuizCategory.valueOf(catName)
            } catch (e: Exception) {
                QuizCategory.FOTOS_MACHEN
            }
            val firstOpened = json.optBoolean("firstOpened", false)
            val childQuizPassed = json.optBoolean("childQuizPassed", false)
            val childQuizPassedAt = json.optLong("childQuizPassedAt", -1L).takeIf { it != -1L }
            val parentQuizPassed = json.optBoolean("parentQuizPassed", false)
            val parentQuizPassedAt = json.optLong("parentQuizPassedAt", -1L).takeIf { it != -1L }
            val pinVerified = json.optBoolean("pinVerified", false)
            val unlocked = json.optBoolean("unlocked", false)
            val unlockedAt = json.optLong("unlockedAt", -1L).takeIf { it != -1L }
            val review30Completed = json.optBoolean("review30Completed", false)
            val review30CompletedAt = json.optLong("review30CompletedAt", -1L).takeIf { it != -1L }
            val review90Completed = json.optBoolean("review90Completed", false)
            val review90CompletedAt = json.optLong("review90CompletedAt", -1L).takeIf { it != -1L }

            return AppPedagogyStatus(
                appKey = appKey,
                appName = appName,
                quizCategory = cat,
                firstOpened = firstOpened,
                childQuizPassed = childQuizPassed,
                childQuizPassedAt = childQuizPassedAt,
                parentQuizPassed = parentQuizPassed,
                parentQuizPassedAt = parentQuizPassedAt,
                pinVerified = pinVerified,
                unlocked = unlocked,
                unlockedAt = unlockedAt,
                review30Completed = review30Completed,
                review30CompletedAt = review30CompletedAt,
                review90Completed = review90Completed,
                review90CompletedAt = review90CompletedAt
            )
        }
    }
}
