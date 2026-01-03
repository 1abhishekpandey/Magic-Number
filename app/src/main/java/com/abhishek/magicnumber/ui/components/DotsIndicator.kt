package com.abhishek.magicnumber.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Default size for inactive dots */
private val DOT_SIZE = 8.dp

/** Size for the current active dot */
private val ACTIVE_DOT_SIZE = 12.dp

/** Spacing between dots */
private val DOT_SPACING = 8.dp

/**
 * A row of dots showing progress through the cards.
 *
 * - Completed dots: Gold (primary color)
 * - Current dot: Gold and slightly larger
 * - Remaining dots: White with reduced opacity
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
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DOT_SPACING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            Dot(
                isCompleted = index < current,
                isCurrent = index == current
            )
        }
    }
}

/**
 * Individual dot component.
 *
 * @param isCompleted Whether this dot represents a completed card
 * @param isCurrent Whether this is the current card's dot
 */
@Composable
private fun Dot(
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    // Animate size for current dot
    val size by animateDpAsState(
        targetValue = if (isCurrent) ACTIVE_DOT_SIZE else DOT_SIZE,
        animationSpec = spring(),
        label = "dot_size"
    )

    // Determine color based on state
    val color = when {
        isCompleted || isCurrent -> MaterialTheme.colorScheme.primary // Gold
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) // Dim white
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}
