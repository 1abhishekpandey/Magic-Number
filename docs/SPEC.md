# Magic Number - Specification

## Overview

Magic Number is an Android app that implements the classic binary number guessing trick. The user thinks of a number, swipes through a series of cards indicating whether their number appears on each card, and the app dramatically reveals the chosen number. The app serves as a self-play demo to experience and understand this mathematical magic trick.

The trick works by leveraging binary representation: each card contains all numbers with a specific binary bit set. Summing the "key numbers" (powers of 2) from cards where the user's number appears yields the original number.

## Functional Requirements

### Core Features

- **Number Guessing Game**: User thinks of a number within configured range, swipes through cards, app reveals the number
- **Configurable Range**: Preset options persisted across sessions
  - 1-31 (5 cards)
  - 1-63 (6 cards)
  - 1-127 (7 cards)
- **Configurable Number Layout**: How numbers are displayed on cards (persisted)
  - Grid layout (rows and columns)
  - Scattered/random artistic arrangement
  - Ascending order (default)
  - Circular arrangement around key number
- **How It Works**: Educational explanation of the binary trick accessible from settings

### User Flow

1. **Launch** → Home screen with single "Start" button (mystical styling)
2. **Think of Number** → User mentally picks a number in the configured range
3. **Card Sequence** → For each card:
   - Card displays with numbers that have a specific binary bit set
   - Key number (power of 2) prominently displayed
   - User swipes RIGHT if their number is visible, LEFT if not
   - Card follows finger, rotates slightly, flies off screen
   - Haptic feedback on swipe
   - Dots indicator shows progress
4. **Dramatic Reveal** → After final card:
   - Final card flips slowly to reveal the guessed number
   - Celebration animation with mystical effects
5. **Play Again** → Single "Play Again" button to restart

### Settings Screen

Accessible via gear icon on home screen. Contains:
- Range selector (preset options)
- Number layout style selector
- "How It Works" explanation

## Technical Specification

### Architecture

**Pattern**: MVVM + Jetpack Compose

```
app/
├── ui/
│   ├── theme/           # Purple/gold mystical theme
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   ├── GameScreen.kt
│   │   ├── RevealScreen.kt
│   │   └── SettingsScreen.kt
│   └── components/
│       ├── SwipeableCard.kt
│       ├── DotsIndicator.kt
│       └── FlipCard.kt
├── viewmodel/
│   ├── GameViewModel.kt
│   └── SettingsViewModel.kt
├── data/
│   ├── PreferencesRepository.kt
│   └── CardGenerator.kt      # Binary logic
└── model/
    ├── Card.kt
    ├── GameState.kt
    └── Settings.kt
```

### Data Model

```kotlin
data class Card(
    val keyNumber: Int,           // Power of 2 (1, 2, 4, 8, 16, 32, 64)
    val numbers: List<Int>,       // All numbers with this bit set
    val bitPosition: Int          // Which bit this card tests (0-6)
)

data class GameState(
    val cards: List<Card>,
    val currentCardIndex: Int,
    val responses: List<Boolean>, // true = YES (right swipe)
    val isComplete: Boolean,
    val revealedNumber: Int?
)

data class Settings(
    val maxNumber: Int,           // 31, 63, or 127
    val numberLayout: NumberLayout
)

enum class NumberLayout {
    GRID, SCATTERED, ASCENDING, CIRCULAR
}
```

### Storage

**DataStore Preferences** for persisting:
- `max_number: Int` (default: 63)
- `number_layout: String` (default: "ASCENDING")

### Card Generation Algorithm

```kotlin
fun generateCards(maxNumber: Int): List<Card> {
    val numBits = log2(maxNumber + 1).toInt()  // 5, 6, or 7 cards
    return (0 until numBits).map { bitPosition ->
        val keyNumber = 1 shl bitPosition  // 2^bitPosition
        val numbers = (1..maxNumber).filter { n ->
            (n and keyNumber) != 0  // Numbers with this bit set
        }
        Card(keyNumber, numbers, bitPosition)
    }
}

fun calculateNumber(responses: List<Boolean>, cards: List<Card>): Int {
    return cards.zip(responses)
        .filter { (_, response) -> response }
        .sumOf { (card, _) -> card.keyNumber }
}
```

### Dependencies

```kotlin
// build.gradle.kts
android {
    minSdk = 29
    targetSdk = 34
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
}
```

## UI/UX Specification

### Theme

**Color Palette**: Deep Purple + Gold (Mystical)
```kotlin
val Purple900 = Color(0xFF1A0033)      // Background
val Purple700 = Color(0xFF4A1A7A)      // Card background
val Purple500 = Color(0xFF7B2CBF)      // Accent
val Gold = Color(0xFFFFD700)           // Key numbers, highlights
val GoldLight = Color(0xFFFFE566)      // Secondary gold
val White = Color(0xFFFFFFFF)          // Text
```

**Typography**: Modern serif for mystical feel, sans-serif for numbers

### Screens

#### Home Screen
- Dark purple gradient background with subtle star particles
- Large mystical "Start" button (gold border, purple fill)
- Gear icon (settings) in top-right corner
- App name "Magic Number" at top with magical glow effect

#### Game Screen
- Card stack visualization (current card prominent, next card peeking behind)
- Current card shows:
  - Key number in prominent position (gold, large)
  - Other numbers in configured layout
  - Subtle mystical border/glow
- Dots indicator at bottom (gold for completed, white for remaining)
- Swipe hints on first card only (subtle arrows)

#### Reveal Screen
- Card flip animation (slow, dramatic - ~1.5 seconds)
- Revealed number in large gold text with glow
- Sparkle/star burst animation on reveal
- "Play Again" button appears after animation completes

#### Settings Screen
- List-style settings with purple/gold styling
- Range selector with visual card count preview
- Layout selector with small visual preview of each style
- "How It Works" expandable section with binary explanation

### Interactions

**Swipe Gesture**:
- Card follows finger position
- Slight rotation based on horizontal offset (±15°)
- Threshold: 100dp horizontal movement to commit
- On release:
  - If past threshold: card flies off screen (250ms), haptic feedback
  - If not past threshold: card springs back to center

**Haptics**:
- Light tick on swipe threshold crossed
- Medium impact on card release
- Strong success on number reveal

### States

**Loading**: Not needed (all local, instant)

**Empty**: N/A

**Error**: N/A (no network, minimal failure modes)

## Edge Cases & Constraints

### Known Limitations

- Portrait orientation only
- No undo for swipes (commit immediately)
- No multiplayer/sharing features in v1
- No game history tracking

### Edge Cases

| Scenario | Handling |
|----------|----------|
| User swipes all NO | Result is 0 - show "0" with message "You picked 0!" |
| User swipes all YES on 1-63 | Result is 63 - valid result |
| App killed mid-game | Game resets on next launch (no state preservation) |
| Rapid swipes | Queue animations, don't skip cards |
| Back button during game | Return to home (abandon game) |

### Security & Privacy

- No network permissions required
- No data collection or analytics
- No user accounts
- All data stored locally in app sandbox

## Open Questions

None - all requirements clarified.

## Decisions Made

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Platform | Android only | User preference |
| Architecture | MVVM + Compose | Modern, recommended approach for new Android apps |
| Storage | DataStore | Type-safe, coroutine-friendly, modern replacement for SharedPreferences |
| Min SDK | API 29 | Covers ~75% devices, enables modern APIs |
| Undo | No undo | Teaches consequence, keeps interaction simple |
| Analytics | None | Privacy-first, fully offline |
| Range config | Preset options | Simpler UX than slider/input, predictable card counts |
| Onboarding | None | Game is intuitive enough, reduces friction |
| Orientation | Portrait only | Swipe cards work better vertically |
| Audio | Haptics only | Subtle feedback without requiring sound |
