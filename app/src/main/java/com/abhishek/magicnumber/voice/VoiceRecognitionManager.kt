package com.abhishek.magicnumber.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents voice commands that can be recognized during the game.
 */
sealed class VoiceCommand {
    data object Yes : VoiceCommand()
    data object No : VoiceCommand()
}

/**
 * Represents the current state of voice recognition.
 */
sealed class VoiceRecognitionState {
    data object Idle : VoiceRecognitionState()
    data object Listening : VoiceRecognitionState()
    data class Error(val message: String) : VoiceRecognitionState()
    data class CommandRecognized(val command: VoiceCommand) : VoiceRecognitionState()
}

/**
 * Manages voice recognition for hands-free game interaction.
 *
 * Uses Android's SpeechRecognizer API to detect YES/NO voice commands.
 * Supports continuous recognition by auto-restarting after each command.
 */
class VoiceRecognitionManager(private val context: Context) {

    private val _state = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    val state: StateFlow<VoiceRecognitionState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningActive = false
    private var hasProcessedCommand = false  // Prevents duplicate command processing

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = VoiceRecognitionState.Listening
        }

        override fun onBeginningOfSpeech() {
            // Speech input has started
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Audio level changed - could be used for UI feedback
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Sound buffer received
        }

        override fun onEndOfSpeech() {
            // Speech input has ended
        }

        override fun onError(error: Int) {
            handleError(error)
        }

        override fun onResults(results: Bundle?) {
            // Final results - process if not already done, then restart
            processFinalResults(results)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Process partial results for faster response (but don't restart yet)
            processPartialResults(partialResults)
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Reserved for future events
        }
    }

    /**
     * Checks if speech recognition is available on this device.
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Starts listening for voice commands.
     * Recognition will auto-restart after each recognized command.
     */
    fun startListening() {
        if (!isAvailable()) {
            _state.value = VoiceRecognitionState.Error("Speech recognition not available")
            return
        }

        isListeningActive = true
        initializeSpeechRecognizer()
        startRecognition()
    }

    /**
     * Stops listening for voice commands.
     */
    fun stopListening() {
        isListeningActive = false
        speechRecognizer?.stopListening()
        _state.value = VoiceRecognitionState.Idle
    }

    /**
     * Releases all resources. Call this when the manager is no longer needed.
     */
    fun destroy() {
        isListeningActive = false
        speechRecognizer?.destroy()
        speechRecognizer = null
        _state.value = VoiceRecognitionState.Idle
    }

    private fun initializeSpeechRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
        }
    }

    private fun startRecognition() {
        hasProcessedCommand = false  // Reset for new recognition session
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            // Optimize for short single-word commands
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 300L)
            // Prefer offline recognition for faster response (if available)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }
        speechRecognizer?.startListening(intent)
    }

    /**
     * Process partial results for fast command detection.
     * Does NOT restart listening - waits for session to end naturally.
     */
    private fun processPartialResults(results: Bundle?) {
        if (hasProcessedCommand) return

        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matches.isNullOrEmpty()) return

        val command = parseCommand(matches)
        if (command != null) {
            hasProcessedCommand = true
            _state.value = VoiceRecognitionState.CommandRecognized(command)
            // Don't restart here - wait for onResults to be called
        }
    }

    /**
     * Process final results and restart listening for next command.
     * This is called when the recognition session ends.
     */
    private fun processFinalResults(results: Bundle?) {
        // If we haven't processed a command yet, try with final results
        if (!hasProcessedCommand) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val command = parseCommand(matches)
                if (command != null) {
                    hasProcessedCommand = true
                    _state.value = VoiceRecognitionState.CommandRecognized(command)
                }
            }
        }

        // Session has ended - restart for next command
        restartListeningIfActive()
    }

    private fun parseCommand(matches: List<String>): VoiceCommand? {
        // Extended keywords for better single-word recognition
        val yesKeywords = setOf("yes", "yep", "yeah", "yup", "correct", "right", "uh-huh", "sure", "okay", "ok", "affirmative")
        val noKeywords = setOf("no", "nope", "nah", "wrong", "left", "negative", "not")

        for (match in matches) {
            val lowerMatch = match.lowercase().trim()

            // Check for exact single-word matches first (most reliable)
            if (lowerMatch in yesKeywords) {
                return VoiceCommand.Yes
            }
            if (lowerMatch in noKeywords) {
                return VoiceCommand.No
            }

            // Check for word boundary matches (e.g., "I think yes" but not "now" matching "no")
            val words = lowerMatch.split(Regex("\\s+"))
            if (words.any { it in yesKeywords }) {
                return VoiceCommand.Yes
            }
            if (words.any { it in noKeywords }) {
                return VoiceCommand.No
            }
        }

        return null
    }

    private fun handleError(error: Int) {
        val errorMessage = mapErrorToMessage(error)

        if (isRecoverableError(error)) {
            restartListeningIfActive()
        } else {
            _state.value = VoiceRecognitionState.Error(errorMessage)
        }
    }

    private fun isRecoverableError(error: Int): Boolean {
        return error == SpeechRecognizer.ERROR_NO_MATCH ||
            error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
            error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY
    }

    private fun restartListeningIfActive() {
        if (isListeningActive) {
            startRecognition()
        }
    }

    private fun mapErrorToMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }
}
