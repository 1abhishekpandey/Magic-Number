package com.abhishek.magicnumber.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhishek.magicnumber.ui.theme.Gold
import com.abhishek.magicnumber.ui.theme.MagicNumberTheme
import com.abhishek.magicnumber.ui.theme.Purple500
import com.abhishek.magicnumber.ui.theme.Purple700
import com.abhishek.magicnumber.ui.theme.Purple800
import com.abhishek.magicnumber.ui.theme.Purple900

/**
 * Home screen with mystical "Start" button and settings access.
 *
 * Features:
 * - Dark purple gradient background
 * - "Magic Number" title with golden glow
 * - Large mystical Start button
 * - Settings gear icon in top-right
 *
 * @param onStartClick Called when user taps Start button
 * @param onSettingsClick Called when user taps settings icon
 * @param modifier Modifier for the root composable
 */
@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Purple800,
                        Purple900,
                        Purple900
                    )
                )
            )
    ) {
        // Settings icon in top-right
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Main content - centered
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title with glow effect
            MagicTitle()

            // Spacer
            Box(modifier = Modifier.weight(0.3f))

            // Start button
            StartButton(onClick = onStartClick)

            // Spacer at bottom
            Box(modifier = Modifier.weight(0.4f))
        }
    }
}

/**
 * Title text with mystical golden glow effect.
 */
@Composable
private fun MagicTitle() {
    Box(contentAlignment = Alignment.Center) {
        // Glow layer (blurred text behind)
        Text(
            text = "Magic Number",
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Gold.copy(alpha = 0.5f)
            ),
            modifier = Modifier.blur(8.dp)
        )

        // Main title
        Text(
            text = "Magic Number",
            style = TextStyle(
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Gold,
                shadow = Shadow(
                    color = Gold.copy(alpha = 0.6f),
                    offset = Offset(0f, 0f),
                    blurRadius = 16f
                )
            )
        )
    }
}

/**
 * Mystical Start button with gold border and purple fill.
 */
@Composable
private fun StartButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(width = 200.dp, height = 64.dp)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Gold, Gold.copy(alpha = 0.6f), Gold)
                ),
                shape = RoundedCornerShape(32.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple700,
            contentColor = Gold
        ),
        shape = RoundedCornerShape(32.dp)
    ) {
        Text(
            text = "Start",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A0033)
@Composable
private fun HomeScreenPreview() {
    MagicNumberTheme {
        HomeScreen(
            onStartClick = {},
            onSettingsClick = {}
        )
    }
}
