package com.example.model.call

/**
 * Direction/Type of call in history:
 * - INCOMING: Incoming answered call
 * - OUTGOING: Outgoing placed call
 * - MISSED: Incoming unanswered/missed call
 */
enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

/**
 * Data model for a call history entry.
 */
data class CallHistoryEntry(
    val id: String,
    val phoneNumber: String,
    val contactName: String? = null,
    val photoUri: String? = null,
    val direction: CallDirection,
    val timestamp: Long,
    val durationSeconds: Int = 0
)
