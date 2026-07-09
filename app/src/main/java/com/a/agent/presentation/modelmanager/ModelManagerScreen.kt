@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.a.agent.presentation.modelmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.a.agent.data.local.ModelType
import com.a.agent.presentation.navigation.popBackStack
import com.a.agent.presentation.util.component.AnimatedContentState
import com.a.agent.presentation.util.component.CustomAnimatedContent
import com.a.agent.presentation.util.component.CustomFloatingActionButton
import com.a.agent.presentation.util.component.CustomSegmentedListItem
import com.a.agent.presentation.util.component.CustomTextField
import com.a.agent.presentation.util.component.CustomTopAppBar
import com.a.agent.presentation.util.component.HeadlineText
import com.a.agent.presentation.util.component.itemColumn
import com.a.agent.presentation.util.component.loadingIndicator
import com.a.agent.presentation.util.component.message
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                if (state.isOnEdit) {
                    CustomFloatingActionButton(
                        onClick = { onAction(ModelManagerAction.DeleteModel) },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        icon = Icons.Rounded.Delete
                    )
                }
                CustomFloatingActionButton(
                    onClick = { onAction(ModelManagerAction.UpsertModel) },
                    icon = Icons.Rounded.Save
                )
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
            .fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(start = 15.dp, end = 15.dp, bottom = 15.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        itemColumn {
            CustomTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                label = { Text(text = "Model URL") },
                value = state.urlTextField,
                onValueChange = { onAction(ModelManagerAction.UrlTextField(it)) },
                enabled = !state.isOnEdit
            )
        }
        spacer()
        when {
            state.isMetadataLoading -> {
                loadingIndicator()
            }
            state.isMetadataError != null -> {
                message(
                    message = state.isMetadataError,
                    isError = true
                )
            }
            state.fileName.isBlank() && state.totalBytes == 0L -> {
                message(
                    message = "Metadata will appear here"
                )
            }
            else -> {
                itemColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    CustomTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        label = { Text(text = "Model Name") },
                        value = state.nameTextField,
                        onValueChange = { onAction(ModelManagerAction.NameTextField(it)) }
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.5.dp)
                    ) {
                        ModelType.entries.forEachIndexed { index, modelType ->
                            CustomSegmentedListItem(
                                onClick = { onAction(ModelManagerAction.TypeRadioButton(modelType)) },
                                index = index,
                                count = ModelType.entries.size,
                                leadingContent = {
                                    RadioButton(
                                        selected = modelType == state.typeRadioButton,
                                        onClick = {}
                                    )
                                },
                                content = { Text(text = modelType.value) }
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.5.dp)
                    ) {
                        CustomSegmentedListItem(
                            index = 0,
                            count = 3,
                            content = { Text(text = "Filename") },
                            supportingContent = { Text(text = state.fileName) }
                        )
                        CustomSegmentedListItem(
                            index = 1,
                            count = 3,
                            content = { Text(text = "Size") },
                            supportingContent = { Text(text = state.totalBytes.toMegaByte()) }
                        )
                        CustomSegmentedListItem(
                            index = 2,
                            count = 3,
                            content = { Text(text = "Compatibility") },
                            supportingContent = { Text(text = if (state.isSupported) "Supported" else "Unsupported") }
                        )
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
            isMetadataError = null
        ),
        onAction = {}
    )
}

