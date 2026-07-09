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
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material3.ButtonGroup
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
import com.a.agent.presentation.navigation.NavigationRoute
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.SupportingText
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
            FloatingActionButton(
                onClick = { navBackStack.add(NavigationRoute.ModelManagerScreen()) },
                content = { Icon(Icons.Rounded.Add, null) }
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
        itemsIndexed(
            items = state.modelEntities
        ) { index, modelEntity ->
            val downloadState = state.downloadState[modelEntity.id]
            CustomSegmentedListItem(
                onClick = { navBackStack.add(NavigationRoute.TextToTextScreen(modelEntity.id)) },
                index = index,
                count = state.modelEntities.size,
                leadingContent = {
                    Icon(
                        imageVector = when (modelEntity.type) {
                            ModelType.TextToText -> Icons.Rounded.Textsms
                            ModelType.ImageToText -> Icons.Rounded.Image
                        },
                        contentDescription = null
                    )
                },
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = modelEntity.name)
                    }
                },
                additionalContent = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            modifier = Modifier
                                .weight(1f),
                            visible = downloadState != null
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                Row {
                                    Text(text = "${downloadState?.downloadedBytes?.toMegaByte() ?: 0}/${downloadState?.totalBytes?.toMegaByte() ?: 0}")
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(text = "${downloadState?.percentage ?: 0}%")
                                }
                                val progress by animateFloatAsState(
                                    targetValue = downloadState?.progress ?: 0f
                                )
                                LinearWavyProgressIndicator(
                                    progress = { progress }
                                )
                            }
                        }
                        Row {
                            FilledTonalIconButton(
                                onClick = { navBackStack.add(NavigationRoute.ModelManagerScreen(modelEntity.id)) },
                                content = { Icon(Icons.Rounded.Info, null) }
                            )
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
                        }
                    }
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
            modelEntities = listOf(
                ModelEntity(
                    id = "1",
                    type = ModelType.TextToText,
                    name = "Preview Model",
                    fileName = "preview.tflite",
                    totalBytes = 1023921,
                    path = File(""),
                    url = "https://",
                    isSupported = true
                )
            ),
            downloadState = mutableMapOf("1" to DownloadInfo(0, 0, 0f, 0))
        ),
        onAction = {}
    )
}