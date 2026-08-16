package com.a.agent.presentation.chatconversation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import coil.compose.AsyncImage
import com.a.agent.data.local.chat.ChatEntity
import com.a.agent.data.local.conversation.ConversationEntity
import com.a.agent.presentation.chatconversation.component.ChatBubble
import com.a.agent.presentation.chatconversation.component.ChatLoading
import com.a.agent.presentation.navigation.Effect
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.CustomSnackBar
import com.a.agent.presentation.util.component.CustomContentBottomSheet
import com.a.agent.presentation.util.component.CustomEmptyBottomSheet
import com.a.agent.presentation.util.component.CustomFadeAnimatedVisibility
import com.a.agent.presentation.util.component.CustomPopupMenu
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomSlideUpAnimatedVisibility
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.openApplicationSettings
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
    val snackBarHostState = remember { SnackbarHostState() }

    Screen(
        navBackStack = navBackStack,
        snackBarHostState = snackBarHostState,
        state = state,
        onAction = onAction
    )

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackBar -> {
                    snackBarHostState.showSnackbar(
                        message = effect.message,
                        withDismissAction = true
                    )
                }
            }
        }
    }
}

@Composable
private fun Screen(
    navBackStack: NavBackStack<NavKey>,
    snackBarHostState: SnackbarHostState,
    state: ConversationState,
    onAction: (ConversationAction) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = state.conversation?.title ?: "Creating Session...",
                action = {
                    CustomPopupMenu(
                        content = { expanded ->
                            IconButton(
                                onClick = { expanded() },
                                content = { Icon(Icons.Rounded.MoreVert, null) }
                            )
                        },
                        items = listOf(
                            Triple(
                                first = { onAction(ConversationAction.TtsLanguageBottomSheet) },
                                second = Icons.Rounded.Language,
                                third = "TTS Language"
                            ),
                            Triple(
                                first = { onAction(ConversationAction.SttLanguageBottomSheet) },
                                second = Icons.Rounded.Language,
                                third = "STT Language"
                            ),
                            Triple(
                                first = { onAction(ConversationAction.DeleteChatBottomSheet) },
                                second = Icons.Rounded.Delete,
                                third = "Clear Chat"
                            )
                        )
                    )
                }
            )
        },
        content = { innerPadding ->
            Content(
                navBackStack = navBackStack,
                innerPadding = innerPadding,
                state = state,
                onAction = onAction
            )
        },
        snackbarHost = { CustomSnackBar(snackBarHostState = snackBarHostState) },
        bottomBar = {
            CustomSlideUpAnimatedVisibility(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding(),
                visible = state.isEngineConversationReady,
            ) {
                Column(
                    modifier = Modifier
                        .padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CustomTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = { Text(text = "Prompt") },
                        value = state.promptTextField,
                        onValueChange = { onAction(ConversationAction.PromptTextField(it)) },
                        maxLines = 3,
                        enabled = !state.isGenerating || !state.isSttRunning
                    )
                    Row(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.imagePicker != null) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            IconButton(
                                                modifier = Modifier
                                                    .size(15.dp),
                                                onClick = { onAction(ConversationAction.ImagePickerButton(null)) },
                                                content = { Icon(Icons.Rounded.Close, null) }
                                            )
                                        }
                                    }
                                ) {
                                    AsyncImage(
                                        modifier = Modifier
                                            .clip(AbsoluteRoundedCornerShape(25))
                                            .size(ButtonDefaults.MediumContainerHeight - 15.dp)
                                            .clickable(
                                                enabled = true,
                                                onClick = { navBackStack.add(NavigationRoute.ImageViewScreen(state.imagePicker)) }
                                            ),
                                        model = state.imagePicker,
                                        contentDescription = null,
                                        contentScale = ContentScale.FillWidth
                                    )
                                }
                            }
                        }
                        val galleryPicker = rememberFilePickerLauncher(
                            type = FileKitType.Image,
                            onResult = { platformFile ->
                                if (platformFile != null) {
                                    onAction(ConversationAction.ImagePickerButton(platformFile))
                                }
                            }
                        )
                        FilledTonalIconToggleButton(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .height(ButtonDefaults.MediumContainerHeight),
                            checked = state.isSttRunning,
                            onCheckedChange = {
                                if (state.isMicrophonePermissionGranted) {
                                    onAction(ConversationAction.ToggleSttButton(it))
                                } else {
                                    onAction(ConversationAction.MicrophonePermissionDeniedBottomSheet)
                                }
                            },
                            content = { Icon(Icons.Rounded.RecordVoiceOver, null) }
                        )
                        FilledTonalButton(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .height(ButtonDefaults.MediumContainerHeight),
                            onClick = { galleryPicker.launch() },
                            content = { Icon(Icons.Rounded.Image, null) }
                        )
                        Button(
                            modifier = Modifier
                                .height(ButtonDefaults.MediumContainerHeight),
                            onClick = { onAction(ConversationAction.GenerateButton) },
                            content = { Icon(Icons.Rounded.Send, null) },
                            enabled = state.promptTextField.isNotBlank() && !state.isGenerating
                        )
                    }
                }
            }
        }
    )

    CustomEmptyBottomSheet(
        isBottomSheetVisible = state.isTtsLanguageBottomSheetVisible,
        title = "TTS Language",
        content = {
            when {
                state.ttsLanguages.isEmpty() -> {
                    item(key = "isAvailableTtsLanguageEmpty") {
                        Message(
                            modifier = Modifier
                                .animateItem(),
                            message = "Empty Language"
                        )
                    }
                }
                else -> {
                    itemsIndexed(
                        items = state.ttsLanguages,
                        key = { index, ttsLanguage -> ttsLanguage.toLanguageTag() }
                    ) { index, ttsLanguage ->
                        CustomSegmentedListItem(
                            modifier = Modifier
                                .animateItem(),
                            onClick = {
                                onAction(ConversationAction.SetTtsLanguageButton(ttsLanguage))
                            },
                            index = index,
                            count = state.ttsLanguages.size,
                            content = { Text(text = ttsLanguage.displayLanguage) },
                            supportingContent = { Text(ttsLanguage.displayName) },
                            trailingContent = {
                                if (state.selectedTtsLanguage == ttsLanguage) {
                                    Icon(Icons.Rounded.Check, null)
                                }
                            }
                        )
                    }
                }
            }
        },
        onCancel = { onAction(ConversationAction.TtsLanguageBottomSheet) }
    )

    CustomEmptyBottomSheet(
        isBottomSheetVisible = state.isSttLanguageBottomSheetVisible,
        title = "STT Language",
        content = {
            when {
                state.sttLanguage.isEmpty() -> {
                    item(key = "isAvailableSttLanguageEmpty") {
                        Message(
                            modifier = Modifier
                                .animateItem(),
                            message = "Empty Language"
                        )
                    }
                }
                else -> {
                    itemsIndexed(
                        items = state.sttLanguage,
                        key = { index, sttLanguage -> sttLanguage.toLanguageTag() }
                    ) { index, sttLanguage ->
                        CustomSegmentedListItem(
                            modifier = Modifier
                                .animateItem(),
                            onClick = { onAction(ConversationAction.SetSttLanguageButton(sttLanguage)) },
                            index = index,
                            count = state.sttLanguage.size,
                            content = { Text(text = sttLanguage.displayLanguage) },
                            supportingContent = { Text(sttLanguage.displayName) },
                            trailingContent = {
                                if (sttLanguage == state.selectedSttLanguage) {
                                    Icon(Icons.Rounded.Check, null)
                                }
                            }
                        )
                    }
                }
            }
        },
        onCancel = { onAction(ConversationAction.SttLanguageBottomSheet) }
    )

    CustomContentBottomSheet(
        isBottomSheetVisible = state.isDeleteChatBottomSheetVisible,
        title = "Delete",
        content = {
            item {
                SupportingText(
                    text = "Are you sure you want to delete all chat in this conversation ?",
                    isSingleLine = false
                )
            }
        },
        isOnError = true,
        confirmText = "Yes",
        onConfirm = { onAction(ConversationAction.DeleteChatButton) },
        onCancel = { onAction(ConversationAction.DeleteChatBottomSheet) }
    )

    CustomContentBottomSheet(
        isBottomSheetVisible = state.isMicrophonePermissionDeniedBottomSheetVisible,
        title = "Microphone Access Denied",
        content = {
            item {
                SupportingText(
                    text = "Microphone permission is denied, to use speech to text please allow microphone permission on app permission settings",
                    isSingleLine = false
                )
            }
        },
        confirmText = "Open Settings",
        onConfirm = { openApplicationSettings(context) },
        onCancel = { onAction(ConversationAction.MicrophonePermissionDeniedBottomSheet) }
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
        val listState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 15.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            if (state.isGenerating) {
                item(key = "isGenerating") {
                    ChatLoading(
                        modifier = Modifier
                            .animateItem()
                    )
                }
            }
            val isAllLoadingComplete = state.isEngineConversationLoading &&
                    state.isConversationLoading &&
                    state.isChatLoading &&
                    state.isTtsLoading
            when {
                isAllLoadingComplete -> {
                    item(key = "isConversationLoading") {
                        LinearWavyProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        )
                    }
                }
                state.isChatError != null -> {
                    item(key = "isChatError") {
                        Message(
                            modifier = Modifier
                                .animateItem(),
                            message = state.isChatError,
                            messageType = MessageType.Error
                        )
                    }
                }
                state.chats.isEmpty() -> {
                    item(key = "isChatsEmpty") {
                        Message(
                            modifier = Modifier
                                .animateItem(),
                            message = "Empty Chat"
                        )
                    }
                }
                else -> {
                    items(
                        items = state.chats,
                        key = { it.id }
                    ) { chatEntity ->
                        ChatBubble(
                            modifier = Modifier
                                .animateItem(),
                            fromUser = chatEntity.fromUser,
                            imagePath = chatEntity.imagePath,
                            onImageClick = { navBackStack.add(NavigationRoute.ImageViewScreen(chatEntity.imagePath!!)) },
                            onSpeechClick = { onAction(ConversationAction.StartTtsButton(chatEntity.chat)) },
                            message = chatEntity.chat
                        )
                    }
                }
            }
            val errorList = buildList {
                if (state.isEngineConversationError != null) {
                    add(state.isEngineConversationError)
                }
                if (state.isConversationError != null) {
                    add(state.isConversationError)
                }
                if (state.isTtsError != null) {
                    add(state.isTtsError)
                }
            }
            if (errorList.isNotEmpty()) {
                stickyHeader(key = "errorBanner") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        verticalArrangement = Arrangement.spacedBy(2.5.dp)
                    ) {
                        errorList.fastForEachIndexed { index, error ->
                            Message(
                                index = index,
                                count = errorList.size,
                                message = error,
                                messageType = MessageType.Error
                            )
                        }
                    }
                }
            }
        }
        LaunchedEffect(state.chats) {
            listState.animateScrollToItem(0)
        }
        val showScrollToBottomButton by remember {
            derivedStateOf { listState.firstVisibleItemIndex > 0 }
        }
        CustomFadeAnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            visible = showScrollToBottomButton
        ) {
            IconButton(
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                onClick = {
                    scope.launch {
                        if (state.chats.isNotEmpty()) {
                            listState.animateScrollToItem(0)
                        }
                    }
                },
                content =  { Icon(Icons.Rounded.ArrowDownward, null) }
            )
        }
        CustomFadeAnimatedVisibility(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            visible = state.isTtsRunning
        ) {
            FilledIconButton(
                onClick = { onAction(ConversationAction.StopTtsButton) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                content = { Icon(Icons.Rounded.Stop, null) }
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Screen(
        navBackStack = rememberNavBackStack(),
        snackBarHostState = remember { SnackbarHostState() },
        state = ConversationState(
            isEngineConversationReady = true,
            isConversationLoading = false,
            conversation = ConversationEntity(
                title = "Conversation Title"
            ),
            chats = listOf(
                ChatEntity(
                    conversationId = "",
                    fromUser = true,
                    imagePath = null,
                    chat = "Message From User"
                ),
                ChatEntity(
                    conversationId = "",
                    fromUser = false,
                    imagePath = null,
                    chat = "Message From Llm"
                )
            )
        ),
        onAction = {}
    )
}