@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.modelmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.TypeSpecimen
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomShrinkLeftAnimatedVisibility
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.component.spacer
import com.a.agent.presentation.util.toMegaByte
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
        floatingActionButton = {
            Row {
                if (state.isOnEdit && state.model.modelSource != ModelSource.Default) {
                    CustomFloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = { onAction(ModelManagerAction.DeleteModel) },
                        icon = Icons.Rounded.Delete
                    )
                }
                val isReadyToSave = state.model.url.isNotBlank() &&
                        state.model.name.isNotBlank() &&
                        state.model.fileName.isNotBlank() &&
                        state.model.totalBytes != 0L &&
                        state.model.modelSource != ModelSource.Default &&
                        state.isModelSupported
                CustomShrinkLeftAnimatedVisibility(
                    visible = isReadyToSave
                ) {
                    CustomFloatingActionButton(
                        modifier = Modifier
                            .padding(start = 10.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = { onAction(ModelManagerAction.UpsertModel) },
                        icon = Icons.Rounded.Save
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
        item(key = "urlTextField") {
            CustomTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                label = { Text(text = "Direct Download Link Url") },
                value = state.model.url,
                onValueChange = { onAction(ModelManagerAction.UrlTextField(it)) },
                singleLine = true,
                enabled = !state.isOnEdit
            )
        }
        spacer()
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

