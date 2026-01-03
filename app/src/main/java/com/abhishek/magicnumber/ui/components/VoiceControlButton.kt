package com.abhishek.magicnumber.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhishek.magicnumber.R
import com.abhishek.magicnumber.ui.theme.Gold
import com.abhishek.magicnumber.ui.theme.Purple500
import com.abhishek.magicnumber.ui.theme.Purple700
import com.abhishek.magicnumber.ui.theme.White
import com.abhishek.magicnumber.voice.VoiceRecognitionState

private const val PULSE_DURATION_MS = 800
private const val ICON_ALPHA_DISABLED = 0.6f
private const val BORDER_ALPHA_DISABLED = 0.5f
private const val ERROR_BG_ALPHA = 0.3f

private val ErrorRed = Color(0xFFE53935)

/**
 * A pill-shaped toggle button for voice control.
 *
 * Displays a microphone icon with a text label. Visual appearance changes
 * based on the current voice recognition state:
 * - Disabled: Purple background with muted styling
 * - Listening: Pulsing icon with gold accent
 * - Error: Red-tinted background for retry
 *
 * @param isEnabled Whether voice recognition is toggled on
 * @param voiceState Current state of voice recognition
 * @param onToggle Called when the button is clicked
 * @param modifier Modifier for the button
 */
@Composable
fun VoiceControlButton(
    isEnabled: Boolean,
    voiceState: VoiceRecognitionState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = voiceState is VoiceRecognitionState.Error
    val isListening = voiceState is VoiceRecognitionState.Listening

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isError -> ErrorRed.copy(alpha = ERROR_BG_ALPHA)
            isEnabled -> Purple500
            else -> Purple700
        },
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> ErrorRed
            isEnabled -> Gold
            else -> Purple500.copy(alpha = BORDER_ALPHA_DISABLED)
        },
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    val label = when {
        isError -> "Retry"
        isListening -> "Listening..."
        else -> "Voice"
    }

    val accessibilityDescription = when {
        isError -> "Voice control error, tap to retry"
        isEnabled -> "Voice control enabled, tap to disable"
        else -> "Voice control disabled, tap to enable"
    }

    // Pulsing animation for listening state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_DURATION_MS),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val iconAlpha = when {
        isListening -> pulseAlpha
        isEnabled -> 1f
        else -> ICON_ALPHA_DISABLED
    }

    Surface(
        onClick = onToggle,
        modifier = modifier
            .semantics {
                contentDescription = accessibilityDescription
                role = Role.Switch
            },
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mic),
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .alpha(iconAlpha),
                tint = if (isEnabled || isError) Gold else White
            )
            Text(
                text = label,
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * A small pill overlay showing the recognized voice command.
 *
 * Displays the command text (e.g., "Yes" or "No") in a styled pill
 * with gold text on a purple background.
 *
 * @param command The recognized command text to display
 * @param modifier Modifier for the pill
 */
@Composable
fun VoiceCommandFeedback(
    command: String,
    modifier: Modifier = Modifier
) {
    val commandText = "\"$command\""

    Surface(
        modifier = modifier
            .semantics {
                contentDescription = "Recognized command: $commandText"
            },
        shape = RoundedCornerShape(16.dp),
        color = Purple700,
        border = BorderStroke(1.dp, Gold)
    ) {
        Text(
            text = commandText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Gold,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
