package com.abhishek.magicnumber.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A card that flips with a 3D Y-axis rotation animation.
 *
 * Used for the dramatic reveal at the end of the game.
 *
 * @param isFlipped Whether the card should show its back (true) or front (false)
 * @param onFlipComplete Called when flip animation completes
 * @param modifier Modifier for the card container
 * @param front Content to show on the front of the card
 * @param back Content to show on the back of the card
 */
@Composable
fun FlipCard(
    isFlipped: Boolean,
    onFlipComplete: () -> Unit,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    // TODO: Implement in Phase 3
}
