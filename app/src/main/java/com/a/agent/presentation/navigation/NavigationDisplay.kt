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
import com.a.agent.presentation.home.HomeScreen
import com.a.agent.presentation.model.ModelScreen
import com.a.agent.presentation.modelmanager.ModelManagerScreen
import com.a.agent.presentation.conversation.TextToTextScreen
import com.a.agent.presentation.imageview.ImageViewScreen
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
            .imePadding(),
        content = {
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
                when (navKey as NavigationRoute) {
                    NavigationRoute.HomeScreen -> NavEntry(
                        key = navKey,
                        content = { HomeScreen(navBackStack) }
                    )
                    NavigationRoute.WorkflowScreen -> NavEntry(
                        key = navKey,
                        content = { WorkFlowScreen(navBackStack) }
                    )
                    NavigationRoute.WorkflowManagerScreen -> NavEntry(
                        key = navKey,
                        content = { WorkflowManagerScreen(navBackStack) }
                    )
                    NavigationRoute.ModelScreen -> NavEntry(
                        key = navKey,
                        content = { ModelScreen(navBackStack) }
                    )
                    is NavigationRoute.ModelManagerScreen -> NavEntry(
                        key = navKey,
                        content = {
                            val route = navKey as NavigationRoute.ModelManagerScreen
                            ModelManagerScreen(navBackStack, route.modelId)
                        }
                    )
                    is NavigationRoute.ConversationScreen -> NavEntry(
                        key = navKey,
                        content = {
                            val route = navKey as NavigationRoute.ConversationScreen
                            TextToTextScreen(navBackStack, route.conversationId)
                        }
                    )
                    is NavigationRoute.ImageViewScreen -> NavEntry(
                        key = navKey,
                        content = {
                            val route = navKey as NavigationRoute.ImageViewScreen
                            ImageViewScreen(navBackStack, route.imagePath)
                        }
                    )
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
    )
}

fun NavBackStack<NavKey>.popBackStack() {
    this.removeAt(this.lastIndex)
}