package com.abhishek.magicnumber.data

import com.abhishek.magicnumber.model.Card
import kotlin.math.log2

/**
 * Generates cards for the binary magic number trick.
 *
 * Each card represents a power of 2 and contains all numbers
 * that have that bit set in their binary representation.
 */
object CardGenerator {

    /**
     * Generates the list of cards needed for the given maximum number.
     *
     * @param maxNumber The maximum number in the range (e.g., 31, 63, 127)
     * @return List of cards, one for each bit position
     */
    fun generateCards(maxNumber: Int): List<Card> {
        val numBits = log2((maxNumber + 1).toDouble()).toInt()

        return (0 until numBits).map { bitPosition ->
            val keyNumber = 1 shl bitPosition // 2^bitPosition
            val numbers = (1..maxNumber).filter { number ->
                (number and keyNumber) != 0 // Check if bit is set
            }
            Card(
                keyNumber = keyNumber,
                numbers = numbers,
                bitPosition = bitPosition
            )
        }
    }

    /**
     * Calculates the user's number based on their responses.
     *
     * @param responses List of boolean responses (true = number was on card)
     * @param cards The cards that were shown
     * @return The calculated number
     */
    fun calculateResult(responses: List<Boolean>, cards: List<Card>): Int {
        return cards.zip(responses)
            .filter { (_, response) -> response }
            .sumOf { (card, _) -> card.keyNumber }
    }
}
