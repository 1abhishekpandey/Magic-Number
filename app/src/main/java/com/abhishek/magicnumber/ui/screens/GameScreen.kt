package com.abhishek.magicnumber.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhishek.magicnumber.model.Card
import com.abhishek.magicnumber.model.GamePhase
import com.abhishek.magicnumber.model.NumberLayout
import com.abhishek.magicnumber.ui.components.DotsIndicator
import com.abhishek.magicnumber.ui.components.FlipCard
import com.abhishek.magicnumber.ui.components.SwipeableCard
import com.abhishek.magicnumber.ui.theme.Gold
import com.abhishek.magicnumber.ui.theme.Purple700
import com.abhishek.magicnumber.ui.theme.Purple800
import com.abhishek.magicnumber.ui.theme.Purple900
import com.abhishek.magicnumber.ui.components.VoiceCommandFeedback
import com.abhishek.magicnumber.ui.components.VoiceControlButton
import com.abhishek.magicnumber.viewmodel.GameViewModel
import com.abhishek.magicnumber.voice.VoiceRecognitionState

/**
 * Game screen where user swipes through cards.
 *
 * @param onBackClick Called when user navigates back
 * @param onPlayAgain Called when user taps Play Again
 * @param modifier Modifier for the root composable
 */
@Composable
fun GameScreen(
    onBackClick: () -> Unit,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: GameViewModel = viewModel(factory = GameViewModel.factory(context))
    val uiState by viewModel.uiState.collectAsState()

    // Permission launcher for RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleVoiceControl()
        }
    }

    // Stop voice control when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopVoiceControl()
        }
    }

    // Handle back button
    BackHandler {
        viewModel.stopVoiceControl()
        onBackClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Purple800, Purple900, Purple900)
                )
            )
    ) {
        when (val phase = uiState.phase) {
            GamePhase.NotStarted -> {
                // Loading state - shouldn't be visible long
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading...", color = Gold)
                }
            }

            GamePhase.InProgress -> {
                // Game in progress - show cards
                GameInProgressContent(
                    cards = uiState.cards,
                    currentCardIndex = uiState.currentCardIndex,
                    numberLayout = uiState.numberLayout,
                    onSwipeLeft = { viewModel.onSwipe(false) },
                    onSwipeRight = { viewModel.onSwipe(true) },
                    isVoiceEnabled = uiState.isVoiceEnabled,
                    voiceState = uiState.voiceState,
                    lastRecognizedCommand = uiState.lastRecognizedCommand,
                    onVoiceToggle = {
                        if (!uiState.isVoiceEnabled) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            viewModel.toggleVoiceControl()
                        }
                    },
                    isVoiceAvailable = viewModel.isVoiceRecognitionAvailable()
                )
            }

            is GamePhase.Calculating -> {
                // "Reading your mind" animation
                CalculatingContent(
                    onRevealClick = { viewModel.onRevealClick() }
                )
            }

            is GamePhase.Revealing -> {
                // Reveal animation
                RevealContent(
                    number = phase.number,
                    onRevealComplete = { viewModel.onRevealComplete() }
                )
            }

            is GamePhase.Complete -> {
                // Game complete - show result and play again
                CompleteContent(
                    number = phase.number,
                    onPlayAgain = onPlayAgain
                )
            }
        }
    }
}

/**
 * Content shown while swiping through cards.
 */
@Composable
private fun GameInProgressContent(
    cards: List<Card>,
    currentCardIndex: Int,
    numberLayout: NumberLayout,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    isVoiceEnabled: Boolean,
    voiceState: VoiceRecognitionState,
    lastRecognizedCommand: String?,
    onVoiceToggle: () -> Unit,
    isVoiceAvailable: Boolean
) {
    val currentCard = cards.getOrNull(currentCardIndex) ?: return
    val nextCard = cards.getOrNull(currentCardIndex + 1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Voice control button in top-right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (isVoiceAvailable) {
                VoiceControlButton(
                    isEnabled = isVoiceEnabled,
                    voiceState = voiceState,
                    onToggle = onVoiceToggle
                )
            }
        }

        // Instruction text
        Text(
            text = "Is your number on this card?",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card stack - shows peek of next card behind current
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Next card peek (behind current card)
            if (nextCard != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.80f)
                        .aspectRatio(0.85f)
                        .graphicsLayer {
                            // Slightly scaled down and offset
                            scaleX = 0.95f
                            scaleY = 0.95f
                            translationY = 16.dp.toPx()
                            alpha = 0.6f
                        }
                ) {
                    CardContent(
                        card = nextCard,
                        numberLayout = numberLayout,
                        showShadow = false
                    )
                }
            }

            // Current card - use key to reset SwipeableCard state for each card
            key(currentCardIndex) {
                // Entrance animation - scale up from peek state
                val scale = remember { Animatable(0.95f) }
                val alpha = remember { Animatable(0.7f) }

                LaunchedEffect(Unit) {
                    // Animate to full size with a quick spring
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                }
                LaunchedEffect(Unit) {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                }

                SwipeableCard(
                    onSwipeLeft = onSwipeLeft,
                    onSwipeRight = onSwipeRight,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(0.85f)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                            this.alpha = alpha.value
                        }
                ) {
                    CardContent(
                        card = currentCard,
                        numberLayout = numberLayout,
                        showShadow = true
                    )
                }
            }

            // Voice command feedback overlay
            if (lastRecognizedCommand != null) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    VoiceCommandFeedback(
                        command = lastRecognizedCommand,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Swipe hints
        Text(
            text = "← NO          YES →",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress dots
        DotsIndicator(
            total = cards.size,
            current = currentCardIndex
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Card content with numbers displayed.
 *
 * @param card The card data to display
 * @param numberLayout How to arrange the numbers
 * @param showShadow Whether to show a drop shadow (for depth effect)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardContent(
    card: Card,
    numberLayout: NumberLayout,
    showShadow: Boolean = true
) {
    // Generate accessibility description for TalkBack
    val numbersDescription = card.numbers.sorted().joinToString(", ")
    val cardDescription = "Numbers on this card: $numbersDescription. " +
        "Swipe right for Yes, swipe left for No."

    // Dynamic sizing based on number count for different ranges
    // 1-31: up to 16 numbers, 1-63: up to 32 numbers, 1-127: up to 64 numbers
    val numberCount = card.numbers.size
    val textSize = when {
        numberCount <= 16 -> 22.sp    // 4×4 grid - can be larger
        numberCount <= 32 -> 18.sp    // 4×8 grid
        else -> 12.sp                 // 4×16 grid - smaller for 16 rows
    }
    val itemsPerRow = 4  // Uniform 4 columns for all ranges

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = cardDescription
            }
            .graphicsLayer {
                if (showShadow) {
                    shadowElevation = 16.dp.toPx()
                    shape = RoundedCornerShape(16.dp)
                    clip = false
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .background(Purple700)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(Gold.copy(alpha = 0.6f), Gold.copy(alpha = 0.3f))),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        val sortedNumbers = when (numberLayout) {
            NumberLayout.ASCENDING -> card.numbers.sorted()
            NumberLayout.SCATTERED -> card.numbers.shuffled()
        }

        // Split numbers into rows
        val rows = sortedNumbers.chunked(itemsPerRow)

        // Grid that fills the entire card
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            rows.forEach { rowNumbers ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowNumbers.forEach { number ->
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = number.toString(),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = textSize,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    // Fill remaining columns if row is not full
                    repeat(itemsPerRow - rowNumbers.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * "Reading your mind" calculating animation content.
 */
@Composable
private fun CalculatingContent(
    onRevealClick: () -> Unit
) {
    // Track whether the 2-second animation period is complete
    var showRevealButton by remember { mutableStateOf(false) }

    // Wait 2 seconds before showing the Reveal button
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        showRevealButton = true
    }

    // Only animate while waiting for button to appear
    val infiniteTransition = rememberInfiniteTransition(label = "stars")

    // Multiple rotation animations at different speeds for stars
    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation1"
    )
    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )
    val rotation3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation3"
    )

    // Pulsing animation for center (stops when button appears)
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Alpha animation for text
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Rotating stars container
        Box(
            modifier = Modifier.size(250.dp),
            contentAlignment = Alignment.Center
        ) {
            // When button appears, stop rotations at current position
            val finalRotation1 = if (showRevealButton) rotation1 else rotation1
            val finalRotation2 = if (showRevealButton) rotation2 else rotation2
            val finalRotation3 = if (showRevealButton) rotation3 else rotation3

            // Outer ring of stars (slowest)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = if (showRevealButton) 0f else finalRotation3 }
            ) {
                StarAt(0.5f, 0f, "✦")      // Top
                StarAt(1f, 0.5f, "★")       // Right
                StarAt(0.5f, 1f, "✧")       // Bottom
                StarAt(0f, 0.5f, "✦")       // Left
            }

            // Middle ring of stars (medium speed, counter-rotating)
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer { rotationZ = if (showRevealButton) 0f else finalRotation2 }
            ) {
                StarAt(0.85f, 0.15f, "★")   // Top-right
                StarAt(0.85f, 0.85f, "✧")   // Bottom-right
                StarAt(0.15f, 0.85f, "✦")   // Bottom-left
                StarAt(0.15f, 0.15f, "★")   // Top-left
            }

            // Inner ring of stars (fastest)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer { rotationZ = if (showRevealButton) 0f else finalRotation1 }
            ) {
                StarAt(0.5f, 0.1f, "✧")
                StarAt(0.9f, 0.5f, "✦")
                StarAt(0.5f, 0.9f, "★")
                StarAt(0.1f, 0.5f, "✧")
            }

            // Center "?" with pulsing effect (stops when button appears)
            Text(
                text = "?",
                color = Gold,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    val scale = if (showRevealButton) 1f else pulse
                    scaleX = scale
                    scaleY = scale
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "Reading your mind..." text (stops animating when button appears)
        Text(
            text = "Reading your mind...",
            color = Gold.copy(alpha = if (showRevealButton) 1f else textAlpha),
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Reveal button - only shown after 2 seconds
        AnimatedVisibility(
            visible = showRevealButton,
            enter = fadeIn()
        ) {
            Button(
                onClick = onRevealClick,
                modifier = Modifier.size(width = 200.dp, height = 56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Purple700,
                    contentColor = Gold
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "Reveal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Placeholder to maintain layout before button appears
        if (!showRevealButton) {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}

/**
 * Helper composable to position a star at relative coordinates within parent.
 */
@Composable
private fun BoxScope.StarAt(
    xFraction: Float,
    yFraction: Float,
    symbol: String
) {
    Text(
        text = symbol,
        color = Gold,
        fontSize = 24.sp,
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(
                x = (xFraction * 220).dp,
                y = (yFraction * 220).dp
            )
    )
}

/**
 * Reveal animation content.
 */
@Composable
private fun RevealContent(
    number: Int,
    onRevealComplete: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        FlipCard(
            isFlipped = true,
            onFlipComplete = onRevealComplete,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(0.85f),
            front = {
                // Back of card (showing first)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Purple700)
                        .border(
                            width = 2.dp,
                            color = Gold.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        color = Gold,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            back = {
                // Front of card showing the number
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Purple700)
                        .border(
                            width = 3.dp,
                            color = Gold,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        color = Gold,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

/**
 * Game complete content with result and play again button.
 */
@Composable
private fun CompleteContent(
    number: Int,
    onPlayAgain: () -> Unit
) {
    // Special message for edge cases
    val headerText = when (number) {
        0 -> "You said NO to all cards!"
        else -> "Your number is"
    }

    val subtitleText = when (number) {
        0 -> "That means your number was..."
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = headerText,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        if (subtitleText != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitleText,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = number.toString(),
            color = Gold,
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.size(width = 200.dp, height = 56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple700,
                contentColor = Gold
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Play Again",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
