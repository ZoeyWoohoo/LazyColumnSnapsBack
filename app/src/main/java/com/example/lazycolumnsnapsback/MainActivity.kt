@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.lazycolumnsnapsback

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lazycolumnsnapsback.ui.theme.LazyColumnSnapsBackTheme
import com.example.lazycolumnsnapsback.ui.theme.SystemBarIconColor
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            if (!LocalInspectionMode.current) {
                SystemBarIconColor(isLight = false)
            }

            LazyColumnSnapsBackTheme {
                val navController = rememberNavController()

                SharedTransitionLayout {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.HOME,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(300)
                                )
                            },
                            exitTransition = {
                                ExitTransition.None
                            },
                            popEnterTransition = {
                                EnterTransition.None
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(300)
                                )
                            }
                        ) {
                            composableWithCompositionLocal(route = Routes.HOME) {
                                HomeScreen(navController)
                            }
                            composableWithCompositionLocal(route = Routes.CHAT) {
                                ChatScreen(viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    Box {
        Text(
            text = "Jump",
            modifier = Modifier
                .align(Alignment.Center)
                .border(1.dp, Color.Black)
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .blankClick {
                    navController.navigate(Routes.CHAT)
                }
        )
    }
}

@Composable
fun ChatScreen(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.sendIntent(Intent.LoadData)
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ScrollListToBottom -> {
                    if (effect.animate) {
                        listState.animateScrollToItem(effect.index)
                    } else {
                        listState.scrollToItem(effect.index)
                    }
                }
            }
        }
    }

    ChatList(
        listState = listState,
        state = state,
        modifier = Modifier
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    )
}

@Composable
fun ChatList(
    listState: LazyListState,
    state: State,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Box {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .padding(
                    horizontal = 14.dp,
                    vertical = 20.dp,
                )
        ) {
            items(
                count = state.items.size,
                key = { index ->
                    state.items[index].toString()
                }
            ) { index ->
                val data = state.items[index]
                val left = index % 2 == 1
                val cornerRadius = 16.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (left) 400.dp else 100.dp)
                        .padding(
                            top = if (index > 0) 16.dp else 0.dp,
                            end = if (left) 60.dp else 0.dp,
                            bottom = 0.dp,
                            start = if (left) 0.dp else 90.dp
                        )
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            RoundedCornerShape(
                                topStart = if (left) 0.dp else cornerRadius,
                                topEnd = if (left) cornerRadius else 0.dp,
                                bottomEnd = cornerRadius,
                                bottomStart = cornerRadius
                            )
                        )
                ) {
                    Text(
                        text = data.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Text(
            text = "Scroll To Bottom ↓",
            fontSize = 14.sp,
            modifier = Modifier
                .padding(end = 14.dp, bottom = 40.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .align(Alignment.BottomEnd)
                .blankClick {
                    coroutineScope.launch {
                        listState.scrollToItem(state.items.lastIndex)
                    }
                }
        )
    }
}

val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

fun NavGraphBuilder.composableWithCompositionLocal(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (
        @JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
    )? = null,
    exitTransition: (
        @JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
    )? = null,
    popEnterTransition: (
        @JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
    )? = enterTransition,
    popExitTransition: (
        @JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
    )? = exitTransition,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route,
        arguments,
        deepLinks,
        enterTransition,
        exitTransition,
        popEnterTransition,
        popExitTransition
    ) {
        CompositionLocalProvider(
            LocalAnimatedVisibilityScope provides this
        ) {
            content(it)
        }
    }
}