package com.example.data.call

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.call.CallDirection
import com.example.model.call.CallHistoryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistence repository for call history (inbound, outbound, missed calls).
 * Automatically logs calls from Telecom / CallManager and provides real-time Flow
 * with clean JSON storage in SharedPreferences.
 */
object CallHistoryRepository {

    private const val TAG = "CallHistoryRepository"
    private const val PREFS_NAME = "tedoki_call_history_prefs"
    private const val KEY_CALL_LOGS = "call_history_entries"
    private const val MAX_LOGS = 150

    private val _historyFlow = MutableStateFlow<List<CallHistoryEntry>>(emptyList())
    val historyFlow: StateFlow<List<CallHistoryEntry>> = _historyFlow.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            isInitialized = true
            loadHistory(context)
        }
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun loadHistory(context: Context): List<CallHistoryEntry> {
        return try {
            val prefs = getPrefs(context)
            val jsonString = prefs.getString(KEY_CALL_LOGS, null)
            val list = mutableListOf<CallHistoryEntry>()
            if (!jsonString.isNullOrBlank()) {
                val array = JSONArray(jsonString)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CallHistoryEntry(
                            id = obj.optString("id", System.currentTimeMillis().toString()),
                            phoneNumber = obj.optString("phoneNumber", ""),
                            contactName = if (obj.has("contactName") && !obj.isNull("contactName")) obj.getString("contactName") else null,
                            photoUri = if (obj.has("photoUri") && !obj.isNull("photoUri")) obj.getString("photoUri") else null,
                            direction = try {
                                CallDirection.valueOf(obj.optString("direction", CallDirection.OUTGOING.name))
                            } catch (e: Exception) {
                                CallDirection.OUTGOING
                            },
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            durationSeconds = obj.optInt("durationSeconds", 0)
                        )
                    )
                }
            }
            // Sort newest first
            list.sortByDescending { it.timestamp }
            _historyFlow.value = list
            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load call history", e)
            emptyList()
        }
    }

    fun addCall(
        context: Context,
        phoneNumber: String,
        contactName: String? = null,
        photoUri: String? = null,
        direction: CallDirection,
        durationSeconds: Int = 0,
        timestamp: Long = System.currentTimeMillis()
    ): CallHistoryEntry {
        val entry = CallHistoryEntry(
            id = "call_${System.currentTimeMillis()}_${(1000..9999).random()}",
            phoneNumber = phoneNumber.trim(),
            contactName = contactName?.trim()?.ifBlank { null },
            photoUri = photoUri,
            direction = direction,
            timestamp = timestamp,
            durationSeconds = durationSeconds
        )

        val currentList = _historyFlow.value.toMutableList()
        currentList.add(0, entry)
        if (currentList.size > MAX_LOGS) {
            currentList.removeAt(currentList.lastIndex)
        }

        saveList(context, currentList)
        return entry
    }

    fun updateCallDuration(context: Context, callId: String, durationSeconds: Int) {
        val currentList = _historyFlow.value.map { entry ->
            if (entry.id == callId) {
                entry.copy(durationSeconds = durationSeconds)
            } else {
                entry
            }
        }
        saveList(context, currentList)
    }

    fun deleteCall(context: Context, callId: String) {
        val currentList = _historyFlow.value.filter { it.id != callId }
        saveList(context, currentList)
    }

    fun clearAll(context: Context) {
        saveList(context, emptyList())
    }

    private fun saveList(context: Context, list: List<CallHistoryEntry>) {
        try {
            val array = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("phoneNumber", item.phoneNumber)
                    put("contactName", item.contactName)
                    put("photoUri", item.photoUri)
                    put("direction", item.direction.name)
                    put("timestamp", item.timestamp)
                    put("durationSeconds", item.durationSeconds)
                }
                array.put(obj)
            }
            getPrefs(context).edit().putString(KEY_CALL_LOGS, array.toString()).apply()
            _historyFlow.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save call history", e)
        }
    }
}
