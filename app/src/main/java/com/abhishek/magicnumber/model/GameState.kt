package com.abhishek.magicnumber.model

/**
 * Represents the current phase of the game.
 */
sealed class GamePhase {
    /** Game has not started yet */
    data object NotStarted : GamePhase()

    /** User is swiping through cards */
    data object InProgress : GamePhase()

    /** "Reading your mind" animation before reveal */
    data class Calculating(val number: Int) : GamePhase()

    /** Card flip animation is playing to reveal the number */
    data class Revealing(val number: Int) : GamePhase()

    /** Game is complete, showing the revealed number */
    data class Complete(val number: Int) : GamePhase()
}

/**
 * Represents the complete state of a game session.
 *
 * @property cards The list of cards to show (generated based on max number)
 * @property currentCardIndex Which card the user is currently viewing
 * @property responses User's responses (true = number was on card, false = wasn't)
 * @property phase Current phase of the game
 * @property numberLayout How numbers should be displayed on cards
 */
data class GameState(
    val cards: List<Card> = emptyList(),
    val currentCardIndex: Int = 0,
    val responses: List<Boolean> = emptyList(),
    val phase: GamePhase = GamePhase.NotStarted,
    val numberLayout: NumberLayout = NumberLayout.ASCENDING
)
