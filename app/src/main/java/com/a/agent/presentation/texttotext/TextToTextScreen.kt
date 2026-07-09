package com.a.agent.presentation.texttotext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.texttotext.component.ChatBubble
import com.a.agent.presentation.util.component.CustomFadeBox
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomSlideUpAnimatedVisibility
import com.a.agent.presentation.util.component.CustomSurfaceIconButton
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.CustomTransparentTextField
import com.a.agent.presentation.util.component.loadingIndicator
import com.a.agent.presentation.util.component.message
import io.ktor.http.parametersOf
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun TextToTextScreen(
    navBackStack: NavBackStack<NavKey>,
    modelId: String,
    viewModel: TextToTextViewModel = koinViewModel {
        parametersOf(modelId)
    }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction

    Screen(
        navBackStack = navBackStack,
        state = state,
        onAction = onAction
    )
}

@Composable
private fun Screen(
    navBackStack: NavBackStack<NavKey>,
    state: TextToTextState,
    onAction: (TextToTextAction) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = state.modelEntity?.name ?: "",
                action = {
                    CustomSurfaceIconButton(
                        onClick = { onAction(TextToTextAction.ClearChat) },
                        icon = Icons.Rounded.Delete
                    )
                }
            )
        },
        content = { innerPadding ->
            CustomFadeBox(
                fadeTop = innerPadding.calculateTopPadding(),
                fadeBottom = innerPadding.calculateBottomPadding()
            ) {
                Content(
                    navBackStack = navBackStack,
                    innerPadding = innerPadding,
                    state = state,
                    onAction = onAction
                )
            }
        },
        bottomBar = {
            CustomSlideUpAnimatedVisibility(
                visible = !state.isModelInitializing
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, bottom = 25.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f)
                            .clip(AbsoluteRoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        CustomTransparentTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            placeholder = { Text(text = "Prompt") },
                            value = state.promptTextField,
                            onValueChange = { onAction(TextToTextAction.PromptTextField(it)) },
                            maxLines = 5
                        )
                    }
                    CustomFloatingActionButton(
                        onClick = { onAction(TextToTextAction.GenerateButton) },
                        icon = Icons.Rounded.Send,
                        isLoading = state.isModelThinking
                    )
                }
            }
        }
    )
}

@Composable
private fun Content(
    navBackStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues,
    state: TextToTextState,
    onAction: (TextToTextAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(start = 15.dp, end = 15.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        when {
            state.isModelInitializing -> {
                loadingIndicator()
            }
            state.isError != null -> {
                message(
                    message = state.isError,
                    isError = true
                )
            }
            state.chatList.isEmpty() -> {
                message(
                    message = "No history chat"
                )
            }
            else -> {
                items(
                    items = state.chatList
                ) { chatEntity ->
                    ChatBubble(
                        modifier = Modifier
                            .animateItem(),
                        fromUser = chatEntity.fromUser,
                        message = chatEntity.chat
                    )
                }
                if (state.isModelThinking) {
                    item {
                        var dotCount by remember {
                            mutableIntStateOf(0)
                        }
                        LaunchedEffect(Unit) {
                            while (state.isModelThinking) {
                                delay(500.milliseconds)
                                dotCount = (dotCount + 1) % 4
                            }
                        }
                        val animateDots = ".".repeat(dotCount)
                        ChatBubble(
                            modifier = Modifier
                                .animateItem(),
                            fromUser = false,
                            message = "Thinking$animateDots"
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Screen(
        navBackStack = rememberNavBackStack(),
        state = TextToTextState(),
        onAction = {}
    )
}