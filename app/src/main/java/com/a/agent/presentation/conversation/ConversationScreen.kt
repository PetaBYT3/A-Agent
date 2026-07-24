package com.a.agent.presentation.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.a.agent.presentation.util.component.BannerItem
import com.a.agent.presentation.util.component.BannerType
import com.a.agent.presentation.util.component.CustomBannerHolder
import com.a.agent.presentation.util.component.CustomFadeBox
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomShrinkUpAnimatedVisibility
import com.a.agent.presentation.util.component.CustomSlideUpAnimatedVisibility
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
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
                visible = !state.isConversationInitializing && state.isEngineOnline,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 25.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CustomTextField(
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
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        var bannerHeight by remember {
            mutableStateOf(0.dp)
        }
        CustomBannerHolder { height ->
            bannerHeight = height

            if (!state.isEngineOnline) {
                BannerItem(
                    message = "Engine Offline. You can only read the chat",
                    bannerType = BannerType.Warning
                )
            }

            when {
                state.isConversationInitializing -> {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                state.isConversationInitializeError != null -> {
                    BannerItem(
                        message = state.isConversationInitializeError,
                        bannerType = BannerType.Error
                    )
                }
                state.chatEntities.isEmpty() -> {
                    BannerItem(
                        message = "Empty Chat",
                        bannerType = BannerType.Info
                    )
                }
            }
        }
        val listState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = bannerHeight),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            if (state.isConversationInitializing) {
                item {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    )
                }
            }
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
                    ChatLoading(
                        modifier = Modifier
                            .animateItem()
                    )
                }
            }
        }
        val showScrollToBottomButton by remember {
            derivedStateOf { listState.canScrollForward }
        }
        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp),
            visible = showScrollToBottomButton,
            enter = fadeIn(tween()),
            exit = fadeOut(tween())
        ) {
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                onClick = {
                    scope.launch {
                        if (state.chatEntities.isNotEmpty()) {
                            listState.animateScrollToItem(state.chatEntities.lastIndex)
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
            isConversationInitializing = false,
            conversationEntity = ConversationEntity(
                title = "Conversation Title"
            )
        ),
        onAction = {}
    )
}