package com.example.sms

import android.Manifest
import android.app.PendingIntent
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private const val TAG = "SmsRepository"
private const val PREFS_NAME = "tedoki_sms_prefs"
private const val KEY_MESSAGES = "saved_sms_messages"

enum class SmsSendStatus {
    PROCESSING,
    SENT,
    DELIVERED,
    FAILED
}

data class SmsMessageItem(
    val id: String = UUID.randomUUID().toString(),
    val phoneNumber: String,
    val senderName: String? = null,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isIncoming: Boolean,
    val isRead: Boolean = true,
    val status: SmsSendStatus = SmsSendStatus.DELIVERED
)

object SmsRepository {

    private val _messagesFlow = MutableStateFlow<List<SmsMessageItem>>(emptyList())
    val messagesFlow: StateFlow<List<SmsMessageItem>> = _messagesFlow.asStateFlow()

    private var isInitialized = false
    private var smsObserver: ContentObserver? = null

    /**
     * Checks if two phone numbers match, considering non-digit characters and country codes.
     */
    fun isSamePhoneNumber(num1: String, num2: String): Boolean {
        val clean1 = num1.replace(Regex("[^0-9]"), "")
        val clean2 = num2.replace(Regex("[^0-9]"), "")
        if (clean1.isEmpty() || clean2.isEmpty()) {
            return num1.trim().equals(num2.trim(), ignoreCase = true)
        }
        if (clean1 == clean2) return true

        // Compare last 8 or 9 digits if lengths are sufficient (e.g. +491701234567 vs 01701234567)
        val minLen = minOf(clean1.length, clean2.length)
        val matchLen = if (minLen >= 8) 8 else minLen
        return clean1.takeLast(matchLen) == clean2.takeLast(matchLen)
    }

    fun init(context: Context) {
        if (!isInitialized) {
            isInitialized = true
            loadMessages(context)
            registerContentObserver(context.applicationContext)
        }
        syncFromSystemSms(context.applicationContext)
    }

    private fun registerContentObserver(context: Context) {
        if (smsObserver != null) return
        try {
            smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)
                    Log.d(TAG, "ContentObserver triggered for SMS URI: $uri")
                    syncFromSystemSms(context)
                }
            }
            context.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                smsObserver!!
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error registering SMS ContentObserver", e)
        }
    }

    /**
     * Reads real SMS messages directly from Android system Telephony provider if READ_SMS permission is granted.
     */
    fun syncFromSystemSms(context: Context) {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasReadPermission) {
            Log.w(TAG, "READ_SMS permission not granted, skipping system SMS sync")
            return
        }

        try {
            val cursor = context.contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE,
                    Telephony.Sms.READ
                ),
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT 100"
            )

            cursor?.use { c ->
                val idIdx = c.getColumnIndex(Telephony.Sms._ID)
                val addrIdx = c.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = c.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = c.getColumnIndex(Telephony.Sms.DATE)
                val typeIdx = c.getColumnIndex(Telephony.Sms.TYPE)
                val readIdx = c.getColumnIndex(Telephony.Sms.READ)

                val systemMessages = mutableListOf<SmsMessageItem>()
                while (c.moveToNext()) {
                    val id = if (idIdx >= 0) c.getString(idIdx) else UUID.randomUUID().toString()
                    val address = if (addrIdx >= 0) c.getString(addrIdx) ?: "" else ""
                    val body = if (bodyIdx >= 0) c.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx >= 0) c.getLong(dateIdx) else System.currentTimeMillis()
                    val type = if (typeIdx >= 0) c.getInt(typeIdx) else Telephony.Sms.MESSAGE_TYPE_INBOX
                    val read = if (readIdx >= 0) c.getInt(readIdx) == 1 else true

                    if (address.isNotBlank() && body.isNotBlank()) {
                        val isIncoming = type == Telephony.Sms.MESSAGE_TYPE_INBOX
                        systemMessages.add(
                            SmsMessageItem(
                                id = "sys_$id",
                                phoneNumber = address,
                                body = body,
                                timestamp = date,
                                isIncoming = isIncoming,
                                isRead = if (isIncoming) read else true
                            )
                        )
                    }
                }

                if (systemMessages.isNotEmpty()) {
                    val currentList = _messagesFlow.value.toMutableList()
                    var changed = false

                    for (sysMsg in systemMessages) {
                        val exists = currentList.any { existing ->
                            existing.id == sysMsg.id || (
                                isSamePhoneNumber(existing.phoneNumber, sysMsg.phoneNumber) &&
                                existing.body == sysMsg.body &&
                                Math.abs(existing.timestamp - sysMsg.timestamp) < 5000
                            )
                        }
                        if (!exists) {
                            currentList.add(sysMsg)
                            changed = true
                        }
                    }

                    if (changed) {
                        val sorted = currentList.sortedBy { it.timestamp }
                        _messagesFlow.value = sorted
                        saveMessages(context, sorted)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from system SMS provider", e)
        }
    }

    private fun loadMessages(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = prefs.getString(KEY_MESSAGES, null)
            if (!jsonString.isNullOrBlank()) {
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<SmsMessageItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val statusStr = obj.optString("status", SmsSendStatus.DELIVERED.name)
                    val statusVal = try { SmsSendStatus.valueOf(statusStr) } catch (e: Exception) { SmsSendStatus.DELIVERED }
                    list.add(
                        SmsMessageItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            phoneNumber = obj.optString("phoneNumber", ""),
                            senderName = if (obj.has("senderName") && !obj.isNull("senderName")) obj.getString("senderName") else null,
                            body = obj.optString("body", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isIncoming = obj.optBoolean("isIncoming", false),
                            isRead = obj.optBoolean("isRead", true),
                            status = statusVal
                        )
                    )
                }
                _messagesFlow.value = list.sortedBy { it.timestamp }
            } else {
                val sampleMsg = SmsMessageItem(
                    id = "sample_welcome",
                    phoneNumber = "555-0101",
                    senderName = "Mama",
                    body = "Hallo! Du kannst mir hier jederzeit SMS schreiben. ❤️",
                    timestamp = System.currentTimeMillis() - 3600000,
                    isIncoming = true,
                    isRead = true,
                    status = SmsSendStatus.DELIVERED
                )
                _messagesFlow.value = listOf(sampleMsg)
                saveMessages(context, listOf(sampleMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading messages", e)
        }
    }

    private fun saveMessages(context: Context, list: List<SmsMessageItem>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            list.forEach { msg ->
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("phoneNumber", msg.phoneNumber)
                    put("senderName", msg.senderName)
                    put("body", msg.body)
                    put("timestamp", msg.timestamp)
                    put("isIncoming", msg.isIncoming)
                    put("isRead", msg.isRead)
                    put("status", msg.status.name)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_MESSAGES, jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving messages", e)
        }
    }

    fun updateMessageStatus(context: Context, messageId: String, newStatus: SmsSendStatus) {
        val current = _messagesFlow.value
        val updated = current.map {
            if (it.id == messageId) it.copy(status = newStatus) else it
        }
        if (updated != current) {
            _messagesFlow.value = updated
            saveMessages(context, updated)
            Log.d(TAG, "Message status updated: id=$messageId, status=$newStatus")
        }
    }

    fun retrySendMessage(context: Context, messageId: String): Boolean {
        val currentList = _messagesFlow.value
        val msg = currentList.find { it.id == messageId } ?: return false
        if (msg.isIncoming) return false

        val updated = currentList.map {
            if (it.id == messageId) it.copy(status = SmsSendStatus.PROCESSING, timestamp = System.currentTimeMillis()) else it
        }
        _messagesFlow.value = updated
        saveMessages(context, updated)

        val sentIntent = PendingIntent.getBroadcast(
            context,
            msg.id.hashCode(),
            Intent("com.example.sms.SMS_SENT").apply {
                putExtra("message_id", msg.id)
                setPackage(context.packageName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deliveredIntent = PendingIntent.getBroadcast(
            context,
            msg.id.hashCode() + 1,
            Intent("com.example.sms.SMS_DELIVERED").apply {
                putExtra("message_id", msg.id)
                setPackage(context.packageName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            @Suppress("DEPRECATION")
            val smsManager: SmsManager = SmsManager.getDefault()
            val parts = smsManager.divideMessage(msg.body)
            if (parts.size > 1) {
                val sentIntents = ArrayList<PendingIntent>()
                val deliveredIntents = ArrayList<PendingIntent>()
                for (i in 0 until parts.size) {
                    sentIntents.add(sentIntent)
                    deliveredIntents.add(deliveredIntent)
                }
                smsManager.sendMultipartTextMessage(msg.phoneNumber, null, parts, sentIntents, deliveredIntents)
            } else {
                smsManager.sendTextMessage(msg.phoneNumber, null, msg.body, sentIntent, deliveredIntent)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retry send SMS via SmsManager: ${e.message}")
            updateMessageStatus(context, msg.id, SmsSendStatus.FAILED)
            false
        }
    }

    fun sendMessage(
        context: Context,
        phoneNumber: String,
        body: String,
        senderName: String? = null
    ): Boolean {
        val cleanNumber = phoneNumber.trim()
        val cleanBody = body.trim()
        if (cleanNumber.isBlank() || cleanBody.isBlank()) return false

        val newMsg = SmsMessageItem(
            phoneNumber = cleanNumber,
            senderName = senderName,
            body = cleanBody,
            timestamp = System.currentTimeMillis(),
            isIncoming = false,
            isRead = true,
            status = SmsSendStatus.PROCESSING
        )

        val updated = _messagesFlow.value + newMsg
        _messagesFlow.value = updated
        saveMessages(context, updated)

        val sentIntent = PendingIntent.getBroadcast(
            context,
            newMsg.id.hashCode(),
            Intent("com.example.sms.SMS_SENT").apply {
                putExtra("message_id", newMsg.id)
                setPackage(context.packageName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deliveredIntent = PendingIntent.getBroadcast(
            context,
            newMsg.id.hashCode() + 1,
            Intent("com.example.sms.SMS_DELIVERED").apply {
                putExtra("message_id", newMsg.id)
                setPackage(context.packageName)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Attempt actual Android SmsManager delivery
        try {
            @Suppress("DEPRECATION")
            val smsManager: SmsManager = SmsManager.getDefault()

            val parts = smsManager.divideMessage(cleanBody)
            if (parts.size > 1) {
                val sentIntents = ArrayList<PendingIntent>()
                val deliveredIntents = ArrayList<PendingIntent>()
                for (i in 0 until parts.size) {
                    sentIntents.add(sentIntent)
                    deliveredIntents.add(deliveredIntent)
                }
                smsManager.sendMultipartTextMessage(cleanNumber, null, parts, sentIntents, deliveredIntents)
            } else {
                smsManager.sendTextMessage(cleanNumber, null, cleanBody, sentIntent, deliveredIntent)
            }
            try { Toast.makeText(context, "SMS gesendet!", Toast.LENGTH_SHORT).show() } catch (ignored: Exception) {}
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send real SMS via SmsManager: ${e.message}")
            updateMessageStatus(context, newMsg.id, SmsSendStatus.FAILED)
            try { Toast.makeText(context, "SMS-Versand fehlgeschlagen.", Toast.LENGTH_SHORT).show() } catch (ignored: Exception) {}
        }

        return true
    }

    fun receiveMessage(
        context: Context,
        phoneNumber: String,
        body: String,
        senderName: String? = null
    ) {
        val cleanNumber = phoneNumber.trim()
        val cleanBody = body.trim()
        if (cleanNumber.isBlank() || cleanBody.isBlank()) return

        val newMsg = SmsMessageItem(
            phoneNumber = cleanNumber,
            senderName = senderName,
            body = cleanBody,
            timestamp = System.currentTimeMillis(),
            isIncoming = true,
            isRead = false
        )

        val current = _messagesFlow.value
        val exists = current.any {
            isSamePhoneNumber(it.phoneNumber, cleanNumber) &&
            it.body == cleanBody &&
            Math.abs(it.timestamp - newMsg.timestamp) < 3000
        }

        if (!exists) {
            val updated = current + newMsg
            _messagesFlow.value = updated
            saveMessages(context, updated)

            try {
                Toast.makeText(
                    context,
                    "Neue Nachricht: $cleanBody",
                    Toast.LENGTH_LONG
                ).show()
            } catch (ignored: Exception) {}
        }
    }

    fun markMessagesAsReadFor(context: Context, phoneNumber: String) {
        val current = _messagesFlow.value
        val updated = current.map { msg ->
            if (isSamePhoneNumber(msg.phoneNumber, phoneNumber) && msg.isIncoming && !msg.isRead) {
                msg.copy(isRead = true)
            } else {
                msg
            }
        }
        if (updated != current) {
            _messagesFlow.value = updated
            saveMessages(context, updated)
        }
    }

    fun deleteMessagesForContact(context: Context, phoneNumber: String) {
        val current = _messagesFlow.value
        val updated = current.filter { !isSamePhoneNumber(it.phoneNumber, phoneNumber) }
        _messagesFlow.value = updated
        saveMessages(context, updated)

        // Try deleting from system SMS provider if permission and role allow it
        try {
            val cleanTail = phoneNumber.replace(Regex("[^0-9]"), "").takeLast(8)
            val selection = if (cleanTail.length >= 6) {
                "${Telephony.Sms.ADDRESS} LIKE ? OR ${Telephony.Sms.ADDRESS} = ?"
            } else {
                "${Telephony.Sms.ADDRESS} = ?"
            }
            val selectionArgs = if (cleanTail.length >= 6) {
                arrayOf("%$cleanTail", phoneNumber.trim())
            } else {
                arrayOf(phoneNumber.trim())
            }

            val deletedRows = context.contentResolver.delete(
                Telephony.Sms.CONTENT_URI,
                selection,
                selectionArgs
            )
            Log.d(TAG, "Deleted $deletedRows messages from system SMS database for number: $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete messages from system SMS provider: ${e.message}")
        }

        try { Toast.makeText(context, "Kontakt und Nachrichten gelöscht", Toast.LENGTH_SHORT).show() } catch (ignored: Exception) {}
    }

    fun getUnreadCount(): Int {
        return _messagesFlow.value.count { it.isIncoming && !it.isRead }
    }

    fun isDefaultSmsApp(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true
        } else {
            val defaultPackage = Telephony.Sms.getDefaultSmsPackage(context)
            defaultPackage == context.packageName
        }
    }

    fun createDefaultSmsIntent(context: Context): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
            roleManager?.createRequestRoleIntent(RoleManager.ROLE_SMS)
        } else {
            @Suppress("DEPRECATION")
            Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            }
        }
    }
}
