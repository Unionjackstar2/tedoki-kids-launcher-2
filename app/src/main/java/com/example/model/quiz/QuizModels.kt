package com.example.model.quiz

enum class QuizCategory(val key: String, val titleDe: String) {
    ANRUFEN("anrufen", "Anrufen & Kontakte"),
    UNBEKANNTE_NUMMERN("unbekannte_nummern", "Unbekannte Rufnummern"),
    NACHRICHTEN("nachrichten", "Nachrichten & Chats"),
    WHATSAPP("whatsapp", "WhatsApp"),
    FOTOS_MACHEN("fotos_machen", "Fotos machen & Kamera"),
    FOTOS_VON_ANDEREN("fotos_von_anderen", "Fotos von anderen"),
    SPRACHNACHRICHTEN("sprachnachrichten", "Sprachnachrichten"),
    BLOCKIEREN("blockieren", "Blockieren & Schutz"),
    PERSOENLICHE_DATEN("persoenliche_daten", "Persönliche Daten"),
    BEVOR_VERSCHICKEN("bevor_verschicken", "Bevor ich etwas verschicke"),
    INTERNET_SURFEN("internet_surfen", "Sicheres Surfen & Internet")
}

data class QuizOption(
    val id: String,
    val text: String
)

data class QuizQuestion(
    val id: String,
    val title: String,
    val situation: String,
    val options: List<QuizOption>,
    val correctIndex: Int,
    val explanation: String
)

data class QuizModule(
    val category: QuizCategory,
    val title: String,
    val childIntroText: String,
    val childQuestions: List<QuizQuestion>,
    val childSummaryPoints: List<String>,
    val parentIntroText: String,
    val parentFocusTopics: List<String>,
    val parentQuestions: List<QuizQuestion>,
    val parentTips: List<String>
)

enum class ReviewDueState {
    NONE,
    DUE_30_DAYS,
    DUE_90_DAYS
}

data class AppPedagogyStatus(
    val appKey: String,
    val appName: String,
    val quizCategory: QuizCategory,
    val firstOpened: Boolean = false,
    val childQuizPassed: Boolean = false,
    val childQuizPassedAt: Long? = null,
    val parentQuizPassed: Boolean = false,
    val parentQuizPassedAt: Long? = null,
    val pinVerified: Boolean = false,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val review30Completed: Boolean = false,
    val review30CompletedAt: Long? = null,
    val review90Completed: Boolean = false,
    val review90CompletedAt: Long? = null,
    val reviewSchedule: List<Int> = listOf(30, 90)
) {
    fun isReviewDue(currentTimeMillis: Long = System.currentTimeMillis()): ReviewDueState {
        if (!unlocked || unlockedAt == null) return ReviewDueState.NONE

        val daysSinceUnlock = (currentTimeMillis - unlockedAt) / (1000L * 60 * 60 * 24)

        if (daysSinceUnlock >= 90 && !review90Completed) {
            return ReviewDueState.DUE_90_DAYS
        }
        if (daysSinceUnlock >= 30 && !review30Completed) {
            return ReviewDueState.DUE_30_DAYS
        }
        return ReviewDueState.NONE
    }

    fun isFullyUnlocked(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return unlocked && isReviewDue(currentTimeMillis) == ReviewDueState.NONE
    }
}
