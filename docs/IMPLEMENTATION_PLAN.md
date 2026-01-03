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

### Step 1.4: Set Up Theme ✅

**What you'll learn**: Material3 theming, custom color schemes, typography

**Tasks**:
- [x] Create `Color.kt` with mystical purple/gold palette:
  ```kotlin
  val Purple900 = Color(0xFF1A0033)  // Background
  val Purple700 = Color(0xFF4A1A7A)  // Card background
  val Purple500 = Color(0xFF7B2CBF)  // Accent
  val Gold = Color(0xFFFFD700)       // Primary
  val GoldLight = Color(0xFFFFE566)  // Secondary
  ```
- [x] Create custom `ColorScheme` for dark theme (MysticalColorScheme)
- [x] Create `Theme.kt` composable with status bar color handling
- [x] Apply theme in `MainActivity` with placeholder content

**Implementation notes**:
- Dark-only theme to maintain mystical atmosphere
- Status bar automatically matches background color
- All Material3 color roles defined (primary, secondary, surface, etc.)

**Best practices to review**:
- Material3 color roles (primary, secondary, surface, etc.)
- Dynamic theming vs static theming
- Why use `CompositionLocalProvider` for theming?

**Checkpoint**: ✅ App shows purple background with gold "Magic Number" text

---

## Phase 2: Data Layer ✅

**Goal**: Build the foundation for data persistence and business logic.

> **Note**: Phase 2 was completed during Phase 1.3 (Package Structure) to provide working placeholders.

### Step 2.1: Create Domain Models ✅

**What you'll learn**: Kotlin data classes, sealed classes, immutability

**Tasks**:
- [x] Create `Card.kt` - Represents a single card with keyNumber, numbers list, bitPosition
- [x] Create `GameState.kt` - Game state with GamePhase sealed class
- [x] Create `Settings.kt` - Settings data class with NumberLayout enum

**Best practices to review**:
- Sealed classes vs enums for state
- Why immutable data classes?
- Default parameter values

**Checkpoint**: ✅ Models compile with no warnings

---

### Step 2.2: Implement Card Generator ✅

**What you'll learn**: Bit manipulation, pure functions, algorithm implementation

**Tasks**:
- [x] Create `CardGenerator.kt` in data package (object, not class)
- [x] Implement `generateCards(maxNumber: Int): List<Card>`
- [x] Implement `calculateResult(responses: List<Boolean>, cards: List<Card>): Int`
- [ ] Write unit tests for card generation (deferred to Phase 6)

**Best practices to review**:
- Pure functions (no side effects)
- Bit manipulation operators (`shl`, `and`)
- Why put business logic in separate class vs ViewModel?

**Checkpoint**: ✅ CardGenerator implemented

---

### Step 2.3: Implement DataStore Repository ✅

**What you'll learn**: DataStore Preferences, Kotlin Flow, Repository pattern

**Tasks**:
- [x] Create `PreferencesRepository.kt`
- [x] Define preference keys (MAX_NUMBER, NUMBER_LAYOUT)
- [x] Implement `settingsFlow: Flow<Settings>` to observe settings
- [x] Implement `suspend fun updateMaxNumber(value: Int)`
- [x] Implement `suspend fun updateNumberLayout(layout: NumberLayout)`
- [x] Handle default values (63, ASCENDING)

**Best practices to review**:
- Why DataStore over SharedPreferences?
- Flow vs LiveData for reactive data
- Repository pattern benefits
- Handling IO operations on correct dispatcher

**Checkpoint**: ✅ PreferencesRepository implemented

---

## Phase 3: Core UI Components

**Goal**: Build reusable, well-designed UI components.

### Step 3.1: Create Swipeable Card Component ✅

**What you'll learn**: Gesture detection, animation, state hoisting

**Tasks**:
- [x] Create `SwipeableCard.kt` composable
- [x] Implement drag gesture with `detectDragGestures`
- [x] Calculate rotation based on horizontal offset (±15°)
- [x] Implement threshold detection (100dp)
- [x] Add spring animation for snap-back
- [x] Add fly-off animation when threshold exceeded
- [x] Expose `onSwipeLeft` and `onSwipeRight` callbacks

**Implementation highlights**:
- Uses `Animatable<Offset>` for smooth, interruptible animations
- `graphicsLayer` for efficient rotation transforms
- Spring physics with `Spring.DampingRatioMediumBouncy`
- Card follows both X and Y during drag, flies off maintaining Y position

**Best practices to review**:
- State hoisting pattern
- `remember` vs `rememberSaveable`
- Gesture composition
- Animation APIs (`animate*AsState`, `Animatable`)

**Checkpoint**: ✅ Card can be swiped and snaps back or flies off

---

### Step 3.2: Add Haptic Feedback ✅

**What you'll learn**: System services, haptic patterns, Android vibration API

**Tasks**:
- [x] Implement light tick for threshold crossing (using `LocalHapticFeedback`)
- [x] Implement medium impact for card release
- [x] Integrate haptics into SwipeableCard

**Implementation notes**:
- Used `LocalHapticFeedback` composition local (no separate manager needed)
- `TextHandleMove` for threshold crossing (light tick)
- `LongPress` for confirmed swipe (stronger feedback)
- Tracks threshold state to avoid repeated haptic triggers

**Best practices to review**:
- Context usage in Compose
- `LocalHapticFeedback` composition local
- When to use system haptics vs custom patterns

**Checkpoint**: ✅ Haptic feedback integrated into SwipeableCard

---

### Step 3.3: Create Flip Card Component ✅

**What you'll learn**: 3D transformations, animation sequencing

**Tasks**:
- [x] Create `FlipCard.kt` composable
- [x] Implement Y-axis rotation animation (0° → 180°)
- [x] Show "back" content when rotation > 90°
- [x] Add cubic ease-in-out for dramatic effect
- [x] Animation duration: 1500ms
- [x] Counter-rotate back content to prevent mirroring
- [x] Haptic feedback on flip completion

**Implementation highlights**:
- Uses `Animatable` with custom cubic easing
- Camera distance = 12f * density for 3D perspective
- Back content counter-rotated 180° to prevent text mirroring
- Includes overloaded version without callback

**Best practices to review**:
- `graphicsLayer` for performant transforms
- Camera distance for 3D perspective
- Custom easing functions

**Checkpoint**: ✅ Card flips smoothly with 3D effect and haptic

---

### Step 3.4: Create Dots Indicator ✅

**What you'll learn**: Simple composables, layout, state-driven UI

**Tasks**:
- [x] Create `DotsIndicator.kt` composable
- [x] Accept `total` and `current` parameters
- [x] Style completed dots as gold (primary), remaining as dim white
- [x] Add spring animation for current dot size (8dp → 12dp)

**Implementation highlights**:
- Uses `Row` with `Arrangement.spacedBy` for clean spacing
- `animateDpAsState` with spring for smooth size transition
- Extracted `Dot` private composable for reusability
- Uses MaterialTheme colors for consistency

**Best practices to review**:
- Composable function naming conventions
- Modifier chaining order
- Accessibility considerations

**Checkpoint**: ✅ Dots indicator shows progress with animation

---

## Phase 4: Screens & Navigation ✅

**Goal**: Build complete screens and connect them with navigation.

### Step 4.1: Set Up Navigation ✅

**What you'll learn**: Compose Navigation, type-safe routes, navigation patterns

**Tasks**:
- [x] Create `Navigation.kt` with sealed class routes
- [x] Set up `NavHost` in MainActivity
- [x] Create `NavController` and pass to screens
- [x] Handle back button behavior

**Implementation highlights**:
- `Screen` sealed class with `Home`, `Game`, `Settings` routes
- `MagicNumberNavHost` composable wrapping `NavHost`
- `BackHandler` used in GameScreen for back button handling
- Navigation callbacks passed to screens as lambdas

**Best practices to review**:
- Why sealed classes for routes?
- Single NavHost pattern
- Navigation state restoration

**Checkpoint**: ✅ Can navigate between all screens

---

### Step 4.2: Implement Home Screen ✅

**What you'll learn**: Screen composition, layout design, click handling

**Tasks**:
- [x] Create `HomeScreen.kt`
- [x] Add gradient background (purple shades)
- [x] Create "Magic Number" title with glow effect
- [x] Create large "Start" button with gold styling
- [x] Add settings gear icon in top-right

**Implementation highlights**:
- `MagicTitle` composable with blur layer behind for glow effect
- `StartButton` with gold border using `Brush.linearGradient`
- Settings icon uses `Icons.Default.Settings`
- Box layout with `Alignment.TopEnd` for settings icon

**Best practices to review**:
- Gradient backgrounds in Compose
- Box vs Column vs Row usage
- Lambda callbacks for navigation

**Checkpoint**: ✅ Home screen looks polished, buttons work

---

### Step 4.3: Create ViewModels ✅

**What you'll learn**: ViewModel, UI state management, event handling

**Tasks**:
- [x] Create `GameViewModel.kt` with game state management
- [x] Create `SettingsViewModel.kt` with settings exposure
- [x] Expose `uiState: StateFlow<GameState>`
- [x] Implement `startGame()`, `onSwipe()`, `onRevealComplete()`, `resetGame()`
- [x] Use ViewModelProvider.Factory for dependency injection

**Implementation highlights**:
- `GameViewModel` auto-starts game in init block
- `SettingsViewModel` exposes `settingsFlow` as StateFlow
- Factory companion objects for manual DI (no Hilt/Koin)
- `WhileSubscribed(5000)` for efficient Flow collection

**Best practices to review**:
- StateFlow vs SharedFlow
- Unidirectional data flow
- ViewModel scope for coroutines
- Why not expose MutableStateFlow?

**Checkpoint**: ✅ ViewModels properly manage state

---

### Step 4.4: Implement Game Screen ✅

**What you'll learn**: Complex screen composition, state consumption

**Tasks**:
- [x] Create `GameScreen.kt` with phase-based rendering
- [x] Collect ViewModel state with `collectAsState()`
- [x] Display numbers on card using `FlowRow`
- [x] Integrate `SwipeableCard` component
- [x] Show `DotsIndicator` at bottom
- [x] Handle swipe callbacks to ViewModel
- [x] Use `key()` to reset card state between transitions

**Implementation highlights**:
- `when (uiState.phase)` for rendering different game phases
- `GameInProgressContent` handles card swiping
- `key(currentCardIndex)` ensures SwipeableCard resets state per card
- Layout respects `numberLayout` setting (sorted/shuffled)

**Best practices to review**:
- Collecting Flow in Compose
- Avoiding recomposition issues
- `key()` composable for state reset

**Checkpoint**: ✅ Can play through entire card sequence

---

### Step 4.5: Implement Reveal ✅

**What you'll learn**: Animation coordination, delayed actions

**Tasks**:
- [x] Integrate reveal into `GameScreen.kt`
- [x] Use `FlipCard` component for dramatic reveal
- [x] Show card back (?) initially, flip to reveal number
- [x] Show "Play Again" button after flip completes
- [x] Transition through Revealing → Complete phases

**Implementation highlights**:
- `RevealContent` composable with FlipCard
- `GamePhase.Revealing(number)` triggers flip animation
- `onFlipComplete` callback transitions to `GamePhase.Complete`
- `CompleteContent` shows large number and Play Again button

**Best practices to review**:
- `LaunchedEffect` for side effects
- Animation coordination with `Animatable`
- State machine pattern for game phases

**Checkpoint**: ✅ Full game loop works end-to-end

---

### Step 4.6: Implement Settings Screen ✅

**What you'll learn**: Form-style UI, preference editing, reactive updates

**Tasks**:
- [x] Create `SettingsViewModel.kt`
- [x] Create `SettingsScreen.kt`
- [x] Add range selector (1-31, 1-63, 1-127)
- [x] Add layout style selector (Ascending, Grid, Scattered, Circular)
- [x] Add "How It Works" expandable section
- [x] Save changes immediately (no save button)
- [x] Navigate back via toolbar back button

**Implementation highlights**:
- `SettingsSection` for consistent section styling
- `SelectableOption` for radio-button-like selection
- `HowItWorksSection` with `AnimatedVisibility` for expand/collapse
- Changes saved immediately via ViewModel methods

**Best practices to review**:
- Two-way data binding pattern
- Immediate save vs explicit save
- Expandable/collapsible sections

**Checkpoint**: ✅ Settings persist across app restarts

---

## Phase 5: Polish & Edge Cases ✅

**Goal**: Handle all edge cases and add finishing touches.

### Step 5.1: Handle Edge Cases ✅

**What you'll learn**: Defensive programming, edge case testing

**Tasks**:
- [x] Handle "all NO" result (show 0 with appropriate message)
- [x] Handle back button during game (returns to home)
- [x] Ensure rapid swipes don't break animation queue
- [x] Orientation lock (portrait only) - configured in manifest

**Implementation highlights**:
- Added `isAnimatingOut` flag to SwipeableCard to prevent interaction during fly-off
- Special message in CompleteContent for number 0: "You said NO to all cards!"
- BackHandler already navigates back to home

**Edge cases handled**:
| Scenario | Behavior |
|----------|-------------------|
| Swipe all NO | Shows "0" with special message |
| Swipe all YES (1-63) | Shows "63" |
| Back during game | Returns to home |
| Rotate device | Stays portrait (manifest lock) |
| Kill app mid-game | Fresh start on relaunch |

**Checkpoint**: ✅ All edge cases handled gracefully

---

### Step 5.2: Add Visual Polish ✅

**What you'll learn**: Animations, shadows, visual effects

**Tasks**:
- [x] Title glow effect (already in HomeScreen)
- [x] Add card shadow with `graphicsLayer.shadowElevation`
- [x] Add card stack peek (next card visible behind current)
- [x] Smooth transitions with spring physics

**Implementation highlights**:
- Card shadow using `shadowElevation = 16.dp`
- Next card peek with `scaleX/Y = 0.95f`, `alpha = 0.6f`, `translationY = 16dp`
- CardContent accepts `showShadow` parameter for flexibility

**Best practices to review**:
- Performance impact of animations
- When to use hardware layers
- Subtle vs distracting animations

**Checkpoint**: ✅ App feels polished and magical

---

### Step 5.3: Accessibility Review ✅

**What you'll learn**: Android accessibility, TalkBack support

**Tasks**:
- [x] Add content descriptions to all interactive elements
- [x] Touch targets already at 48dp+ (buttons, cards)
- [x] Add semantic descriptions for card content
- [x] Add custom accessibility actions for swipe gestures

**Implementation highlights**:
- SwipeableCard: Custom `CustomAccessibilityAction` for "Answer Yes" and "Answer No"
- CardContent: Full content description with all numbers listed
- TalkBack reads: "Card 1. Numbers on this card: 1, 3, 5... Swipe right for Yes, swipe left for No."

**Best practices to review**:
- `semantics` modifier usage
- Custom accessibility actions
- Testing with accessibility tools

**Checkpoint**: ✅ App is usable with TalkBack

---

### Step 5.4: Create App Icon ✅

**What you'll learn**: Adaptive icons, image assets

**Tasks**:
- [x] Design app icon (magic card with question mark and sparkles)
- [x] Create adaptive icon with foreground and background layers
- [x] Create monochrome icon for themed icons (Android 13+)
- [x] Update adaptive icon XML references

**Implementation highlights**:
- Background: Purple gradient (#2D0A4E to #1A0033)
- Foreground: Card shape with gold border, question mark, sparkle stars
- Monochrome: Simplified card with question mark cutout
- Uses vector drawables for all density support

**Best practices to review**:
- Adaptive icon specifications (108dp viewport, 66dp safe zone)
- Vector drawables for scalability
- Monochrome icon for Material You

**Checkpoint**: ✅ App icon appears correctly on all launchers

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
