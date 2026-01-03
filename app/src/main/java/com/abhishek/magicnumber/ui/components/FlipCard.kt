package com.abhishek.magicnumber.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** Duration of the flip animation in milliseconds */
private const val FLIP_DURATION_MS = 1500

/** Camera distance multiplier for 3D perspective effect */
private const val CAMERA_DISTANCE_MULTIPLIER = 12f

/**
 * A card that flips with a 3D Y-axis rotation animation.
 *
 * Used for the dramatic reveal at the end of the game.
 * The flip is slow and dramatic (~1.5 seconds) with easing.
 *
 * @param isFlipped Whether the card should show its back (true) or front (false)
 * @param onFlipComplete Called when flip animation completes
 * @param modifier Modifier for the card container
 * @param front Content to show on the front of the card (visible when not flipped)
 * @param back Content to show on the back of the card (visible when flipped)
 */
@Composable
fun FlipCard(
    isFlipped: Boolean,
    onFlipComplete: () -> Unit,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Animatable for smooth rotation (0° = front, 180° = back)
    val rotation = remember { Animatable(0f) }

    // Animate when isFlipped changes
    LaunchedEffect(isFlipped) {
        val targetRotation = if (isFlipped) 180f else 0f
        rotation.animateTo(
            targetValue = targetRotation,
            animationSpec = tween(
                durationMillis = FLIP_DURATION_MS,
                // EaseInOutCubic for dramatic slow-start slow-end effect
                easing = { fraction ->
                    // Cubic ease in-out
                    if (fraction < 0.5f) {
                        4 * fraction * fraction * fraction
                    } else {
                        1 - (-2 * fraction + 2).let { it * it * it } / 2
                    }
                }
            )
        )
        // Trigger haptic and callback when flip completes
        if (isFlipped) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onFlipComplete()
        }
    }

    // Determine which side to show based on rotation angle
    val showBack = rotation.value > 90f

    Box(
        modifier = modifier
            .graphicsLayer {
                // Set camera distance for 3D perspective
                cameraDistance = CAMERA_DISTANCE_MULTIPLIER * density

                // Apply Y-axis rotation
                rotationY = rotation.value

                // When showing back, we need to counter-rotate the content
                // so it's not mirrored (since we're looking at it from behind)
            }
    ) {
        if (showBack) {
            // Back content - needs to be counter-rotated to appear correctly
            Box(
                modifier = Modifier.graphicsLayer {
                    rotationY = 180f // Counter-rotate so text isn't mirrored
                }
            ) {
                back()
            }
        } else {
            front()
        }
    }
}

/**
 * Variant of FlipCard without completion callback.
 */
@Composable
fun FlipCard(
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    FlipCard(
        isFlipped = isFlipped,
        onFlipComplete = {},
        modifier = modifier,
        front = front,
        back = back
    )
}
