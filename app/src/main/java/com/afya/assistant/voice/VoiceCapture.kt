package com.afya.assistant.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple voice capture using Vosk for offline speech-to-text.
 */
@Singleton
class VoiceCapture @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var model: Model? = null
    private var speechService: SpeechService? = null
    
    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state
    
    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript
    
    /**
     * Initialize with language model. Call once at app start.
     */
    fun initialize(): Boolean {
        return try {
            val modelPath = File(context.filesDir, "vosk-model").absolutePath
            if (File(modelPath).exists()) {
                model = Model(modelPath)
                _state.value = VoiceState.Ready
                true
            } else {
                _state.value = VoiceState.Error("Model not found")
                false
            }
        } catch (e: Exception) {
            _state.value = VoiceState.Error(e.message ?: "Init failed")
            false
        }
    }
    
    /**
     * Start listening for speech.
     */
    fun start() {
        val m = model ?: return
        
        try {
            val recognizer = Recognizer(m, 16000f)
            speechService = SpeechService(recognizer, 16000f).apply {
                startListening(object : RecognitionListener {
                    override fun onPartialResult(text: String?) {
                        text?.let { parseResult(it, partial = true) }
                    }
                    override fun onResult(text: String?) {
                        text?.let { parseResult(it, partial = false) }
                    }
                    override fun onFinalResult(text: String?) {
                        text?.let { parseResult(it, partial = false) }
                    }
                    override fun onError(e: Exception?) {
                        _state.value = VoiceState.Error(e?.message ?: "Error")
                    }
                    override fun onTimeout() {
                        stop()
                    }
                })
            }
            _state.value = VoiceState.Listening
        } catch (e: Exception) {
            _state.value = VoiceState.Error(e.message ?: "Start failed")
        }
    }
    
    /**
     * Stop listening.
     */
    fun stop() {
        speechService?.stop()
        speechService = null
        _state.value = VoiceState.Ready
    }
    
    /**
     * Clear transcript.
     */
    fun clear() {
        _transcript.value = ""
    }
    
    private fun parseResult(json: String, partial: Boolean) {
        // Vosk returns {"text": "..."} or {"partial": "..."}
        val key = if (partial) "partial" else "text"
        val regex = "\"$key\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        regex.find(json)?.groupValues?.getOrNull(1)?.let { text ->
            if (text.isNotBlank()) {
                _transcript.value = text
            }
        }
    }
}

sealed class VoiceState {
    object Idle : VoiceState()
    object Ready : VoiceState()
    object Listening : VoiceState()
    data class Error(val message: String) : VoiceState()
}
