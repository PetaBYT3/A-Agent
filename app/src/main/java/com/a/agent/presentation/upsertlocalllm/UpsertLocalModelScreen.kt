package com.a.agent.presentation.upsertlocalllm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.TypeSpecimen
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
import com.a.agent.data.local.ModelEntity
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomShrinkLeftAnimatedVisibility
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.CustomUndismissableBottomSheet
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.TitleText
import com.a.agent.presentation.util.toMegaByte
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun UpsertLocalModelScreen(
    navBackStack: NavBackStack<NavKey>,
    modelId: String,
    viewModel: UpsertLocalModelViewModel = koinViewModel {
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
    state: UpsertLocalModelState,
    onAction: (UpsertLocalModelAction) -> Unit
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
        floatingActionButton = {
            Row {
                if (state.isOnEdit) {
                    CustomFloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = { onAction(UpsertLocalModelAction.DeleteModelButton) },
                        icon = Icons.Rounded.Delete
                    )
                } else {
                    val filePicker = rememberFilePickerLauncher(
                        type = FileKitType.File(),
                        onResult = { platformFile ->
                            onAction(UpsertLocalModelAction.FilePickerButton(platformFile))
                        }
                    )
                    CustomFloatingActionButton(
                        onClick = { filePicker.launch() },
                        icon = Icons.Rounded.AttachFile
                    )
                }
                CustomShrinkLeftAnimatedVisibility(
                    visible = state.model.name.isNotBlank()
                ) {
                    Row {
                        CustomFloatingActionButton(
                            modifier = Modifier
                                .padding(start = 10.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            onClick = { onAction(UpsertLocalModelAction.UpsertModelButton) },
                            icon = Icons.Rounded.Save
                        )
                    }
                }
            }
        }
    )

    CustomUndismissableBottomSheet(
        isBottomSheetVisible = state.isUpsertModelLoading,
        content = {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TitleText(
                        text = "Upsert And Importing Model..."
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
    state: UpsertLocalModelState,
    onAction: (UpsertLocalModelAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        when {
            state.isModelLoading -> {
                item(key = "isModelLoading") {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
            state.isModelError != null -> {
                item(key = "isModelError") {
                    Message(
                        message = state.isModelError,
                        messageType = MessageType.Error
                    )
                }
            }
            state.model == ModelEntity.Empty -> {
                item(key = "isModelEmpty") {
                    Message(
                        message = "File Metadata Will Appear Here"
                    )
                }
            }
            else -> {
                item(key = "modelMetadata") {
                    Column(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CustomTextField(
                            modifier = Modifier
                                .fillMaxWidth(),
                            label = { Text(text = "Name") },
                            value = state.model.name,
                            onValueChange = { onAction(UpsertLocalModelAction.NameTextField(it)) }
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
                                    supportingContent = { Text(text = triple.third) }
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
        state = UpsertLocalModelState(),
        onAction = {}
    )
}