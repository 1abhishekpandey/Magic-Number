package com.abhishek.magicnumber.model

/**
 * Represents a single card in the magic number trick.
 *
 * @property keyNumber The power of 2 this card represents (1, 2, 4, 8, 16, 32, 64)
 * @property numbers All numbers that have this bit set (appear on this card)
 * @property bitPosition Which bit position this card tests (0-6)
 */
data class Card(
    val keyNumber: Int,
    val numbers: List<Int>,
    val bitPosition: Int
)
