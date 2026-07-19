package com.a.agent.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.data.local.ConversationEntity
import com.a.agent.data.local.ModelEntity
import com.a.agent.domain.model.LlmModelEngineBackend
import com.a.agent.domain.model.LlmModelEngineConfiguration
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.util.component.CustomComposableBottomSheet
import com.a.agent.presentation.util.component.CustomEmptyBottomSheet
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomSurfaceIconButton
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.component.itemColumn
import com.a.agent.presentation.util.component.itemRow
import com.a.agent.presentation.util.component.listTitle
import com.a.agent.presentation.util.component.loadingIndicator
import com.a.agent.presentation.util.component.message
import com.a.agent.presentation.util.component.spacer
import com.a.agent.presentation.util.toMegaByte
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
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CustomTopAppBar(
                contentPadding = PaddingValues(end = 7.dp),
                title = "Home",
                action = {
                    CustomSurfaceIconButton(
                        onClick = {},
                        icon = Icons.Rounded.Person
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
                horizontalArrangement = Arrangement.spacedBy(15.dp)
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
                    icon = if (state.isModelEngineOnline) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                    isLoading = state.isModelEngineLoading
                )
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
            itemsIndexed(
                items = state.downloadedModelEntities
            ) { index, modelEntity ->
                CustomSegmentedListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    index = index,
                    count = state.downloadedModelEntities.size,
                    onClick = { onAction(HomeAction.SelectModel(modelEntity)) },
                    content = { Text(text = modelEntity.name) },
                    supportingContent = { Text(text = modelEntity.fileName) },
                    trailingContent = {
                        if (state.selectedModelEntity.id == modelEntity.id) {
                            Icon(Icons.Rounded.Check, null)
                        }
                    }
                )
            }
        },
        onCancel = { onAction(HomeAction.DownloadedModelBottomSheet) }
    )

    CustomComposableBottomSheet(
        isBottomSheetVisible = state.upsertConversationBottomSheet,
        content = {
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
            .fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(start = 15.dp, end = 15.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        listTitle("Model Configuration") {
            BadgedBox(
                badge = { Badge { Text(text = "3") } }
            ) {
                CustomSurfaceIconButton(
                    onClick = { navBackStack.add(NavigationRoute.ModelScreen) },
                    icon = Icons.Rounded.ArrowForward
                )
            }
        }
        if (state.llmModelEngineConfiguration == null && state.selectedModelEntity == ModelEntity.Empty) {
            loadingIndicator()
        }
        if (state.initializeError != null) {
            message(
                message = state.initializeError,
                messageType = MessageType.Error
            )
        }
        if (state.llmModelEngineConfiguration != null && state.selectedModelEntity != ModelEntity.Empty) {
            itemColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.5.dp)
            ) {
                CustomSegmentedListItem(
                    index = 0,
                    count = 3,
                    overline = {
                        val contentColor = if (state.isModelEngineOnline) {
                            Color.Green
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                        Text(
                            color = contentColor,
                            text = if (state.isModelEngineOnline) "Online" else "Offline"
                        )
                    },
                    content = {
                        Text(
                            style = MaterialTheme.typography.headlineSmall,
                            text = state.selectedModelEntity.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        Spacer(modifier = Modifier.height(15.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(20.dp),
                                imageVector = Icons.Rounded.InsertDriveFile,
                                contentDescription = null
                            )
                            Text(text = state.selectedModelEntity.fileName)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(20.dp),
                                imageVector = Icons.Rounded.FileDownload,
                                contentDescription = null
                            )
                            Text(text = state.selectedModelEntity.totalBytes.toMegaByte())
                        }
                    },
                    trailingContent = {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            FilledTonalIconButton(
                                onClick = { onAction(HomeAction.DownloadedModelBottomSheet) },
                                content = { Icon(Icons.Rounded.Repeat, null) },
                                enabled = !state.isModelEngineOnline
                            )
                        }
                    }
                )
                CustomSegmentedListItem(
                    index = 1,
                    count = 3,
                    content = { SupportingText(text = "Processing Backend") },
                    trailingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            LlmModelEngineBackend.entries.forEach { llmModelEngineBackend ->
                                InputChip(
                                    selected = llmModelEngineBackend == state.llmModelEngineConfiguration.processingBackend,
                                    onClick = { onAction(HomeAction.ProcessBackendChip(llmModelEngineBackend)) },
                                    label = { Text(text = llmModelEngineBackend.name) },
                                    enabled = !state.isModelEngineOnline
                                )
                            }
                        }
                    }
                )
                CustomSegmentedListItem(
                    index = 2,
                    count = 3,
                    content = { SupportingText(text = "Vision Backend") },
                    trailingContent = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            LlmModelEngineBackend.entries.forEach { llmModelEngineBackend ->
                                InputChip(
                                    selected = llmModelEngineBackend == state.llmModelEngineConfiguration.visionBackend,
                                    onClick = { onAction(HomeAction.VisionBackendChip(llmModelEngineBackend)) },
                                    label = { Text(text = llmModelEngineBackend.name) },
                                    enabled = !state.isModelEngineOnline
                                )
                            }
                        }
                    }
                )
            }
        }
        spacer()
        listTitle("Conversation")
        when {
            state.conversationError != null -> {
                message(
                    message = state.conversationError,
                    messageType = MessageType.Error
                )
            }
            state.conversationEntities.isEmpty() -> {
                message(
                    message = "No Conversation"
                )
            }
        }
        itemsIndexed(
            items = state.conversationEntities
        ) { index, conversationEntity ->
            CustomSegmentedListItem(
                onClick = { navBackStack.add(NavigationRoute.ConversationScreen(conversationEntity.id)) },
                index = index,
                count = state.conversationEntities.size,
                content = { Text(text = conversationEntity.title) }
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Screen(
        navBackStack = rememberNavBackStack(),
        state = HomeState(
            llmModelEngineConfiguration = LlmModelEngineConfiguration(
                selectedModelId = "",
                processingBackend = LlmModelEngineBackend.GPU,
                visionBackend = LlmModelEngineBackend.GPU
            ),
            selectedModelEntity = ModelEntity(
                name = "Model Preview",
                url = "",
                path = "",
                fileName = "preview.litertlm",
                totalBytes = 132123231
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