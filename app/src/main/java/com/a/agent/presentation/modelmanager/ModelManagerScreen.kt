@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.modelmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.data.local.ModelSource
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.component.listTitle
import com.a.agent.presentation.util.component.spacer
import com.a.agent.presentation.util.toMegaByte
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ModelManagerScreen(
    navBackStack: NavBackStack<NavKey>,
    modelId: String,
    viewModel: ModelManagerViewModel = koinViewModel {
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
    state: ModelManagerState,
    onAction: (ModelManagerAction) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = "Model Manager"
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
        bottomBar = {
            if (state.model.modelSource != ModelSource.Default) {
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
                            onClick = { onAction(ModelManagerAction.DeleteModel) },
                            content = { Icon(Icons.Rounded.Delete, null) }
                        )
                    }
                    val isReadyToSave = when {
                        state.model.modelSource == ModelSource.Local -> {
                            state.model.name.isNotBlank() &&
                            state.model.fileName.isNotBlank() &&
                            state.model.totalBytes != 0L &&
                            state.model.modelSource != ModelSource.Default &&
                            state.isModelSupported
                        }
                        else -> {
                            state.model.url.isNotBlank() &&
                            state.model.name.isNotBlank() &&
                            state.model.fileName.isNotBlank() &&
                            state.model.totalBytes != 0L &&
                            state.model.modelSource != ModelSource.Default &&
                            state.isModelSupported
                        }
                    }
                    Button(
                        modifier = Modifier
                            .height(ButtonDefaults.MediumContainerHeight),
                        onClick = { onAction(ModelManagerAction.UpsertModel) },
                        content = { Icon(Icons.Rounded.Save, null) },
                        enabled = isReadyToSave
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
    state: ModelManagerState,
    onAction: (ModelManagerAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        listTitle(
            title = "Llm Source",
            content = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = state.model.modelSource != ModelSource.Local,
                        onClick = { onAction(ModelManagerAction.LlmSourceChip(ModelSource.Url)) },
                        label = { Text(text = "Url") },
                        enabled = !state.isOnEdit
                    )
                    FilterChip(
                        selected = state.model.modelSource == ModelSource.Local,
                        onClick = { onAction(ModelManagerAction.LlmSourceChip(ModelSource.Local)) },
                        label = { Text(text = "Local") },
                        enabled = !state.isOnEdit
                    )
                }
            }
        )
        when {
            state.model.modelSource == ModelSource.Local -> {
                item(key = "localFilePicker") {
                    val filePicker = rememberFilePickerLauncher(
                        type = FileKitType.File(),
                        onResult = { platformFile ->
                            if (platformFile != null) {
                                onAction(ModelManagerAction.LocalFilePicker(platformFile))
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
                        label = { Text(text = "Direct Download Link Url") },
                        value = state.model.url,
                        onValueChange = { onAction(ModelManagerAction.UrlTextField(it)) },
                        singleLine = true,
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
            state.model.name.isBlank() && state.model.totalBytes == 0L -> {
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CustomTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            label = { Text(text = "Model Name") },
                            value = state.model.name,
                            onValueChange = { onAction(ModelManagerAction.NameTextField(it)) },
                            enabled = state.model.modelSource != ModelSource.Default
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.5.dp)
                        ) {
                            val metadataItem = listOf(
                                Triple(
                                    first = Icons.Rounded.InsertDriveFile,
                                    second = "File Name",
                                    third = state.model.fileName
                                ),
                                Triple(
                                    first = Icons.Rounded.Download,
                                    second = "Size",
                                    third = state.model.totalBytes.toMegaByte()
                                ),
                                Triple(
                                    first = Icons.Rounded.TypeSpecimen,
                                    second = "Compatibility",
                                    third = if (state.isModelSupported) "Supported" else "Unsupported"
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
        state = ModelManagerState(
            isOnEdit = true,
            isMetadataError = null
        ),
        onAction = {}
    )
}

