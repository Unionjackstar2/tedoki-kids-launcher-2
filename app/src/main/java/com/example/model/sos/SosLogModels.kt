package com.example.model.sos

import java.util.UUID

enum class SosCallStatus {
    INITIATED,    // Notruf getätigt
    FAILED,       // Notruf fehlgeschlagen
    NOT_ATTEMPTED // Kein Notruf getätigt
}

enum class SosSmsSendStatus {
    SENT,          // SMS versendet
    SENDING,       // SMS wird versendet
    FAILED,        // SMS fehlgeschlagen
    NOT_REQUESTED  // Keine SMS (z. B. 110/112)
}

enum class SosSmsDeliveryStatus {
    DELIVERED,     // SMS zugestellt
    NOT_DELIVERED, // SMS nicht zugestellt
    PENDING,       // Zustellung ausstehend
    NOT_REQUESTED  // Keine SMS
}

enum class SosOverallOutcome {
    SUCCESS,       // 🟢 SOS erfolgreich
    PARTIAL,       // 🟠 SOS teilweise erfolgreich
    FAILED         // 🔴 SOS fehlgeschlagen
}

data class SosLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val emergencyNumber: String = "",
    val callStatus: SosCallStatus = SosCallStatus.INITIATED,
    val smsSendStatus: SosSmsSendStatus = SosSmsSendStatus.NOT_REQUESTED,
    val smsDeliveryStatus: SosSmsDeliveryStatus = SosSmsDeliveryStatus.NOT_REQUESTED
) {
    val overallOutcome: SosOverallOutcome
        get() {
            val callOk = callStatus == SosCallStatus.INITIATED
            val hasSms = smsSendStatus != SosSmsSendStatus.NOT_REQUESTED
            val smsOk = smsSendStatus == SosSmsSendStatus.SENT && smsDeliveryStatus == SosSmsDeliveryStatus.DELIVERED
            val smsFailed = smsSendStatus == SosSmsSendStatus.FAILED || smsDeliveryStatus == SosSmsDeliveryStatus.NOT_DELIVERED

            return when {
                // All required components succeeded
                callOk && (!hasSms || smsOk) -> SosOverallOutcome.SUCCESS
                // Both call and SMS failed, or call failed and no SMS was requested
                !callOk && (hasSms && smsFailed) -> SosOverallOutcome.FAILED
                !callOk && !hasSms -> SosOverallOutcome.FAILED
                // In between / partial success
                else -> SosOverallOutcome.PARTIAL
            }
        }
}
