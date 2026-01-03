package com.abhishek.magicnumber.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** Threshold in dp that must be exceeded to commit a swipe */
private val SWIPE_THRESHOLD = 100.dp

/** Maximum rotation angle in degrees */
private const val MAX_ROTATION_DEGREES = 15f

/** How much horizontal movement (in dp) causes full rotation */
private val ROTATION_SCALE = 200.dp

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
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val hapticFeedback = LocalHapticFeedback.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    // Threshold in pixels
    val swipeThresholdPx = with(density) { SWIPE_THRESHOLD.toPx() }
    val rotationScalePx = with(density) { ROTATION_SCALE.toPx() }

    // Animatable offset for smooth animations
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val coroutineScope = rememberCoroutineScope()

    // Track if we've crossed threshold to trigger haptic only once
    var hasTriggeredThresholdHaptic by remember { mutableStateOf(false) }

    // Calculate rotation based on horizontal offset
    val rotation = (offset.value.x / rotationScalePx * MAX_ROTATION_DEGREES)
        .coerceIn(-MAX_ROTATION_DEGREES, MAX_ROTATION_DEGREES)

    // Check if we've crossed threshold and trigger haptic
    val isPastThreshold = abs(offset.value.x) > swipeThresholdPx
    if (isPastThreshold && !hasTriggeredThresholdHaptic) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        hasTriggeredThresholdHaptic = true
    } else if (!isPastThreshold && hasTriggeredThresholdHaptic) {
        // Reset when user drags back within threshold
        hasTriggeredThresholdHaptic = false
    }

    Box(
        modifier = modifier
            .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
            .graphicsLayer {
                rotationZ = rotation
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            val horizontalOffset = offset.value.x

                            when {
                                // Swiped right past threshold
                                horizontalOffset > swipeThresholdPx -> {
                                    // Haptic feedback for confirmed swipe
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                    // Animate off screen to the right
                                    offset.animateTo(
                                        targetValue = Offset(screenWidthPx * 1.5f, offset.value.y),
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                    onSwipeRight()
                                }
                                // Swiped left past threshold
                                horizontalOffset < -swipeThresholdPx -> {
                                    // Haptic feedback for confirmed swipe
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                    // Animate off screen to the left
                                    offset.animateTo(
                                        targetValue = Offset(-screenWidthPx * 1.5f, offset.value.y),
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                    onSwipeLeft()
                                }
                                // Threshold not reached - spring back to center
                                else -> {
                                    offset.animateTo(
                                        targetValue = Offset.Zero,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            offset.animateTo(
                                targetValue = Offset.Zero,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offset.snapTo(
                                Offset(
                                    x = offset.value.x + dragAmount.x,
                                    y = offset.value.y + dragAmount.y
                                )
                            )
                        }
                    }
                )
            }
    ) {
        content()
    }
}

/**
 * Calculates the swipe progress as a value between -1 (full left) and 1 (full right).
 * 0 means centered, values beyond ±1 mean past threshold.
 */
fun calculateSwipeProgress(offsetX: Float, thresholdPx: Float): Float {
    return (offsetX / thresholdPx).coerceIn(-1.5f, 1.5f)
}
