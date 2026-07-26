package com.a.agent.presentation.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.a.agent.presentation.conversation.TextToTextScreen
import com.a.agent.presentation.conversationmanager.ConversationManagerScreen
import com.a.agent.presentation.home.HomeScreen
import com.a.agent.presentation.imageview.ImageViewScreen
import com.a.agent.presentation.model.ModelScreen
import com.a.agent.presentation.modelmanager.ModelManagerScreen
import com.a.agent.presentation.upsertlocalllm.UpsertLocalModelScreen
import com.a.agent.presentation.workflow.WorkFlowScreen
import com.a.agent.presentation.workflowmanagerscreen.WorkflowManagerScreen
import org.koin.compose.koinInject

@Composable
fun NavigationDisplay(
    navigationEvent: NavigationDisplayEvent = koinInject()
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
                is Event.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(
                        message = event.message,
                        withDismissAction = true
                    )
                }
                Event.PopBackStack -> {
                    navBackStack.popBackStack()
                }
                is Event.Replace -> {
                    navBackStack.clear()
                    navBackStack.add(event.route)
                }
                is Event.Navigate -> {
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
                NavigationRoute.WorkflowScreen -> NavEntry(
                    key = navKey,
                    content = { WorkFlowScreen(navBackStack) }
                )
                NavigationRoute.WorkflowManagerScreen -> NavEntry(
                    key = navKey,
                    content = { WorkflowManagerScreen(navBackStack) }
                )
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
                is NavigationRoute.ModelScreen -> {
                    NavEntry(navKey) {
                        ModelScreen(navBackStack)
                    }
                }
                is NavigationRoute.ModelManagerScreen -> {
                    NavEntry(navKey) {
                        ModelManagerScreen(navBackStack, navKey.modelId)
                    }
                }
                is NavigationRoute.UpsertLocalModelScreen -> {
                    NavEntry(navKey) {
                        UpsertLocalModelScreen(navBackStack, navKey.modelId)
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
        SnackbarHost(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .padding(bottom = 50.dp),
            hostState = snackBarHostState,
        )
    }
}

fun NavBackStack<NavKey>.popBackStack() {
    this.removeAt(this.lastIndex)
}