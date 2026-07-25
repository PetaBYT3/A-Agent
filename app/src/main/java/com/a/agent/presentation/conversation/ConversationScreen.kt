package com.a.agent.presentation.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import coil.compose.AsyncImage
import com.a.agent.data.local.ConversationEntity
import com.a.agent.presentation.conversation.component.ChatBubble
import com.a.agent.presentation.conversation.component.ChatLoading
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.component.CustomFadeAnimatedVisibility
import com.a.agent.presentation.util.component.CustomFadeBox
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomShrinkUpAnimatedVisibility
import com.a.agent.presentation.util.component.CustomSlideUpAnimatedVisibility
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.CustomTransparentTextField
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun TextToTextScreen(
    navBackStack: NavBackStack<NavKey>,
    conversationId: String,
    viewModel: ConversationViewModel = koinViewModel {
        parametersOf(conversationId)
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
    state: ConversationState,
    onAction: (ConversationAction) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = state.conversationEntity.title.ifBlank { "Initializing..." },
                action = {
                    IconButton(
                        onClick = { onAction(ConversationAction.ClearChat) },
                        content = { Icon(Icons.Rounded.MoreVert, null) }
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
                visible = !state.isConversationInitializing && state.isEngineOnline == true,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .navigationBarsPadding(),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(15.dp)
                    ) {
                        CustomShrinkUpAnimatedVisibility(
                            visible = state.imageInput != null
                        ) {
                            BadgedBox(
                                modifier = Modifier
                                    .padding(bottom = 10.dp),
                                badge = {
                                    Badge {
                                        IconButton(
                                            modifier = Modifier
                                                .size(10.dp),
                                            onClick = { onAction(ConversationAction.ImageInputPicker(null)) },
                                            content = { Icon(Icons.Rounded.Close, null) }
                                        )
                                    }
                                }
                            ) {
                                AsyncImage(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(AbsoluteRoundedCornerShape(15.dp))
                                        .clickable(
                                            enabled = true,
                                            onClick = { navBackStack.add(NavigationRoute.ImageViewScreen(state.imageInput!!)) }
                                        ),
                                    model = state.imageInput,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CustomTransparentTextField(
                                modifier = Modifier
                                    .weight(1f),
                                placeholder = { Text(text = "Prompt") },
                                value = state.promptTextField,
                                onValueChange = { onAction(ConversationAction.PromptTextField(it)) },
                                maxLines = 3
                            )
                            val galleryPicker = rememberFilePickerLauncher(
                                type = FileKitType.Image,
                                onResult = { platformFile ->
                                    if (platformFile != null) {
                                        onAction(ConversationAction.ImageInputPicker(platformFile))
                                    }
                                }
                            )
                            CustomFloatingActionButton(
                                onClick = { galleryPicker.launch() },
                                icon = Icons.Rounded.Image
                            )
                            CustomFloatingActionButton(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                onClick = { onAction(ConversationAction.GenerateButton) },
                                icon = Icons.Rounded.Send
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun Content(
    navBackStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues,
    state: ConversationState,
    onAction: (ConversationAction) -> Unit
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .padding(innerPadding)
    ) {
        val listState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            if (state.isEngineOnline == false) {
                stickyHeader(key = "isEngineOffline") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = "Engine Offline, You Can Only Read The Chat",
                        messageType = MessageType.Warning
                    )
                }
            }
            when {
                state.isConversationInitializing -> {
                    item(key = "isConversationInitializing") {
                        LinearWavyProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        )
                    }
                }
                state.isConversationInitializeError != null -> {
                    item(key = "isConversationInitializeError") {
                        Message(
                            modifier = Modifier
                                .animateItem(),
                            message = state.isConversationInitializeError,
                            messageType = MessageType.Error
                        )
                    }
                }
                state.chatEntities.isEmpty() -> {
                    item(key = "isChatEntitiesEmpty") {
                        Message(
                            modifier = Modifier
                                .animateItem(),
                            message = "Empty Chat"
                        )
                    }
                }
                else -> {
                    items(
                        items = state.chatEntities,
                        key = { it.id }
                    ) { chatEntity ->
                        ChatBubble(
                            modifier = Modifier
                                .animateItem(),
                            fromUser = chatEntity.fromUser,
                            imagePath = chatEntity.imagePath,
                            onImageClick = { navBackStack.add(NavigationRoute.ImageViewScreen(chatEntity.imagePath!!)) },
                            message = chatEntity.chat
                        )
                    }
                    if (state.isModelThinking) {
                        item(key = "isModelThinking") {
                            ChatLoading(
                                modifier = Modifier
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }
        val showScrollToBottomButton by remember {
            derivedStateOf { listState.canScrollForward }
        }
        CustomFadeAnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            visible = showScrollToBottomButton
        ) {
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                onClick = {
                    scope.launch {
                        if (state.chatEntities.isNotEmpty()) {
                            listState.animateScrollToItem(state.chatEntities.lastIndex, Int.MAX_VALUE)
                        }
                    }
                },
                content =  { Icon(Icons.Rounded.ArrowDownward, null) }
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Screen(
        navBackStack = rememberNavBackStack(),
        state = ConversationState(
            isEngineOnline = true,
            isConversationInitializing = false,
            conversationEntity = ConversationEntity(
                title = "Conversation Title"
            )
        ),
        onAction = {}
    )
}