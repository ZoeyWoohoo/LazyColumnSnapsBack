# LazyColumn Snaps Back Issue with SharedTransitionLayout

## Problem Description

In Compose applications using `SharedTransitionLayout`, when `LazyColumn` calls `listState.scrollToItem(lastIndex)`, the list automatically "snaps back" to the bottom position when users try to scroll down, preventing them from normally viewing the list content.

## Demo Video

You can find a screen recording demonstrating the issue here:

[▶️ Demo Raw File (MP4)](recorder/Screenrecorder-2025-07-28-20-41-00-442.mp4)

https://github.com/user-attachments/assets/1b2b6398-fb6c-41d1-964d-c0b6920cdb8f

## Steps to Reproduce

1. Launch the app and click the "Jump" button to enter the chat interface
2. The app will automatically load data and scroll to the bottom of the list (via `scrollToItem(lastIndex)`)
3. Try to scroll up the list to view historical messages
4. The list will automatically snap back to the bottom, preventing normal scrolling

## Technical Environment

- **Android Compose Version**: Using the latest Compose BOM
- **SharedTransitionLayout**: Experimental API (`@OptIn(ExperimentalSharedTransitionApi::class)`)
- **LazyColumn**: Using `rememberLazyListState()` to manage scroll state
- **Navigation**: Using Compose Navigation with SharedTransitionLayout

## Code Implementation

### Key Code Snippets

```kotlin
// MainActivity.kt - SharedTransitionLayout wrapper
SharedTransitionLayout {
    CompositionLocalProvider(
        LocalSharedTransitionScope provides this
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            // ... navigation configuration
        ) {
            composableWithCompositionLocal(route = Routes.CHAT) {
                ChatScreen(viewModel)
            }
        }
    }
}

// ChatScreen.kt - LazyColumn scroll logic
LaunchedEffect(viewModel.effect) {
    viewModel.effect.collect { effect ->
        when (effect) {
            is Effect.ScrollListToBottom -> {
                if (effect.animate) {
                    listState.animateScrollToItem(effect.index)
                } else {
                    listState.scrollToItem(effect.index) // This triggers the issue
                }
            }
        }
    }
}
```

### Issue Trigger Conditions

1. Using `SharedTransitionLayout` to wrap the entire navigation container
2. Calling `listState.scrollToItem(lastIndex)` in `LazyColumn`
3. User attempts to manually scroll the list

## Expected Behavior

Users should be able to:
- Normally scroll up to view historical messages
- Keep the list at the user's scrolled position
- Not automatically snap back to the bottom

## Actual Behavior

- When users scroll up, the list automatically snaps back to the bottom
- Unable to normally view historical messages
- Scroll experience is broken

## Impact Scope

This issue affects all application scenarios using `SharedTransitionLayout` with `LazyColumn`, especially:
- Chat applications
- Message lists
- List interfaces that need to automatically scroll to the bottom

## Temporary Solutions

Currently, the issue can be mitigated by:
1. Removing the `SharedTransitionLayout` wrapper (if shared transition animations are not needed)

## Related Files

- `MainActivity.kt`: Main interface and SharedTransitionLayout configuration
- `MainViewModel.kt`: Data loading and scroll logic
- `MainContract.kt`: State and intent definitions

## Reproduction Project

This is a minimal reproduction project that demonstrates the core issue:
- Simple chat interface simulation
- Auto-scroll to bottom functionality
- SharedTransitionLayout integration