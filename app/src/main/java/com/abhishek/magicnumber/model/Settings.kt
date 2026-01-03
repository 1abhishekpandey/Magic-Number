package com.abhishek.magicnumber.model

/**
 * How numbers are arranged on each card.
 */
enum class NumberLayout {
    /** Numbers listed in ascending order (default) */
    ASCENDING,

    /** Numbers scattered randomly across the card */
    SCATTERED
}

/**
 * User-configurable settings for the app.
 *
 * @property maxNumber The maximum number in the range (31, 63, or 127)
 * @property numberLayout How numbers should be displayed on cards
 */
data class Settings(
    val maxNumber: Int = 63,
    val numberLayout: NumberLayout = NumberLayout.ASCENDING
) {
    /** Number of cards needed for this range */
    val cardCount: Int
        get() = when (maxNumber) {
            31 -> 5
            63 -> 6
            127 -> 7
            else -> 6
        }
}
