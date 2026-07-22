package com.a.agent.presentation.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgeDefaults
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.conversation.component.ChatBubble
import com.a.agent.presentation.conversation.component.ChatLoading
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.util.component.CustomFadeBox
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomFullScreenDialog
import com.a.agent.presentation.util.component.CustomPopupMenu
import com.a.agent.presentation.util.component.CustomSlideUpAnimatedVisibility
import com.a.agent.presentation.util.component.CustomSurfaceIconButton
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.CustomTransparentTextField
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.loadingIndicator
import com.a.agent.presentation.util.component.message
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import io.github.vinceglb.filekit.readBytes
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
                    CustomSurfaceIconButton(
                        onClick = { onAction(ConversationAction.ClearChat) },
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
            AnimatedVisibility(
                visible = !state.isConversationInitializing && state.isEngineOnline,
                enter = slideInVertically(
                    initialOffsetY = { it }
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it }
                ) + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, bottom = 25.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        modifier = Modifier
                            .heightIn(max = 200.dp)
                            .weight(1f)
                            .clip(AbsoluteRoundedCornerShape(28.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CustomSlideUpAnimatedVisibility(
                                    visible = state.imageInput != null
                                ) {
                                    BadgedBox(
                                        modifier = Modifier
                                            .padding(start = 12.5.dp, top = 12.5.dp),
                                        badge = {
                                            Badge {
                                                IconButton(
                                                    modifier = Modifier
                                                        .size(15.dp),
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
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
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
                                val items = listOf(
                                    Triple(
                                        first = { galleryPicker.launch() },
                                        second = Icons.Rounded.Image,
                                        third = "Image"
                                    )
                                )
                                CustomPopupMenu(
                                    content = { expand ->
                                        IconButton(
                                            onClick = expand,
                                            content = { Icon(Icons.Rounded.AttachFile, null) }
                                        )
                                    },
                                    items = items
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                            }
                        }
                    }
                    CustomFloatingActionButton(
                        onClick = { onAction(ConversationAction.GenerateButton) },
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
    state: ConversationState,
    onAction: (ConversationAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(
            bottom = innerPadding.calculateBottomPadding() + 15.dp,
            start = 15.dp,
            end = 15.dp
        ),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        if (!state.isEngineOnline) {
            stickyHeader {
                Message(
                    message = "Model engine is offline, you can only read the conversation",
                    messageType = MessageType.Warning
                )
            }
        }
        when {
            state.isConversationInitializing -> {
                loadingIndicator()
            }
            state.isConversationInitializeError != null -> {
                message(
                    message = state.isConversationInitializeError,
                    messageType = MessageType.Error
                )
            }
            state.chatEntities.isEmpty() -> {
                message(
                    message = "No history chat"
                )
            }
            else -> {
                items(
                    items = state.chatEntities
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
                    item {
                        ChatLoading()
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
        state = ConversationState(
            isConversationInitializing = false,
            conversationEntity = ConversationEntity(
                title = "Conversation Title"
            )
        ),
        onAction = {}
    )
}