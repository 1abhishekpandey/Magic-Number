package com.abhishek.magicnumber.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhishek.magicnumber.data.CardGenerator
import com.abhishek.magicnumber.data.PreferencesRepository
import com.abhishek.magicnumber.model.GamePhase
import com.abhishek.magicnumber.model.GameState
import com.abhishek.magicnumber.voice.VoiceCommand
import com.abhishek.magicnumber.voice.VoiceRecognitionManager
import com.abhishek.magicnumber.voice.VoiceRecognitionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for the game screen.
 *
 * Manages game state and handles user interactions during gameplay.
 */
class GameViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val voiceRecognitionManager: VoiceRecognitionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    init {
        // Auto-start game when ViewModel is created
        startGame()
        observeVoiceState()
    }

    /**
     * Starts a new game by generating cards based on current settings.
     */
    fun startGame() {
        viewModelScope.launch {
            val settings = preferencesRepository.settingsFlow.first()
            val cards = CardGenerator.generateCards(settings.maxNumber)

            _uiState.value = GameState(
                cards = cards,
                currentCardIndex = 0,
                responses = emptyList(),
                phase = GamePhase.InProgress,
                numberLayout = settings.numberLayout
            )
        }
    }

    /**
     * Records user's swipe response and advances to next card.
     *
     * @param isYes true if user swiped right (number was on card)
     */
    fun onSwipe(isYes: Boolean) {
        val current = _uiState.value
        if (current.phase != GamePhase.InProgress) return

        val newResponses = current.responses + isYes
        val nextIndex = current.currentCardIndex + 1

        _uiState.value = if (nextIndex >= current.cards.size) {
            // All cards shown, calculate and show calculating animation
            stopVoiceControl()  // Stop listening when game ends
            val result = CardGenerator.calculateResult(newResponses, current.cards)
            current.copy(
                responses = newResponses,
                phase = GamePhase.Calculating(result)
            )
        } else {
            // Move to next card
            current.copy(
                currentCardIndex = nextIndex,
                responses = newResponses
            )
        }
    }

    /**
     * Called when user clicks the Reveal button.
     */
    fun onRevealClick() {
        val current = _uiState.value
        val calculatingPhase = current.phase as? GamePhase.Calculating ?: return

        _uiState.value = current.copy(
            phase = GamePhase.Revealing(calculatingPhase.number)
        )
    }

    /**
     * Called when reveal animation completes.
     */
    fun onRevealComplete() {
        val current = _uiState.value
        val revealingPhase = current.phase as? GamePhase.Revealing ?: return

        _uiState.value = current.copy(
            phase = GamePhase.Complete(revealingPhase.number)
        )
    }

    /**
     * Resets game to initial state.
     */
    fun resetGame() {
        _uiState.value = GameState()
    }

    /**
     * Observes voice recognition state and triggers swipes on voice commands.
     */
    private fun observeVoiceState() {
        viewModelScope.launch {
            voiceRecognitionManager.state.collect { voiceState ->
                _uiState.value = _uiState.value.copy(voiceState = voiceState)

                if (voiceState is VoiceRecognitionState.CommandRecognized) {
                    handleVoiceCommand(voiceState.command)
                }
            }
        }
    }

    /**
     * Handles a recognized voice command by triggering the appropriate swipe.
     */
    private fun handleVoiceCommand(command: VoiceCommand) {
        viewModelScope.launch {
            val commandText = when (command) {
                is VoiceCommand.Yes -> "Yes"
                is VoiceCommand.No -> "No"
            }
            _uiState.value = _uiState.value.copy(lastRecognizedCommand = commandText)

            // Wait 1 second to show the highlighted command before swiping
            delay(1000)

            val isYes = command is VoiceCommand.Yes
            onSwipe(isYes)

            // Clear the highlight after swipe animation starts
            _uiState.value = _uiState.value.copy(lastRecognizedCommand = null)
        }
    }

    /**
     * Toggles voice control on/off.
     */
    fun toggleVoiceControl() {
        val current = _uiState.value
        val newEnabled = !current.isVoiceEnabled

        _uiState.value = current.copy(isVoiceEnabled = newEnabled)

        if (newEnabled) {
            voiceRecognitionManager.startListening()
        } else {
            voiceRecognitionManager.stopListening()
        }
    }

    /**
     * Checks if voice recognition is available on this device.
     */
    fun isVoiceRecognitionAvailable(): Boolean {
        return voiceRecognitionManager.isAvailable()
    }

    /**
     * Stops voice control and resets voice state.
     */
    fun stopVoiceControl() {
        voiceRecognitionManager.stopListening()
        _uiState.value = _uiState.value.copy(
            isVoiceEnabled = false,
            lastRecognizedCommand = null
        )
    }

    override fun onCleared() {
        super.onCleared()
        voiceRecognitionManager.destroy()
    }

    companion object {
        /**
         * Factory for creating GameViewModel with dependencies.
         */
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = PreferencesRepository(context.applicationContext)
                    val voiceManager = VoiceRecognitionManager(context.applicationContext)
                    return GameViewModel(repository, voiceManager) as T
                }
            }
        }
    }
}
