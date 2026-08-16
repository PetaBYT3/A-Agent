@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.data.local.conversation.ConversationDetailEntity
import com.a.agent.data.local.conversation.ConversationEntity
import com.a.agent.data.local.llm.LlmEntity
import com.a.agent.data.local.llm.LlmSource
import com.a.agent.domain.model.Configuration
import com.a.agent.domain.model.LlmBackend
import com.a.agent.presentation.navigation.Effect
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.util.CustomSnackBar
import com.a.agent.presentation.util.component.CustomEmptyBottomSheet
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.CustomUndismissableBottomSheet
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.component.listTitle
import com.a.agent.presentation.util.component.spacer
import com.a.agent.presentation.util.openApplicationSettings
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    navBackStack: NavBackStack<NavKey>,
    viewModel: HomeViewModel = koinViewModel()
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
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Home",
                action = {
                    IconButton(
                        onClick = { navBackStack.add(NavigationRoute.SettingsScreen) },
                        content = { Icon(Icons.Rounded.Settings, null) }
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
        floatingActionButton = {
            CustomFloatingActionButton(
                onClick = { navBackStack.add(NavigationRoute.ConversationManagerScreen()) },
                icon = Icons.Rounded.Add
            )
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { onAction(HomeAction.UpdatePermission) }
    )
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.RECORD_AUDIO
            )
        )
    }

    CustomEmptyBottomSheet(
        isBottomSheetVisible = state.isConfigurationBottomSheetVisible,
        title = "Configuration",
        content = {
            listTitle("Processing Backend")
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    LlmBackend.entries.fastForEachIndexed { index, backend ->
                        SegmentedButton(
                            selected = backend == state.configuration.processing,
                            onClick = { onAction(HomeAction.ProcessBackendChip(backend)) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = LlmBackend.entries.size
                            ),
                            label = { Text(text = backend.name) }
                        )
                    }
                }
            }
            spacer()
            listTitle("Vision Backend")
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    LlmBackend.entries.fastForEachIndexed { index, backend ->
                        SegmentedButton(
                            selected = backend == state.configuration.vision,
                            onClick = { onAction(HomeAction.VisionBackendChip(backend)) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = LlmBackend.entries.size
                            ),
                            label = { Text(text = backend.name) }
                        )
                    }
                }
            }
        },
        onCancel = { onAction(HomeAction.ConfigurationBottomSheetVisibility) }
    )

    CustomEmptyBottomSheet(
        isBottomSheetVisible = state.isDownloadedLlmBottomSheetVisible,
        title = "Change",
        content = {
            listTitle("Downloaded LLM")
            when {
                state.downloadedLlm.isEmpty() -> {
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
                        items = state.downloadedLlm,
                        key = { index, modelEntity -> modelEntity.id }
                    ) { index, modelEntity ->
                        CustomSegmentedListItem(
                            modifier = Modifier
                                .animateItem(),
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            index = index,
                            count = state.downloadedLlm.size,
                            onClick = { onAction(HomeAction.SelectModel(modelEntity)) },
                            content = { Text(text = modelEntity.name) },
                            supportingContent = {
                                SupportingText(
                                    text = modelEntity.fileName,
                                    isSingleLine = true
                                )
                            },
                            trailingContent = {
                                if (state.selectedLlm?.id == modelEntity.id) {
                                    Icon(Icons.Rounded.Check, null)
                                }
                            }
                        )
                    }
                }
            }
        },
        onCancel = { onAction(HomeAction.DownloadedLlmBottomSheetVisibility) }
    )

    CustomUndismissableBottomSheet(
        isBottomSheetVisible = state.isEngineLoading,
        title = state.engineTitle,
        content = {
            item {
                SupportingText(
                    text = state.engineStatus,
                    isSingleLine = true
                )
            }
            spacer()
            item {
                LinearWavyProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
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
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        val permissionStatus = buildList {
            if (!state.isNotificationPermissionGranted) {
                add("Notification Permission Denied")
            }
            if (!state.isStoragePermissionGranted) {
                add("Storage Permission Denied")
            }
            if (!state.isMicrophonePermissionGranted) {
                add("Microphone Permission Denied")
            }
        }
        if (permissionStatus.isNotEmpty()) {
            itemsIndexed(
                items = permissionStatus,
                key = { index, permission -> permission }
            ) { index, permission ->
                Message(
                    modifier = Modifier
                        .animateItem(),
                    index = index,
                    count = permissionStatus.size,
                    message = permission,
                    messageType = MessageType.Error
                )
            }
            item(key = "openPermissionSettingsButton") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = { openApplicationSettings(context) },
                        content = { Text(text = "Open App Settings") }
                    )
                }
            }
            spacer()
        }
        listTitle(title = "Selected LLM")
        when {
            state.isConfigurationLoading -> {
                item(key = "isConfigurationLoading") {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    )
                }
            }
            state.isConfigurationError != null -> {
                item(key = "isConfigurationError") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = state.isConfigurationError,
                        messageType = MessageType.Error
                    )
                }
            }
            state.selectedLlm == null -> {
                item(key = "noSelectedLlm") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = "No LLM Selected"
                    )
                }
            }
            else -> {
                item(key = "selectedLlm") {
                    CustomSegmentedListItem(
                        modifier = Modifier
                            .animateItem(),
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            overlineContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        overline = {
                            Text(
                                text = if (state.isEngineOnline) "Online" else "Offline"
                            )
                        },
                        content = {
                            Text(
                                text = state.selectedLlm.name,
                                style = MaterialTheme.typography.displaySmall,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = state.isEngineOnline,
                                onCheckedChange = { onAction(HomeAction.ToggleEngine) }
                            )
                        }
                    )
                }
            }
        }
        item(key = "llmOptions") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
                    .animateItem(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
            ) {
                SplitButtonLayout(
                    leadingButton = {
                        SplitButtonDefaults.LeadingButton(
                            onClick = { onAction(HomeAction.ConfigurationBottomSheetVisibility) },
                            content = { Text(text = "Configuration") },
                            enabled = !state.isEngineOnline
                        )
                    },
                    trailingButton = {
                        SplitButtonDefaults.TrailingButton(
                            onClick = { onAction(HomeAction.DownloadedLlmBottomSheetVisibility) },
                            content = { Text(text = "Change") },
                            enabled = !state.isEngineOnline
                        )
                    }
                )
                FilledTonalButton(
                    onClick = { navBackStack.add(NavigationRoute.LlmScreen) },
                    content = { Text(text = "View All") }
                )
            }
        }
        spacer()
        listTitle("Conversation")
        when {
            state.isConversationsLoading -> {
                item(key = "isConversationsLoading") {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    )
                }
            }
            state.isConversationsError != null -> {
                item(key = "isConversationsError") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = state.isConversationsError,
                        messageType = MessageType.Error
                    )
                }
            }
            state.conversationDetails.isEmpty() -> {
                item(key = "isConversationsEmpty") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = "Empty Conversation"
                    )
                }
            }
            else -> {
                itemsIndexed(
                    items = state.conversationDetails,
                    key = { index, conversationEntity -> conversationEntity.conversation.id }
                ) { index, conversationEntity ->
                    CustomSegmentedListItem(
                        modifier = Modifier
                            .animateItem(),
                        onClick = { navBackStack.add(NavigationRoute.ConversationScreen(conversationEntity.conversation.id)) },
                        index = index,
                        count = state.conversationDetails.size,
                        content = { Text(text = conversationEntity.conversation.title) },
                        supportingContent = { Text(text = "${conversationEntity.totalChats} Messages") },
                        trailingContent = {
                            IconButton(
                                onClick = { navBackStack.add(NavigationRoute.ConversationManagerScreen(conversationEntity.conversation.id)) },
                                content = { Icon(Icons.Rounded.MoreVert, null) }
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
        snackBarHostState = remember { SnackbarHostState() },
        state = HomeState(
            isConfigurationLoading = false,
            isConfigurationBottomSheetVisible = false,
            configuration = Configuration(
                selectedLlmId = "123",
                processing = LlmBackend.GPU,
                vision = LlmBackend.GPU,
                maxNumTokens = 128
            ),
            selectedLlm = LlmEntity(
                name = "Model Preview",
                url = "",
                path = "",
                fileName = "preview.litertlm",
                totalBytes = 132123231,
                llmSource = LlmSource.Default,
                isDownloaded = true
            ),
            isEngineOnline = false,
            conversationDetails = listOf(
                ConversationDetailEntity(
                    conversation = ConversationEntity(
                        title = "Conversation 2"
                    ),
                    totalChats = 10
                )
            )
        ),
        onAction = {}
    )
}