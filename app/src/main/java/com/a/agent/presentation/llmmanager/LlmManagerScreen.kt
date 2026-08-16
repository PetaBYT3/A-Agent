@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.llmmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.TypeSpecimen
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.data.local.llm.LlmSource
import com.a.agent.data.util.toMegaByte
import com.a.agent.presentation.navigation.Effect
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.CustomSnackBar
import com.a.agent.presentation.util.component.CustomContentBottomSheet
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.component.listTitle
import com.a.agent.presentation.util.component.spacer
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ModelManagerScreen(
    navBackStack: NavBackStack<NavKey>,
    modelId: String,
    viewModel: LlmManagerViewModel = koinViewModel {
        parametersOf(modelId)
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
    state: LlmManagerState,
    onAction: (LlmManagerAction) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = "LLM Manager"
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
            if (state.llmSourceChip != LlmSource.Default) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    if (state.isOnEdit) {
                        Button(
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .height(ButtonDefaults.MediumContainerHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            onClick = { onAction(LlmManagerAction.DeleteLlmBottomSheet) },
                            content = { Icon(Icons.Rounded.Delete, null) }
                        )
                    }
                    val isReadyToSave = (state.llmUrlTextField.isNotBlank() || state.llmLocalPlatformFile != null) &&
                            state.llmNameTextField.isNotBlank() &&
                            state.llmFileName.isNotBlank() &&
                            state.llmFileSize != 0L &&
                            state.isLlmSupported
                    Button(
                        modifier = Modifier
                            .height(ButtonDefaults.MediumContainerHeight),
                        onClick = { onAction(LlmManagerAction.UpsertLlmButton) },
                        content = {
                            if (state.isUpsertLlmButtonLoading) {
                                LoadingIndicator(
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                            } else {
                                Icon(Icons.Rounded.Save, null)
                            }
                        },
                        enabled = isReadyToSave && !state.isUpsertLlmButtonLoading
                    )
                }
            }
        }
    )

    CustomContentBottomSheet(
        isBottomSheetVisible = state.isDeleteLlmBottomSheetVisible,
        title = "Delete",
        content = {
            item {
                SupportingText(
                    text = "Are you sure you want to delete this LLM ?"
                )
            }
        },
        isOnError = true,
        confirmText = "Yes",
        onConfirm = { onAction(LlmManagerAction.DeleteLlmButton) },
        onCancel = { onAction(LlmManagerAction.DeleteLlmBottomSheet) }
    )
}

@Composable
private fun Content(
    navBackStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues,
    state: LlmManagerState,
    onAction: (LlmManagerAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        listTitle(
            title = "Llm Source"
        )
        when {
            state.llmSourceChip == LlmSource.Local -> {
                item(key = "localFilePicker") {
                    val filePicker = rememberFilePickerLauncher(
                        type = FileKitType.File(),
                        onResult = { platformFile ->
                            if (platformFile != null) {
                                onAction(LlmManagerAction.LocalFilePicker(platformFile))
                            }
                        }
                    )
                    CustomSegmentedListItem(
                        modifier = Modifier
                            .height(56.dp)
                            .animateItem(),
                        onClick = { if (!state.isOnEdit) filePicker.launch() },
                        content = { Text(text = "Attach File") },
                        trailingContent = { Icon(Icons.Rounded.AttachFile, null) }
                    )
                }
            }
            else -> {
                item(key = "urlTextField") {
                    CustomTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        label = { Text(text = "HuggingFace Direct Link") },
                        value = state.llmUrlTextField,
                        onValueChange = { onAction(LlmManagerAction.UrlTextField(it)) },
                        singleLine = true,
                        enabled = !state.isOnEdit
                    )
                }
            }
        }
        item(key = "llmSourceButtonGroup") {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                SingleChoiceSegmentedButtonRow() {
                    SegmentedButton(
                        selected = state.llmSourceChip != LlmSource.Local,
                        onClick = { onAction(LlmManagerAction.LlmSourceChip(LlmSource.Url)) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = 0,
                            count = 2
                        ),
                        label = { Text(text = "Url") },
                        enabled = !state.isOnEdit
                    )
                    SegmentedButton(
                        selected = state.llmSourceChip == LlmSource.Local,
                        onClick = { onAction(LlmManagerAction.LlmSourceChip(LlmSource.Local)) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = 1,
                            count = 2
                        ),
                        label = { Text(text = "Local") },
                        enabled = !state.isOnEdit
                    )
                }
            }
        }
        spacer()
        listTitle("Llm Metadata")
        when {
            state.isMetadataLoading -> {
                item(key = "isMetadataLoading") {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                    )
                }
            }
            state.isMetadataError != null -> {
                item(key = "isMetadataError") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = state.isMetadataError,
                        messageType = MessageType.Error
                    )
                }
            }
            state.llmFileName.isBlank() && state.llmFileSize == 0L -> {
                item(key = "isMetadataEmpty") {
                    Message(
                        modifier = Modifier
                            .animateItem(),
                        message = "Model Metadata Will Appear Here",
                        messageType = MessageType.Info
                    )
                }
            }
            else -> {
                item(key = "modelMetadata") {
                    Column(
                        modifier = Modifier
                            .animateItem(),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        CustomTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            label = { Text(text = "Model Name") },
                            value = state.llmNameTextField,
                            onValueChange = { onAction(LlmManagerAction.NameTextField(it)) },
                            enabled = state.llm?.llmSource != LlmSource.Default
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.5.dp)
                        ) {
                            val metadataItem = listOf(
                                Triple(
                                    first = Icons.Rounded.InsertDriveFile,
                                    second = "File Name",
                                    third = state.llmFileName
                                ),
                                Triple(
                                    first = Icons.Rounded.Download,
                                    second = "Size",
                                    third = state.llmFileSize.toMegaByte()
                                ),
                                Triple(
                                    first = Icons.Rounded.TypeSpecimen,
                                    second = "Compatibility",
                                    third = if (state.isLlmSupported) "Supported" else "Unsupported"
                                )
                            )
                            metadataItem.fastForEachIndexed { index, triple ->
                                CustomSegmentedListItem(
                                    index = index,
                                    count = metadataItem.size,
                                    leadingContent = { Icon(triple.first, null) },
                                    content = { Text(text = triple.second) },
                                    supportingContent = {
                                        SupportingText(
                                            text = triple.third,
                                            isSingleLine = true
                                        )
                                    }
                                )
                            }
                        }
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
        snackBarHostState = remember { SnackbarHostState() },
        state = LlmManagerState(
            isOnEdit = true,
            isMetadataError = null,
            isUpsertLlmButtonLoading = true
        ),
        onAction = {}
    )
}

