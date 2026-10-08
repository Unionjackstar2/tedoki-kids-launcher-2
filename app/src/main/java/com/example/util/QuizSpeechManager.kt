package com.example.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

/**
 * Text-to-Speech manager tailored for the pedagoical quizzes in TEDOKI Kids Launcher.
 * Features:
 * - Child-friendly speech rate (0.92f) and warm pitch for high comprehension
 * - German language support with fallback to device default
 * - Real-time speech status observation for animated Vorlesen/Stopp button states
 * - Safe lifecycle management
 */
class QuizSpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var pendingTextToSpeak: String? = null
    private val _isSpeaking = mutableStateOf(false)
    val isSpeaking: State<Boolean> = _isSpeaking

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.let { engine ->
                val result = engine.setLanguage(Locale.GERMAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine.setLanguage(Locale.getDefault())
                }
                engine.setSpeechRate(0.92f)
                engine.setPitch(1.05f)

                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                    }
                })

                pendingTextToSpeak?.let { text ->
                    pendingTextToSpeak = null
                    speak(text)
                }
            }
        }
    }

    /**
     * Reads aloud the given text. If not yet initialized, queues it to be spoken once ready.
     */
    fun speak(text: String) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return

        if (!isInitialized) {
            pendingTextToSpeak = cleanText
            return
        }

        try {
            tts?.stop()
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "quiz_${System.currentTimeMillis()}")
            }
            val result = tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "quiz_utterance")
            if (result == TextToSpeech.SUCCESS) {
                _isSpeaking.value = true
            } else {
                _isSpeaking.value = false
            }
        } catch (e: Exception) {
            _isSpeaking.value = false
        }
    }

    /**
     * Stops any currently ongoing speech.
     */
    fun stop() {
        pendingTextToSpeak = null
        try {
            tts?.stop()
        } catch (ignored: Exception) {}
        _isSpeaking.value = false
    }

    /**
     * Completely releases the TTS engine resources.
     */
    fun shutdown() {
        pendingTextToSpeak = null
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (ignored: Exception) {}
        tts = null
        _isSpeaking.value = false
    }
}
