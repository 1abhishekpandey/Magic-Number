package com.abhishek.magicnumber.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
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
import com.abhishek.magicnumber.viewmodel.GameViewModel

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

    // Handle back button
    BackHandler { onBackClick() }

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
                    onSwipeRight = { viewModel.onSwipe(true) }
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
    onSwipeRight: () -> Unit
) {
    val currentCard = cards.getOrNull(currentCardIndex) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Instruction text
        Text(
            text = "Is your number on this card?",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card - use key to reset SwipeableCard state for each card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            key(currentCardIndex) {
                SwipeableCard(
                    onSwipeLeft = onSwipeLeft,
                    onSwipeRight = onSwipeRight,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .aspectRatio(0.7f)
                ) {
                    CardContent(
                        card = currentCard,
                        numberLayout = numberLayout
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
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardContent(
    card: Card,
    numberLayout: NumberLayout
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Purple700)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(Gold.copy(alpha = 0.6f), Gold.copy(alpha = 0.3f))),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Key number in top-left
        Text(
            text = card.keyNumber.toString(),
            color = Gold,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Numbers in the center
        FlowRow(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 48.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center,
            maxItemsInEachRow = 6
        ) {
            val sortedNumbers = when (numberLayout) {
                NumberLayout.ASCENDING -> card.numbers.sorted()
                NumberLayout.GRID -> card.numbers.sorted()
                NumberLayout.SCATTERED -> card.numbers.shuffled()
                NumberLayout.CIRCULAR -> card.numbers.sorted()
            }

            sortedNumbers.forEach { number ->
                Text(
                    text = number.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .size(40.dp)
                )
            }
        }
    }
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
                .aspectRatio(0.7f),
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your number is",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 20.sp
        )

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
