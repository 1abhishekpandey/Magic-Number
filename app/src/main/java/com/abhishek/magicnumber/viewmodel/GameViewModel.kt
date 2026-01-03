package com.abhishek.magicnumber.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.magicnumber.data.CardGenerator
import com.abhishek.magicnumber.data.PreferencesRepository
import com.abhishek.magicnumber.model.GamePhase
import com.abhishek.magicnumber.model.GameState
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
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

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
            // All cards shown, calculate and reveal
            val result = CardGenerator.calculateResult(newResponses, current.cards)
            current.copy(
                responses = newResponses,
                phase = GamePhase.Revealing(result)
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
}
