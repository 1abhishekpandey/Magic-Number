package com.abhishek.magicnumber.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A row of dots showing progress through the cards.
 *
 * Completed dots are shown in gold, remaining dots in white/dim.
 *
 * @param total Total number of dots (cards)
 * @param current Current position (0-indexed)
 * @param modifier Modifier for the dots row
 */
@Composable
fun DotsIndicator(
    total: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    // TODO: Implement in Phase 3
}
