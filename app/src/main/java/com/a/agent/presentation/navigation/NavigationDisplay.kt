package com.a.agent.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.a.agent.presentation.chatconversation.TextToTextScreen
import com.a.agent.presentation.conversationmanager.ConversationManagerScreen
import com.a.agent.presentation.home.HomeScreen
import com.a.agent.presentation.imageview.ImageViewScreen
import com.a.agent.presentation.llm.ModelScreen
import com.a.agent.presentation.llmmanager.ModelManagerScreen
import com.a.agent.presentation.settings.SettingsScreen
import org.koin.compose.koinInject

@Composable
fun NavigationDisplay(
    navigationEvent: NavigationDisplayBackStack = koinInject()
) {
    val navBackStack = rememberNavBackStack(
        NavigationRoute.HomeScreen
    )
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(navigationEvent.event) {
        navigationEvent.event.collect { event ->
            when (event) {
                BackStack.PopBackStack -> {
                    navBackStack.popBackStack()
                }
                is BackStack.Replace -> {
                    navBackStack.clear()
                    navBackStack.add(event.route)
                }
                is BackStack.Navigate -> {
                    navBackStack.add(event.route)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        val transitionDuration = 300
        NavDisplay(
            modifier = Modifier
                .fillMaxSize(),
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(transitionDuration, easing = FastOutSlowInEasing)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(transitionDuration, easing = FastOutSlowInEasing)
                )
            },
            popTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(transitionDuration, easing = FastOutSlowInEasing)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(transitionDuration, easing = FastOutSlowInEasing)
                )
            },
            predictivePopTransitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(transitionDuration, easing = FastOutSlowInEasing)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(transitionDuration, easing = FastOutSlowInEasing)
                )
            },
            backStack = navBackStack
        ) { navKey ->
            when (navKey) {
                NavigationRoute.HomeScreen -> {
                    NavEntry(navKey) {
                        HomeScreen(navBackStack)
                    }
                }
                NavigationRoute.SettingsScreen -> {
                    NavEntry(navKey) {
                        SettingsScreen(navBackStack)
                    }
                }
                is NavigationRoute.ConversationScreen -> {
                    NavEntry(navKey) {
                        TextToTextScreen(navBackStack, navKey.conversationId)
                    }
                }
                is NavigationRoute.ConversationManagerScreen -> {
                    NavEntry(navKey) {
                        ConversationManagerScreen(navBackStack, navKey.conversationId)
                    }
                }
                is NavigationRoute.LlmScreen -> {
                    NavEntry(navKey) {
                        ModelScreen(navBackStack)
                    }
                }
                is NavigationRoute.LlmManagerScreen -> {
                    NavEntry(navKey) {
                        ModelManagerScreen(navBackStack, navKey.modelId)
                    }
                }
                is NavigationRoute.ImageViewScreen -> {
                    NavEntry(navKey) {
                        ImageViewScreen(navBackStack, navKey.imagePath)
                    }
                }
                else -> error("Unknown Nav Key: $navKey")
            }
        }
    }
}

fun NavBackStack<NavKey>.popBackStack() {
    this.removeAt(this.lastIndex)
}