@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.data.local.ConversationEntity
import com.a.agent.data.local.ModelEntity
import com.a.agent.data.local.ModelSource
import com.a.agent.domain.model.LlmModelEngineBackend
import com.a.agent.domain.model.LlmModelEngineConfiguration
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.util.component.CustomComposableBottomSheet
import com.a.agent.presentation.util.component.CustomEmptyBottomSheet
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomPopupMenu
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomShrinkLeftAnimatedVisibility
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.CustomUndismissableBottomSheet
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.component.TitleText
import com.a.agent.presentation.util.component.itemColumn
import com.a.agent.presentation.util.component.listTitle
import com.a.agent.presentation.util.component.spacer
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    navBackStack: NavBackStack<NavKey>,
    viewModel: HomeViewModel = koinViewModel()
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
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Home",
                action = {
                    IconButton(
                        onClick = {},
                        content = { Icon(Icons.Rounded.Person, null) }
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
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CustomShrinkLeftAnimatedVisibility(
                    visible = state.selectedModelEntity != ModelEntity.Empty
                ) {
                    CustomFloatingActionButton(
                        containerColor = if (state.isModelEngineOnline) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        contentColor = if (state.isModelEngineOnline) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                        onClick = { onAction(HomeAction.ToggleModelEngine) },
                        icon = if (state.isModelEngineOnline) Icons.Rounded.Stop else Icons.Rounded.PlayArrow
                    )
                }
                CustomFloatingActionButton(
                    onClick = { onAction(HomeAction.UpsertConversationBottomSheet) },
                    icon = Icons.Rounded.Add
                )
            }
        }
    )

    CustomEmptyBottomSheet(
        isBottomSheetVisible = state.downloadedModelBottomSheet,
        content = {
            listTitle("Change Model")
            when {
                state.downloadedModels.isEmpty() -> {
                    item(key = "isDownloadedModelsEmpty") {
                        Message(
                            modifier = Modifier
                                .animateItem(),
                            message = "Downloaded Model Empty"
                        )
                    }
                }
                else -> {
                    itemsIndexed(
                        items = state.downloadedModels,
                        key = { index, modelEntity -> modelEntity.id }
                    ) { index, modelEntity ->
                        CustomSegmentedListItem(
                            modifier = Modifier
                                .animateItem(),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            index = index,
                            count = state.downloadedModels.size,
                            onClick = { onAction(HomeAction.SelectModel(modelEntity)) },
                            content = { Text(text = modelEntity.name) },
                            supportingContent = {
                                SupportingText(
                                    text = modelEntity.fileName,
                                    isSingleLine = true
                                )
                            },
                            trailingContent = {
                                if (state.selectedModelEntity.id == modelEntity.id) {
                                    Icon(Icons.Rounded.Check, null)
                                }
                            }
                        )
                    }
                }
            }
        },
        onCancel = { onAction(HomeAction.DownloadedModelBottomSheet) }
    )

    CustomEmptyBottomSheet(
        isBottomSheetVisible = state.llmModelEngineConfigurationBottomSheet,
        content = {
            listTitle("Model Configuration")
            itemColumn(
                verticalArrangement = Arrangement.spacedBy(2.5.dp)
            ) {
                val llmModelEngineConfiguration = 2
                CustomSegmentedListItem(
                    index = 0,
                    count = llmModelEngineConfiguration,
                    content = { Text(text = "Processing Backend") },
                    trailingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            LlmModelEngineBackend.entries.fastForEach { llmModelEngineBackend ->
                                InputChip(
                                    selected = llmModelEngineBackend == state.llmModelEngineConfiguration.processingBackend,
                                    onClick = { onAction(HomeAction.ProcessBackendChip(llmModelEngineBackend)) },
                                    label = { Text(text = llmModelEngineBackend.name) }
                                )
                            }
                        }
                    }
                )
                CustomSegmentedListItem(
                    index = 1,
                    count = llmModelEngineConfiguration,
                    content = { Text(text = "Vision Backend") },
                    trailingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            LlmModelEngineBackend.entries.forEach { llmModelEngineBackend ->
                                InputChip(
                                    selected = llmModelEngineBackend == state.llmModelEngineConfiguration.visionBackend,
                                    onClick = { onAction(HomeAction.VisionBackendChip(llmModelEngineBackend)) },
                                    label = { Text(text = llmModelEngineBackend.name) }
                                )
                            }
                        }
                    }
                )
            }
        },
        onCancel = { onAction(HomeAction.LlmModelEngineConfigurationBottomSheet) }
    )

    CustomComposableBottomSheet(
        isBottomSheetVisible = state.upsertConversationBottomSheet,
        content = {
            listTitle("Available Model")
            itemColumn {
                CustomTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = { Text(text = "Title") },
                    value = state.conversationNameTextField,
                    onValueChange = { onAction(HomeAction.ConversationNameTextField(it)) }
                )
            }
        },
        confirmText = "Create Conversation",
        onConfirm = { onAction(HomeAction.UpsertConversationButton) },
        onCancel = { onAction(HomeAction.UpsertConversationBottomSheet) }
    )

    CustomUndismissableBottomSheet(
        isBottomSheetVisible = state.isModelEngineLoading,
        content = {
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TitleText(
                        text = if (state.isModelEngineOnline) {
                            "Shutting Down The Model Engine..."
                        } else {
                            "Starting Up The Model Engine..."
                        }
                    )
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
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
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        listTitle(
            title = "Selected Model",
            content = {
                AssistChip(
                    onClick = { navBackStack.add(NavigationRoute.ModelScreen) },
                    label = { Text(text = "View All") },
                    trailingIcon = {
                        if (state.totalDownloadingProgress != 0) {
                            Badge { Text(text = state.totalDownloadingProgress.toString()) }
                        }
                    }
                )
            }
        )
        when {
            state.isLlmModelEngineConfigurationLoading -> {
                item(key = "isLlmModelEngineConfigurationError") {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    )
                }
            }
            state.isLlmModelEngineConfigurationError != null -> {
                item(key = "isLlmModelEngineConfigurationError") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = state.isLlmModelEngineConfigurationError,
                        messageType = MessageType.Error
                    )
                }
            }
            state.selectedModelEntity == ModelEntity.Empty -> {
                item(key = "noModelSelected") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = "No Model Selected",
                        messageType = MessageType.Warning
                    )
                }
            }
            else -> {
                item(key = "selectedModel") {
                    CustomSegmentedListItem(
                        modifier = Modifier
                            .animateItem(),
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        overline = {
                            Text(
                                color = if (state.isModelEngineOnline) Color.Green else Color.Red,
                                text = if (state.isModelEngineOnline) "Online" else "Offline"
                            )
                        },
                        content = {
                            Text(
                                text = state.selectedModelEntity.name,
                                style = MaterialTheme.typography.displaySmall,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
        item(key = "changeAndConfigurationButton") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
                contentAlignment = Alignment.CenterEnd
            ) {
                SplitButtonLayout(
                    leadingButton = {
                        SplitButtonDefaults.LeadingButton(
                            onClick = { onAction(HomeAction.DownloadedModelBottomSheet) },
                            content = { Text(text = "Change") },
                            enabled = !state.isModelEngineOnline
                        )
                    },
                    trailingButton = {
                        SplitButtonDefaults.TrailingButton(
                            onClick = { onAction(HomeAction.LlmModelEngineConfigurationBottomSheet) },
                            content = { Text(text = "Configuration") },
                            enabled = !state.isModelEngineOnline
                        )
                    }
                )
            }
        }
        spacer()
        listTitle("Conversation")
        when {
            state.isConversationsLoading -> {
                item(key = "isConversationLoading") {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                    )
                }
            }
            state.conversationError != null -> {
                item(key = "isConversationError") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = state.conversationError,
                        messageType = MessageType.Error
                    )
                }
            }
            state.conversationEntities.isEmpty() -> {
                item(key = "isConversationEmpty") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = "Empty Conversation"
                    )
                }
            }
            else -> {
                itemsIndexed(
                    items = state.conversationEntities,
                    key = { index, conversationEntity -> conversationEntity.id }
                ) { index, conversationEntity ->
                    CustomSegmentedListItem(
                        modifier = Modifier
                            .animateItem(),
                        onClick = { navBackStack.add(NavigationRoute.ConversationScreen(conversationEntity.id)) },
                        index = index,
                        count = state.conversationEntities.size,
                        content = { Text(text = conversationEntity.title) },
                        trailingContent = {
                            val items = listOf(
                                Triple(
                                    first = {},
                                    second = Icons.Rounded.Edit,
                                    third = "Edit"
                                ),
                                Triple(
                                    first = { },
                                    second = Icons.Rounded.Delete,
                                    third = "Delete"
                                )
                            )
                            CustomPopupMenu(
                                content = { expand ->
                                    IconButton(
                                        onClick = expand,
                                        content = { Icon(Icons.Rounded.MoreVert, null) }
                                    )
                                },
                                items = items
                            )
                        }
                    )
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
        state = HomeState(
            totalDownloadingProgress = 2,
            isLlmModelEngineConfigurationLoading = false,
            llmModelEngineConfiguration = LlmModelEngineConfiguration(
                selectedModelId = "123",
                processingBackend = LlmModelEngineBackend.GPU,
                visionBackend = LlmModelEngineBackend.GPU,
                maxNumTokens = 128
            ),
            selectedModelEntity = ModelEntity(
                name = "Model Preview",
                url = "",
                path = "",
                fileName = "preview.litertlm",
                totalBytes = 132123231,
                modelSource = ModelSource.Default,
                isDownloaded = true
            ),
            isModelEngineOnline = false,
            conversationEntities = listOf(
                ConversationEntity(
                    title = "Conversation 1"
                ),
                ConversationEntity(
                    title = "Conversation 2"
                )
            )
        ),
        onAction = {}
    )
}