package com.abhishek.magicnumber.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhishek.magicnumber.model.NumberLayout
import com.abhishek.magicnumber.ui.theme.Gold
import com.abhishek.magicnumber.ui.theme.Purple500
import com.abhishek.magicnumber.ui.theme.Purple700
import com.abhishek.magicnumber.ui.theme.Purple800
import com.abhishek.magicnumber.ui.theme.Purple900
import com.abhishek.magicnumber.viewmodel.SettingsViewModel

/**
 * Settings screen for configuring game options.
 *
 * @param onBackClick Called when user navigates back
 * @param modifier Modifier for the root composable
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(context))
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Purple800, Purple900, Purple900)
                )
            )
    ) {
        // Top bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Gold
                )
            }

            Text(
                text = "Settings",
                color = Gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Settings content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Number Range Section
            SettingsSection(title = "Number Range") {
                RangeSelector(
                    selectedMaxNumber = settings.maxNumber,
                    onSelect = { viewModel.updateMaxNumber(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Number Layout Section
            SettingsSection(title = "Number Layout") {
                LayoutSelector(
                    selectedLayout = settings.numberLayout,
                    onSelect = { viewModel.updateNumberLayout(it) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // How It Works Section
            HowItWorksSection()
        }
    }
}

/**
 * Settings section with title and content.
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

/**
 * Range selector with preset options.
 */
@Composable
private fun RangeSelector(
    selectedMaxNumber: Int,
    onSelect: (Int) -> Unit
) {
    val options = listOf(
        31 to "1-31 (5 cards)",
        63 to "1-63 (6 cards)",
        127 to "1-127 (7 cards)"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (maxNumber, label) ->
            SelectableOption(
                label = label,
                isSelected = selectedMaxNumber == maxNumber,
                onClick = { onSelect(maxNumber) }
            )
        }
    }
}

/**
 * Layout selector with options.
 */
@Composable
private fun LayoutSelector(
    selectedLayout: NumberLayout,
    onSelect: (NumberLayout) -> Unit
) {
    val options = listOf(
        NumberLayout.ASCENDING to "Ascending",
        NumberLayout.GRID to "Grid",
        NumberLayout.SCATTERED to "Scattered",
        NumberLayout.CIRCULAR to "Circular"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (layout, label) ->
            SelectableOption(
                label = label,
                isSelected = selectedLayout == layout,
                onClick = { onSelect(layout) }
            )
        }
    }
}

/**
 * A selectable option row.
 */
@Composable
private fun SelectableOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Purple700 else Purple800)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Gold else Purple500.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Gold else MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Expandable "How It Works" section.
 */
@Composable
private fun HowItWorksSection() {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        // Header (clickable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Purple800)
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "How It Works",
                color = Gold,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Icon(
                imageVector = if (isExpanded)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = Gold
            )
        }

        // Content (expandable)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Purple800.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "This is the Binary Magic Trick!",
                    color = Gold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = """
                        Each card represents a power of 2 (1, 2, 4, 8, 16, 32, 64).

                        A number appears on a card if that power of 2 is part of its binary representation.

                        For example, 19 = 16 + 2 + 1, so 19 appears on the cards for 16, 2, and 1.

                        When you swipe YES, the app adds that card's value. The sum equals your number!
                    """.trimIndent(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
