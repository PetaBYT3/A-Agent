@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.model

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.data.local.ModelEntity
import com.a.agent.data.local.ModelType
import com.a.agent.data.remote.DownloadInfo
import com.a.agent.presentation.model.component.CustomLinearProgressIndicator
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.component.AnimatedContentState
import com.a.agent.presentation.util.component.CustomAnimatedContent
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.Message
import com.a.agent.presentation.util.component.MessageType
import com.a.agent.presentation.util.component.SupportingText
import com.a.agent.presentation.util.component.listTitle
import com.a.agent.presentation.util.component.message
import com.a.agent.presentation.util.component.spacer
import com.a.agent.presentation.util.toMegaByte
import org.koin.compose.viewmodel.koinViewModel
import java.io.File

@Composable
fun ModelScreen(
    navBackStack: NavBackStack<NavKey>,
    viewModel: ModelViewModel = koinViewModel()
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
    state: ModelState,
    onAction: (ModelAction) -> Unit
) {
    Scaffold(
        topBar = {
            CustomTopAppBar(
                onNavigationClick = { navBackStack.popBackStack() },
                title = "Models",
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
            CustomFloatingActionButton(
                onClick = { navBackStack.add(NavigationRoute.ModelManagerScreen()) },
                icon = Icons.Rounded.Add
            )
        }
    )
}

@Composable
private fun Content(
    navBackStack: NavBackStack<NavKey>,
    innerPadding: PaddingValues,
    state: ModelState,
    onAction: (ModelAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(start = 15.dp, end = 15.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        listTitle("Downloaded Model")
        item { 
            CustomAnimatedContent(
                isLoading = state.isDownloadedModelLoading,
                isError = state.downloadedModelError != null,
                isEmpty = state.downloadedModelEntities.isEmpty()
            ) { animatedContentState ->
                when (animatedContentState) {
                    AnimatedContentState.IsLoading -> {
                        ContainedLoadingIndicator()
                    }
                    AnimatedContentState.IsError -> {
                        Message(
                            message = state.downloadedModelError ?: "",
                            messageType = MessageType.Error
                        )
                    }
                    AnimatedContentState.IsEmpty -> {
                        Message(
                            message = "No Downloaded Model Available"
                        )
                    }
                    AnimatedContentState.Success -> {}
                }
            }
        }
        itemsIndexed(
            items = state.downloadedModelEntities
        ) { index, modelEntity ->
            val downloadState = state.downloadState[modelEntity.id]
            CustomSegmentedListItem(
                onClick = { navBackStack.add(NavigationRoute.ModelManagerScreen(modelEntity.id)) },
                index = index,
                count = state.downloadedModelEntities.size,
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = modelEntity.name)
                    }
                },
                trailingContent = {
                    FilledTonalIconButton(
                        colors = if (downloadState != null) {
                            IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        } else {
                            IconButtonDefaults.filledTonalIconButtonColors()
                        },
                        onClick = { onAction(ModelAction.ToggleDownload(modelEntity)) },
                        content = {
                            Icon(
                                imageVector = if (downloadState != null) Icons.Rounded.Close else Icons.Rounded.RestartAlt,
                                contentDescription = null
                            )
                        }
                    )
                },
                supportingContent = { Text(text = modelEntity.fileName) },
                additionalContent = {
                    CustomLinearProgressIndicator(
                        downloadInfo = downloadState
                    )
                }
            )
        }
        spacer()
        listTitle("Require Download Model")
        item {
            CustomAnimatedContent(
                isLoading = state.isRequireDownloadModelLoading,
                isError = state.requireDownloadModelError != null,
                isEmpty = state.requireDownloadModelEntities.isEmpty()
            ) { animatedContentState ->
                when (animatedContentState) {
                    AnimatedContentState.IsLoading -> {
                        ContainedLoadingIndicator()
                    }
                    AnimatedContentState.IsError -> {
                        Message(
                            message = state.requireDownloadModelError ?: "",
                            messageType = MessageType.Error
                        )
                    }
                    AnimatedContentState.IsEmpty -> {
                        Message(
                            message = "No Require Download Model Available",
                            messageType = MessageType.Info
                        )
                    }
                    AnimatedContentState.Success -> {}
                }
            }
        }
        itemsIndexed(
            items = state.requireDownloadModelEntities
        ) { index, modelEntity ->
            val downloadState = state.downloadState[modelEntity.id]
            CustomSegmentedListItem(
                onClick = { navBackStack.add(NavigationRoute.ModelManagerScreen(modelEntity.id)) },
                index = index,
                count = state.requireDownloadModelEntities.size,
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = modelEntity.name)
                    }
                },
                trailingContent = {
                    FilledTonalIconButton(
                        colors = if (downloadState != null) {
                            IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        } else {
                            IconButtonDefaults.filledTonalIconButtonColors()
                        },
                        onClick = { onAction(ModelAction.ToggleDownload(modelEntity)) },
                        content = {
                            Icon(
                                imageVector = if (downloadState != null) Icons.Rounded.Close else Icons.Rounded.Download,
                                contentDescription = null
                            )
                        }
                    )
                },
                supportingContent = { Text(text = modelEntity.fileName) },
                additionalContent = {
                    CustomLinearProgressIndicator(
                        downloadInfo = downloadState
                    )
                }
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Screen(
        navBackStack = rememberNavBackStack(),
        state = ModelState(
            downloadedModelEntities = listOf(
                ModelEntity(
                    id = "1",
                    name = "Preview Model",
                    fileName = "preview.tflite",
                    totalBytes = 1023921,
                    path = "",
                    url = "https://"
                )
            ),
            requireDownloadModelEntities = listOf(
                ModelEntity(
                    name = "Preview Model",
                    url = "",
                    path = "",
                    fileName = "preview.litertlm",
                    totalBytes = 2193123
                )
            ),
            downloadState = mutableMapOf("1" to DownloadInfo(0, 0, 0f, 0))
        ),
        onAction = {}
    )
}