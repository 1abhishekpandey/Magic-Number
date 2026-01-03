package com.abhishek.magicnumber.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A card that can be swiped left (NO) or right (YES).
 *
 * Features:
 * - Card follows finger position during drag
 * - Rotates slightly based on horizontal offset (±15°)
 * - Threshold of 100dp to commit swipe
 * - Springs back if threshold not reached
 * - Flies off screen when threshold exceeded
 *
 * @param onSwipeLeft Called when user swipes left past threshold
 * @param onSwipeRight Called when user swipes right past threshold
 * @param modifier Modifier for the card container
 * @param content The content to display on the card
 */
@Composable
fun SwipeableCard(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // TODO: Implement in Phase 3
}
