package com.example.data.sos

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.sos.SosCallStatus
import com.example.model.sos.SosLogEntry
import com.example.model.sos.SosSmsDeliveryStatus
import com.example.model.sos.SosSmsSendStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

object SosLogRepository {

    private const val TAG = "SosLogRepository"
    private const val PREFS_NAME = "tedoki_sos_log_prefs"
    private const val KEY_ENTRIES = "sos_log_entries"
    private const val RETENTION_PERIOD_MILLIS = 90L * 24 * 60 * 60 * 1000L // 90 Tage

    private val _logsFlow = MutableStateFlow<List<SosLogEntry>>(emptyList())
    val logsFlow: StateFlow<List<SosLogEntry>> = _logsFlow.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            isInitialized = true
            loadLogs(context)
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun loadLogs(context: Context): List<SosLogEntry> {
        return try {
            val prefs = getPrefs(context)
            val jsonString = prefs.getString(KEY_ENTRIES, null)
            val now = System.currentTimeMillis()
            val cutoff = now - RETENTION_PERIOD_MILLIS

            val list = mutableListOf<SosLogEntry>()
            if (!jsonString.isNullOrBlank()) {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val timestamp = obj.optLong("timestamp", now)

                    // Automatische Bereinigung von Einträgen älter als 90 Tage
                    if (timestamp >= cutoff) {
                        list.add(
                            SosLogEntry(
                                id = obj.optString("id"),
                                timestamp = timestamp,
                                emergencyNumber = obj.optString("emergencyNumber", ""),
                                callStatus = try {
                                    SosCallStatus.valueOf(obj.optString("callStatus", SosCallStatus.INITIATED.name))
                                } catch (e: Exception) {
                                    SosCallStatus.INITIATED
                                },
                                smsSendStatus = try {
                                    SosSmsSendStatus.valueOf(obj.optString("smsSendStatus", SosSmsSendStatus.NOT_REQUESTED.name))
                                } catch (e: Exception) {
                                    SosSmsSendStatus.NOT_REQUESTED
                                },
                                smsDeliveryStatus = try {
                                    SosSmsDeliveryStatus.valueOf(obj.optString("smsDeliveryStatus", SosSmsDeliveryStatus.NOT_REQUESTED.name))
                                } catch (e: Exception) {
                                    SosSmsDeliveryStatus.NOT_REQUESTED
                                }
                            )
                        )
                    }
                }
            }
            val sorted = list.sortedByDescending { it.timestamp }
            _logsFlow.value = sorted
            // Falls veraltete Einträge gelöscht wurden, persistieren
            if (jsonString != null && list.size != JSONArray(jsonString).length()) {
                saveLogs(context, sorted)
            }
            sorted
        } catch (e: Exception) {
            Log.e(TAG, "Error loading SOS logs", e)
            emptyList()
        }
    }

    private fun saveLogs(context: Context, list: List<SosLogEntry>) {
        try {
            val array = JSONArray()
            list.forEach { entry ->
                val obj = JSONObject().apply {
                    put("id", entry.id)
                    put("timestamp", entry.timestamp)
                    put("emergencyNumber", entry.emergencyNumber)
                    put("callStatus", entry.callStatus.name)
                    put("smsSendStatus", entry.smsSendStatus.name)
                    put("smsDeliveryStatus", entry.smsDeliveryStatus.name)
                }
                array.put(obj)
            }
            getPrefs(context).edit().putString(KEY_ENTRIES, array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving SOS logs", e)
        }
    }

    fun addOrUpdateEntry(context: Context, entry: SosLogEntry) {
        init(context)
        val current = _logsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            current[index] = entry
        } else {
            current.add(0, entry)
        }
        val sorted = current.sortedByDescending { it.timestamp }
        _logsFlow.value = sorted
        saveLogs(context, sorted)
    }

    fun updateSmsStatus(
        context: Context,
        id: String,
        sendStatus: SosSmsSendStatus,
        deliveryStatus: SosSmsDeliveryStatus
    ) {
        init(context)
        val current = _logsFlow.value
        val updated = current.map {
            if (it.id == id) {
                it.copy(
                    smsSendStatus = sendStatus,
                    smsDeliveryStatus = deliveryStatus
                )
            } else {
                it
            }
        }
        _logsFlow.value = updated
        saveLogs(context, updated)
    }

    fun updateCallStatus(
        context: Context,
        id: String,
        callStatus: SosCallStatus
    ) {
        init(context)
        val current = _logsFlow.value
        val updated = current.map {
            if (it.id == id) {
                it.copy(callStatus = callStatus)
            } else {
                it
            }
        }
        _logsFlow.value = updated
        saveLogs(context, updated)
    }

    fun deleteEntry(context: Context, id: String) {
        init(context)
        val current = _logsFlow.value.filter { it.id != id }
        _logsFlow.value = current
        saveLogs(context, current)
    }

    fun clearAllLogs(context: Context) {
        _logsFlow.value = emptyList()
        getPrefs(context).edit().remove(KEY_ENTRIES).apply()
    }
}
