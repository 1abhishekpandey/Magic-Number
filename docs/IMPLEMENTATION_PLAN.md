# Magic Number - Implementation Plan

A step-by-step guide designed for learning Android development best practices.

---

## Phase 1: Project Setup & Foundation

**Goal**: Establish a clean, well-configured Android project with proper architecture foundations.

### Step 1.1: Create Android Project ✅

**What you'll learn**: Android project structure, Gradle Kotlin DSL, version catalogs

**Tasks**:
- [x] Create new Android project in Android Studio
  - Application name: "Magic Number"
  - Package name: `com.abhishek.magicnumber`
  - Minimum SDK: API 29 (Android 10)
  - Build configuration: Kotlin DSL
- [x] Clean up default generated files (remove sample UI)
- [x] Set up version catalog (`libs.versions.toml`)
- [x] Lock to portrait orientation in AndroidManifest.xml

**Best practices to review**:
- Why use Gradle Kotlin DSL over Groovy?
- Version catalog benefits for dependency management
- Project structure conventions

**Checkpoint**: ✅ App compiles and runs

---

### Step 1.2: Configure Dependencies ✅

**What you'll learn**: Compose BOM, dependency injection setup, modern Android libraries

**Tasks**:
- [x] Add Compose BOM for consistent versions
- [x] Add Material3, Animation, Navigation dependencies
- [x] Add ViewModel and Lifecycle dependencies
- [x] Add DataStore Preferences dependency
- [x] Configure compose compiler options

**Dependencies added to `libs.versions.toml`**:
```kotlin
navigationCompose = "2.8.5"
datastorePreferences = "1.1.2"

androidx-compose-animation = { group = "androidx.compose.animation", name = "animation" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }
```

**Best practices to review**:
- What is a BOM and why use it?
- Platform vs implementation dependencies
- Keeping dependencies minimal

**Checkpoint**: ✅ All dependencies resolve, build successful

---

### Step 1.3: Create Package Structure ✅

**What you'll learn**: Clean Architecture layering, package organization

**Tasks**:
- [x] Create package structure following MVVM:
  ```
  com.abhishek.magicnumber/
  ├── ui/
  │   ├── theme/
  │   ├── screens/
  │   └── components/
  ├── viewmodel/
  ├── data/
  └── model/
  ```
- [x] Add placeholder files to prevent empty package issues

**Files created**:
- `model/Card.kt` - Card data class with keyNumber, numbers, bitPosition
- `model/GameState.kt` - GameState and GamePhase sealed class
- `model/Settings.kt` - Settings data class and NumberLayout enum
- `data/CardGenerator.kt` - Card generation algorithm (fully implemented)
- `data/PreferencesRepository.kt` - DataStore repository (fully implemented)
- `viewmodel/GameViewModel.kt` - Game ViewModel (fully implemented)
- `ui/screens/*.kt` - Placeholder screen composables
- `ui/components/*.kt` - Placeholder component composables

**Best practices to review**:
- Feature-based vs layer-based packaging
- Why separate `model` from `data`?
- Where do repositories belong?

**Checkpoint**: ✅ Package structure visible, build successful

---

### Step 1.4: Set Up Theme

**What you'll learn**: Material3 theming, custom color schemes, typography

**Tasks**:
- [ ] Create `Color.kt` with mystical purple/gold palette:
  ```kotlin
  val Purple900 = Color(0xFF1A0033)  // Background
  val Purple700 = Color(0xFF4A1A7A)  // Card background
  val Purple500 = Color(0xFF7B2CBF)  // Accent
  val Gold = Color(0xFFFFD700)       // Primary
  val GoldLight = Color(0xFFFFE566)  // Secondary
  ```
- [ ] Create custom `ColorScheme` for dark theme
- [ ] Create `Typography.kt` with app fonts
- [ ] Create `Theme.kt` composable
- [ ] Apply theme in `MainActivity`

**Best practices to review**:
- Material3 color roles (primary, secondary, surface, etc.)
- Dynamic theming vs static theming
- Why use `CompositionLocalProvider` for theming?

**Checkpoint**: App shows purple background with themed status bar

---

## Phase 2: Data Layer

**Goal**: Build the foundation for data persistence and business logic.

### Step 2.1: Create Domain Models

**What you'll learn**: Kotlin data classes, sealed classes, immutability

**Tasks**:
- [ ] Create `Card.kt`:
  ```kotlin
  data class Card(
      val keyNumber: Int,
      val numbers: List<Int>,
      val bitPosition: Int
  )
  ```
- [ ] Create `GameState.kt`:
  ```kotlin
  data class GameState(
      val cards: List<Card>,
      val currentCardIndex: Int,
      val responses: List<Boolean>,
      val phase: GamePhase
  )

  sealed class GamePhase {
      object NotStarted : GamePhase()
      object InProgress : GamePhase()
      data class Revealing(val number: Int) : GamePhase()
      data class Complete(val number: Int) : GamePhase()
  }
  ```
- [ ] Create `NumberLayout.kt` enum
- [ ] Create `Settings.kt` data class

**Best practices to review**:
- Sealed classes vs enums for state
- Why immutable data classes?
- Default parameter values

**Checkpoint**: Models compile with no warnings

---

### Step 2.2: Implement Card Generator

**What you'll learn**: Bit manipulation, pure functions, algorithm implementation

**Tasks**:
- [ ] Create `CardGenerator.kt` in data package
- [ ] Implement `generateCards(maxNumber: Int): List<Card>`
  - Calculate number of bits needed: `log2(maxNumber + 1)`
  - For each bit position, create a card with all numbers having that bit set
- [ ] Implement `calculateResult(responses: List<Boolean>, cards: List<Card>): Int`
  - Sum key numbers where response is true
- [ ] Write unit tests for card generation

**Algorithm deep-dive**:
```kotlin
// For maxNumber = 63, we need 6 cards (bits 0-5)
// Card for bit 0 (keyNumber = 1): contains 1, 3, 5, 7, 9... (odd numbers)
// Card for bit 1 (keyNumber = 2): contains 2, 3, 6, 7, 10, 11...
// etc.

fun generateCards(maxNumber: Int): List<Card> {
    val numBits = (maxNumber + 1).toDouble().let {
        kotlin.math.log2(it).toInt()
    }
    return (0 until numBits).map { bitPosition ->
        val keyNumber = 1 shl bitPosition
        val numbers = (1..maxNumber).filter { (it and keyNumber) != 0 }
        Card(keyNumber, numbers, bitPosition)
    }
}
```

**Best practices to review**:
- Pure functions (no side effects)
- Bit manipulation operators (`shl`, `and`)
- Why put business logic in separate class vs ViewModel?

**Checkpoint**: Unit tests pass for card generation

---

### Step 2.3: Implement DataStore Repository

**What you'll learn**: DataStore Preferences, Kotlin Flow, Repository pattern

**Tasks**:
- [ ] Create `PreferencesRepository.kt`
- [ ] Define preference keys:
  ```kotlin
  private object PreferencesKeys {
      val MAX_NUMBER = intPreferencesKey("max_number")
      val NUMBER_LAYOUT = stringPreferencesKey("number_layout")
  }
  ```
- [ ] Implement `settingsFlow: Flow<Settings>` to observe settings
- [ ] Implement `suspend fun updateMaxNumber(value: Int)`
- [ ] Implement `suspend fun updateNumberLayout(layout: NumberLayout)`
- [ ] Handle default values gracefully

**Best practices to review**:
- Why DataStore over SharedPreferences?
- Flow vs LiveData for reactive data
- Repository pattern benefits
- Handling IO operations on correct dispatcher

**Checkpoint**: Can read/write preferences (test via ViewModel)

---

## Phase 3: Core UI Components

**Goal**: Build reusable, well-designed UI components.

### Step 3.1: Create Swipeable Card Component

**What you'll learn**: Gesture detection, animation, state hoisting

**Tasks**:
- [ ] Create `SwipeableCard.kt` composable
- [ ] Implement drag gesture with `detectDragGestures`
- [ ] Calculate rotation based on horizontal offset (±15°)
- [ ] Implement threshold detection (100dp)
- [ ] Add spring animation for snap-back
- [ ] Add fly-off animation when threshold exceeded
- [ ] Expose `onSwipeLeft` and `onSwipeRight` callbacks

**Key concepts**:
```kotlin
@Composable
fun SwipeableCard(
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val rotation = (offsetX / 20).coerceIn(-15f, 15f)

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .graphicsLayer { rotationZ = rotation }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { /* Check threshold, animate */ },
                    onDrag = { change, dragAmount ->
                        offsetX += dragAmount.x
                    }
                )
            }
    ) {
        content()
    }
}
```

**Best practices to review**:
- State hoisting pattern
- `remember` vs `rememberSaveable`
- Gesture composition
- Animation APIs (`animate*AsState`, `Animatable`)

**Checkpoint**: Card can be swiped and snaps back or flies off

---

### Step 3.2: Add Haptic Feedback

**What you'll learn**: System services, haptic patterns, Android vibration API

**Tasks**:
- [ ] Create `HapticManager.kt` utility
- [ ] Implement light tick for threshold crossing
- [ ] Implement medium impact for card release
- [ ] Implement success pattern for reveal
- [ ] Integrate haptics into SwipeableCard

**Best practices to review**:
- Context usage in Compose
- `LocalHapticFeedback` composition local
- When to use system haptics vs custom patterns

**Checkpoint**: Feel haptic feedback when swiping cards

---

### Step 3.3: Create Flip Card Component

**What you'll learn**: 3D transformations, animation sequencing

**Tasks**:
- [ ] Create `FlipCard.kt` composable
- [ ] Implement Y-axis rotation animation (0° → 180°)
- [ ] Show "back" content when rotation > 90°
- [ ] Add easing for dramatic effect (slow start, slow end)
- [ ] Make animation duration configurable (~1.5 seconds)

**Key concepts**:
```kotlin
@Composable
fun FlipCard(
    isFlipped: Boolean,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = EaseInOutCubic)
    )

    Box(
        modifier = Modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        }
    ) {
        if (rotation <= 90f) front() else back()
    }
}
```

**Best practices to review**:
- `graphicsLayer` for performant transforms
- Camera distance for 3D perspective
- Custom easing functions

**Checkpoint**: Card flips smoothly with 3D effect

---

### Step 3.4: Create Dots Indicator

**What you'll learn**: Simple composables, layout, state-driven UI

**Tasks**:
- [ ] Create `DotsIndicator.kt` composable
- [ ] Accept `total` and `current` parameters
- [ ] Style completed dots as gold, remaining as white/dim
- [ ] Add subtle scale animation for current dot

**Best practices to review**:
- Composable function naming conventions
- Modifier chaining order
- Accessibility considerations

**Checkpoint**: Dots indicator shows progress correctly

---

## Phase 4: Screens & Navigation

**Goal**: Build complete screens and connect them with navigation.

### Step 4.1: Set Up Navigation

**What you'll learn**: Compose Navigation, type-safe routes, navigation patterns

**Tasks**:
- [ ] Create `Navigation.kt` with sealed class routes:
  ```kotlin
  sealed class Screen(val route: String) {
      object Home : Screen("home")
      object Game : Screen("game")
      object Settings : Screen("settings")
  }
  ```
- [ ] Set up `NavHost` in MainActivity
- [ ] Create `NavController` and pass to screens
- [ ] Handle back button behavior

**Best practices to review**:
- Why sealed classes for routes?
- Single NavHost pattern
- Navigation state restoration

**Checkpoint**: Can navigate between placeholder screens

---

### Step 4.2: Implement Home Screen

**What you'll learn**: Screen composition, layout design, click handling

**Tasks**:
- [ ] Create `HomeScreen.kt`
- [ ] Add gradient background (purple shades)
- [ ] Create "Magic Number" title with glow effect
- [ ] Create large "Start" button with gold styling
- [ ] Add settings gear icon in top-right
- [ ] Optional: Add subtle star particle animation in background

**UI Structure**:
```kotlin
@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(gradientBrush)) {
        // Settings icon top-right
        // Title centered top
        // Start button centered
    }
}
```

**Best practices to review**:
- Gradient backgrounds in Compose
- Box vs Column vs Row usage
- Lambda callbacks for navigation

**Checkpoint**: Home screen looks polished, buttons work

---

### Step 4.3: Create Game ViewModel

**What you'll learn**: ViewModel, UI state management, event handling

**Tasks**:
- [ ] Create `GameViewModel.kt`
- [ ] Inject `CardGenerator` and `PreferencesRepository`
- [ ] Expose `uiState: StateFlow<GameUiState>`
- [ ] Implement `startGame()` - generates cards based on settings
- [ ] Implement `onSwipe(isYes: Boolean)` - records response, advances
- [ ] Implement `onRevealComplete()` - transitions to complete phase
- [ ] Implement `resetGame()`

**State design**:
```kotlin
data class GameUiState(
    val cards: List<Card> = emptyList(),
    val currentCardIndex: Int = 0,
    val responses: List<Boolean> = emptyList(),
    val phase: GamePhase = GamePhase.NotStarted,
    val numberLayout: NumberLayout = NumberLayout.ASCENDING
)
```

**Best practices to review**:
- StateFlow vs SharedFlow
- Unidirectional data flow
- ViewModel scope for coroutines
- Why not expose MutableStateFlow?

**Checkpoint**: ViewModel logic works (test via logs)

---

### Step 4.4: Implement Game Screen

**What you'll learn**: Complex screen composition, state consumption

**Tasks**:
- [ ] Create `GameScreen.kt`
- [ ] Collect ViewModel state with `collectAsState()`
- [ ] Show card stack with current card on top
- [ ] Display numbers on card based on layout setting
- [ ] Integrate `SwipeableCard` component
- [ ] Show `DotsIndicator` at bottom
- [ ] Handle swipe callbacks to ViewModel
- [ ] Transition to reveal when all cards complete

**Layout challenge**: Display numbers in different layouts (grid, ascending, scattered, circular)
```kotlin
@Composable
fun CardNumbers(
    numbers: List<Int>,
    layout: NumberLayout,
    keyNumber: Int
) {
    when (layout) {
        NumberLayout.ASCENDING -> AscendingLayout(numbers, keyNumber)
        NumberLayout.GRID -> GridLayout(numbers, keyNumber)
        NumberLayout.SCATTERED -> ScatteredLayout(numbers, keyNumber)
        NumberLayout.CIRCULAR -> CircularLayout(numbers, keyNumber)
    }
}
```

**Best practices to review**:
- Collecting Flow in Compose
- Avoiding recomposition issues
- State-driven animations

**Checkpoint**: Can play through entire card sequence

---

### Step 4.5: Implement Reveal Screen

**What you'll learn**: Animation coordination, delayed actions

**Tasks**:
- [ ] Create `RevealScreen.kt` (or integrate into GameScreen)
- [ ] Use `FlipCard` component for dramatic reveal
- [ ] Show card back initially, flip to reveal number
- [ ] Add sparkle/celebration animation after flip
- [ ] Show "Play Again" button after animation completes
- [ ] Strong haptic on reveal

**Animation sequence**:
1. Card appears (back showing)
2. After 500ms delay, card starts flipping
3. Flip takes 1.5 seconds
4. Sparkle animation triggers when flip completes
5. "Play Again" button fades in

**Best practices to review**:
- `LaunchedEffect` for side effects
- Animation coordination with `Animatable`
- `AnimatedVisibility` for enter/exit

**Checkpoint**: Full game loop works end-to-end

---

### Step 4.6: Implement Settings Screen

**What you'll learn**: Form-style UI, preference editing, reactive updates

**Tasks**:
- [ ] Create `SettingsViewModel.kt`
- [ ] Create `SettingsScreen.kt`
- [ ] Add range selector (radio buttons or segmented control)
  - 1-31 (5 cards)
  - 1-63 (6 cards)
  - 1-127 (7 cards)
- [ ] Add layout style selector with visual previews
- [ ] Add "How It Works" expandable section
- [ ] Save changes immediately (no save button)
- [ ] Navigate back via toolbar or back button

**Best practices to review**:
- Two-way data binding pattern
- Immediate save vs explicit save
- Expandable/collapsible sections

**Checkpoint**: Settings persist across app restarts

---

## Phase 5: Polish & Edge Cases

**Goal**: Handle all edge cases and add finishing touches.

### Step 5.1: Handle Edge Cases

**What you'll learn**: Defensive programming, edge case testing

**Tasks**:
- [ ] Handle "all NO" result (show 0 with appropriate message)
- [ ] Handle back button during game (confirm abandon or just go back)
- [ ] Ensure rapid swipes don't break animation queue
- [ ] Test orientation lock (portrait only)
- [ ] Verify app survives process death and restoration

**Edge cases to test**:
| Scenario | Expected Behavior |
|----------|-------------------|
| Swipe all NO | Shows "0" |
| Swipe all YES (1-63) | Shows "63" |
| Back during game | Returns to home |
| Rotate device | Stays portrait |
| Kill app mid-game | Fresh start on relaunch |

**Best practices to review**:
- Process death handling
- Configuration change handling
- Testing edge cases systematically

**Checkpoint**: All edge cases handled gracefully

---

### Step 5.2: Add Visual Polish

**What you'll learn**: Animations, shadows, visual effects

**Tasks**:
- [ ] Add glow effect to title text
- [ ] Add subtle card shadow
- [ ] Add card stack peek (next card visible behind current)
- [ ] Smooth all transitions with appropriate easing
- [ ] Add loading placeholder if needed (probably not needed)

**Optional enhancements**:
- Star particle background animation on home screen
- Shimmer effect on gold elements
- Pulsing glow on "Start" button

**Best practices to review**:
- Performance impact of animations
- When to use hardware layers
- Subtle vs distracting animations

**Checkpoint**: App feels polished and magical

---

### Step 5.3: Accessibility Review

**What you'll learn**: Android accessibility, TalkBack support

**Tasks**:
- [ ] Add content descriptions to all interactive elements
- [ ] Ensure touch targets are minimum 48dp
- [ ] Test with TalkBack enabled
- [ ] Verify color contrast meets WCAG guidelines
- [ ] Add semantic descriptions for card content

**Best practices to review**:
- `semantics` modifier usage
- Custom accessibility actions
- Testing with accessibility tools

**Checkpoint**: App is usable with TalkBack

---

### Step 5.4: Create App Icon

**What you'll learn**: Adaptive icons, image assets

**Tasks**:
- [ ] Design app icon (magic cards with sparkles)
- [ ] Create adaptive icon with foreground and background layers
- [ ] Generate all density variants
- [ ] Update `AndroidManifest.xml`

**Best practices to review**:
- Adaptive icon specifications
- Safe zone for icon content
- Monochrome icon for themed icons (Android 13+)

**Checkpoint**: App icon appears correctly on all launchers

---

## Phase 6: Testing & Release

**Goal**: Ensure quality and prepare for distribution.

### Step 6.1: Write Unit Tests

**What you'll learn**: Testing ViewModels, testing pure functions

**Tasks**:
- [ ] Test `CardGenerator.generateCards()` for all ranges
- [ ] Test `CardGenerator.calculateResult()` with various inputs
- [ ] Test `GameViewModel` state transitions
- [ ] Test edge cases (all YES, all NO, mixed)

**Best practices to review**:
- AAA pattern (Arrange, Act, Assert)
- Testing coroutines with `runTest`
- Mocking dependencies

**Checkpoint**: All tests pass

---

### Step 6.2: Write UI Tests

**What you'll learn**: Compose testing, UI automation

**Tasks**:
- [ ] Test navigation flow (Home → Game → Reveal → Home)
- [ ] Test swipe gestures
- [ ] Test settings persistence
- [ ] Test full game play-through

**Best practices to review**:
- `ComposeTestRule` usage
- Semantic matchers
- Test robots pattern

**Checkpoint**: UI tests pass

---

### Step 6.3: Final Review & Release Prep

**What you'll learn**: Release checklist, code review

**Tasks**:
- [ ] Remove all debug logs
- [ ] Enable ProGuard/R8 minification
- [ ] Review all TODO comments
- [ ] Verify app signing configuration
- [ ] Create release build and test on device
- [ ] Write app store description (if publishing)

**Best practices to review**:
- Release vs debug build differences
- ProGuard rules for libraries
- Version naming conventions

**Checkpoint**: Release APK ready

---

## Summary

| Phase | Focus | Key Learnings |
|-------|-------|---------------|
| 1 | Setup | Project structure, Gradle, theming |
| 2 | Data | Models, algorithms, DataStore |
| 3 | Components | Gestures, animations, reusability |
| 4 | Screens | Navigation, ViewModels, state management |
| 5 | Polish | Edge cases, accessibility, visual effects |
| 6 | Testing | Unit tests, UI tests, release prep |

**Estimated steps**: 23 steps across 6 phases

Each step is designed to be reviewable and teaches specific Android/Compose concepts. Take your time with each step to understand the "why" behind each decision.
