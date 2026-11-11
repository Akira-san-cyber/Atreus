package com.iistable.atreus.ai
import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    init { tts = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) tts?.language = Locale.getDefault() } }
    fun speak(text: String) { tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "") }
    fun shutdown() { tts?.shutdown() }
}
